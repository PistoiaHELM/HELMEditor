/**
 * *****************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ****************************************************************************
 */
package org.helm.editor.componentPanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.helm.editor.controller.ModelController;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.NotationParser;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.NucleotideConverter;
import org.helm.notation.tools.PeptideSequenceParser;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.tools.xHelmNotationParser;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * Extract LoadPanel from MacromoleculeEditor, and generalize the call by using
 * getNotation() and setNotation() routines Implement two more load methods and
 * Compound Number
 * 
 * @author zhangtianhong
 */
public class LoadPanel extends JPanel {

	private MacromoleculeEditor editor;
	private String[] selections = { "Nucleotide Sequence", "HELM Notation",
			"XHELM Notation", "Peptide Sequence" };
	private String _ownerCode;
	private JTextField inputText;
	private LoadPanelInputDialog inputDialog;

	public LoadPanel(final MacromoleculeEditor editor, String ownerCode) {
		this.editor = editor;

		_ownerCode = ownerCode;

		final JComboBox selectionCombo = new JComboBox(selections);
		selectionCombo.setSelectedIndex(0);
		selectionCombo.setToolTipText("Click to select loading category");

		inputText = new JTextField();
		inputText.setPreferredSize(new Dimension(220, 20));

		inputText.setToolTipText("Enter data to be loaded");
		inputText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (null == inputDialog) {
						inputDialog = new LoadPanelInputDialog(editor
								.getFrame(), inputText);
					}

					if (!inputDialog.isVisible()) {
						inputDialog.refreshContent();
						inputDialog.setVisible(true);
					}

				}
			}
		});

		final JCheckBox clearCheckBox = new JCheckBox("Reset", false);
		clearCheckBox
				.setToolTipText("Check this box to clear all structures before loading");

		JButton loadButton = new JButton("Load");
		loadButton.setToolTipText("Click to load structure");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (clearCheckBox.isSelected()) {
					editor.reset();
				}

				String input = inputText.getText().trim();
				int selection = selectionCombo.getSelectedIndex();
				performLoading(selection, input);
			}
		});

		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel westPanel = new JPanel();
		westPanel.add(new JLabel("                     "));
		westPanel.add(selectionCombo);

		JPanel inputTextPanel = new JPanel();
		inputTextPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		inputTextPanel.add(inputText, c);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(clearCheckBox);
		buttonPanel.add(loadButton);

		panel.add(westPanel, BorderLayout.WEST);
		panel.add(inputTextPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.EAST);

		add(panel, BorderLayout.CENTER);
	}

	private void loadHELMNotation(String inputText) {
		try {
			String existingNotation = editor.getNotation();

			// remove square bracket around CHEM monomers if exist, toolkit
			// function assumes no square bracket
			String processedInput = NotationParser
					.removeChemMonomerBracket(inputText);

			String complexNotation = ComplexNotationParser
					.standardize(processedInput);

			String newNotation = null;
			if (null != existingNotation
					&& existingNotation.trim().length() > 0) {
				newNotation = ComplexNotationParser.getCombinedComlexNotation(
						existingNotation, complexNotation);
			} else {
				newNotation = complexNotation;
			}
			try {
				// refresh images for adhoc monomers
				for (Monomer m : MonomerFactory.getInstance().getMonomerStore()
						.getAllMonomersList()) {
					if (m.isAdHocMonomer()) {
						org.helm.editor.utility.MonomerNodeHelper
								.generateImageFile(m, true);
					}
				}
			} catch (Exception ex) {

			}
			editor.synchronizeZoom();
			ModelController.notationUpdated(newNotation, _ownerCode);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(editor.getFrame(), ex.getMessage(),
					"Error Loading HELM Notation", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(LoadPanel.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	private void loadXHELMNotation(String inputText) {
		try {
			String existingNotation = editor.getNotation();

			Document doc = new SAXBuilder().build(new StringReader(inputText));

			String helm = xHelmNotationParser.getComplexNotationString(doc
					.getRootElement());
			MonomerStore store = xHelmNotationParser.getMonomerStore(doc
					.getRootElement());
			// processes notation and writes inline monomers to store
			ComplexNotationParser.validateComplexNotation(helm, store);

			// add monomers, but cancel loading when adding failed
			helm = MonomerStoreCache.getInstance().addExternalMonomers(
					editor.getFrame(), store, helm);
			if (helm == null)
				return;

			String complexNotation = ComplexNotationParser.standardize(helm,
					store);

			String newNotation = null;
			if (null != existingNotation
					&& existingNotation.trim().length() > 0) {
				newNotation = ComplexNotationParser.getCombinedComlexNotation(
						existingNotation, complexNotation);
			} else {
				newNotation = complexNotation;
			}

			editor.synchronizeZoom();
			ModelController.notationUpdated(newNotation, _ownerCode);
		} catch (Exception ex) {
			// JF: HELM-24: "Error Loading HELM" durch " Error loading XHELM"
			// ersetzt
			JOptionPane.showMessageDialog(editor.getFrame(), ex.getMessage(),
					"Error Loading XHELM Notation", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(LoadPanel.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	private void loadNucleotideSeqeuence(String inputText) {
		try {
			String existingNotation = editor.getNotation();
			String complexNotation = NucleotideConverter.getInstance()
					.getComplexNotation(inputText);
			String newNotation = null;
			if (null != existingNotation
					&& existingNotation.trim().length() > 0) {
				newNotation = ComplexNotationParser.getCombinedComlexNotation(
						existingNotation, complexNotation);
			} else {
				newNotation = complexNotation;
			}

			editor.synchronizeZoom();
			ModelController.notationUpdated(newNotation, _ownerCode);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(editor.getFrame(), ex.getMessage(),
					"Error Loading Nucleotide Sequence",
					JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(LoadPanel.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	private void loadPeptideSequence(String inputText) {
		try {
			String existingNotation = editor.getNotation();
			String simpleNotation = PeptideSequenceParser
					.getNotation(inputText);
			String complexNotation = SimpleNotationParser
					.getComplextNotationForPeptide(simpleNotation);
			String newNotation = null;
			if (null != existingNotation
					&& existingNotation.trim().length() > 0) {
				newNotation = ComplexNotationParser.getCombinedComlexNotation(
						existingNotation, complexNotation);
			} else {
				newNotation = complexNotation;
			}

			editor.synchronizeZoom();
			ModelController.notationUpdated(newNotation, _ownerCode);
		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(editor.getFrame(), ex.getMessage(),
							"Error Loading Peptide Sequence",
							JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(LoadPanel.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	private void performLoading(int selection, String inputText) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (null == inputText || inputText.trim().length() == 0) {
			JOptionPane.showMessageDialog(editor.getFrame(),
					"Input text can not be empty", "Load Warning",
					JOptionPane.WARNING_MESSAGE);
		} else {
			switch (selection) {
			case 0:
				loadNucleotideSeqeuence(inputText);
				break;
			case 1:
				loadHELMNotation(inputText);
				break;
			case 2:
				loadXHELMNotation(inputText);
				break;
			case 3:
				loadPeptideSequence(inputText);
				break;
			default:
				break;
			}
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
