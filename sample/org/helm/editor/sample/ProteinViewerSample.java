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
 *****************************************************************************
 */
package org.helm.editor.sample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.helm.editor.protein.view.ProteinViewer;

/**
 *
 * @author zhangtianhong
 */
public class ProteinViewerSample {

    public static void main(String[] args) {
        try {

            String notation = "PEPTIDE1{A.G.C.K.L.L.K.K}|PEPTIDE2{A.G.K.C.[seC].G.C.L.A.G.K.[seC].G.C.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L.A.G.K.[seC].G.A.L}$PEPTIDE2,PEPTIDE1,7:R3-3:R3|PEPTIDE2,PEPTIDE2,4:R3-14:R3$$PEPTIDE1{lc}|PEPTIDE2{hc}$";

            JFrame viewerFrame = new JFrame("Protein Viewer");
            final ProteinViewer viewer = new ProteinViewer();

            viewer.setNotation(notation);
//            viewer.setLettersPerLine(10);
//            viewer.setPositionLabelMode(PeptideSequenceViewer.TOP_TICK_MODE);


            JButton setNotationButton = new JButton("Input HELM Notation");
            setNotationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String notation = JOptionPane.showInputDialog(null, "Please enter text notation here");
                    long start = System.currentTimeMillis();
                    if (notation.length() > 0) {
                        viewer.setNotation(notation);
                    }

//                    PeptidePolymer polymer = new PeptidePolymer(notation);
//                    viewer.setPeptidePolymer(polymer);
                    long end = System.currentTimeMillis();
                    System.err.println("MacroMoleculeViewer.setNotation() took " + (end - start) + " msecs");
                }
            });

            viewerFrame.getContentPane().setLayout(new BorderLayout());
            viewerFrame.getContentPane().add(viewer, BorderLayout.CENTER);
            viewerFrame.getContentPane().add(setNotationButton, BorderLayout.SOUTH);
            viewerFrame.setMinimumSize(new Dimension(200, 200));
            viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            viewerFrame.pack();
            viewerFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
