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
package org.helm.editor.action;

import chemaxon.struc.Molecule;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.editor.TextViewer;
import org.helm.editor.utility.ClipBoardProcessor;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.NotationParser;
import org.helm.editor.worker.PDBFileGenerator;
import org.helm.notation.MonomerStore;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.StructureParser;
import org.helm.notation.tools.xHelmNotationExporter;
import org.helm.notation.tools.xHelmNotationParser;

/**
 * treat everything in the drawing pane as one structure
 * 
 * @author zhangtianhong
 */
public class TextMenuAction extends AbstractAction {

	public static final int COPY_ACTION_TYPE = 1;
	public static final int SHOW_ACTION_TYPE = 2;
	public static final int SAVE_ACTION_TYPE = 3;
	protected int actionType;
	public static final String NOTATION_TEXT_TYPE = "HELM Notation";
	public static final String CANONICAL_HELM_TEXT_TYPE = "Canonical HELM Notation";
	public static final String XHELM_TEXT_TYPE = "xHELM Notation";
	public static final String MOLFILE_TEXT_TYPE = "MDL Molfile";
	public static final String SMILES_TEXT_TYPE = "SMILES";
	public static final String PDB_TEXT_TYPE = "PDB Format";
	protected String textType;
	private TextViewer viewer;
	protected MacromoleculeEditor editor;
	protected JFileChooser chooser = new JFileChooser();

	public TextMenuAction(MacromoleculeEditor editor, String textType,
			int actionType) {
		super(textType);
		this.editor = editor;
		this.textType = textType;
		this.actionType = actionType;
		viewer = TextViewer.getInstance(editor.getFrame());
	}

	public void actionPerformed(ActionEvent e) {
		String title = textType;
		if (actionType == COPY_ACTION_TYPE) {
			title = "Copy " + textType;
		} else if (actionType == SHOW_ACTION_TYPE) {
			title = "Show " + textType;
		} else if (actionType == SAVE_ACTION_TYPE) {
			title = "Save " + textType;
		}
		String notation = editor.getNotation();
		MonomerStore store = editor.getMonomerStore();
		if (null == notation || notation.trim().length() == 0) {
			JOptionPane.showMessageDialog(editor.getFrame(),
					"Structure is empty!", title, JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			editor.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			String text = "";
			if (textType.equals(NOTATION_TEXT_TYPE)) {
				text = NotationParser.addChemMonomerBracket(notation);
				;
			} else if (textType.equals(CANONICAL_HELM_TEXT_TYPE)) {
				text = ComplexNotationParser.getCanonicalNotation(notation,
						store);
			} else if (textType.equals(XHELM_TEXT_TYPE)) {
				text = NotationParser.addChemMonomerBracket(notation);
				text = xHelmNotationExporter.writeXHELM(text, store);
			} else if (textType.equals(SMILES_TEXT_TYPE)) {
				String smiles = ComplexNotationParser.getComplexPolymerSMILES(
						notation, store);
				Molecule mol = StructureParser.getMolecule(smiles);
				mol.dearomatize();
				mol.clean(2, null);
				text = mol.exportToFormat("smiles");
			} else if (textType.equals(PDB_TEXT_TYPE)) {
				new PDBFileGenerator(editor, notation, this, store).execute();
			} else if (textType.equals(MOLFILE_TEXT_TYPE)) {
				String smiles = ComplexNotationParser.getComplexPolymerSMILES(
						notation, store);
				Molecule mol = StructureParser.getMolecule(smiles);
				mol.dearomatize();
				mol.clean(2, null);
				text = mol.exportToFormat("mol");
			} else {
				throw new UnsupportedOperationException(
						"Unsupported operation type :" + textType);
			}

			if (!textType.equals(PDB_TEXT_TYPE)) {
				processResult(text);
			}

		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
		} finally {
			if (!textType.equals(PDB_TEXT_TYPE)) {
				editor.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public void processResult(String text) {
		switch (actionType) {
		case SHOW_ACTION_TYPE:
			viewer.setTitle(textType);
			viewer.setText(text);
			break;
		case COPY_ACTION_TYPE:
			ClipBoardProcessor.copy(text);
			break;
		case SAVE_ACTION_TYPE:
			save(text);
			break;
		default:
			break;
		}
	}

	protected TextFileFilter getTextFileFilter(String textType) {
		TextFileFilter fileFilter = null;
		if (textType.equals(NOTATION_TEXT_TYPE)) {
			// JF: XHELM-23 Punkt vor helm als ersten Parameter, da sonst auch
			// xhelm-Dateien angezeigt werden
			fileFilter = new TextFileFilter(".helm",
					"HELM Notation File (*.helm)");
		} else if (textType.equals(CANONICAL_HELM_TEXT_TYPE)) {
			fileFilter = new TextFileFilter("chelm",
					"Canonical HELM Notation File (*.chelm)");
		} else if (textType.equals(XHELM_TEXT_TYPE)) {
			fileFilter = new TextFileFilter("xhelm",
					"Exchangeable HELM Notation File (*.xhelm)");
		} else if (textType.equals(SMILES_TEXT_TYPE)) {
			fileFilter = new TextFileFilter("smi", "SMILES File (*.smi)");
		} else if (textType.equals(PDB_TEXT_TYPE)) {
			fileFilter = new TextFileFilter("pdb", "PDB Format (*.pdb)");
		} else if (textType.equals(MOLFILE_TEXT_TYPE)) {
			fileFilter = new TextFileFilter("mol", "MDL Molfile (*.mol)");
		} else {
			throw new UnsupportedOperationException(
					"Unsupported structure format type :" + textType);
		}
		return fileFilter;
	}

	protected void save(String text) {
		String title = "Save " + textType + " File";
		TextFileFilter fileFilter = getTextFileFilter(textType);
		chooser.setFileFilter(fileFilter);

		if (chooser.showSaveDialog(editor.getFrame()) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().toString();
			if (!name.endsWith(fileFilter.getExtension())) {
				name = name + "." + fileFilter.getExtension();
			}
			try {
				editor.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				FileOutputStream fos = new FileOutputStream(new File(name));
				fos.write(text.getBytes());
				fos.close();
				JOptionPane.showMessageDialog(editor.getFrame(), textType
						+ " saved successfully to " + name, title,
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				ExceptionHandler.handleException(ex);
			} finally {
				editor.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	class TextFileFilter extends FileFilter {

		String endsWith = null;
		String description = null;

		public TextFileFilter(String endsWith, String description) {
			this.endsWith = endsWith;
			this.description = description;
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			} else {
				return f.getName().endsWith(endsWith);
			}
		}

		@Override
		public String getDescription() {
			return description;
		}

		public String getExtension() {
			return endsWith;
		}
	}
}
