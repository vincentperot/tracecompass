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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * This class contains static helper methods to work with the container tree in the StateSystem.
 * This abstract a lot of work for the builder and should not be used directly.
 *
 * @author Francis Jolivet
 * @author Sébastien Lorrain
 */
public class ContainerHelper {

    // This is a hack to have a unique container ID
    // Only one thread at a time may read/update this counter, preventing two
    // thread
    // creating a container with the same ID
    private static Integer nextContainerID = new Integer(0);

    private static synchronized int getNextContainerID() {
        return nextContainerID++;
    }

    /**
     * This set the root container info.
     *
     * @param ssb
     *          ITmfStateSystemBuilder
     * @param timestamp
     *          The timestamp
     * @param rootContainerInfo
     *          The container info.
     */
    public static void setRootContainerInfo(ITmfStateSystemBuilder ssb, long timestamp, ContainerInfo rootContainerInfo) {
        int rootContainerQuark = ContainerHelper.getRootContainerQuark(ssb);
        ContainerHelper.setContainerInfo(ssb, rootContainerQuark, timestamp, rootContainerInfo);
    }

    /**
     * Get the root container quark
     *
     * @param ssb
     *           ITmfStateSystemBuilder
     * @return the quark
     */
    public static int getRootContainerQuark(ITmfStateSystemBuilder ssb)
    {
        return ssb.getQuarkAbsoluteAndAdd(ContainerAttributes.ROOT);
    }

    /**
     * This is a high-level function that does two things :
     * 1rst it will create a new container attached to "parentNSInum".
     * 2nd it will attach the first task given by childTid/childVTid in the new container.
     * When creating a new PID namespace (a PID container), it will always come with the creation of a new task
     * which in turn will be the "child_reaper" of that PID namespace (think of it like the "init" of the PID namespace).
     *
     * @param ssb
     *          ITmfStateSystemBuilder
     * @param timestamp
     *          The timestamp
     * @param parentNSInum
     *          The INode of the container in which we add another container,
     *          i.e. the parent container of the new container.
     * @param childTid
     *          The first task's TID in the new container.
     * @param childVTid
     *          The first task's VTID in the new container.
     * @param cInfo
     *          The container info of the new container.
     */
    public static void addContainerAndTask(final ITmfStateSystemBuilder ssb, long timestamp, int parentNSInum, int childTid, int childVTid, ContainerInfo cInfo) {
        int newContainerQuark = ContainerHelper.attachNewContainer(ssb, parentNSInum, timestamp, cInfo);
        ContainerHelper.addTaskToContainer(ssb, newContainerQuark, timestamp, childVTid, childTid);
    }

    private static int attachNewContainer(ITmfStateSystemBuilder ssb, int parentContainerINode, long timestamp, ContainerInfo containerInfo) {

        int parentContainerQuark = -1;
        try {
            parentContainerQuark = findContainerQuarkWithInode(ssb, parentContainerINode);
        } catch (AttributeNotFoundException e)
        {
            // If we try to attach a container to an inexisting container
            // parent,
            // we end up here.
            e.printStackTrace();
        }

        // TODO fix the containerNumber hack!!
        int containerQuark = ssb.getQuarkRelativeAndAdd(
                parentContainerQuark,
                ContainerAttributes.CONTAINERS_SECTION,
                ContainerAttributes.CONTAINER_ID + Integer.toString(ContainerHelper.getNextContainerID()));

        setContainerInfo(ssb, containerQuark, timestamp, containerInfo);

        return containerQuark;
    }

