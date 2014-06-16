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

import java.util.HashSet;
import java.util.Set;

import org.helm.editor.utility.MonomerInfoUtils;

import y.base.Edge;
import y.base.Graph;
import y.base.Node;

/**
 * Iterator that iterates only on sense i.e. does not walk on hydrogen bonds
 * 
 * 
 * @author Dmitry Zhelezov
 * 
 */
public class SenseIterator extends AbstractBfsIterator {

	public SenseIterator(Graph graph, Node startNode) {
		super(graph, startNode);
	}

	@Override
	protected boolean isOk(Node node) {
		return !MonomerInfoUtils.isChemicalModifierPolymer(node);
	}

	@Override
	protected Set<Node> getAdjacentNodes(Node node) {
		Set<Node> result = new HashSet<Node>();
		Graph graph = node.getGraph();
		Edge[] edges = graph.getEdgeArray();
		for (Edge e : edges) {
			if (MonomerInfoUtils.isPair(e)) {
				continue;
			}
			if ((e.source() == node) || (e.target() == node)) {
				result.add(e.opposite(node));
			}
		}
		return result;
	}

}
