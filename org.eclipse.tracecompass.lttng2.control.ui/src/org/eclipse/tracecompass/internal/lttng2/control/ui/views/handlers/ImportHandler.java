/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of streamed traces
 *   Patrick Tasse - Add support for source location
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConnectionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConnectionManager;
import org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.LttngRelaydConsumer;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IImportDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ImportFileInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ctf.core.CtfConstants;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler implementation to import traces from a (remote) session to a
 * tracing project.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportHandler extends BaseControlViewHandler {

    private static final int BUFFER_IN_KB = 16;

    private static final int BYTES_PER_KB = 1024;

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Name of default project to import traces to */
    public static final String DEFAULT_REMOTE_PROJECT_NAME = "Remote"; //$NON-NLS-1$

    /** The preference key to remeber whether or not the user wants the notification shown next time **/
    private static final String NOTIFY_IMPORT_STREAMED_PREF_KEY = "NOTIFY_IMPORT_STREAMED"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The command parameter
     */
    protected CommandParameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        fLock.lock();
        try {
            final CommandParameter param = fParam.clone();

            // create default project
            IProject project = TmfProjectRegistry.createProject(DEFAULT_REMOTE_PROJECT_NAME, null, null);

            if (param.getSession().isLiveTrace()) {
                importLiveTrace(new LttngRelaydConnectionInfo(param.getSession().getLiveUrl(), param.getSession().getLivePort(), param.getSession().getName()), project);
                return null;
            } else if (param.getSession().isStreamedTrace()) {

                IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                String notify = store.getString(NOTIFY_IMPORT_STREAMED_PREF_KEY);
                if (!MessageDialogWithToggle.ALWAYS.equals(notify)) {
                    MessageDialogWithToggle.openInformation(window.getShell(), null, Messages.TraceControl_ImportDialogStreamedTraceNotification, Messages.TraceControl_ImportDialogStreamedTraceNotificationToggle, false, store, NOTIFY_IMPORT_STREAMED_PREF_KEY);
                }

                // Streamed trace
                TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                TmfTraceFolder traceFolder = projectElement.getTracesFolder();

                ImportTraceWizard wizard = new ImportTraceWizard();
                wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
                WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                dialog.open();
                return null;
            }

            // Remote trace
            final IImportDialog dialog = TraceControlDialogFactory.getInstance().getImportDialog();
            dialog.setSession(param.getSession());
            dialog.setDefaultProject(DEFAULT_REMOTE_PROJECT_NAME);

            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_ImportJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {

                    MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, Messages.TraceControl_ImportFailure, null);
                    List<ImportFileInfo> traces = dialog.getTracePathes();
                    IProject selectedProject = dialog.getProject();
                    for (Iterator<ImportFileInfo> iterator = traces.iterator(); iterator.hasNext();) {
                        try {

                            if (monitor.isCanceled()) {
                                status.add(Status.CANCEL_STATUS);
                                break;
                            }

                            ImportFileInfo remoteFile = iterator.next();

                            downloadTrace(remoteFile, selectedProject, monitor);

                            // Set trace type
                            IFolder traceFolder = remoteFile.getDestinationFolder();

                            IResource file = traceFolder.findMember(remoteFile.getLocalTraceName());

                            if (file != null) {
                                TraceTypeHelper helper = null;

                                try {
                                    helper = TmfTraceTypeUIUtils.selectTraceType(file.getLocation().toOSString(), null, null);
                                } catch (TmfTraceImportException e) {
                                    // the trace did not match any trace type
                                }

                                if (helper != null) {
                                    status.add(TmfTraceTypeUIUtils.setTraceType(file, helper));
                                }

                                URI uri = remoteFile.getImportFile().toURI();
                                String sourceLocation = URIUtil.toUnencodedString(uri);
                                file.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
                            }
                        } catch (ExecutionException e) {
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ImportFailure, e));
                        } catch (CoreException e) {
                            status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ImportFailure, e));
                        }
                    }
                    return status;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        TraceSessionComponent session = null;
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not
                    // destroyed
                    TraceSessionComponent tmpSession = (TraceSessionComponent) element;
                    if ((tmpSession.isSnapshotSession() || tmpSession.isLiveTrace() || (tmpSession.getSessionState() == TraceSessionState.INACTIVE)) && (!tmpSession.isDestroyed())) {
                        session = tmpSession;
                    }
                }
            }
        }
        boolean isEnabled = session != null;

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new CommandParameter(session);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Downloads a trace from the remote host to the given project.
     *
     * @param trace
     *            - trace information of trace to import
     * @param project
     *            - project to import to
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     */
    private static void downloadTrace(ImportFileInfo trace, IProject project, IProgressMonitor monitor)
            throws ExecutionException {
        try {
            IFileStore importRoot = trace.getImportFile();

            IFolder traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
            if (!traceFolder.exists()) {
                throw new ExecutionException(Messages.TraceControl_ImportDialogInvalidTracingProject + " (" + TmfTracesFolder.TRACES_FOLDER_NAME + ")"); //$NON-NLS-1$//$NON-NLS-2$
            }

            IFolder destinationFolder = trace.getDestinationFolder();
            TraceUtils.createFolder(destinationFolder, monitor);

            String traceName = trace.getLocalTraceName();
            IFolder folder = destinationFolder.getFolder(traceName);
            if (folder.exists()) {
                if (!trace.isOverwrite()) {
                    throw new ExecutionException(Messages.TraceControl_ImportDialogTraceAlreadyExistError + ": " + traceName); //$NON-NLS-1$
                }
            } else {
                folder.create(true, true, null);
            }

            IFileStore[] sources = importRoot.childStores(EFS.NONE, new NullProgressMonitor());
            SubMonitor subMonitor = SubMonitor.convert(monitor, sources.length);
            subMonitor.beginTask(Messages.TraceControl_DownloadTask, sources.length);

            for (IFileStore source : sources) {
                if (subMonitor.isCanceled()) {
                    monitor.setCanceled(true);
                    return;
                }
                SubMonitor childMonitor = subMonitor.newChild(1);
                IFileInfo info = source.fetchInfo();
                if (!info.isDirectory()) {
                    IPath destination = folder.getLocation().addTrailingSeparator().append(source.getName());
                    subMonitor.setTaskName(Messages.TraceControl_DownloadTask + ' ' + traceName + '/' + source.getName());
                    try (InputStream in = source.openInputStream(EFS.NONE, new NullProgressMonitor())) {
                        copy(in, destination, childMonitor, info.getLength());
                    }
                }
            }
        } catch (IOException e) {
            throw new ExecutionException(e.toString(), e);
        } catch (CoreException e) {
            throw new ExecutionException(e.toString(), e);
        }
    }

    private static void copy(InputStream in, IPath destination, SubMonitor monitor, long length) throws IOException {
        try (OutputStream out = new FileOutputStream(destination.toFile())) {
            monitor.setWorkRemaining((int) (length / BYTES_PER_KB));
            byte[] buf = new byte[BYTES_PER_KB * BUFFER_IN_KB];
            int counter = 0;
            for (;;) {
                int n = in.read(buf);
                if (n <= 0) {
                    return;
                }
                out.write(buf, 0, n);
                counter = (counter % BYTES_PER_KB) + n;
                monitor.worked(counter / BYTES_PER_KB);
            }
        }
    }

    private static void importLiveTrace(final LttngRelaydConnectionInfo connectionInfo, final IProject project) {
        Job job = new Job(Messages.TraceControl_ImportJob) {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    // We initiate the connection first so that we can retrieve the trace path
                    LttngRelaydConsumer lttngRelaydConsumer = LttngRelaydConnectionManager.getInstance().getConsumer(connectionInfo);
                    try {
                        lttngRelaydConsumer.connect();
                    } catch (CoreException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, org.eclipse.tracecompass.internal.lttng2.control.ui.relayd.Messages.LttngRelaydConnectionManager_ConnectionError, e);
                    }
                    initializeTraceResource(connectionInfo, lttngRelaydConsumer.getTracePath(), project);
                    return Status.OK_STATUS;
                } catch (CoreException | TmfTraceImportException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ImportHandler_LiveTraceInitError, e);
                }
            }

        };
        job.setSystem(true);
        job.schedule();
    }


    private static void initializeTraceResource(final LttngRelaydConnectionInfo connectionInfo, final String tracePath, final IProject project) throws CoreException, TmfTraceImportException {
        IFolder folder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
        IFolder traceFolder = folder.getFolder(connectionInfo.getSessionName());
        Path location = new Path(tracePath);
        IStatus result = ResourcesPlugin.getWorkspace().validateLinkLocation(folder, location);
        if (result.isOK()) {
            traceFolder.createLink(location, IResource.REPLACE, new NullProgressMonitor());
        } else {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, result.getMessage()));
        }

        TraceTypeHelper selectedTraceType = TmfTraceTypeUIUtils.selectTraceType(location.toOSString(), null, null);
        // No trace type was determined.
        TmfTraceTypeUIUtils.setTraceType(traceFolder, selectedTraceType);

        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        final TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
        final List<TmfTraceElement> traces = tracesFolder.getTraces();
        TmfTraceElement found = null;
        for (TmfTraceElement candidate : traces) {
            if (candidate.getName().equals(connectionInfo.getSessionName())) {
                found = candidate;
            }
        }

        if (found == null) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ImportHandler_LiveTraceElementError));
        }

        // Properties used to be able to reopen a trace in live mode
        traceFolder.setPersistentProperty(CtfConstants.LIVE_HOST, connectionInfo.getHost());
        traceFolder.setPersistentProperty(CtfConstants.LIVE_PORT, Integer.toString(connectionInfo.getPort()));
        traceFolder.setPersistentProperty(CtfConstants.LIVE_SESSION_NAME, connectionInfo.getSessionName());

        final TmfTraceElement finalTrace = found;
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                TmfOpenTraceHelper.openTraceFromElement(finalTrace);
            }
        });
    }
}
