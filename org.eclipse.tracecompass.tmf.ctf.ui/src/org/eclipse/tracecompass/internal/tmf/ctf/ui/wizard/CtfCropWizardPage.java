package org.eclipse.tracecompass.internal.tmf.ctf.ui.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.tmf.ctf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConflictHandler;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.barcharts.TmfBarChartViewer;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.dialogs.WizardResourceImportPage;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.swtchart.ISeries;

public class CtfCropWizardPage extends WizardResourceImportPage {

    private final class PreviewWindow extends TmfBarChartViewer {
        private CTFTrace fVisTrace;

        private PreviewWindow(Composite parent, String title, String xLabel, String yLabel, int barWidth) {
            super(parent, title, xLabel, yLabel, barWidth);
            getSwtChart().getAxisSet().getYAxis(0).getTick().setVisible(false);
        }

        public void setTrace(CTFTrace trace) {
            fVisTrace = trace;
            addSeries(fVisTrace.toString(), new RGB(128, 128, 255));
            setEndTime(getTimeEnd());
            setStartTime(getTimeStart());
            getSwtChart().getAxisSet().getYAxis(0).getTick().setVisible(false);
            setWindowStartTime(getStartTime());
            setWindowEndTime(getEndTime());
            getSwtChart().getLegend().setVisible(false);
            updateContent();
        }

        @Override
        protected void readData(ISeries series, long start, long end, int nb) {
            if (fTrace != null) {
                double[] y = fVisTrace.getPreview(nb);
                double[] x = getXAxis(start, end, nb);
                drawChart(series, x, y);
            }
        }
    }

    public CtfCropWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle("cropper");
        setDescription("Cut up ctf files");
        root = ResourcesPlugin.getWorkspace().getRoot();

      // Locate the target trace folder
        IFolder traceFolder = null;
        Object element = selection.getFirstElement();

