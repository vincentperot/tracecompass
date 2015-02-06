package org.eclipse.tracecompass.internal.tmf.ui.view;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.tracecompass.internal.tmf.core.parsers.PointOfInterest;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.PlatformUI;

/**
 * @author ematkho
 *
 */
public class PointsOfInterestView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.regionsofinterest"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private Composite fCanvas = null;
    private Group fDropzone = null;

    /**
     *
     */
    public PointsOfInterestView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        fCanvas = new Composite(parent, SWT.NONE);
        fCanvas.setLayout(new FillLayout());
        fDropzone = new Group(fCanvas, SWT.BORDER_SOLID);
        fDropzone.setText("Click here with a report in a clipboard");
        fDropzone.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                Clipboard cb = new Clipboard(e.display);
                String contents = cb.getContents(TextTransfer.getInstance()).toString();
                cb.dispose();
                Collection<PointOfInterest> markers = PointOfInterest.parse(contents);
                ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
                TmfTimeRange timeRange = activeTrace.getTimeRange();
                IResource res = activeTrace.getResource();

                for (PointOfInterest poi : markers) {
                    ITmfTimestamp ts = poi.getOffsetTimestamp(timeRange);
                    if (ts != null) {
                        try {
                            TmfEventsEditor ed = (TmfEventsEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                            IResource bookmarksFile = ed.getFile();
                            final IMarker bookmark = bookmarksFile.createMarker(IMarker.BOOKMARK);
                            if (bookmark.exists()) {
                                bookmark.setAttribute(IMarker.MESSAGE, poi.getMessage());
                                long eventsCount = activeTrace.getNbEvents();

                                ITmfTimestamp endTime = new TmfNanoTimestamp(activeTrace.getTimeRange().getEndTime());
                                ITmfTimestamp startTime = new TmfNanoTimestamp(activeTrace.getTimeRange().getStartTime());
                                long offset = ts.getValue() - startTime.getValue();
                                long duration = endTime.getValue() - startTime.getValue();
                                final Long rank = Long.valueOf((long) ( (double) offset / (double) duration *eventsCount));
                                final int location = rank.intValue();
                                bookmark.setAttribute(IMarker.LOCATION, Integer.valueOf(location));
                            }
                            IMarker marker = res.createMarker(poi.getMessage());
                            marker.setAttribute(IMarker.BOOKMARK, poi.getMessage());

                        } catch (CoreException e1) {
                        }
                        System.out.println(ts.toString() + " " + poi.getMessage());
                    }
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public void setFocus() {

    }

}
