/*
 * MonomerMergeManager.java
 *
 * Created on November 24, 2008, 12:59 PM
 */
package org.helm.editor.manager;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.model.Monomer;
import org.jdom.JDOMException;

import java.awt.Cursor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 
 * @author Stefanie Maisel
 */
public class MonomerMergeManager extends javax.swing.JDialog {

	private static final String defaultPolymerType = "PEPTIDE";
	private MacromoleculeEditor editor;
	private MonomerViewer viewer;
	private javax.swing.JButton closeButton;
	private javax.swing.JPanel monomerDetailPanel;
	private javax.swing.JPanel monomerListPanel;
	private org.jdesktop.swingx.JXTable monomerTable;
	private javax.swing.JPanel monomerViewerPanel;
	private javax.swing.JComboBox polymerTypeComboBox;
	private javax.swing.JLabel polymerTypeLabel;
	private javax.swing.JButton registerButton;
	private javax.swing.JScrollPane tableScrollPane;

	/** Creates new form MonomerMergeManager */
	public MonomerMergeManager(MacromoleculeEditor editor, boolean modal) {
		super(editor.getFrame(), modal);
		this.editor = editor;
		initComponents();

		viewer = MonomerViewer.getNamedInstance("MonomerMergeManager");
		viewer.setModifiableStatus(false);
		monomerViewerPanel.add(viewer);
	}

	
	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	private void initComponents() {

		polymerTypeLabel = new javax.swing.JLabel();

		monomerListPanel = new javax.swing.JPanel();
		tableScrollPane = new javax.swing.JScrollPane();
		monomerTable = new org.jdesktop.swingx.JXTable();
		monomerDetailPanel = new javax.swing.JPanel();
		monomerViewerPanel = new javax.swing.JPanel();
		registerButton = new javax.swing.JButton();
		closeButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("HELM Editor Monomer Merge Dialog");

		polymerTypeLabel.setText("Polymer Type");

		MonomerStore store = MonomerStoreCache.getInstance()
				.getUnregisteredMonomers();
		if (store != null && store.getPolymerTypeSet().size() > 0) {

			Set<String> polymerTypeSet = store.getPolymerTypeSet();

			String[] polymerTypes = Arrays.copyOf(polymerTypeSet.toArray(),
					polymerTypeSet.toArray().length, String[].class);

			polymerTypeComboBox = new javax.swing.JComboBox(polymerTypes);

			String polymerType = (String) store.getPolymerTypeSet().toArray()[0];
			polymerTypeComboBox.setSelectedItem(polymerType);

			MonomerTableModel model = new MonomerTableModel(polymerType, store);
			monomerTable.setModel(model);

		} else {
			polymerTypeComboBox = new javax.swing.JComboBox();
			monomerTable.setModel(new javax.swing.table.DefaultTableModel(
					new Object[][] {}, new String[] { "Symbol",
							"Natural Analog", "Name", "Structure" }));
		}

		polymerTypeComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						polymerTypeComboBoxActionPerformed(evt);
					}
				});

		monomerListPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Monomer List"));

		monomerTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int rowIndex = monomerTable.getSelectedRow();
						int row = monomerTable.convertRowIndexToModel(rowIndex);
						if (rowIndex >= 0
								&& rowIndex < monomerTable.getRowCount()) {
							Monomer monomer = ((MonomerTableModel) monomerTable
									.getModel()).getMonomerList().get(row);
							viewer.setMonomer(monomer);
						}

					}
				});

		tableScrollPane.setViewportView(monomerTable);

		// Layout

		org.jdesktop.layout.GroupLayout monomerListPanelLayout = new org.jdesktop.layout.GroupLayout(
				monomerListPanel);
		monomerListPanel.setLayout(monomerListPanelLayout);
		monomerListPanelLayout.setHorizontalGroup(monomerListPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(monomerListPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.add(tableScrollPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								717, Short.MAX_VALUE).addContainerGap()));
		monomerListPanelLayout.setVerticalGroup(monomerListPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(monomerListPanelLayout
						.createSequentialGroup()
						.add(tableScrollPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								237, Short.MAX_VALUE).addContainerGap()));

		monomerDetailPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Monomer Detail"));

		monomerViewerPanel.setBorder(javax.swing.BorderFactory
				.createEtchedBorder());
		monomerViewerPanel.setLayout(new java.awt.BorderLayout());

		registerButton.setText("Register");
		registerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				registerButtonActionPerformed(evt);
			}
		});

		closeButton.setText("Close");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeButtonActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout monomerDetailPanelLayout = new org.jdesktop.layout.GroupLayout(
				monomerDetailPanel);
		monomerDetailPanel.setLayout(monomerDetailPanelLayout);
		monomerDetailPanelLayout
				.setHorizontalGroup(monomerDetailPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								monomerDetailPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(monomerDetailPanelLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.TRAILING)
												.add(org.jdesktop.layout.GroupLayout.LEADING,
														monomerViewerPanel,
														org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
														717, Short.MAX_VALUE)
												.add(org.jdesktop.layout.GroupLayout.LEADING,
														monomerDetailPanelLayout
																.createSequentialGroup()
																.add(registerButton)
																.addPreferredGap(
																		org.jdesktop.layout.LayoutStyle.RELATED,
																		403,
																		Short.MAX_VALUE)
																.add(closeButton)))
										.addContainerGap()));
		monomerDetailPanelLayout
				.setVerticalGroup(monomerDetailPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								monomerDetailPanelLayout
										.createSequentialGroup()
										.add(monomerViewerPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												256, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(monomerDetailPanelLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.BASELINE)
												.add(closeButton,
														org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
														28, Short.MAX_VALUE)
												.add(registerButton))
										.addContainerGap()));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.add(layout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.TRAILING)
										.add(org.jdesktop.layout.GroupLayout.LEADING,
												monomerDetailPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.add(org.jdesktop.layout.GroupLayout.LEADING,
												monomerListPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.add(org.jdesktop.layout.GroupLayout.LEADING,
												layout.createSequentialGroup()
														.add(polymerTypeLabel)
														.add(18, 18, 18)
														.add(polymerTypeComboBox,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																115,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(38, 38, 38)
										// .add(saveButton)
										)).addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.BASELINE)
								.add(polymerTypeLabel)
								.add(polymerTypeComboBox,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						// .add(saveButton)
						)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED)
						.add(monomerListPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED)
						.add(monomerDetailPanel,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE).addContainerGap()));

		pack();
	}
	
	public void refreshContent(String polymerType) {
		MonomerStore store = MonomerStoreCache.getInstance()
				.getUnregisteredMonomers();

		MonomerTableModel model = new MonomerTableModel(polymerType, store);
		monomerTable.setModel(model);
	}

	
	public MacromoleculeEditor getEditor() {
		return editor;
	}

	public MonomerViewer getMonomerViewer() {
		return viewer;
	}

	public String getPolymerType() {
		return (String) polymerTypeComboBox.getSelectedItem();
	}

	

	private void polymerTypeComboBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		String polymerType = (String) polymerTypeComboBox.getSelectedItem();
		monomerTable
				.setModel(new MonomerTableModel(polymerType, MonomerStoreCache
						.getInstance().getUnregisteredMonomers()));
	}

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		setVisible(false);
	}

	private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int rowIndex = monomerTable.getSelectedRow();
		int row = monomerTable.convertRowIndexToModel(rowIndex);
		if (rowIndex >= 0 && rowIndex < monomerTable.getRowCount()) {
			Monomer monomer = ((MonomerTableModel) monomerTable.getModel())
					.getMonomerList().get(row);
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				monomer.setNewMonomer(false);
				monomer.setAdHocMonomer(false);


				MonomerStore localStore = MonomerFactory.getInstance().getMonomerStore();
				if (!localStore.hasMonomer(monomer.getPolymerType(), monomer.getAlternateId())){
					localStore.addMonomer(monomer);		
				} 
								
				MonomerFactory.getInstance().saveMonomerCache();

				refreshContent(getPolymerType());
				getEditor().updatePolymerPanels();

				JOptionPane.showMessageDialog(getParent(),
						"Successfully registered external monomer",
						"Register Success", JOptionPane.INFORMATION_MESSAGE);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			} catch (Exception e) {
				Logger.getLogger(MonomerMergeManager.class.getName()).log(
						Level.SEVERE, null, e);
				JOptionPane.showMessageDialog(getParent(),
						"Error while registering monomer", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