        if (element instanceof TmfTraceFolder) {
            fTmfTraceFolder = (TmfTraceFolder) element;
            traceFolder = fTmfTraceFolder.getResource();
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                    fTmfTraceFolder = projectElement.getTracesFolder();
                    traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

        // If no tracing project was selected or trace folder doesn't exist use
        // default tracing project
        if (traceFolder == null) {
            IProject project = TmfProjectRegistry.createProject(
                    TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
            fTmfTraceFolder = projectElement.getTracesFolder();
            traceFolder = project.getFolder(TmfTracesFolder.TRACES_FOLDER_NAME);
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = traceFolder;
            String path = traceFolder.getFullPath().toString();
            setContainerFieldValue(path);
        }

        if (fTmfTraceFolder == null) {
            throw new IllegalArgumentException();
        }

    }

//    private boolean fProjectNameSetByUser;
    private CTFTrace fTrace;
    private long fTimeStart;
    private long fTimeEnd;
    private Text fStart;
    private Text fEnd;
    private PreviewWindow fHistogram;
    Text fProjectName;
    Text fLocation;
    IWorkspaceRoot root;
    Job tracePopulator;

//    private IStructuredSelection fSelection;
    private TmfTraceFolder fTmfTraceFolder;
    private IFolder fTargetFolder;

    /**
     * Validates the contents of the page, setting the page error message and
     * Finish button state accordingly
     */
    private void validatePage() {
//        // Don't generate an error if project name or location is empty, but do
//        // disable Finish button.
//        String msg = null;
//        boolean complete = true; // ultimately treated as false if msg != null
//
//        String name = fProjectName.getText().trim();
//        if (name.isEmpty()) {
//            complete = false;
//        } else {
//            IStatus status = ResourcesPlugin.getWorkspace().validateName(name,
//                    IResource.FOLDER);
//            if (!status.isOK()) {
//                msg = status.getMessage();
//            } else {
//                IProject project = root.getProject(name);
//                if (project.exists()) {
//                    msg = "project";
//                }
//            }
//        }
//        if (msg == null) {
//            String loc = fLocation.getText().trim();
//            if (loc.isEmpty()) {
//                complete = false;
//            } else {
//                final File file = new File(loc);
//                if (file.isDirectory()) {
//                    // Set the project name to the directory name but not if the
//                    // user has supplied a name
//                    if (!fProjectNameSetByUser && !name.equals(file.getName())) {
//                        fProjectName.setText(file.getName());
//                    }
//                } else {
//                }
//            }
//        }
//
//        setErrorMessage(msg);
//        setPageComplete((msg == null) && complete);
        // TODO
        setPageComplete(true);
    }

    private void addTraceSelector(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText("CTF Trace Location");

        fLocation = new Text(group, SWT.BORDER);
        fLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fLocation.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validatePage();
            }
        });
        validatePage();

        Button browse = new Button(group, SWT.NONE);
        browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(fLocation
                        .getShell());
                dialog.setMessage("Select a CTF trace directory");
                final String dir = dialog.open();
                if (dir != null) {
                    try {
                        final CTFTrace trace = new CTFTrace(dir);
                        fTrace = trace;
                        fLocation.setText(dir);
                        tracePopulator = new Job(dir) {

                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                monitor.beginTask("index trace", 1);
                                try (CTFTraceReader tr = new CTFTraceReader(
                                        trace)) {
                                    tr.populateIndex();
                                    monitor.done();
                                    return Status.OK_STATUS;
                                } catch (CTFReaderException readException) {
                                    Activator.logError(readException.getMessage(), readException);
                                }
                                validatePage();
                                return new Status(IStatus.ERROR, "me", "failed reading trace");
                            }

                        };
                        tracePopulator.schedule();
                        tracePopulator.join();
                        if (tracePopulator.getResult().isOK()) {
                            setTimeStart(trace.getCurrentStartTime());
                            fStart.setText(Long.toString(getTimeStart()));
                            setTimeEnd(trace.getCurrentEndTime());
                            fEnd.setText(Long.toString(getTimeEnd()));
                            fHistogram.setTrace(trace);
                        }
                    } catch (CTFReaderException | InterruptedException e1) {
                        // swallow me
                    }
                }
                validatePage();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void addSimpleTimerangeSelector(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText("Time Range");
        GridData layoutData = GridDataFactory.fillDefaults().grab(true, false).create();
        GridLayout layout = GridLayoutFactory.swtDefaults().numColumns(2).create();
        Composite comp = new Composite(group, SWT.NONE);
        comp.setLayoutData(layoutData);
        comp.setLayout(layout);
        Label title = new Label(comp, SWT.NONE);
        title.setText("Start time");
        fStart = new Text(comp, SWT.NONE);
        fStart.setLayoutData(layoutData);
        comp = new Composite(group, SWT.NONE);
        comp.setLayoutData(layoutData);
        comp.setLayout(layout);
        title = new Label(comp, SWT.NONE);
        title.setText("End time");
        fEnd = new Text(comp, SWT.NONE);
        fEnd.setLayoutData(layoutData);
    }

    private void addVisualTimerangeSelector(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(GridLayoutFactory.fillDefaults().create());
        group.setSize(64, 64);
        group.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        group.setText("time range");
        fHistogram = new PreviewWindow(group, null, "time", "events", 1);
        fHistogram.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

    }

    long getTimeStart() {
        return fTimeStart;
    }

    private void setTimeStart(long timeStart) {
        fTimeStart = timeStart;
    }

    long getTimeEnd() {
        return fTimeEnd;
    }

    private void setTimeEnd(long timeEnd) {
        fTimeEnd = timeEnd;
    }

    /**
     * Is the trace ready to be spliced?
     *
     * @return true if ready
     */
    public boolean isReady() {
        return (getTimeEnd() > 0) && (getTimeStart() > 0);
    }

    protected CTFTrace getTrace() {
        return fTrace;
    }

    /**
     * Finishes the wizard page.
     *
     * @return <code>true</code> if successful else <code>false</code>
     */
    public boolean finish() {

        final MyImportOperation importOperation = new MyImportOperation();
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    importOperation.run(monitor);
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            ErrorDialog.openError(getShell(), "Spliting Error", "Error during splitting of trace",
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e));

