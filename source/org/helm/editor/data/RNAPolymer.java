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
package org.helm.editor.data;


import java.util.BitSet;
import java.util.List;

import org.helm.notation.MonomerStore;
import org.helm.notation.NotationException;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.NucleotideSequenceParser;
import org.helm.notation.tools.SimpleNotationParser;

/**
 * Extracted from PFRED by Simon Xi;
 * Used by RNATableCellRenderer;
 * Input data could be single strand nucleotide sequence, simple RNA notation or complex notation
 * @author zhangtianhong
 */
public class RNAPolymer {

    private String originalData = null;
    private String complexNotation = null;
    private List<PolymerNode> nodes = null;
    private String[] singleLetterSeq = null;
    private int[][] basePairing = null;
    private BitSet[] modifiedPosphatePos = null;
    private BitSet[] modifiedBaseOrSugarPos = null;
    private BitSet[] modifiedBasePos = null;
    private String[][] sugarCodes = null;
    public static final int EMPTY_DATA_MODE = 1;
    public static final int VALID_DATA_MODE = 2;
    public static final int INVALID_DATA_MODE = 3;
    private int dataMode = VALID_DATA_MODE;
    boolean isNotation = false;

    public RNAPolymer() {
        this(null);
    }

    public RNAPolymer(String input) {
        originalData = input;
        if (null == input || input.length() == 0 || input.equalsIgnoreCase("RNA1{}$$$$")) {
            dataMode = EMPTY_DATA_MODE;
        } else {
            if (input.indexOf("$") >= 0) {
                complexNotation = input;
                isNotation = true;
            } else if (input.indexOf("(") >= 0) {
                isNotation = true;
                try {
                    complexNotation = SimpleNotationParser.getComplextNotationForRNA(input);
                } catch (Exception ex) {
                    dataMode = INVALID_DATA_MODE;
                }

            } else {
                try {
                    String simpleNotation = NucleotideSequenceParser.getNotation(input);
                    complexNotation = SimpleNotationParser.getComplextNotationForRNA(simpleNotation);
                } catch (Exception ex) {
                    dataMode = INVALID_DATA_MODE;
                }
            }

            if (dataMode == VALID_DATA_MODE) {
                try {

                    nodes = ComplexNotationParser.getPolymerNodeList(complexNotation);
                    if (nodes.size() > 2) {
                        dataMode = INVALID_DATA_MODE;
                    } else {
                        for (PolymerNode node : nodes) {
                            if (!node.getType().equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                                dataMode = INVALID_DATA_MODE;
                                break;
                            }
                        }
                        if (dataMode == VALID_DATA_MODE && nodes.size() == 2) {
                            String basePair = ComplexNotationParser.getAllBasePairString(complexNotation);
                            if (null == basePair || basePair.length() == 0) {
                                dataMode = INVALID_DATA_MODE;
                            }

                        }
                    }

                } catch (NotationException ex) {
                    dataMode = INVALID_DATA_MODE;
                }
            }

            if (dataMode == VALID_DATA_MODE) {
                try {
                    initialize();
                } catch (Exception ex) {
                    dataMode = INVALID_DATA_MODE;
                }

                if (getSingleLetterSeq() == null || getSingleLetterSeq().equals("X")) {
                    dataMode = INVALID_DATA_MODE;
                }
            }

        }
    }

    public String getNotation() {
        return complexNotation;
    }

    public boolean isRNANotation() {
        return isNotation;
    }

    private void initialize() throws Exception {
        int size = nodes.size();

        singleLetterSeq = new String[size];
        modifiedBaseOrSugarPos = new BitSet[size];
        modifiedPosphatePos = new BitSet[size];
        modifiedBasePos = new BitSet[size];
        sugarCodes = new String[size][];
        
        MonomerStore monomerStore=MonomerStoreCache.getInstance().getCombinedMonomerStore();
        for (int i = 0; i < size; i++) {
            PolymerNode node = nodes.get(i);
            List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel(), false);

            singleLetterSeq[i] = getSingleLetterSequence(bases);

            modifiedBaseOrSugarPos[i] = getModifiedBaseOrSugarPositions(bases,monomerStore);

            modifiedPosphatePos[i] = getModifiedPosphatePositions(bases);

            sugarCodes[i] = getSugarCodes(bases);

            modifiedBasePos[i] = getModifiedBasePositions(bases,monomerStore);
        }

