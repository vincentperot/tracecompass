package org.eclipse.tracecompass.internal.tmf.ctf.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class SplitCtfTrace extends Wizard implements IImportWizard {

    private CtfCropWizardPage fPage;

    public SplitCtfTrace() {
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fPage = new CtfCropWizardPage("Split trace", selection);
        addPage(fPage);

    }

    @Override
    public boolean canFinish() {
        // TODO Auto-generated method stub
        return fPage.isReady();
    }

    @Override
    public boolean performFinish() {
        return fPage.finish();
    }

}
