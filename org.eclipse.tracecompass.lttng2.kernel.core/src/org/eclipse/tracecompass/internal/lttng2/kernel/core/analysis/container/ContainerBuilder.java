/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sébastien Lorrain
 *   Francis Jolivet
 ******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.container;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;

/**
 *
 * This class/builder was designed because we cannot guarantee that we will
 * receive the statedump events in order (parent tasks before children tasks).
 *
 * This builder caches statedump_process_status events on the fly and stores the
 * information in a Task class. If the container holding the task has been added
 * to the statesystem, we can process the task. Otherwise, this builder holds
 * tasks that dosent have their parent container yet.
 *
 * When a new task is processed or a new container is created, every other tasks
 * are re-processed.
 *
 * @author Francis Jolivet
 * @author Sébastien Lorrain
 *
 */

public class ContainerBuilder {

    private boolean rootContainerInfoAdded = false;
    private HashMap<Integer, TaskContainerEvent> hmTasks; // tid, Task
    private LinkedList<TaskContainerEvent> unresolvedTasks;// ppid, Task

    /**
     * Constructor
     */
    public ContainerBuilder()
    {
        hmTasks = new HashMap<>();
        unresolvedTasks = new LinkedList<>();
    }

    /**
     * Takes the statesystembuilder and a new task to be processed
     *
     * @param ssb
     *            The statesystembuilder
     * @param t
     *            The task to add and process. If it cannot be processed right
     *            away, it will be cached until it's parent container is found.
     */
    public void insertTaskContainerEvent(ITmfStateSystemBuilder ssb, TaskContainerEvent t)
    {
        // Store the new entry in the hashmap
        checkNotNull(t);
        // Since we encounter the deepest level first, we can encounter multiple
        // time the statedump for all levels of the task in the namespaces
        // Thats why we check first if we alerady added the task
        if (!hmTasks.containsKey(t.getTid())) {
            hmTasks.put(t.getTid(), t);

            // We wait to have the root container info before processing tasks.
            // This info is added in the stateprovider
            if (rootContainerInfoAdded)
            {
                int rootContainerQuark = ContainerHelper.getRootContainerQuark(ssb);
                ContainerInfo rootContainerInfo = ContainerHelper.getContainerInfo(ssb, rootContainerQuark);
                if (rootContainerInfo == null) {
                    throw new IllegalStateException("Error, root container seems added but container info is unavailable!"); //$NON-NLS-1$
                }

                // If the task is in the root container, only change CPU states
                if (t.getNsInode() == rootContainerInfo.getContainerInode())
                {
                    setTaskCPUStatus(ssb, t);
                }
                else if (isParentContainerAdded(ssb, t))
                {
                    // If the task is not in the root container, and are able to
                    // add it
                    // add it and resolve unprocessed tasks
                    addTaskAndResolveOrphans(ssb, t);
                }
                else {
                    // Add task to orphans
                    unresolvedTasks.add(t);
                }
            }
        }
    }

    /**
     * This method provides the root container information. As long as this info is not set,
     * we cannot manipulate containers (add containers, add tasks to containers, etc).
     * All task added to the builder will be cached until the root container information is added.
     *
     * @param ssb
     *          ITmfStateSystemBuilder
     * @param timestamp
     *          TimeStamp
     * @param cinfo
     *          Container info
     */
    public void addRootContainerInfo(ITmfStateSystemBuilder ssb, long timestamp, ContainerInfo cinfo) {
        if (!rootContainerInfoAdded)
        {
            ContainerHelper.setRootContainerInfo(ssb, timestamp, cinfo);
            rootContainerInfoAdded = true;
        }
    }

    //This will process every taskEvents that were cached that can now be added to the container tree.
    private void addTaskAndResolveOrphans(ITmfStateSystemBuilder ssb, TaskContainerEvent t) {
        // Parent container was added, we can add the task
        processTaskEvent(ssb, t);

        setTaskCPUStatus(ssb, t);

        // We added a task, resolve all conflict!
        // Maybe a parent will adopt nice children...
        boolean retryUnresolvedTasks = false;
        do {
            retryUnresolvedTasks = false;
            /*
             * Premature Optimisation is the root of all evil. This would be
             * O(n^2) if statedump /task would not arrive in order. BUT this
             * never occurs in practice.
             */

            for (Iterator<TaskContainerEvent> iterator = unresolvedTasks.iterator(); iterator.hasNext();) {
                final TaskContainerEvent orphan = iterator.next();
                if (orphan == null) {
                    iterator.remove();
                    continue;
                }
                if (isParentContainerAdded(ssb, orphan)) {
                    processTaskEvent(ssb, orphan);

                    setTaskCPUStatus(ssb, t);

                    iterator.remove();
                    retryUnresolvedTasks = true; // we added a task, try to
                                                 // resolve orphan again
                }
            }
        } while (retryUnresolvedTasks);
    }