    /**
     * This method returns the container information given the container quark.
     * This methods raise an exception if the container info is not found because by design,
     * we should always be able to find the container info given a containerQuark
     * as it is initialized with the container creation.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param containerQuark
     *      the top-level quark of the container info we want to get.
     * @return ContainerInfo
     *      The info of the container if we were able to find it. Return null and throw and exception otherwise.
     */
    public static @Nullable ContainerInfo getContainerInfo(ITmfStateSystemBuilder ssb, int containerQuark)
    {
        ContainerInfo cinfo = null;
        try {
            int containerInfoQuark = ssb.getQuarkRelative(containerQuark, ContainerAttributes.CONTAINER_INFO);
            int containerPPIDQuark = ssb.getQuarkRelative(containerInfoQuark, ContainerAttributes.PPID);
            int containerINodeQuark = ssb.getQuarkRelative(containerInfoQuark, ContainerAttributes.CONTAINER_INODE);
            int containerHostnameQuark = ssb.getQuarkRelative(containerInfoQuark, ContainerAttributes.HOSTNAME);

            int ppid = ssb.queryOngoingState(containerPPIDQuark).unboxInt();
            int inode = ssb.queryOngoingState(containerINodeQuark).unboxInt();
            String hostname = ssb.queryOngoingState(containerHostnameQuark).unboxStr();

            cinfo = new ContainerInfo(inode, ppid, hostname);

        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return cinfo;

    }

    // Container info should not change after the container is created
    private static void setContainerInfo(ITmfStateSystemBuilder ssb, int containerQuark, long timestamp, ContainerInfo containerInfo) {

        // This ensure that the quark is indeed a container quark
        assert (ssb.getAttributeName(containerQuark).startsWith(ContainerAttributes.CONTAINER_ID));

        try {
            int containerInfoQuark = ssb.getQuarkRelativeAndAdd(containerQuark, ContainerAttributes.CONTAINER_INFO);
            int containerPPIDQuark = ssb.getQuarkRelativeAndAdd(containerInfoQuark, ContainerAttributes.PPID);
            ssb.modifyAttribute(timestamp, TmfStateValue.newValueInt(containerInfo.getContainerParentTID()), containerPPIDQuark);

            int containerINodeQuark = ssb.getQuarkRelativeAndAdd(containerInfoQuark, ContainerAttributes.CONTAINER_INODE);
            ssb.modifyAttribute(timestamp, TmfStateValue.newValueInt(containerInfo.getContainerInode()), containerINodeQuark);

            int containerHostnameQuark = ssb.getQuarkRelativeAndAdd(containerInfoQuark, ContainerAttributes.HOSTNAME);
            ssb.modifyAttribute(timestamp, TmfStateValue.newValueString(containerInfo.getContainerHostname()), containerHostnameQuark);
        } catch (AttributeNotFoundException e)
        {
            // This should not happen...
            e.printStackTrace();
        }

    }

    /**
     * This will explore ALL task contained by ALL container to find the parent container of the given task TID.
     * Note that the TID is the TID viewed from the root container, i.e. the "real" TID of the task, not the VTID.
     * If the TID is not found, it returns the root container quark.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param taskTid
     *      The "real" TID of the task
     * @return
     *      Returns the quark of the container containing the task. If not found, return the root container quark.
     */
    public static int getContainerFromTaskTid(ITmfStateSystemBuilder ssb, int taskTid)// returns
                                                                                      // containerQuark
    {
        // The root of the namespace containers. This quark is the parent of all
        // namespaces!
        int rootQuark = getRootContainerQuark(ssb);
        int parentContainerQuark = -1;

        try {
            List<Integer> VTIDsSection = ssb.getSubAttributes(rootQuark, true, ContainerAttributes.CONTAINER_TASKS);

            if (VTIDsSection.size() >= 1)
            {
                // Expand VTIDs section and iterates over all VTID found in
                // containers
                // Check their real TID, compare it to the concerned TID

                for (Integer quarkVTIDsSection : VTIDsSection) {
                    List<Integer> VTIDs = ssb.getSubAttributes(quarkVTIDsSection, false);

                    for (Integer quarkVTid : VTIDs) {
                        int real_tid_quark = ssb.getQuarkRelative(quarkVTid, ContainerAttributes.REAL_TID);
                        int tid_in_container = ssb.queryOngoingState(real_tid_quark).unboxInt();
                        if (taskTid == tid_in_container)
                        {
                            // We got the tid in a container somewhere, return
                            // the container quark!
                            parentContainerQuark = ssb.getParentAttributeQuark(quarkVTid);
                            parentContainerQuark = ssb.getParentAttributeQuark(parentContainerQuark);
                            break;
                        }
                    }
                }
            }

        } catch (AttributeNotFoundException e) {
            // This would mean that manipulation on container quark trees
            // occured BEFORE setting the root node
            e.printStackTrace();
        }

        return parentContainerQuark == -1 ?
                rootQuark : parentContainerQuark;
    }

    /**
     * This method will explore EVERY containers trying to find the container with the given Inode.
     * If the container is not found, it will raise an AttributeNotFoundException.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param containerINode
     *      The INode of the container we want to find
     * @return
     *      The quark of the container
     *
     * @throws AttributeNotFoundException
     *      If we were unable to find the given container.
     */
    public static int findContainerQuarkWithInode(ITmfStateSystemBuilder ssb, int containerINode) throws AttributeNotFoundException {
        // The root of the namespace containers. This quark is the parent of all
        // namespaces!
        int rootQuark = getRootContainerQuark(ssb);
        int parentContainerQuark = -1;

        try {
            List<Integer> ContainerINodeList = ssb.getSubAttributes(rootQuark, true, ContainerAttributes.CONTAINER_INODE);

            if (ContainerINodeList.size() >= 1)
            {
                // Expand container INodes and iterates over all INodes found.
                // Check to see if we can find the requested INode

                for (Integer quarkContainerINode : ContainerINodeList) {
                    int iNode = ssb.queryOngoingState(quarkContainerINode).unboxInt();
                    if (iNode == containerINode)
                    {
                        // We got the tid in a container somewhere, return the
                        // container quark!
                        int containerInfoQuark = ssb.getParentAttributeQuark(quarkContainerINode);
                        parentContainerQuark = ssb.getParentAttributeQuark(containerInfoQuark);
                        break;
                    }
                }
            }

        } catch (AttributeNotFoundException e) {
            // This would mean that manipulation on container quark trees
            // occured BEFORE setting the root node
            e.printStackTrace();
        }

        if(parentContainerQuark == -1)
        {
            throw new AttributeNotFoundException("Cannot find the container quark"); //$NON-NLS-1$
        }

        return parentContainerQuark;
    }

    /**
     * Given a container quark, this appends the task VITD/TID to the container.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param containerQuark
     *      The quark of the container
     * @param timestamp
     *      The timestamp
     * @param VTID
     *      The VTID of the task
     * @param real_TID
     *      The "real" TID of the task
     */
    public static void addTaskToContainer(ITmfStateSystemBuilder ssb, int containerQuark, long timestamp, int VTID, int real_TID) {

        // This ensure that the quark is indeed a container quark
        assert (ssb.getAttributeName(containerQuark).startsWith(ContainerAttributes.CONTAINER_ID));

        int VTIDSectionQuark = getVTIDsSection(ssb, containerQuark);

        int vtidQuark = ssb.getQuarkRelativeAndAdd(VTIDSectionQuark, Integer.toString(VTID));

        int real_tidQuark = ssb.getQuarkRelativeAndAdd(vtidQuark, ContainerAttributes.REAL_TID);
        try {
            ssb.modifyAttribute(timestamp, TmfStateValue.newValueInt(real_TID), real_tidQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            // TODO LOG me don't use e.printstackTrace
            e.printStackTrace();
        }
    }

    /**
     * This scans every container for the given real TID, and if found, removes it from the container along with it's VTID
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param timestamp
     *      The timestamp
     * @param realTIDToRemove
     *      The "real" tid of the task to remove
     * @return
     *      true if the task was found and removed.
     */
    public static boolean removeTaskFromContainer(ITmfStateSystemBuilder ssb, long timestamp, int realTIDToRemove)
    {
        int containerQuark = getContainerFromTaskTid(ssb, realTIDToRemove);

        if (containerQuark != ContainerHelper.getRootContainerQuark(ssb))
        {
            /*
             * This is highly inefficient...we get all TID of container and
             * search for corresponding quark. At the same time, we don't have
             * the VTID information in the tracepoint!! This is O(n), where n is
             * the number of task in a container
             */
            try {
                List<Integer> RealTIDList = ssb.getSubAttributes(containerQuark, true, ContainerAttributes.REAL_TID);

                if (RealTIDList.size() >= 1)
                {
                    for (Integer realTIDQuark : RealTIDList) {
                        int realTIDvalue = ssb.queryOngoingState(realTIDQuark).unboxInt();
                        if (realTIDToRemove == realTIDvalue) {

                            int VTIDQuark = ssb.getParentAttributeQuark(realTIDQuark);
                            ssb.removeAttribute(timestamp, VTIDQuark);
                            return true;
                        }
                    }
                }
            } catch (AttributeNotFoundException e) {
                // TODO Auto-generated catch block

                // This would happen if the task is not yet added and we try to
                // remove it!
                e.printStackTrace();
            }
        }
        return false;
    }

    //Explore the container and return the section containing the tasks
    private static int getVTIDsSection(ITmfStateSystemBuilder ssb, int containerQuark) {
        // This ensure that the quark is indeed a container quark
        assert (ssb.getAttributeName(containerQuark).startsWith(ContainerAttributes.CONTAINER_ID));
        int VTIDsSectionQuark = -1;
        VTIDsSectionQuark = ssb.getQuarkRelativeAndAdd(containerQuark, ContainerAttributes.CONTAINER_TASKS);
        return VTIDsSectionQuark;
    }

    /**
     * This update recursively a container tree with a given CPU status.
     * If a nested container CPU is set to "Running", the parent container corresponding CPU will be set to "shared" recursively.
     * If a CPU is set to "Idle", the corresponding parent containers CPU will also be set to Idle.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param containerQuark
     *      The quark of the container
     * @param timeStamp
     *      The timestamp
     * @param cpuId
     *      The numerical ID of the CPU to change state
     * @param CpuState
     *      The state of the CPU
     */
    public static void updateContainerCPUState(ITmfStateSystemBuilder ssb, int containerQuark, long timeStamp, int cpuId, ITmfStateValue CpuState) {

        // This ensure that the quark is indeed a container quark
        assert (ssb.getAttributeName(containerQuark).startsWith(ContainerAttributes.CONTAINER_ID));

        try {
            // The CPUs root quark of the container container passed in
            // agrument.
            int cpuQuark = ssb.getQuarkRelativeAndAdd(containerQuark, new String[] { ContainerAttributes.CONTAINER_CPU, Integer.toString(cpuId) });
            ssb.modifyAttribute(timeStamp, CpuState, cpuQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int rootQuark = ssb.getQuarkAbsoluteAndAdd(ContainerAttributes.ROOT);

        // If the container is not the root, propagate CPU state changes up to
        // the root recursively
        if (containerQuark != rootQuark) {

            int parentNestedContainersQuark = ssb.getParentAttributeQuark(containerQuark);
            int parentContainerQuark = ssb.getParentAttributeQuark(parentNestedContainersQuark);

            switch (CpuState.unboxInt())
            {
            case ContainerCpuState.CPU_STATUS_IDLE:
                ContainerHelper.updateContainerCPUState(ssb, parentContainerQuark, timeStamp, cpuId, ContainerCpuState.CPU_STATUS_IDLE_VALUE);
                break;
            case ContainerCpuState.CPU_STATUS_RUNNING:
                ContainerHelper.updateContainerCPUState(ssb, parentContainerQuark, timeStamp, cpuId, ContainerCpuState.CPU_STATUS_SHARED_VALUE);
                break;

            case ContainerCpuState.CPU_STATUS_SHARED:
                ContainerHelper.updateContainerCPUState(ssb, parentContainerQuark, timeStamp, cpuId, ContainerCpuState.CPU_STATUS_SHARED_VALUE);
                break;
            default:
                break;
            }
        }
    }

    /**
     * This methods set the running "real" TID of a CPU.
     *
     * @param ssb
     *      ITmfStateSystemBuilder
     * @param containerQuark
     *      The container quark
     * @param timeStamp
     *      The timestamp
     * @param cpuId
     *      The numerical ID of the CPU
     * @param tid
     *      The "real" TID of the task that is run by the CPU.
     */
    public static void setCpuCurrentlyRunningTask(ITmfStateSystemBuilder ssb, int containerQuark, long timeStamp, int cpuId, int tid) {

        // This ensure that the quark is indeed a container quark
        assert (ssb.getAttributeName(containerQuark).startsWith(ContainerAttributes.CONTAINER_ID));

        // The CPUs root quark of the container container passed in agrument.
        int cpuQuark = ssb.getQuarkRelativeAndAdd(containerQuark, new String[] { ContainerAttributes.CONTAINER_CPU, Integer.toString(cpuId) });

        int cpuRunningVTIDQuark = ssb.getQuarkRelativeAndAdd(cpuQuark, ContainerAttributes.RUNNING_VTID);
        try {
            ssb.modifyAttribute(timeStamp, TmfStateValue.newValueInt(tid), cpuRunningVTIDQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            // This should never happens...
            e.printStackTrace();
        }
    }
}
