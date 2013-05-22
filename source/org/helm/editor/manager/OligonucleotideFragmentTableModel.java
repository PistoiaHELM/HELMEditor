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
package org.helm.editor.manager;

import org.helm.notation.NotationException;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.ComplexNotationParser;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author zhangtianhong
 */
public class OligonucleotideFragmentTableModel extends AbstractTableModel {

    private List<Fragment> fragments = new ArrayList<Fragment>();
    private String[] columnNames = {"Row #","5' Cut Steps", "3' Cut Steps", "Oligonucleotide Sequence", "Mol. Weight"};

    public static final int STRUCTURE_COLUMN_INDEX = 3;
    public static final int NON_STRUCTURE_COLUMN_MAX_WIDTH = 100;

    public OligonucleotideFragmentTableModel() {
    }


    public int getRowCount() {
         return fragments.size();
    }

    public int getColumnCount() {
        return columnNames.length;

    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Fragment f = fragments.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex+1;
            case 1:
                return f.getFiveEndStep();
            case 2:
                return f.getThreeEndStep();
            case 3:
                return f.getNotation();
            case 4:
                return f.getMolWeight();
            default:
                return "N/A";
        }
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
        fireTableDataChanged();
    }

    public void clear() {
        this.fragments = new ArrayList<Fragment>();
        fireTableDataChanged();
    }

    public static class Fragment {
        private int fiveEndStep;
        private int threeEndStep;
        private String notation;
        private double molWeight;

        public double getMolWeight() {
            return molWeight;
        }

        public void setMolWeight(double molWeight) {
            this.molWeight = molWeight;
        }

        public String getNotation() {
            return notation;
        }

        public void setNotation(String notation) {
            this.notation = notation;
        }

        public int getFiveEndStep() {
            return fiveEndStep;
        }

        public void setFiveEndStep(int fiveEndStep) {
            this.fiveEndStep = fiveEndStep;
        }

        public int getThreeEndStep() {
            return threeEndStep;
        }

        public void setThreeEndStep(int threeEndStep) {
            this.threeEndStep = threeEndStep;
        }
    }

    public static class FragmentationParameter {
        private String inputComplexNotation = "";
        private String simpleRNANotation = "";
        private boolean startFromFiveEnd = false;
        private boolean startFromThreeEnd = false;
        private int monomerNumber = 0;
        private String stepText = "";
        private int step = 0;

        public int getMonomerNumber() {
            return monomerNumber;
        }

        public void setMonomerNumber(int monomerNumber) {
            this.monomerNumber = monomerNumber;
        }

        public String getInputComplexNotation() {
            return inputComplexNotation;
        }

        public void setInputComplexNotation(String inputComplexNotation) {
            this.inputComplexNotation = inputComplexNotation;
        }

        public String getSimpleRNANotation() {
            return simpleRNANotation;
        }

        public void setSimpleRNANotation(String simpleRNANotation) {
            this.simpleRNANotation = simpleRNANotation;
        }

        public boolean isStartFromFiveEnd() {
            return startFromFiveEnd;
        }

        public void setStartFromFiveEnd(boolean startFromFiveEnd) {
            this.startFromFiveEnd = startFromFiveEnd;
        }

        public boolean isStartFromThreeEnd() {
            return startFromThreeEnd;
        }

        public void setStartFromThreeEnd(boolean startFromThreeEnd) {
            this.startFromThreeEnd = startFromThreeEnd;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public String getStepText() {
            return stepText;
        }

        public void setStepText(String stepText) {
            this.stepText = stepText;
        }

        public String validate() {
            StringBuilder sb = new StringBuilder();
            try {
                List<PolymerNode> polymerNodes = ComplexNotationParser.getPolymerNodeList(inputComplexNotation);
                if (null == polymerNodes || polymerNodes.isEmpty()) {
                    sb.append("- There is no oligonucleotide sequence to fragment;\n");
                } else {
                    if (polymerNodes.size() != 1) {
                        sb.append("- This fragmenter only supports single nucleotide sequence;\n");
                    } else {
                        if (polymerNodes.get(0).getType().equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                            simpleRNANotation = polymerNodes.get(0).getLabel();
                        } else {
                            sb.append("- This fragmenter only supports single nucleotide sequence;\n");
                        }
                    }
                }

            } catch (NotationException ex) {
                sb.append("- Error parsing input notation;\n");
            }

            if (startFromFiveEnd || startFromThreeEnd) {
                //do nothing
            } else {
                sb.append("- At least one end needs to be specified to start frgamentation;\n");
            }

            try {
                step = Integer.parseInt(stepText);
                if (step <=0) {
                    sb.append("- Number of steps to perform must be positive integer;\n");
                }
            } catch (NumberFormatException nfe) {
                sb.append("- Number of steps to perform must be positive integer;\n");
            }

            return sb.toString();
        }

    }
}

