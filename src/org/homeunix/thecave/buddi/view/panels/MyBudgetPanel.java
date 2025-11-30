package org.homeunix.thecave.buddi.view.panels;

import ca.digitalcave.moss.swing.MossFrame;
import ca.digitalcave.moss.swing.MossPanel;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
 * Legacy Swing Panel - Disabled during MVVM Migration
 */
public class MyBudgetPanel extends MossPanel {
    public MyBudgetPanel(MossFrame parent) {
        super(true);
    }

    public void init() {
    }

    public void updateContent() {
    }

    // Stub methods referenced by MainFrame menus
    public List<BudgetCategory> getSelectedBudgetCategories() {
        return Collections.emptyList();
    }

    public BudgetCategoryType getSelectedPeriodType() {
        return null;
    }

    public Date getSelectedDate() {
        return new Date();
    }

    public org.homeunix.thecave.buddi.model.swing.MyBudgetTreeTableModel getTreeTableModel() {
        return null;
    }

    public void fireStructureChanged() {
    }

    public void setPreviousPeriod() {
    }

    public void setNextPeriod() {
    }
}
