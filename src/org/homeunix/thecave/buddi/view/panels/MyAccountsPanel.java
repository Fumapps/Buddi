package org.homeunix.thecave.buddi.view.panels;

import ca.digitalcave.moss.swing.MossFrame;
import ca.digitalcave.moss.swing.MossPanel;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import java.util.Collections;
import java.util.List;

/*
 * Legacy Swing Panel - Disabled during MVVM Migration
 */
public class MyAccountsPanel extends MossPanel {
    public MyAccountsPanel(MossFrame parent) {
        super(true);
    }

    public void init() {
    }

    public void updateContent() {
    }

    // Stub methods referenced by MainFrame menus
    public List<Account> getSelectedAccounts() {
        return Collections.emptyList();
    }

    public List<AccountType> getSelectedAccountTypes() {
        return Collections.emptyList();
    }

    public void fireStructureChanged() {
    }
}
