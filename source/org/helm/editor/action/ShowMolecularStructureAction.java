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

import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.ChemicalStructureViewer;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.notation.MonomerStore;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.StructureParser;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * One class to handle all "Show Molecular Structure" related actions
 * 
 * @author zhangtianhong
 */
public class ShowMolecularStructureAction extends AbstractAction {

	public static final String ALL_STRUCTURE_TYPE = "All";
	public static final String SELECTED_STRUCTURE_TYPE = "Selected";
	private MacromoleculeEditor editor;
	private ChemicalStructureViewer viewer;
	private String structureType;

	public ShowMolecularStructureAction(MacromoleculeEditor editor,
			ChemicalStructureViewer viewer, String structureType) {
		super(structureType);
		this.editor = editor;
		this.viewer = viewer;
		this.structureType = structureType;
	}

	public void actionPerformed(ActionEvent e) {
		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			String notation = "";
			String warningMsg = "";
			String smiles = "";
			if (structureType.equals(ALL_STRUCTURE_TYPE)) {
				notation = editor.getNotation();
				if (null == notation || notation.length() == 0) {
					warningMsg = "Empty Structure!";
				}
			} else if (structureType.equals(SELECTED_STRUCTURE_TYPE)) {
				notation = editor.getSelectedNotation();
				if (null == notation || notation.length() == 0) {
					warningMsg = "No Selected Structure!";
				}
			} else {
				throw new UnsupportedOperationException(
						"Unsupported operation type :" + structureType);
			}

			if (warningMsg.length() > 0) {
				JOptionPane.showMessageDialog(editor.getFrame(), warningMsg,
						"Molecular Structure", JOptionPane.WARNING_MESSAGE);
			} else {

				// SM 2014-04-14 show structure did fail for monomers coming
				// from xhelm (XHELM-15)
				MonomerStore store = MonomerStoreCache.getInstance()
						.getCombinedMonomerStore();

				// TY
				notation = org.helm.editor.utility.NotationParser
						.transferDynamicChemicalModifiersToMonomers(notation,
								store);

				smiles = ComplexNotationParser.getComplexPolymerSMILES(
						notation, store);
				Molecule mol = StructureParser.getMolecule(smiles);
				mol.dearomatize();
				viewer.setStructure(mol);
			}

		} catch (Exception ex) {
			Logger.getLogger(ShowMolecularStructureAction.class.getName()).log(
					Level.WARNING,
					ShowMolecularStructureAction.class.getName(), ex);
			ExceptionHandler.handleException(ex);
		}

		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
