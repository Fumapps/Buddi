/*
 * Created on Aug 5, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.view.swing;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.util.InternalFormatter;

public class MyBudgetTableAmountCellRenderer extends DefaultTableCellRenderer {
	public static final long serialVersionUID = 0;

	private final StringBuilder sb = new StringBuilder(); 
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value instanceof Object[]){
			Object[] values = (Object[]) value;

			if (sb.length() > 0)
				sb.delete(0, sb.length());

			BudgetCategory category = (BudgetCategory) values[0];
			long budgetAmount = (Long) values[1];
			long childTotal = (Long) values[2];
			int depth = (Integer) values[3];
			long actual = (Long) values[4];
			long actualIncludingSubs = (Long) values[5];

			boolean hasBudgetValue = budgetAmount != 0 || childTotal != 0;
			boolean hasActualValue = actual != 0 || actualIncludingSubs != 0;
			boolean showCurrentBudget = PrefsModel.getInstance().isShowCurrentBudget();

			if (!hasBudgetValue) {
				if (hasActualValue && !showCurrentBudget) {
					TextFormatter.appendFormattedCurrency(sb, actual, actual < 0,
						InternalFormatter.isRed(category, actual));
					if (actualIncludingSubs != actual) {
						sb.append(" (");
						TextFormatter.appendFormattedCurrency(sb, actualIncludingSubs, actualIncludingSubs < 0,
							InternalFormatter.isRed(category, actualIncludingSubs));
						sb.append(")");
					}
				}
				else {
					//To make the table easier to read, we don't include $0.00 in it; we use --- instead.
					sb.append("---");
				}
			}
			else {
				//Display the amount for this budget category
				TextFormatter.appendFormattedCurrency(sb, budgetAmount, 
					InternalFormatter.isRed(category, budgetAmount), false);
				
				//If there is anything in sub categories, add it in brackets.
				if (childTotal != budgetAmount && childTotal != 0){
					sb.append(" (");
					TextFormatter.appendFormattedCurrency(sb,
						childTotal, 
						InternalFormatter.isRed(category, childTotal),
						false);
					sb.append(")");
				}
			}

			if (showCurrentBudget && hasBudgetValue){
				sb.append(" / ");
				if(actual != 0) {
					TextFormatter.appendFormattedCurrency(sb, actual, actual < 0, actual < 0);
				} else {
					sb.append("---");
				}

				if(actualIncludingSubs != actual) {
					sb.append(" (");
					TextFormatter.appendFormattedCurrency(sb, actualIncludingSubs, actualIncludingSubs < 0,
						actualIncludingSubs < 0);
					sb.append(")");
				}
			}

			for (int i = 0; i < depth; i++){
				sb.insert(0, "&nbsp&nbsp&nbsp "); 
			}
			
			sb.insert(0, "<html>");
			sb.append("</html>");
			this.setText(sb.toString());
		}

		if (hasFocus && isSelected) {
			table.editCellAt(row,column);
			table.getCellEditor(row, column).getTableCellEditorComponent(table, value, isSelected, row, column).requestFocus();
		}

		return this;
	}
}
