/*
 * Created on Jul 30, 2007 by wyatt
 * Refactored to use MVVM pattern via MyAccountsViewModel.
 */
package org.homeunix.thecave.buddi.view.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.homeunix.thecave.buddi.Const;
import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.model.swing.MyAccountTreeTableModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.MainFrame;
import org.homeunix.thecave.buddi.view.menu.items.EditEditTransactions;
import org.homeunix.thecave.buddi.view.swing.MyAccountTableAmountCellRenderer;
import org.homeunix.thecave.buddi.view.swing.MyAccountTreeNameCellRenderer;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import ca.digitalcave.moss.common.OperatingSystemUtil;
import ca.digitalcave.moss.swing.MossPanel;

public class MyAccountsPanel extends MossPanel {
	public static final long serialVersionUID = 0;

	private final JXTreeTable tree;
	private final JLabel balanceLabel;

	private final MyAccountTreeTableModel treeTableModel;
	private final MyAccountsViewModel viewModel;

	private final MainFrame parent;

	private final PropertyChangeListener viewModelListener;

	public MyAccountsPanel(MainFrame parent) {
		super(true);
		this.parent = parent;
		this.treeTableModel = new MyAccountTreeTableModel((Document) parent.getDocument());
		this.viewModel = new MyAccountsViewModel((Document) parent.getDocument());

		viewModelListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (MyAccountsViewModel.PROPERTY_NET_WORTH_TEXT.equals(evt.getPropertyName())) {
					updateNetWorthLabel();
				} else if (MyAccountsViewModel.PROPERTY_ACCOUNT_TREE_CHANGED.equals(evt.getPropertyName())) {
					updateTreeExpansionStates();
				}
			}
		};

		tree = new JXTreeTable(){
			public static final long serialVersionUID = 0;
			@Override
			public String getToolTipText(MouseEvent event) {
				if (PrefsModel.getInstance().isShowTooltips()){
					Point p = event.getPoint();
					int rowIndex = rowAtPoint(p);

					if (this.getPathForRow(rowIndex) != null){
						Object[] path = this.getPathForRow(rowIndex).getPath();
						if (path != null && path.length > 0){
							Object node = path[path.length - 1];
							if (node instanceof Account)
								return ((Account) node).getNotes();
						}
					}
				}
				return null;
			}
		};
		tree.setColumnFactory(new ColumnFactory(){
			@Override
			public void configureTableColumn(TableModel arg0, TableColumnExt arg1) {
				super.configureTableColumn(arg0, arg1);

				arg1.setCellRenderer(new MyAccountTableAmountCellRenderer((Document) MyAccountsPanel.this.parent.getDocument()));
			}
		});
		tree.setTreeTableModel(treeTableModel);
		balanceLabel = new JLabel("Change Me");

		open();
	}

	public List<Account> getSelectedAccounts(){
		return viewModel.getSelectedAccounts(tree.getSelectedRows() != null ? 
			convertRowIndicesToValues(tree.getSelectedRows()) : null);
	}

	public List<AccountType> getSelectedTypes(){
		return viewModel.getSelectedAccountTypes(tree.getSelectedRows() != null ? 
			convertRowIndicesToValues(tree.getSelectedRows()) : null);
	}

	/**
	 * Convert tree row indices to their corresponding values (Account or AccountType objects).
	 */
	private Object[] convertRowIndicesToValues(int[] rowIndices) {
		if (rowIndices == null || rowIndices.length == 0) {
			return new Object[0];
		}
		Object[] values = new Object[rowIndices.length];
		for (int i = 0; i < rowIndices.length; i++) {
			values[i] = tree.getModel().getValueAt(rowIndices[i], -1);
		}
		return values;
	}

	public void actionPerformed(ActionEvent e) {

	}

	public void init() {
		super.init();

		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		tree.setClosedIcon(null);
		tree.setOpenIcon(null);
		tree.setLeafIcon(null);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addHighlighter(HighlighterFactory.createAlternateStriping(Const.COLOR_EVEN_ROW, Const.COLOR_ODD_ROW));

		tree.setTreeCellRenderer(new MyAccountTreeNameCellRenderer());

		// Subscribe to ViewModel changes
		viewModel.addPropertyChangeListener(viewModelListener);

		JScrollPane listScroller = new JScrollPane(tree);

		JPanel balanceLabelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		balanceLabelPanel.add(balanceLabel);

		JPanel listScrollerPanel = new JPanel(new BorderLayout());
		listScrollerPanel.add(listScroller, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel(); 
		mainPanel.setLayout(new BorderLayout());

		mainPanel.add(listScrollerPanel, BorderLayout.CENTER);
		mainPanel.add(balanceLabelPanel, BorderLayout.SOUTH);

		if (OperatingSystemUtil.isMac()){
			listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}

		tree.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent arg0) {
				parent.updateButtons();
			}
		});

		tree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() >= 2)
					new EditEditTransactions(parent).doClick();
				super.mouseClicked(arg0);
			}
		});

		tree.addTreeExpansionListener(new TreeExpansionListener(){
			public void treeCollapsed(TreeExpansionEvent event) {
				Object o = event.getPath().getPath()[event.getPath().getPath().length - 1];
				if (o instanceof AccountType){
					AccountType t = (AccountType) o;
					viewModel.setAccountTypeExpanded(t, false);
				}
			}
			public void treeExpanded(TreeExpansionEvent event) {
				Object o = event.getPath().getPath()[event.getPath().getPath().length - 1];
				if (o instanceof AccountType){
					AccountType t = (AccountType) o;
					viewModel.setAccountTypeExpanded(t, true);
				}				
			}
		});

		updateButtons();

		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Update the net worth label from the ViewModel.
	 * Called when the ViewModel's net worth property changes.
	 */
	private void updateNetWorthLabel() {
		balanceLabel.setText(viewModel.getNetWorthText());
	}

	/**
	 * Update tree expansion states from the ViewModel.
	 * Called when the ViewModel's tree structure changes.
	 */
	private void updateTreeExpansionStates() {
		// Restore the state of the expanded / unrolled nodes.
		for (AccountType t : viewModel.getAccountTypes()) {
			TreePath path = new TreePath(new Object[]{treeTableModel.getRoot(), t});
			if (t.isExpanded())
				tree.expandPath(path);
			else
				tree.collapsePath(path);
		}
		tree.invalidate();
	}

	/**
	 * Called by MainFrame to trigger a refresh of the tree structure.
	 * Notifies the underlying tree table model to fire a structure change event.
	 */
	public void fireStructureChanged(){
		treeTableModel.fireStructureChanged();
	}
}
