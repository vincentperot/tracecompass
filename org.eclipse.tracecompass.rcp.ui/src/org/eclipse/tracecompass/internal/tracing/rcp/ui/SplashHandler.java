/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tracing.rcp.ui;


import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.splash.BasicSplashHandler;

/**
 * Custom splash handler
 *
 * @author Bernd Hufmann
 *
 */
public class SplashHandler extends BasicSplashHandler {

    @Override
    public void init(Shell splash) {

        super.init(splash);
        String progressString = null;
        String messageString = null;

        // Try to get the progress bar and message updater.
        IProduct product = Platform.getProduct();
        if(product != null) {
            progressString = product.getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
        }
        Rectangle progressRect = StringConverter.asRectangle(progressString, new Rectangle(10, 300, 480, 15));
        setProgressRect(progressRect);

        // Use height zero to hide it (a little trick)
        Rectangle messageRect = StringConverter.asRectangle(messageString, new Rectangle(10, 280, 490, 0));
        setMessageRect(messageRect);

        //Set font color.
        try {
            setForeground(getSplash().getDisplay().getSystemColor(SWT.COLOR_WHITE).getRGB());
        } catch(IllegalArgumentException e) {
        }

        // Set the software version.
        final Point softwareVersion = new Point(10, 280);
        getContent().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setForeground(getForeground());
                e.gc.drawText(NLS.bind(Messages.SplahScreen_VersionString, TracingRcpPlugin.getDefault().getBundle().getVersion().toString()), softwareVersion.x, softwareVersion.y, true);
            }
        });
    }
}
