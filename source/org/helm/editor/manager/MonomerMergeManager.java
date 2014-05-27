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
import java.util.Map;
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
	private javax.swing.JButton registerButton;
	private javax.swing.JScrollPane tableScrollPane;

	/** Creates new form MonomerMergeManager */
	public MonomerMergeManager(MacromoleculeEditor editor, boolean modal) {
		super(editor.getFrame(), modal);
		this.editor = editor;
		initComponents();

		viewer = MonomerViewer.getNamedInstance("MonomerMergeManager");
		
		
		viewer.setModifiableStatus(false);
		viewer.setIdEditable(true);
		viewer.setNameEditable(true);
		viewer.setNaturalAnalogEditable(true);
		viewer.setAttachmentTableEditable(true);
		monomerViewerPanel.add(viewer);
		viewer.clear();
	}

	
	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	private void initComponents() {

		monomerListPanel = new javax.swing.JPanel();
		tableScrollPane = new javax.swing.JScrollPane();
		monomerTable = new org.jdesktop.swingx.JXTable();
		monomerDetailPanel = new javax.swing.JPanel();
		monomerViewerPanel = new javax.swing.JPanel();
		registerButton = new javax.swing.JButton();
		closeButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("HELM Editor Monomer Merge Dialog");

	
		MonomerStore store = MonomerStoreCache.getInstance()
				.getUnregisteredMonomers();
		if (store != null && store.getPolymerTypeSet().size() > 0) {


			MonomerMergeTableModel model = new MonomerMergeTableModel(store.getAllMonomersList());
			monomerTable.setModel(model);

		} else {
			monomerTable.setModel(new javax.swing.table.DefaultTableModel(
					new Object[][] {}, new String[] { "Symbol",
							"Natural Analog", "Name", "Structure" }));
		}


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
							Monomer monomer = ((MonomerMergeTableModel) monomerTable
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
										).addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
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
	
	public void refreshContent() {
		MonomerStore store = MonomerStoreCache.getInstance()
				.getUnregisteredMonomers();

		MonomerMergeTableModel model = new MonomerMergeTableModel(store.getAllMonomersList());
		monomerTable.setModel(model);
	}

	
	public MacromoleculeEditor getEditor() {
		return editor;
	}

	public MonomerViewer getMonomerViewer() {
		return viewer;
	}

	


	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		setVisible(false);
	}
	private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int rowIndex = monomerTable.getSelectedRow();
		int row = monomerTable.convertRowIndexToModel(rowIndex);
		if (rowIndex >= 0 && rowIndex < monomerTable.getRowCount()) {
			Monomer monomer = ((MonomerMergeTableModel) monomerTable.getModel())
					.getMonomerList().get(row);
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				

				Monomer tmp = viewer.getEditedMonomer();
				
								
				String alternateId = monomer.getAlternateId();
				String polymerType = monomer.getPolymerType();
				
				
				MonomerStore localStore = MonomerFactory.getInstance()
						.getMonomerStore();
				MonomerStore externalStore=MonomerStoreCache.getInstance().getExternalStore();
				
				// monomer comes from external store
				if (externalStore!=null && externalStore.hasMonomer(polymerType,
						alternateId)) {
					Monomer extMon=externalStore.getMonomer(polymerType, alternateId);
					
					
					
					//check if monomer structure already exists in local db
					if (!viewer.isValidNewMonomer()){
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						return;
					}

					extMon.setNewMonomer(false);
					extMon.setAdHocMonomer(false);					
					
				
					extMon.setAlternateId(tmp.getAlternateId());
					extMon.setName(tmp.getName());
					extMon.setNaturalAnalog(tmp.getNaturalAnalog());
					extMon.setAttachmentList(tmp.getAttachmentList());
					extMon.setMolfile(tmp.getMolfile());
					
					localStore.addMonomer(extMon, true);
				}
				// monomer comes from local store
				else {
					Monomer locMon = localStore.getMonomer(polymerType,
							alternateId);
					String oldId=locMon.getAlternateId();
					
					locMon.setNewMonomer(false);
					locMon.setAdHocMonomer(false);
					
					locMon.setAlternateId(tmp.getAlternateId());
					locMon.setName(tmp.getName());
					locMon.setNaturalAnalog(tmp.getNaturalAnalog());
					locMon.setAttachmentList(tmp.getAttachmentList());
					locMon.setMolfile(tmp.getMolfile());
					
					Map<String,Monomer> monomers=localStore.getMonomerDB().get(polymerType);
					Monomer m=monomers.remove(oldId);
					monomers.put(locMon.getAlternateId(), m);
										
					MonomerFactory.setDBChanged(true);
					
				}

				MonomerFactory.getInstance().saveMonomerCache();

				refreshContent();
				getEditor().updatePolymerPanels();
				getEditor().replaceAlternateId(alternateId, tmp.getAlternateId());
				
				getEditor().onDropCompleteEvent(null);
								
				viewer.clear();
				
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
