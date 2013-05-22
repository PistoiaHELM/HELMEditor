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
import org.helm.editor.controller.ModelController;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.worker.PDBFileGenerator;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.StructureParser;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * try to decompose structures in drawing pane into multiple structures
 *
 * @author zhangtianhong
 */
public class FileMenuAction extends TextMenuAction {

    public static final int OPEN_ACTION_TYPE = 4;

    public FileMenuAction(MacromoleculeEditor editor, String textType, int actionType) {
        super(editor, textType, actionType);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (actionType == OPEN_ACTION_TYPE) {
            if (textType.equals(NOTATION_TEXT_TYPE)) {

                String title = "Open " + textType + " File";
                TextFileFilter fileFilter = getTextFileFilter(textType);

                chooser.setFileFilter(fileFilter);
                if (chooser.showOpenDialog(editor.getFrame()) == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().toString();

                    List<String> notations = new ArrayList<String>();
                    try {
                        editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        FileReader fr = new FileReader(name);
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
                        Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                        ExceptionHandler.handleException(ex);
                        return;
                    } finally {
                        editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }

                    if (notations.isEmpty()) {
                        JOptionPane.showMessageDialog(editor.getFrame(), "The input file " + name + " is empty!", title, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    List<String> invalidNotations = new ArrayList<String>();
                    for (int i = 0; i < notations.size(); i++) {
                        String note = notations.get(i);
                        try {
                            ComplexNotationParser.validateComplexNotation(note);
                        } catch (Exception ex) {
                            invalidNotations.add(note);
                        }
                    }

                    if (!invalidNotations.isEmpty()) {
                        int result = JOptionPane.showConfirmDialog(editor.getFrame(),
                                "The input file " + name + " contains " + invalidNotations.size() + " invalid notation(s),\ndo you want to skip them and continue?",
                                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (JOptionPane.NO_OPTION == result) {
                            return;
                        } else {
                            notations.removeAll(invalidNotations);
                        }
                    }

                    String notation = editor.getNotation();
                    if (null != notation && notation.trim().length() > 0) {
                        int result = JOptionPane.showConfirmDialog(editor.getFrame(),
                                "Structures exist in the sketch pane,\ndo you want to clear them before opening your file?",
                                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                        editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        for (String note : notations) {
                            String standardNote = ComplexNotationParser.standardize(note);
                            notation = ComplexNotationParser.getCombinedComlexNotation(notation, standardNote);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                        ExceptionHandler.handleException(ex);
                        return;
                    } finally {
                        editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }

                    editor.synchronizeZoom();
                    ModelController.notationUpdated(notation, editor.getOwnerCode());
                }
            }
        } else if (actionType == SAVE_ACTION_TYPE) {
            String title = "Save " + textType + " File";
            String notation = editor.getNotation();
            if (null == notation || notation.trim().length() == 0) {
                JOptionPane.showMessageDialog(editor.getFrame(), "Structure is empty!", title, JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] notations = new String[0];
            try {
                editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                notations = ComplexNotationParser.decompose(notation);
            } catch (Exception ex) {
                Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                ExceptionHandler.handleException(ex);
                return;
            } finally {
                editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }


            String textToSave = null;
            if (textType.equals(NOTATION_TEXT_TYPE)) {
                StringBuilder sb = new StringBuilder();
                for (String note : notations) {
                    sb.append(note);
                    sb.append("\n");
                }
                textToSave = sb.toString();
            } else if (textType.equals(SMILES_TEXT_TYPE)) {
                StringBuilder sb = new StringBuilder();
                try {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (String note : notations) {
                        String smiles = ComplexNotationParser.getComplexPolymerSMILES(note);
                        Molecule mol = StructureParser.getMolecule(smiles);
                        mol.dearomatize();
                        mol.clean(2, null);
                        String text = mol.exportToFormat("smiles");
                        sb.append(text);
                        sb.append("\n");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionHandler.handleException(ex);
                    return;
                } finally {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                textToSave = sb.toString();

            } else if (textType.equals(PDB_TEXT_TYPE)) {
                StringBuilder sb = new StringBuilder();
                try {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (int i = 0; i < notations.length; i++) {
                        String note = notations[i];
                        String smiles = ComplexNotationParser.getComplexPolymerSMILES(note);
                        String pdb = PDBFileGenerator.SMILES2OpenBabelPDB(smiles);
                        pdb.replace("UNNAMED", ""+(i+1));
                        sb.append(pdb);
                        sb.append("$$$$\n");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionHandler.handleException(ex);
                    return;
                } finally {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                textToSave = sb.toString();
            } else if (textType.equals(MOLFILE_TEXT_TYPE)) {
                StringBuilder sb = new StringBuilder();
                try {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (int i = 0; i < notations.length; i++) {
                        String note = notations[i];
                        String smiles = ComplexNotationParser.getComplexPolymerSMILES(note);
                        Molecule mol = StructureParser.getMolecule(smiles);
                        mol.dearomatize();
                        mol.clean(2, null);
                        String text = mol.exportToFormat("mol");
                        text = "Record " + (i + 1) + " " + text;
                        sb.append(text);
                        sb.append("$$$$\n");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FileMenuAction.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionHandler.handleException(ex);
                    return;
                } finally {
                    editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

                textToSave = sb.toString();
            } else {
                throw new UnsupportedOperationException("Unsupported structure format type :" + textType);
            }
            
            save(textToSave);
          
        }
    }
}
