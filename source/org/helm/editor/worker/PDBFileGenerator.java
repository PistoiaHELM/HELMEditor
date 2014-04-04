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
package org.helm.editor.worker;

import org.helm.editor.action.TextMenuAction;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.MonomerStore;
import org.helm.notation.tools.ComplexNotationParser;

import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;
import org.openbabel.OBConversion;
import org.openbabel.OBMol;
import org.openbabel.OBOp;

/**
 *
 * @author zhangtianhong
 */
public class PDBFileGenerator extends SwingWorker<String, Void> {

    private MacromoleculeEditor editor;
    private String HELMNotation;
    private TextMenuAction menuAction;
    private MonomerStore monomerStore;

    public PDBFileGenerator(MacromoleculeEditor editor, String HELMNotation, TextMenuAction menuAction,MonomerStore monomerStore) {
        this.editor = editor;
        this.HELMNotation = HELMNotation;
        this.menuAction = menuAction;
        this.monomerStore=monomerStore;
    }

    @Override
    protected String doInBackground() throws Exception {
        String smiles = ComplexNotationParser.getComplexPolymerSMILES(this.HELMNotation,this.monomerStore);
        String pdb = SMILES2OpenBabelPDB(smiles);
        return pdb;
    }

    @Override
    protected void done() {
        try {
            editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            String text = get();
            menuAction.processResult(text);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(editor.getFrame(), ex.getMessage(), "Erroring Generating PDB File", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(PDBFileGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Requires the installation of OpenBabelGui application on Windows, which can be freely downloaded and installed from http://openbabel.org
     * Also, the installed application needs to be on the system path 
     */
    public static String SMILES2OpenBabelPDB(String smiles) {

        System.loadLibrary("openbabel_java");

        OBConversion conv = new OBConversion();

        OBMol mol = new OBMol();
        conv.SetInFormat("smi");
        conv.SetOutFormat("pdb");
        conv.ReadString(mol, smiles);

        OBOp gen3d = OBOp.FindType("Gen3D");
        gen3d.Do(mol);

//        conv.AddOption("h", OBConversion.Option_type.GENOPTIONS);
//        conv.AddOption("gen3D", OBConversion.Option_type.GENOPTIONS);

        return conv.WriteString(mol);
    }
}
