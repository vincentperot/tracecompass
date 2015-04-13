package org.eclipse.tracecompass.internal.tmf.ctf.ui.wizard;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.barcharts.TmfBarChartViewer;
import org.swtchart.ISeries;

public class CtfCropWizardPage extends WizardPage {

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

    public CtfCropWizardPage(String pageName) {
        super(pageName);
        setTitle("cropper");
        setDescription("Cut up ctf files");
        root = ResourcesPlugin.getWorkspace().getRoot();
    }

    private boolean fProjectNameSetByUser;
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

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        addProjectNameSelector(comp);
        addTraceSelector(comp);
        addSimpleTimerangeSelector(comp);
        addVisualTimerangeSelector(comp);

        setControl(comp);
    }

    private void addProjectNameSelector(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText("Select Folder to import to");

        fProjectName = new Text(group, SWT.BORDER);
        fProjectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fProjectName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validatePage();
                if (fProjectName.getText().trim().isEmpty()) {
                    fProjectNameSetByUser = false;
                }
            }
        });

        // Note that the modify listener gets called not only when the user
        // enters text but also when we
        // programatically set the field. This listener only gets called when
        // the user modifies the field
        fProjectName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                fProjectNameSetByUser = true;
            }
        });
    }

    /**
     * Validates the contents of the page, setting the page error message and
     * Finish button state accordingly
     */
    private void validatePage() {
        // Don't generate an error if project name or location is empty, but do
        // disable Finish button.
        String msg = null;
        boolean complete = true; // ultimately treated as false if msg != null

        String name = fProjectName.getText().trim();
        if (name.isEmpty()) {
            complete = false;
        } else {
            IStatus status = ResourcesPlugin.getWorkspace().validateName(name,
                    IResource.FOLDER);
            if (!status.isOK()) {
                msg = status.getMessage();
            } else {
                IProject project = root.getProject(name);
                if (project.exists()) {
                    msg = "project";
                }
            }
        }
        if (msg == null) {
            String loc = fLocation.getText().trim();
            if (loc.isEmpty()) {
                complete = false;
            } else {
                final File file = new File(loc);
                if (file.isDirectory()) {
                    // Set the project name to the directory name but not if the
                    // user has supplied a name
                    if (!fProjectNameSetByUser && !name.equals(file.getName())) {
                        fProjectName.setText(file.getName());
                    }
                } else {
                }
            }
        }

        setErrorMessage(msg);
        setPageComplete((msg == null) && complete);
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

}