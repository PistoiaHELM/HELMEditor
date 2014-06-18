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

import org.helm.editor.data.NodeMapKeys;
import org.helm.notation.model.Monomer;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.notation.model.Attachment;
import java.util.Comparator;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;

/**
 * compare two hyper node
 * 
 * @author LIH25
 */
public class NodeComparator implements Comparator<Node> {

	/**
	 * The same node that the two nodes being compared to are both connected to,
	 * if there is any
	 */
	private Node parentNode = null;

	public NodeComparator(Node parentNode) {
		super();
		this.parentNode = parentNode;
	}

	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	public int compare(final Node node1, final Node node2) {
		Graph graph = node1.getGraph();
		final NodeMap nodeNameMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION);
		final NodeMap nodeTypeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);

		String type1 = (String) nodeTypeMap.get(node1);
		String type2 = (String) nodeTypeMap.get(node2);

		if (comparePolymerType(type1, type2) != 0) {
			return comparePolymerType(type1, type2);
		} else { // two node are of the same type
			if (parentNode != null) {
				Edge edge1 = parentNode.getEdge(node1);
				Edge edge2 = parentNode.getEdge(node2);

				EdgeMap edgeMap = (EdgeMap) graph
						.getDataProvider(EdgeMapKeys.DESCRIPTION);
				String edgeDesc1 = (String) edgeMap.get(edge1);
				String edgeDesc2 = (String) edgeMap.get(edge2);

				if (edgeDesc1.contains(Attachment.PAIR_ATTACHMENT)) {
					return -1;
				} else if (edgeDesc2.contains(Attachment.PAIR_ATTACHMENT)) {
					return 1;
				}
			}

			String name1 = (String) nodeNameMap.get(node1);
			String name2 = (String) nodeNameMap.get(node2);

			if (name1.compareToIgnoreCase(name2) != 0) {
				return name1.compareToIgnoreCase(name2);
			} else {
				return (node1.degree() - node2.degree());
			}
		}

	}

	/**
	 * compare two poly types
	 * 
	 * @param polyType1
	 * @param polyType2
	 * @return -1: polyType1 < polyType2; 0 : equal; 1: polyType1 > polyType2
	 */
	private static int comparePolymerType(String polyType1, String polyType2) {
		// NUCLIEC_ACID_POLYMER_TYPE < PEPTIDE_POLYMER_TYPE < CHEM
		if (polyType1.equalsIgnoreCase(polyType2)) {
			return 0;
		} else if (polyType1
				.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return -1;
		} else if (polyType1.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
			if (polyType2.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				return 1;
			} else { // polyType2.equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)
				return -1;
			}
		} else // if(polyType1.equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)){
		{
			return 1;
		}
	}
}