//            handleError(
//                    Messages.TracePackageExtractManifestOperation_ErrorReadingManifest,
//                    e);
            return false;
        } catch (InterruptedException e) {
            // Cancelled
            return false;
        }

        IStatus status = importOperation.getStatus();
        if (status.getSeverity() == IStatus.ERROR) {
            ErrorDialog.openError(getShell(), "Spliting Error", "Error during splitting of trace", status);
//            handleErrorStatus(status);
            return false;
        }

        return true;
    }

    class MyImportOperation extends TmfWorkspaceModifyOperation {

        IStatus fStatus;
        private ImportConflictHandler fConflictHandler;

        public MyImportOperation() {
            fConflictHandler = new ImportConflictHandler(getContainer().getShell(), fTmfTraceFolder, ImportConfirmation.SKIP);
        }

        @Override
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
            try {

                IPath bla = new Path(fTrace.getTraceDirectory().toString());
                String traceName = bla.lastSegment() + ".cropped";

                // Temporary directory to contain any extracted files
                IFolder destTempFolder = fTargetFolder.getProject().getFolder(".tracecrop");
                if (destTempFolder.exists()) {
                    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                    destTempFolder.delete(true, subMonitor);
                }
                SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                destTempFolder.create(IResource.HIDDEN, true, subMonitor);

                IPath tmpTrace = destTempFolder.getLocation().append(traceName);

//                String path = fTargetFolder.getLocation().toString() + "/cropped";
                // TODO pass on monitor
//                String path = traceFolder.getFullPath().toString();
                fTrace.crop(getTimeStart(), getTimeEnd(), tmpTrace.toString());
                TraceTypeHelper traceTypeHelper = null;

                try {
                    traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(tmpTrace.toString(), null, null);
                } catch (TmfTraceImportException e) {
                    // the trace did not match any trace type
                }

                if (traceTypeHelper == null) {
                    throw new CTFReaderException("trace type cannot be set");
                }
//                IResource importedResource = ResourcesPlugin.getWorkspace().getRoot().findMember(fTargetFolder + "/cropped");
                IResource importedResource = importResource(tmpTrace, monitor);

                TmfTraceTypeUIUtils.setTraceType(importedResource, traceTypeHelper);

                if (destTempFolder.exists()) {
                    subMonitor = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                    destTempFolder.delete(true, subMonitor);
                }

            } catch (CTFReaderException e) {
                fStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
                return;
            }
            fStatus = Status.OK_STATUS;
        }


        private IResource importResource(IPath sourcePath, IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException, CoreException {

            String traceName = sourcePath.lastSegment();

            IPath tracePath = fTmfTraceFolder.getPath().append(traceName);



            String newName = fConflictHandler.checkAndHandleNameClash(tracePath, monitor);
            if (newName == null) {
                return null;
            }

            tracePath = fTmfTraceFolder.getPath().append(newName);

            List<File> subList = new ArrayList<>();

            File sourceDirectory = new File(sourcePath.toString());

            IPath containerPath = fTargetFolder.getFullPath();
                containerPath = tracePath;

                File[] array = sourceDirectory.listFiles();
                for (int i = 0; i < array.length; i++) {
                    if (!array[i].isDirectory()) {
                        subList.add(array[i]);
                    }
                }

                FileSystemStructureProvider fileSystemStructureProvider = FileSystemStructureProvider.INSTANCE;

            IOverwriteQuery myQueryImpl = new IOverwriteQuery() {
                @Override
                public String queryOverwrite(String file) {
                    return IOverwriteQuery.NO_ALL;
                }
            };

//            monitor.setTaskName(Messages.ImportTraceWizard_ImportOperationTaskName + " " + fileSystemElement.getFileSystemObject().getAbsolutePath(fBaseSourceContainerPath.toOSString())); //$NON-NLS-1$
            ImportOperation operation = new ImportOperation(containerPath, sourceDirectory, fileSystemStructureProvider, myQueryImpl, subList);
            operation.setContext(getShell());

            operation.setCreateContainerStructure(false);
            operation.setOverwriteResources(false);
            operation.setCreateLinks(false);
            operation.setVirtualFolders(false);

            operation.run(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
//            String sourceLocation = fileSystemElement.getSourceLocation();
            IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(tracePath);
//            if (sourceLocation != null) {
//                resource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);

            return resource;
        }

        public IStatus getStatus() {
            return fStatus;
        }
    }

    @Override
    protected void createSourceGroup(Composite parent) {
        addTraceSelector(parent);
        addSimpleTimerangeSelector(parent);
        addVisualTimerangeSelector(parent);
    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        // TODO Auto-generated method stub
        return null;
    }

}