package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;

/**
 * TODO
 *
 */
public final class ConnectionTreeLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof IRemoteConnection) {
            IRemoteConnection rc = (IRemoteConnection) element;
            return rc.getName();
        } else if (element instanceof IRemoteConnectionType) {
            IRemoteConnectionType rs = (IRemoteConnectionType) element;
            return rs.getName();
        }
        return Messages.TraceControl_UnknownNode;
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IRemoteConnection) {
            return Activator.getDefault().loadIcon(NewConnectionDialog.CONNECTION_ICON_FILE);
        }
        return Activator.getDefault().loadIcon(NewConnectionDialog.PROVIDERS_ICON_FILE);
    }
}