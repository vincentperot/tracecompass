package org.eclipse.tracecompass.internal.tmf.ui.view;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.tracecompass.internal.tmf.core.parsers.ROIParser;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * @author ematkho
 *
 */
public class RegionsOfInterest extends TmfView {

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
    public RegionsOfInterest() {
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
                Collection<IMarker> markers= ROIParser.parse(contents);
                for( IMarker marker:markers){

                }
                System.out.println(contents);
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
