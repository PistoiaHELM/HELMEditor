/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * MonomerReplacementManager.java
 *
 * Created on Apr 21, 2009, 10:51:29 AM
 */
package org.helm.editor.manager;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import org.jdom.JDOMException;

import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.view.Graph2D;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NotationException;
import org.helm.editor.controller.ModelController;
import org.helm.editor.controller.TemplateLocator;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.DragAndDropSupport;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.SimpleNotationParser;

/**
 * 
 * @author zhangtianhong
 */
public class MonomerReplacementManager extends javax.swing.JDialog {

	private static final int COMMON_NOTATION_PREFIX_SUFFIX_LENGHT = 5;

	private String _ownerCode;

	/** Creates new form MonomerReplacementManager */
	public MonomerReplacementManager(MacromoleculeEditor editor, boolean modal,
			String ownerCode) {
		super(editor.getFrame(), modal);
		this.editor = editor;
		_ownerCode = ownerCode;
		initComponents();
		customInit();
	}

	private void customInit() {
		Vector types = new Vector();

		try {
			Set keys = MonomerStoreCache.getInstance()
					.getCombinedMonomerStore().getMonomerDB().keySet();
			// Set keys = MonomerFactory.getInstance().getMonomerDB().keySet();
			for (Iterator i = keys.iterator(); i.hasNext();) {
				types.add((String) i.next());
			}
		} catch (Exception ex) {
			types.add(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
			types.add(Monomer.PEPTIDE_POLYMER_TYPE);
			types.add(Monomer.CHEMICAL_POLYMER_TYPE);
			Logger.getLogger(MonomerReplacementManager.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		polymerTypeCombo.setModel(new DefaultComboBoxModel(types));
		polymerTypeCombo.setSelectedItem(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
		existingMonomerText.setText("P");
		newMonomerText.setText("sP");

		Dimension size = getPreferredSize();
		setMinimumSize(size);
		setMaximumSize(size);
		setLocationRelativeTo(editor.getFrame());
		setResizable(false);
	}

	public void replace() {

		final String polymerType = (String) polymerTypeCombo.getSelectedItem();
		final String existingMonomerID = existingMonomerText.getText().trim();
		final String newMonomerID = newMonomerText.getText().trim();

		// Graph2D current = DataRegistry.getInstance().getGraph2D();
		// GraphManager graphManager =
		// DataRegistry.getInstance().getGraphManager();
		// Graph2D viewGraph = editor.getView().getGraph2D();

		Graph2D current = editor.getView().getGraph2D();
		GraphManager graphManager = editor.getGraphManager();

		try {
			// TODO: viewGraph != current!
			// current = viewGraph;
			// NodeCursor selectedNodes = viewGraph.selectedNodes();

			NodeCursor selectedNodes = current.selectedNodes();
			if (selectedNodes.size() == 0) {
				replaceWholeGraph(polymerType, existingMonomerID, newMonomerID);
				return;
			}

			new SimpleNotationParser() {
				{
					validateMonomerReplacement(polymerType, existingMonomerID,
							newMonomerID);
				}
			};

			for (; selectedNodes.ok(); selectedNodes.next()) {
				// Node newNode =
				// TemplateLocator.getInstance().getTemplate(polymerType,
				// newMonomerID);

				if (MonomerInfoUtils.matches(selectedNodes.node(),
						existingMonomerID, polymerType)) {
					// newNode = DragAndDropSupport.copySingleNode(editor,
					// current, newNode);
					// DragAndDropSupport.replaceMonomer(current, graphManager,
					// newNode, selectedNodes.node());
					replaceSingleNode(existingMonomerID, newMonomerID,
							selectedNodes.node());
				}
			}
			String newNotation = Graph2NotationTranslator
					.getNewNotation(graphManager);

			ModelController.notationUpdated(newNotation, _ownerCode);
			current.unselectAll();
			dispose();

			return;

		} catch (Exception ex) {
			Logger.getLogger(MonomerReplacementManager.class.getName()).log(
					Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, "Error replacing monomers:\n"
					+ ex.getMessage(), "Monomer Replacement",
					JOptionPane.ERROR_MESSAGE);

			dispose();
		} finally {
			current.unselectAll();
			dispose();
		}

		try {
			Graph2NotationTranslator.updateHyperGraph(current, graphManager);
			String notation = Graph2NotationTranslator
					.getNewNotation(graphManager);
			ModelController.notationUpdated(notation, _ownerCode);
		} catch (Exception ex) {
			Logger.getLogger(MonomerReplacementManager.class.getName()).log(
					Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, "Error replacing monomers:\n"
					+ ex.getMessage(), "Monomer Replacement",
					JOptionPane.ERROR_MESSAGE);

			dispose();
		}

		dispose();
	}

	private void replaceSingleNode(String oldMonomerId, String newMonomerId,
			Node oldNode) {
		Graph g = oldNode.getGraph();
		Node hyperNode = (Node) ((NodeMap) g
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE))
				.get(oldNode);
		Graph gh = hyperNode.getGraph();
		String polymerNotation = (String) ((NodeMap) gh
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION))
				.get(hyperNode);
		int nodePosition = (Integer) ((NodeMap) g
				.getDataProvider(NodeMapKeys.MONOMER_POSITION)).get(oldNode);
		String[] parts = polymerNotation.split("[.()]");
		String toReplace = parts[nodePosition - 1];
		if ((oldMonomerId.length() > 1)) {
			if (newMonomerId.length() > 1)
				toReplace = toReplace.replace(oldMonomerId, newMonomerId);
			else
				toReplace = toReplace.replace("[" + oldMonomerId + "]",
						newMonomerId);
		} else if (newMonomerId.length() > 1) {
			toReplace = toReplace.replace(oldMonomerId, "[" + newMonomerId
					+ "]");
		} else {
			toReplace = toReplace.replace(oldMonomerId, newMonomerId);
		}

		String newNotation = "";
		int partIndex = 0;
		int stringIndex = 0;

		while (stringIndex < polymerNotation.length()) {
			String remainingPart = polymerNotation.substring(stringIndex);
			if ((partIndex < parts.length)
					&& remainingPart.startsWith(parts[partIndex])) {
				stringIndex += parts[partIndex].length();
				if (partIndex == nodePosition - 1) {
					parts[nodePosition - 1] = toReplace;
				}
				newNotation += parts[partIndex];
				partIndex++;
			} else {
				newNotation += polymerNotation.charAt(stringIndex);
				stringIndex++;
			}
		}

		((NodeMap) gh.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION))
				.set(hyperNode, newNotation);
	}

	public void replaceWholeGraph(String polymerType, String existingMonomerID,
			String newMonomerID) throws MonomerException, IOException,
			JDOMException, NotationException {
		String notation = editor.getNotation();
		String newNotation = ComplexNotationParser.replaceMonomer(notation,
				polymerType, existingMonomerID, newMonomerID);

		ModelController.notationUpdated(newNotation, _ownerCode);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		inputPanel = new javax.swing.JPanel();
		polymerTypeLabel = new javax.swing.JLabel();
		polymerTypeCombo = new javax.swing.JComboBox();
		existingMonomerLabel = new javax.swing.JLabel();
		newMonomerLabel = new javax.swing.JLabel();
		existingMonomerText = new javax.swing.JTextField();
		newMonomerText = new javax.swing.JTextField();
		cancelButton = new javax.swing.JButton();
		replaceButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Replace Monomer");

		inputPanel.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));

