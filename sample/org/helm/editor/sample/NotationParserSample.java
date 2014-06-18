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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.helm.editor.data.GraphPair;
import org.helm.editor.utility.NotationParser;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.SimpleNotationParser;
import org.jdom.JDOMException;

/**
 *
 * @author ZHANGTIANHONG
 */
public class NotationParserSample {

    public static void main(String[] args) {
        try {
            // Start new test
            GraphPair graphPair = NotationParser.getGraphPair("RNA1{R(C)P.RP.R(A)P.RP.R(A)P.R(U)P}$RNA1,RNA1,4:R3-9:R3$$$");
            System.out.println(graphPair.getGraph());
            // End new test

            MonomerStore monomerStore=MonomerFactory.getInstance().getMonomerStore();
            String notation = "RNA1{R(A)P.[mR]P.[mR](U)P.[mR](T)P.[dR](U)}$$$$";
            ArrayList<PolymerNode> polymerNodeList = (ArrayList<PolymerNode>) NotationParser.getPolymerList(notation,monomerStore);
            for (PolymerNode node : polymerNodeList) {
                if (node.getType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                    ArrayList<Nucleotide> nucleotideList = (ArrayList<Nucleotide>) SimpleNotationParser.getNucleotideList(node.getLabel());
                    System.out.println("-----------");
                    for (Nucleotide nc : nucleotideList) {
                        if (nc.getSugarMonomer() != null) {
                            System.out.println("--Suger: " + nc.getSugarMonomer().getName() + ", id = " + nc.getSugarMonomer().getId());
                        }
                        if (nc.getBaseMonomer(monomerStore) != null) {
                            System.out.println("--Base: " + nc.getBaseMonomer(monomerStore).getName() + ", id = " + nc.getSugarMonomer().getId());
                        }
                        if (nc.getPhosphateMonomer() != null) {
                            System.out.println("--Phosphate: " + nc.getPhosphateMonomer().getName() + ", id = " + nc.getSugarMonomer().getId());
                        }
                    }

                }

            }
//            ComponentTableView.getAllComponentString(notation);

            //test getAllSimplePolymerNotation
            ArrayList<String> polymerStringList = NotationParser.getAllSimplePolymerNotation(notation);
            for (String string : polymerStringList) {
                System.out.println(string);
            }

        } catch (StructureException ex) {
            Logger.getLogger(NotationParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotationException ex) {
            Logger.getLogger(NotationParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MonomerException ex) {
            Logger.getLogger(NotationParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NotationParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(NotationParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