    /**
     * This will set the proper state of the container CPUs given the taskCpuEvent.
     * If the taskCpuEvent is a running task, it will also set the running TID of the CPU.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param taskCpuEvent
     *      The task CPU event containing the timestamp, TID and CPU id and state.
     */
    public static void setTaskCPUStatus(ITmfStateSystemBuilder ssb, TaskCPUEvent taskCpuEvent) {
        int containerQuark = ContainerHelper.getContainerFromTaskTid(ssb, taskCpuEvent.getTid());
        // If the task is set to a "running" state, update the CPU quark to the
        // current task's TID
        if (taskCpuEvent.getCpuState().unboxInt() != ContainerCpuState.CPU_STATUS_IDLE) {
            ContainerHelper.setCpuCurrentlyRunningTask(ssb, containerQuark, taskCpuEvent.getTs(), taskCpuEvent.getCpuId(), taskCpuEvent.getTid());
        }
        // Change the state of the CPU to reflect the task's state
        ContainerHelper.updateContainerCPUState(ssb, containerQuark, taskCpuEvent.getTs(), taskCpuEvent.getCpuId(), taskCpuEvent.getCpuState());
    }

    /*
     * This function will try to find the parent container of the task. If found, return true. Return false otherwise.
     * This method is used to determine if we can process the task or not.
     * If we can, that means that the container of the PARENT task was added (the parent container can be root container or not).
     */

    private boolean isParentContainerAdded(ITmfStateSystemBuilder ssb, TaskContainerEvent task)
    {
        /*
         * The only time that a parent container is not yet instanciated is when
         * a task got a parent container that is NOT the root container and that
         * this parent container is not created yet.
         */

        // If root container not added yet, parent container is certanly not
        if (!rootContainerInfoAdded) {
            return false;
        }

        int rootContainerQuark = ContainerHelper.getRootContainerQuark(ssb);
        ContainerInfo rootContainerInfo = ContainerHelper.getContainerInfo(ssb, rootContainerQuark);
        if (rootContainerInfo == null) {
            throw new IllegalStateException("Error, root container seems added but container info is unavailable!"); //$NON-NLS-1$
        }

        // If the task is not in the root namespace, try to find the container
        // with its inode
        if (task.getNsInode() != rootContainerInfo.getContainerInode())
        {
            TaskContainerEvent parent = hmTasks.get(task.getPpid());

            try{
                ContainerHelper.findContainerQuarkWithInode(ssb, parent.getNsInode());
            } catch (AttributeNotFoundException e)
            {
                //We cannot find the container. This can happen, and means that the parent container was not added yet.
                return false;
            }
        }
        return true;
    }

    private void processTaskEvent(ITmfStateSystemBuilder ssb, TaskContainerEvent t)
    {
        // This function takes for granted that task NS != root
        if (!rootContainerInfoAdded) {
            throw new IllegalStateException("Error, cannot process task while root container info is not valid!"); //$NON-NLS-1$
        }

        int rootContainerQuark = ContainerHelper.getRootContainerQuark(ssb);
        ContainerInfo rootContainerInfo = ContainerHelper.getContainerInfo(ssb, rootContainerQuark);
        if (rootContainerInfo == null) {
            throw new IllegalStateException("Error, root container seems added but container info is unavailable!"); //$NON-NLS-1$
        }

        TaskContainerEvent parent = hmTasks.get(t.getPpid());

        // If the task is in the root container, do not process it.
        // We dont keep track of the task associated with the root container.
        if (parent != null && t.getNsInode() != rootContainerInfo.getContainerInode())
        {
            // We found the task container, add it to it.
            try{
                int taskContainerINode = ContainerHelper.findContainerQuarkWithInode(ssb, t.getNsInode());
                if (taskContainerINode != rootContainerQuark)
                {
                    int containerQuark = ContainerHelper.findContainerQuarkWithInode(ssb, t.getNsInode());
                    ContainerHelper.addTaskToContainer(ssb, containerQuark, t.getTs(), t.getVtid(), t.getTid());

                }
            } catch (AttributeNotFoundException e)
            {
                /*
                 * The container does not exist.
                 * Create it
                */
                if (t.getNsInode() != parent.getNsInode())
                {
                    // This function takes for granted that the parent container of
                    // the task exists
                    if (!isParentContainerAdded(ssb, t)) {
                        throw new IllegalStateException("the parent container of the task does not exist!"); //$NON-NLS-1$
                    }

                    ContainerInfo cInfo = new ContainerInfo(t.getNsInode(), parent.getTid(), "TODO_ADD_HOSTNAME"); //$NON-NLS-1$
                    ContainerHelper.addContainerAndTask(ssb, t.getTs(), parent.getNsInode(), t.getTid(), t.getVtid(), cInfo);
                }
            }

        }
    }

}
