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
package org.helm.editor.protein.view;

import java.util.BitSet;
import java.util.List;

import org.helm.notation.model.Monomer;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.model.ComplexPolymer;
import org.helm.notation.model.PolymerEdge;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author zhangtianhong
 */
public class PeptidePolymer {

    private String complexNotation;

    private List<PolymerNode> nodes;
    private List<PolymerEdge> edges;
    private List<String> annotations; //put empty string if no annotation
    private String[] singleLetterSeq = null;
    private BitSet[] modifiedPos = null;
    private int[][] connection = null;
    public static final int EMPTY_DATA_MODE = 1;
    public static final int VALID_DATA_MODE = 2;
    public static final int INVALID_DATA_MODE = 3;
    private int dataMode = VALID_DATA_MODE;

    public PeptidePolymer() {
        this(null);
    }

    public PeptidePolymer(String complexNotation) {
        setNotation(complexNotation);
    }

    public String getNotation() {
        return complexNotation;
    }

    public void setNotation(String notation) {
        this.complexNotation = notation;
        if (null == complexNotation || complexNotation.length() == 0) {
            dataMode = EMPTY_DATA_MODE;
        } else {
            try {
                ComplexPolymer cp = ComplexNotationParser.parse(complexNotation);
                nodes = cp.getPolymerNodeList();
                edges = cp.getPolymerEdgeList();

                Map<String, String> annMap = cp.getPolymerNodeAnnotationMap();
                if (null != annMap && annMap.size() >0) {
                    annotations = new ArrayList<String>();
                    for (PolymerNode node : nodes) {
                        if (annMap.containsKey(node.getId())) {
                            annotations.add(annMap.get(node.getId()));
                        } else {
                            annotations.add("");
                        }
                    }

                } else {
                    annotations = new ArrayList<String>();
                    for (int i=0; i<nodes.size(); i++) {
                        annotations.add("");
                    }
                }

                for (PolymerNode node : nodes) {
                    if (!node.getType().equals(Monomer.PEPTIDE_POLYMER_TYPE)) {
                        dataMode = INVALID_DATA_MODE;
                        break;
                    }
                }
            } catch (Exception ex) {
                dataMode = INVALID_DATA_MODE;
            }

            if (dataMode == VALID_DATA_MODE) {
                try {
                    int size = nodes.size();

                    singleLetterSeq = new String[size];
                    modifiedPos = new BitSet[size];
                    for (int i = 0; i < size; i++) {
                        PolymerNode node = nodes.get(i);
                        singleLetterSeq[i] = SimpleNotationParser.getPeptideSequence(node.getLabel());
                        modifiedPos[i] = getModifiedPositions(node.getLabel());
                    }

                    size = edges.size();
                    connection = new int[size][4];
                    for (int i = 0; i < size; i++) {
                        PolymerEdge edge = edges.get(i);
                        String sourceNode = edge.getSourceNode();
                        int sourceSeqIndex = getSequenceIndex(sourceNode);
                        int sourceMonIndex = edge.getSourceMonomerNumber();

                        String targetNode = edge.getTargetNode();
                        int targetSeqIndex = getSequenceIndex(targetNode);
                        int targetMonIndex = edge.getTargetMonomerNumber();

                        connection[i] = new int[]{sourceSeqIndex, sourceMonIndex, targetSeqIndex, targetMonIndex};
                    }
                } catch (Exception ex) {
                    dataMode = INVALID_DATA_MODE;
                }
            }
        }
    }

    private BitSet getModifiedPositions(String simplePeptideNotation) throws Exception {
        BitSet bits = new BitSet();
        List<Monomer> monomerList = SimpleNotationParser.getMonomerList(simplePeptideNotation, Monomer.PEPTIDE_POLYMER_TYPE);

        int monomerCount = monomerList.size();
        for (int j = 0; j < monomerCount; j++) {
            Monomer m = monomerList.get(j);

            if ((m != null && m.isModified())) {
                bits.set(j);
            }
        }
        return bits;
    }

    private int getSequenceIndex(String nodeID) {
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            PolymerNode node = nodes.get(i);
            if (nodeID.equals(node.getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    public String getAnnotation(int strandIndex) {
        return annotations.get(strandIndex);
    }

    public int getLength(int strandIndex) {
        String seq = getSingleLetterSeq(strandIndex);
        if (null == seq) {
            seq = "";
        }
        return seq.length();
    }

    public String getSingleLetterSeq(int strandIndex) {
        if (singleLetterSeq != null) {
            if (singleLetterSeq.length <= strandIndex) {
                return null;
            }
            return singleLetterSeq[strandIndex];
        }
        return null;
    }

    public BitSet getModifiedPos(int strandIndex) {
        if (modifiedPos != null && modifiedPos.length > strandIndex) {
            return modifiedPos[strandIndex];
        }
        return new BitSet();

    }

    /*
     * one (1) based index for seq and monomer
     * 1. source seq index, 2. source monomer index, 3. target seq index, 4. target monomer index
     */
    public int[] getConnection(int connectionIndex) {
        if (connection != null && connection.length > connectionIndex) {
            return connection[connectionIndex];
        }
        return null;

    }

    public int getNumberOfStrands() {

        if (nodes == null) {
            nodes = new ArrayList<PolymerNode>();
        }
        return nodes.size();

    }

    public int getNumberOfConnections() {

        if (edges == null) {
            edges = new ArrayList<PolymerEdge>();
        }
        return edges.size();

    }

    public int getDataMode() {
        return dataMode;
    }

}
