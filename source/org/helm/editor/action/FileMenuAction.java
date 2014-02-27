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
package org.helm.editor.action;

import chemaxon.struc.Molecule;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.helm.editor.controller.ModelController;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.NotationParser;
import org.helm.editor.worker.PDBFileGenerator;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerStore;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.StructureParser;
import org.helm.notation.tools.xHelmNotationParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * try to decompose structures in drawing pane into multiple structures
 * 
 * @author zhangtianhong
 */
public class FileMenuAction extends TextMenuAction {

	public static final int OPEN_ACTION_TYPE = 4;

	public FileMenuAction(MacromoleculeEditor editor, String textType,
			int actionType) {
		super(editor, textType, actionType);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (actionType == OPEN_ACTION_TYPE) {

			// User chooses file
			TextFileFilter fileFilter = getTextFileFilter(textType);

			chooser.setFileFilter(fileFilter);
			if (chooser.showOpenDialog(editor.getFrame()) == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().toString();
				String title = "Open " + textType + " File";

				// loading file
				if (textType.equals(NOTATION_TEXT_TYPE)) {
					loadNotationFile(name, title);
				} else if (textType.equals(XHELM_TEXT_TYPE)) {
					try {
						loadXHelmNotationFile(name, title);
					} catch (Exception ex) {
						ExceptionHandler.handleException(ex);
						return;
					} finally {
						editor.getFrame()
								.setCursor(
										Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}

			}

		} else if (actionType == SAVE_ACTION_TYPE) {
			String title = "Save " + textType + " File";
			String notation = editor.getNotation();
			if (null == notation || notation.trim().length() == 0) {
				JOptionPane.showMessageDialog(editor.getFrame(),
						"Structure is empty!", title,
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			String[] notations = new String[0];
			try {
				editor.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				notations = ComplexNotationParser.decompose(notation);
			} catch (Exception ex) {
				ExceptionHandler.handleException(ex);
				return;
			} finally {
				editor.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			String textToSave = null;
			if (textType.equals(NOTATION_TEXT_TYPE)) {
				StringBuilder sb = new StringBuilder();
				try {
					for (String helm : notations) {
						String canHelm = ComplexNotationParser
								.getCanonicalNotation(helm);
						String processedNote = NotationParser
								.addChemMonomerBracket(canHelm);
						;
						sb.append(processedNote);
						sb.append("\n");
					}
				} catch (Exception ex) {
					ExceptionHandler.handleException(ex);
					return;
				} finally {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				textToSave = sb.toString();
			} else if (textType.equals(XHELM_TEXT_TYPE)) {
				textToSave = xHelmNotationParser.writeXHELM(notation,
						MonomerStoreCache.getInstance()
								.getCombinedMonomerStore());
			} else if (textType.equals(CANONICAL_HELM_TEXT_TYPE)) {
				StringBuilder sb = new StringBuilder();
				try {
					for (String helm : notations) {
						String canHelm = ComplexNotationParser
								.getCanonicalNotation(helm);
						sb.append(canHelm);
						sb.append("\n");
					}
				} catch (Exception ex) {
					ExceptionHandler.handleException(ex);
					return;
				} finally {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				textToSave = sb.toString();
			} else if (textType.equals(SMILES_TEXT_TYPE)) {
				StringBuilder sb = new StringBuilder();
				try {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					for (String note : notations) {
						String smiles = ComplexNotationParser
								.getComplexPolymerSMILES(note);
						Molecule mol = StructureParser.getMolecule(smiles);
						mol.dearomatize();
						mol.clean(2, null);
						String text = mol.exportToFormat("smiles");
						sb.append(text);
						sb.append("\n");
					}
				} catch (Exception ex) {
					ExceptionHandler.handleException(ex);
					return;
				} finally {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				textToSave = sb.toString();

			} else if (textType.equals(PDB_TEXT_TYPE)) {
				StringBuilder sb = new StringBuilder();
				try {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					for (int i = 0; i < notations.length; i++) {
						String note = notations[i];
						String smiles = ComplexNotationParser
								.getComplexPolymerSMILES(note);
						String pdb = PDBFileGenerator
								.SMILES2OpenBabelPDB(smiles);
						pdb.replace("UNNAMED", "" + (i + 1));
						sb.append(pdb);
						sb.append("$$$$\n");
					}
				} catch (Exception ex) {
					ExceptionHandler.handleException(ex);
					return;
				} finally {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				textToSave = sb.toString();
			} else if (textType.equals(MOLFILE_TEXT_TYPE)) {
				StringBuilder sb = new StringBuilder();
				try {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					for (int i = 0; i < notations.length; i++) {
						String note = notations[i];
						String smiles = ComplexNotationParser
								.getComplexPolymerSMILES(note);
						Molecule mol = StructureParser.getMolecule(smiles);
						mol.dearomatize();
						mol.clean(2, null);
						String text = mol.exportToFormat("mol");
						text = "Record " + (i + 1) + " " + text;
						sb.append(text);
						sb.append("$$$$\n");
					}
				} catch (Exception ex) {
					ExceptionHandler.handleException(ex);
					return;
				} finally {
					editor.getFrame().setCursor(
							Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				textToSave = sb.toString();
			} else {
				throw new UnsupportedOperationException(
						"Unsupported structure format type :" + textType);
			}

			save(textToSave);

		}
	}

	private void loadXHelmNotationFile(String fileName, String title) {

		FileInputStream in;
		Element xHELMRootElement;
		try {
			in = new FileInputStream(fileName);

			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);

			xHELMRootElement = doc.getRootElement();

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(editor.getFrame(), "The input file "
					+ fileName + " could not be read!", title,
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String helmString;
		MonomerStore store;
		try {
			helmString = xHelmNotationParser
					.getComplexNotationString(xHELMRootElement);

			// read monomers to store
			store = xHelmNotationParser.getMonomerStore(xHELMRootElement);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(editor.getFrame(),
					"The HELM code could not be read!", title,
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			ComplexNotationParser.validateComplexNotation(helmString, store);
			MonomerStoreCache.getInstance().setExternalMonomers(store);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(editor.getFrame(),
					"Invalid Notation!", title, JOptionPane.WARNING_MESSAGE);
			return;
		}

		String notation = editor.getNotation();
		if (null != notation && notation.trim().length() > 0) {
			int result = JOptionPane
					.showConfirmDialog(
							editor.getFrame(),
							"Structures exist in the sketch pane,\ndo you want to clear them before opening your file?",
							title, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (JOptionPane.YES_OPTION == result) {
				editor.reset();
				notation = editor.getNotation();
			}
		}

		if (null == notation || notation.trim().length() == 0) {
			notation = helmString;
		}

		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		editor.synchronizeZoom();
		ModelController.notationUpdated(notation, editor.getOwnerCode());

	}

	private void loadNotationFile(String fileName, String title) {

		List<String> notations = new ArrayList<String>();
		try {
			editor.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (null != line) {
				if (line.length() > 0) {
					notations.add(line);
				}
				line = br.readLine();
			}
			fr.close();
			br.close();
		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
			return;
		} finally {
			editor.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		if (notations.isEmpty()) {
			JOptionPane.showMessageDialog(editor.getFrame(), "The input file "
					+ fileName + " is empty!", title,
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<String> invalidNotations = new ArrayList<String>();
		for (int i = 0; i < notations.size(); i++) {
			String note = notations.get(i);
			note = NotationParser.removeChemMonomerBracket(note);
			notations.set(i, note);
			try {
				ComplexNotationParser.validateComplexNotation(note);
			} catch (Exception ex) {
				invalidNotations.add(note);
			}
		}

		if (!invalidNotations.isEmpty()) {
			int result = JOptionPane
					.showConfirmDialog(
							editor.getFrame(),
							"The input file "
									+ fileName
									+ " contains "
									+ invalidNotations.size()
									+ " invalid notation(s),\ndo you want to skip them and continue?",
							title, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (JOptionPane.NO_OPTION == result) {
				return;
			} else {
				notations.removeAll(invalidNotations);
			}
		}

		String notation = editor.getNotation();
		if (null != notation && notation.trim().length() > 0) {
			int result = JOptionPane
					.showConfirmDialog(
							editor.getFrame(),
							"Structures exist in the sketch pane,\ndo you want to clear them before opening your file?",
							title, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (JOptionPane.YES_OPTION == result) {
				editor.reset();
				notation = editor.getNotation();
			}
		}

		if (null == notation || notation.trim().length() == 0) {
			notation = notations.get(0);
			notations.remove(0);
		}

		try {
			editor.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			for (String note : notations) {
				String standardNote = ComplexNotationParser.standardize(note);
				notation = ComplexNotationParser.getCombinedComlexNotation(
						notation, standardNote);
			}
		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
			return;
		} finally {
			editor.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		editor.synchronizeZoom();
		ModelController.notationUpdated(notation, editor.getOwnerCode());

	}
}
