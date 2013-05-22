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
package test;

import org.helm.editor.editor.MacroMoleculeViewer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author lih25
 */
public class ViewerSample {

    public static void main(String[] args) {
        try {

            String notation = "RNA1{R(A)P.R(G)P.R(A)P.R(A)P.R(G)P.R(U)P.R(U)P.R(A)P.R(C)P.R(U)P.R(G)P.R(G)P.R(C)P.R(A)P.R(A)P.R(U)P.R(C)P.R(U)P.R(C)P.R(A)P.R(A)P.R(G)P.R(U)P.R(C)P.R(A)}|RNA2{R(U)P.R(G)P.R(A)P.R(C)P.R(U)P.R(U)P.R(G)P.R(A)P.R(G)P.R(A)P.R(U)P.R(U)P.R(G)P.R(C)P.R(C)P.R(A)P.R(G)P.R(U)P.R(A)P.R(A)P.R(C)P.R(U)P.R(U)P.R(C)P.R(U)P.R(C)P.R(U)}$$RNA1,RNA2,32:pair-44:pair|RNA1,RNA2,59:pair-17:pair|RNA1,RNA2,62:pair-14:pair|RNA1,RNA2,38:pair-38:pair|RNA1,RNA2,11:pair-65:pair|RNA1,RNA2,50:pair-26:pair|RNA1,RNA2,26:pair-50:pair|RNA1,RNA2,41:pair-35:pair|RNA1,RNA2,71:pair-5:pair|RNA1,RNA2,14:pair-62:pair|RNA1,RNA2,35:pair-41:pair|RNA1,RNA2,53:pair-23:pair|RNA1,RNA2,8:pair-68:pair|RNA1,RNA2,2:pair-74:pair|RNA1,RNA2,44:pair-32:pair|RNA1,RNA2,20:pair-56:pair|RNA1,RNA2,56:pair-20:pair|RNA1,RNA2,68:pair-8:pair|RNA1,RNA2,29:pair-47:pair|RNA1,RNA2,74:pair-2:pair|RNA1,RNA2,17:pair-59:pair|RNA1,RNA2,5:pair-71:pair|RNA1,RNA2,23:pair-53:pair|RNA1,RNA2,47:pair-29:pair|RNA1,RNA2,65:pair-11:pair$RNA2{as}|RNA1{ss}$";

            JFrame viewerFrame = new JFrame("Macromolecule Viewer");
            final MacroMoleculeViewer viewer = new MacroMoleculeViewer(true);

            viewer.setNotation(notation);

            JButton setNotationButton = new JButton("Input HELM Notation");
            setNotationButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String notation = JOptionPane.showInputDialog(null, "Please enter text notation here");
                    long start = System.currentTimeMillis();
                    viewer.setNotation(notation);
                    long end = System.currentTimeMillis();
                    System.err.println("MacroMoleculeViewer.setNotation() took " + (end - start) + " msecs");
                }
            });

            viewerFrame.add(viewer, BorderLayout.CENTER);
            viewerFrame.add(setNotationButton, BorderLayout.SOUTH);
            viewerFrame.pack();
            viewerFrame.setVisible(true);
            viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } catch (Exception e) {
        }
    }
}
