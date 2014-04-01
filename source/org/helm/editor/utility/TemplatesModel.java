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
package org.helm.editor.utility;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.notation.model.Nucleotide;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 *
 * @author lih25
 */
/**
 * Very simple <code>ListModel</code> that uses the <code>NodeRealizer</code>s
 * of a frozen graph as its data.
 */
public class TemplatesModel extends AbstractListModel {

    private final Node[] data;
    private final Graph2D graph;

    public TemplatesModel(final Graph2D graph) {
        this.graph = graph;
//        NodeList nodeList = new NodeList(graph.nodes());
        HierarchyManager nhm = graph.getHierarchyManager();

        if (nhm != null) {
            final NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION);
            this.graph.sortNodes(new Comparator() {

                public int compare(Object o1, Object o2) {
                    NodeRealizer nr1 = graph.getRealizer((Node) o1);
                    NodeRealizer nr2 = graph.getRealizer((Node) o2);

                    Nucleotide nucleotide1 = new Nucleotide(nr1.getLabelText(), (String) nodeMap.get(o1));
                    Nucleotide nucleotide2 = new Nucleotide(nr2.getLabelText(), (String) nodeMap.get(o2));

                    if (nucleotide1.getNaturalAnalog() != null && nucleotide2.getNaturalAnalog() != null) {
                        return (nucleotide1.getNaturalAnalog().compareToIgnoreCase(nucleotide2.getNaturalAnalog()));
                    } else {
                        return (nr1.getLabelText().compareTo(nr2.getLabelText()));
                    }
                }
            });
        } else {

            final NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
            this.graph.sortNodes(new Comparator() {

                public int compare(Object o1, Object o2) {
                    try {
                        MonomerInfo mi1 = (MonomerInfo) nodeMap.get((Node) o1);
                        MonomerInfo mi2 = (MonomerInfo) nodeMap.get((Node) o2);
                        return mi1.getMonomerID().compareToIgnoreCase(mi2.getMonomerID());
//                        Monomer monomer1 = MonomerFactory.getInstance().getMonomerDB().get(mi1.getPolymerType()).get(mi1.getMonomerID());
//                        Monomer monomer2 = MonomerFactory.getInstance().getMonomerDB().get(mi2.getPolymerType()).get(mi2.getMonomerID());
//                        String st1 = monomer1.getNaturalAnalog() + "-" + monomer1.getAlternateId();
//                        String st2 = monomer2.getNaturalAnalog() + "-" + monomer2.getAlternateId();
//                        return st1.compareToIgnoreCase(st2);
                    } catch (Exception ex) {
                        Logger.getLogger(TemplatesModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return 0;
                }
            });

        }

        data = this.graph.getNodeArray();
    }


    public int getSize() {
        return data.length;
    }

    public Object getElementAt(final int index) {
        return ((Graph2D) data[index].getGraph()).getRealizer(data[index]);
    }
    
    public Node getNodeByMonomerId(String monomerId) {
    	
    	if ((monomerId == null) || (data == null)) {
    		return null;
    	}

    	for(int i = 0; i < data.length; i++){
    		if (monomerId.equals(MonomerInfoUtils.getMonomerLabelText(data[i], graph))) {
    			return ((NodeRealizer)getElementAt(i)).getNode(); 
    		}
    	}
    	    	
    	return null;
    }
}