		polymerTypeLabel.setText("Polymer Type ");

		polymerTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

		existingMonomerLabel.setText("Existing Monomer ");

		newMonomerLabel.setText("New Monomer");

		existingMonomerText.setText("jTextField1");

		newMonomerText.setText("jTextField2");

		org.jdesktop.layout.GroupLayout inputPanelLayout = new org.jdesktop.layout.GroupLayout(
				inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout
				.setHorizontalGroup(inputPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(inputPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(inputPanelLayout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.LEADING,
												false)
										.add(existingMonomerLabel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.add(polymerTypeLabel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.add(newMonomerLabel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
								.add(21, 21, 21)
								.add(inputPanelLayout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.TRAILING)
										.add(polymerTypeCombo, 0, 100,
												Short.MAX_VALUE)
										.add(existingMonomerText,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												100, Short.MAX_VALUE)
										.add(newMonomerText,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												100, Short.MAX_VALUE))
								.addContainerGap()));
		inputPanelLayout
				.setVerticalGroup(inputPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(inputPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.add(inputPanelLayout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.BASELINE)
										.add(polymerTypeLabel)
										.add(polymerTypeCombo,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.UNRELATED)
								.add(inputPanelLayout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.BASELINE)
										.add(existingMonomerLabel)
										.add(existingMonomerText,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(
										org.jdesktop.layout.LayoutStyle.UNRELATED)
								.add(inputPanelLayout
										.createParallelGroup(
												org.jdesktop.layout.GroupLayout.BASELINE)
										.add(newMonomerLabel)
										.add(newMonomerText,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		replaceButton.setText("Replace");
		replaceButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				replaceButtonActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(inputPanel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										layout.createSequentialGroup()
												.add(replaceButton)
												.addPreferredGap(
														org.jdesktop.layout.LayoutStyle.UNRELATED)
												.add(cancelButton)))
						.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout
						.createSequentialGroup()
						.addContainerGap()
						.add(inputPanel,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED, 8,
								Short.MAX_VALUE)
						.add(layout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.BASELINE)
								.add(cancelButton).add(replaceButton))
						.addContainerGap()));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void replaceButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_replaceButtonActionPerformed
		replace();
	}// GEN-LAST:event_replaceButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
		setVisible(false);
	}// GEN-LAST:event_cancelButtonActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel existingMonomerLabel;
	private javax.swing.JTextField existingMonomerText;
	private javax.swing.JPanel inputPanel;
	private javax.swing.JLabel newMonomerLabel;
	private javax.swing.JTextField newMonomerText;
	private javax.swing.JComboBox polymerTypeCombo;
	private javax.swing.JLabel polymerTypeLabel;
	private javax.swing.JButton replaceButton;
	// End of variables declaration//GEN-END:variables
	private MacromoleculeEditor editor;

}
