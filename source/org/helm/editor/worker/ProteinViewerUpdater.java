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

import org.helm.editor.protein.view.PeptidePolymer;
import org.helm.editor.protein.view.ProteinViewer;
import org.helm.notation.model.MoleculeInfo;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.StructureParser;
import java.awt.Cursor;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author zhangtianhong
 */
public class ProteinViewerUpdater extends SwingWorker<Void, Void> {

    private String notation;
    private ProteinViewer viewer;
    private PeptidePolymer peptidePolymer;
    private StringBuilder stringBuilder;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public ProteinViewerUpdater(ProteinViewer viewer, String notation) {
        this.viewer = viewer;
        this.notation = notation;
    }

    @Override
    protected Void doInBackground() throws Exception {

        peptidePolymer = new PeptidePolymer(notation);
        stringBuilder = new StringBuilder();

        String[] notations = null;
        try {
            notations = ComplexNotationParser.decompose(notation);
        } catch (Exception ex) {
            Logger.getLogger(ProteinViewerUpdater.class.getName()).log(Level.SEVERE, null, ex);
            stringBuilder.append("**unable to decompose**\n");
            return null;
        }

        for (int i = 0; i < notations.length; i++) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("No: " + (i + 1) + "\n");
            try {
//                String smiles = ComplexNotationParser.getComplexPolymerSMILES(notations[i]);
//                MoleculeInfo mi = StructureParser.getMoleculeInfo(smiles);
                MoleculeInfo mi = ComplexNotationParser.getMoleculeInfo(notations[i]);
                stringBuilder.append("MF: " + mi.getMolecularFormula() + "\n");
                stringBuilder.append("MW: " + decimalFormat.format(mi.getMolecularWeight()) + "\n");
            } catch (Exception ex) {
                Logger.getLogger(ProteinViewer.class.getName()).log(Level.SEVERE, null, ex);
                stringBuilder.append("**unable to calculate**\n");
            }
        }

        return null;
    }

    @Override
    protected void done() {
        viewer.setPeptidePolymer(peptidePolymer);
        viewer.setMoleculeProperty(stringBuilder.toString());
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