        initializeBasePairing();
    }

    private String getSingleLetterSequence(List<Nucleotide> bases) throws Exception {

        //get dna or rna flap
        if (bases == null || bases.size() == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return null;
        }


        int baseCount = bases.size();

        StringBuffer result = new StringBuffer();


        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            String b = n.getNaturalAnalog();
            if (n.getSugarMonomer() != null && n.getSugarMonomer().getAlternateId() != null
                    && n.getSugarMonomer().getAlternateId().equals("dR")) {
                b = b.toLowerCase();
            }
            result.append(b);

        }
        return result.toString();
    }

    private BitSet getModifiedBasePositions(List<Nucleotide> bases,MonomerStore monomerStore) throws Exception {

        BitSet bits = new BitSet();

        //get dna or rna flap
        if (bases == null || bases.size() == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return bits;
        }


        int baseCount = bases.size();
        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            if ((n.getBaseMonomer(monomerStore) != null && n.getBaseMonomer(monomerStore).isModified())) {
                bits.set(j);
            }
        }
        return bits;
    }

    private BitSet getModifiedBaseOrSugarPositions(List<Nucleotide> bases,MonomerStore monomerStore) throws Exception {

        BitSet bits = new BitSet();

        //get dna or rna flap
        if (bases == null || bases.size() == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return bits;
        }


        int baseCount = bases.size();
        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            if ((n.getBaseMonomer(monomerStore) != null && n.getBaseMonomer(monomerStore).isModified()) || (n.getSugarMonomer() != null && n.getSugarMonomer().isModified())) {
                bits.set(j);
            }
        }
        return bits;
    }

    private String[] getSugarCodes(List<Nucleotide> bases) throws Exception {
        //get dna or rna flap
        if (bases == null || bases.size() == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return null;
        }

        String[] codes = new String[bases.size()];
        for (int j = 0; j < codes.length; j++) {
            Nucleotide n = bases.get(j);
            Monomer sugar = n.getSugarMonomer();
            if (sugar != null) {
                codes[j] = sugar.getAlternateId();
            }
        }
        return codes;
    }

    private BitSet getModifiedPosphatePositions(List<Nucleotide> bases) throws Exception {
        BitSet bits = new BitSet();

        //get dna or rna flap
        if (bases == null || bases.size() == 0) {
            //System.err.println("unable to convert oligo_notation to single string for "+ node.getLabel());
            return bits;
        }

        int baseCount = bases.size();

        for (int j = 0; j < baseCount; j++) {
            Nucleotide n = bases.get(j);

            if (n.getPhosphateMonomer() != null && n.getPhosphateMonomer().isModified()) {
                bits.set(j);
            }
        }
        return bits;
    }

    private void initializeBasePairing() throws Exception {
        if (!isDoubleStrand()) {
            return;
        }

        String basepair_notation = ComplexNotationParser.getAllBasePairString(complexNotation);


        if (basepair_notation == null) {
            return;
        }

        //parse them into base pairs
        //it should look something like this: RNA1,RNA2,14:pair-44:pair|RNA1,RNA2,17:pair-41:pair
        String[] fields = basepair_notation.split("\\|");
        int len = fields.length;
        int[][] pairing = new int[fields.length][2];
        int count = 0;
        for (int i = 0; i < len; i++) {
            String field = fields[i];
            String[] fields2 = field.split(",");
            if (fields2.length != 3) {//field1 and 2 are the RNA names
                continue; //skip this
            }
            field = fields2[2];
            fields2 = field.split("-");
            if (fields2.length != 2) {
                continue;
            }


            int index = fields2[0].indexOf(":");
            if (index < 0) {
                continue;
            }
            int pos1 = Integer.parseInt(fields2[0].substring(0, index));


            index = fields2[1].indexOf(":");
            if (index < 0) {
                continue;
            }
            int pos2 = Integer.parseInt(fields2[1].substring(0, index));

            pairing[count][0] = (pos1 + 1) / 3 - 1;
            pairing[count][1] = (pos2 + 1) / 3 - 1;
            count++;

        }

        if (count == 0) {
            return;
        }

        int[][] results = new int[count][2];
        for (int i = 0; i < count; i++) {
            results[i][0] = pairing[i][0];
            results[i][1] = pairing[i][1];
        }

        basePairing = results;
    }

    public String toString() {
        return originalData;
    }

    public int getLength() {
        if (!isDoubleStrand()) {
            return singleLetterSeq[0].length();
        }
        return getLength(0);
    }

    public boolean isDoubleStrand() {
        return nodes.size() == 2;
    }

    public String getSingleLetterSeq() {
        if (singleLetterSeq != null && singleLetterSeq.length > 0) {
            return singleLetterSeq[0];
        }
        return null;
    }

    //************************************** below are functions relevant only to multiple strands ***************************
    //return basepairing of the strands
    public int[][] getBasePairing() {

        if (!isDoubleStrand()) {
            return null;
        }

        return basePairing;
    }

    public int getLength(int strand) {

        if (nodes == null || nodes.size() <= strand) {
            return 0;
        }

        PolymerNode node = nodes.get(strand);
        try {

            return getLength(node);
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getLength(PolymerNode node) throws Exception {
        if (node == null) {
            return 0;
        }

        List<Nucleotide> bases = SimpleNotationParser.getNucleotideList(node.getLabel(), false);
        if (bases == null) {
            return 0;
        }
        return bases.size();

    }

    public String getSingleLetterSeq(int strand) {
        if (singleLetterSeq != null) {
            if (singleLetterSeq.length <= strand) {
                return null;
            }
            return singleLetterSeq[strand];
        }
        return null;
    }

    public BitSet getModifiedBaseOrSugarPos(int strand) {
        if (modifiedBaseOrSugarPos != null && modifiedBaseOrSugarPos.length > strand) {
            return modifiedBaseOrSugarPos[strand];
        }
        return new BitSet();

    }

    public BitSet getModifiedBasePos(int strand) {
        if (modifiedBasePos != null && modifiedBasePos.length > strand) {
            return modifiedBasePos[strand];
        }
        return new BitSet();

    }

    public BitSet getModifiedPhophatePos(int strand) {
        if (modifiedPosphatePos != null && modifiedPosphatePos.length > strand) {
            return modifiedPosphatePos[strand];
        }

        return new BitSet();
    }

    public String[] getSugarCodes(int strand) {
        if (sugarCodes != null && sugarCodes.length > strand) {
            return sugarCodes[strand];
        }
        return null;
    }

    public int getNumberOfStrands() {

        if (nodes == null) {
            if (singleLetterSeq != null) {
                return 1;
            }
            return 0;
        }
        return nodes.size();

    }

    /**
     * @return the dataMode
     */
    public int getDataMode() {
        return dataMode;
    }
}
