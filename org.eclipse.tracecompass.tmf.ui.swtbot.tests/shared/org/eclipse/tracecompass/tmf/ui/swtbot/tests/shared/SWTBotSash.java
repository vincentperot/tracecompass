package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.hamcrest.SelfDescribing;

/**
 * SWT Bot sash control
 */
public class SWTBotSash extends AbstractSWTBotControl<Sash> {

    /**
     * The widget wrapper
     * @param w the sash
     * @param description the description
     * @throws WidgetNotFoundException if there is no widget
     */
    public SWTBotSash(Sash w, SelfDescribing description) throws WidgetNotFoundException {
        super(w, description);
    }

    /**
     * Get the central point of the sash
     * @return the center point, good for dragging
     */
    public Point getPoint() {
        return UIThreadRunnable.syncExec(new Result<Point>() {

            @Override
            public Point run() {
                Rectangle rect = widget.getBounds();
                return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
            }

        });
    }

    private Event makeMouseEvent(Point p, boolean click) {
        return createMouseEvent(p.x, p.y, 1, click ? SWT.BUTTON1 : null, click ? 1 : 0);
    }

    /**
     * Simulate a drag
     * @param dst to this destination
     */
    public void drag(final Point dst) {
        final Point src = getPoint();
        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                Event meMove = makeMouseEvent(src, false);
                display.post(meMove);
                Event meDown = makeMouseEvent(src, true);
                display.post(meDown);
                Event meMoveTarget = makeMouseEvent(dst, false);
                display.post(meMoveTarget);
                Event meUp = makeMouseEvent(dst, true);
                display.post(meUp);
            }
        });
    }

}
