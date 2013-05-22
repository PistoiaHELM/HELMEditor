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

import chemaxon.marvin.plugin.PluginException;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.manager.OligonucleotideFragmentDialog;
import org.helm.editor.manager.OligonucleotideFragmentTableModel.Fragment;
import org.helm.editor.manager.OligonucleotideFragmentTableModel.FragmentationParameter;
import org.helm.notation.model.MoleculeInfo;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.DeepCopy;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.tools.StructureParser;
import java.awt.Cursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.SwingWorker;
import org.jdom.JDOMException;

/**
 *
 * @author zhangtianhong
 */
public class OligonucleotideFragmenter extends SwingWorker<List<Fragment>, Void> {

    public static final int MIN_OLIGO_LENGTH = 2;
    private FragmentationParameter parameter;
    private OligonucleotideFragmentDialog dialog;

    public OligonucleotideFragmenter(OligonucleotideFragmentDialog dialog, FragmentationParameter parameter) {
        this.dialog = dialog;
        this.parameter = parameter;
    }

    @Override
    protected List<Fragment> doInBackground() throws Exception {
        List<Fragment> fragments = new ArrayList<Fragment>();
        String simpleNotation = parameter.getSimpleRNANotation();
        boolean startFromFive = parameter.isStartFromFiveEnd();
        boolean startFromThree = parameter.isStartFromThreeEnd();
        int monomerNum = parameter.getMonomerNumber();
        int step = parameter.getStep();

        List<Nucleotide> nucleotides = SimpleNotationParser.getNucleotideList(simpleNotation, false);
        addNewFragment(fragments, 0, 0, getNotation(nucleotides));

        int currentStep = 1;
        List<Nucleotide> curList = DeepCopy.copy(nucleotides);
        String notation;
        if (startFromFive && startFromThree) {
            while (currentStep <= step && curList.size() > MIN_OLIGO_LENGTH) {
                int fiveStep = 0;
                int threeStep = 0;
                while (fiveStep <= currentStep) {
                    threeStep = currentStep - fiveStep;
                    curList = cut(nucleotides, monomerNum, fiveStep, threeStep);
                    notation = getNotation(curList);
                    addNewFragment(fragments, fiveStep, threeStep, notation);
                    fiveStep++;
                }
                currentStep++;
            }

        } else if (startFromFive && !startFromThree) {
            while (currentStep <= step && curList.size() > MIN_OLIGO_LENGTH) {
                curList = cut(nucleotides, monomerNum, currentStep, 0);
                notation = getNotation(curList);
                addNewFragment(fragments, currentStep, 0, notation);
                currentStep++;
            }
        } else if (!startFromFive && startFromThree) {
            while (currentStep <= step && curList.size() > MIN_OLIGO_LENGTH) {
                curList = cut(nucleotides, monomerNum, 0, currentStep);
                notation = getNotation(curList);
                addNewFragment(fragments, 0, currentStep, notation);
                currentStep++;
            }
        } else {
            throw new UnsupportedOperationException("Starting position for fragmentation is not provided");
        }

        return fragments;
    }

    @Override
    protected void done() {
        try {
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            List<Fragment> list = get();
            dialog.refreshResults(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error performing oligonucleotide fragmentation\n" + ex.getMessage(), "Oligonucleotide Fragmentation", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(OligonucleotideFragmenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getNotation(List<Nucleotide> nucleotides) throws NotationException, MonomerException, StructureException, JDOMException, IOException {
        String simpleNoation = SimpleNotationParser.getSimpleNotationFromNucleotideList(nucleotides);
        return SimpleNotationParser.getComplextNotationForRNA(simpleNoation);
    }

    private double getMolWeight(String notation) throws NotationException, MonomerException, StructureException, JDOMException, IOException, PluginException {
        String smiles = ComplexNotationParser.getComplexPolymerSMILES(notation);
        MoleculeInfo mi = StructureParser.getMoleculeInfo(smiles);
        return mi.getMolecularWeight();
    }

    private void addNewFragment(List<Fragment> list, int fiveEndStep, int threeEndStep, String notation) throws NotationException, MonomerException, StructureException, JDOMException, IOException, PluginException {
        Fragment fragment = new Fragment();
        fragment.setFiveEndStep(fiveEndStep);
        fragment.setThreeEndStep(threeEndStep);
        fragment.setNotation(notation);
        fragment.setMolWeight(getMolWeight(notation));
        list.add(fragment);
    }

    private List<Nucleotide> cut(List<Nucleotide> list, int monomerNumPerStep, int fiveEndStep, int threeEndStep) throws NotationException, MonomerException, StructureException, JDOMException, IOException, PluginException, ClassNotFoundException {
        List<Nucleotide> curList = DeepCopy.copy(list);

        //5' end cutting
        int currentStep = 0;
        while (currentStep < fiveEndStep && curList.size() > MIN_OLIGO_LENGTH) {
            Monomer sugarMonomer = curList.get(0).getSugarMonomer();
            boolean isFiveComplete = (null != sugarMonomer) ? true : false;
            if (isFiveComplete) {
                if (monomerNumPerStep == 1) {
                    String newNotation = curList.get(0).getLinkerNotation();
                    Nucleotide nuc = new Nucleotide(newNotation, Nucleotide.STARTING_POSITION_TYPE);
                    curList.remove(0);
                    curList.add(0, nuc);
                } else if (monomerNumPerStep == 2) {
                    curList.remove(0);
                } else {
                    throw new UnsupportedOperationException("Number of backbone monomer to remove per step can only be 1 or 2");
                }
            } else {
                if (monomerNumPerStep == 1) {
                    curList.remove(0);
                } else if (monomerNumPerStep == 2) {
                    curList.remove(0);

                    String newNotation = curList.get(0).getLinkerNotation();
                    Nucleotide nuc = new Nucleotide(newNotation, Nucleotide.STARTING_POSITION_TYPE);
                    curList.remove(0);
                    curList.add(0, nuc);
                } else {
                    throw new UnsupportedOperationException("Number of backbone monomer to remove per step can only be 1 or 2");
                }

            }
            currentStep++;
        }

        //3' end cutting
        currentStep = 0;
        while (currentStep < threeEndStep && curList.size() > MIN_OLIGO_LENGTH) {
            int index = curList.size() - 1;
            Monomer pMonomer = curList.get(index).getPhosphateMonomer();
            boolean isThreeComplete = (null != pMonomer) ? true : false;
            if (isThreeComplete) {
                if (monomerNumPerStep == 1) {
                    String newNotation = curList.get(index).getNucleosideNotation();
                    Nucleotide nuc = new Nucleotide(newNotation, Nucleotide.ENDING_POSITION_TYPE);
                    curList.remove(index);
                    curList.add(index, nuc);
                } else if (monomerNumPerStep == 2) {
                    curList.remove(index);
                } else {
                    throw new UnsupportedOperationException("Number of backbone monomer to remove per step can only be 1 or 2");
                }
            } else {
                if (monomerNumPerStep == 1) {
                    curList.remove(index);
                } else if (monomerNumPerStep == 2) {
                    curList.remove(index);

                    index = curList.size() - 1;
                    String newNotation = curList.get(index).getNucleosideNotation();
                    Nucleotide nuc = new Nucleotide(newNotation, Nucleotide.ENDING_POSITION_TYPE);
                    curList.remove(index);
                    curList.add(index, nuc);
                } else {
                    throw new UnsupportedOperationException("Number of backbone monomer to remove per step can only be 1 or 2");
                }

            }
            currentStep++;
        }

        return curList;
    }
}
