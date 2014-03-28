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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.helm.editor.protein.edit.ProteinEditor;
import org.helm.editor.protein.view.ProteinViewer;

/**
 *
 * @author zhangtianhong
 */
public class ProteinEditorSample {

    public static void main(String[] args) {
        try {

            JFrame viewerFrame = new JFrame("Protein Sequence Editor");
            final ProteinEditor panel = new ProteinEditor();
            panel.setPreferredSize(new Dimension(600, 400));
            panel.setMinimumSize(new Dimension(600, 400));

            JButton showNotationButton = new JButton("Show Notation");
            showNotationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String notation = panel.getNotation();
                        JTextArea area = new JTextArea();
                        area.setColumns(40);
                        area.setRows(10);
                        area.setLineWrap(true);
                        area.setText(notation);
                        area.setPreferredSize(new Dimension(500, 200));
                        JOptionPane.showMessageDialog(panel, area, "Notation", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, ex.getMessage());
                    }
                }
            });

            JButton setNotationButton = new JButton("Set Notation");
            setNotationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String notation = JOptionPane.showInputDialog(null, "Please enter text notation here");
                    if (null != notation && notation.length() > 0) {
                        panel.setNotation(notation);
                    }
                }
            });

            JButton showViewerButton = new JButton("Show Structure");
            showViewerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String notation = panel.getNotation();
                        ProteinViewer viewer = new ProteinViewer();
                        viewer.setPreferredSize(new Dimension(600, 200));
                        viewer.setNotation(notation);
                        JOptionPane.showMessageDialog(panel, viewer, "Structure", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel, ex.getMessage());
                    }
                }
            });

            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalGlue());
            buttonBox.add(showNotationButton);
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(setNotationButton);
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(showViewerButton);
            buttonBox.add(Box.createHorizontalGlue());

            viewerFrame.getContentPane().setLayout(new BorderLayout());
            viewerFrame.getContentPane().add(panel, BorderLayout.CENTER);
            viewerFrame.getContentPane().add(buttonBox, BorderLayout.SOUTH);
            viewerFrame.setMinimumSize(new Dimension(600, 400));
            viewerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            viewerFrame.pack();
            viewerFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
