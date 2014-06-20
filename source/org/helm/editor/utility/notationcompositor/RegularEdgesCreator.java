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
package org.helm.editor.utility.notationcompositor;

import java.util.Map;

import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;

import org.helm.editor.data.EdgeMapKeys;

public class RegularEdgesCreator implements NotationCreator {

	private static Condition EDGE_CONDITION = new NotPairableEdgesCondition();

	private static final int HYPER_GRAPH_POSITION = 0;
	private static final int NAME_MAP_POSITION = 1;

	@SuppressWarnings("unchecked")
	public String createNotationPart(Object[] args) {

		Graph hyperGraph = (Graph) args[HYPER_GRAPH_POSITION];
		Map<Node, String> nameMap = (Map<Node, String>) args[NAME_MAP_POSITION];

		return appendEdgesWithCondition(hyperGraph, nameMap, EDGE_CONDITION)
				+ NotationCompositor.NOTATION_PART_ENDING;
	}

	public static String appendEdgesWithCondition(Graph hyperGraph,
			Map<Node, String> nameMap, Condition condition) {

		StringBuilder notation = new StringBuilder();

		EdgeMap hyperEdgeMap = (EdgeMap) hyperGraph
				.getDataProvider(EdgeMapKeys.DESCRIPTION);
		EdgeCursor edges = hyperGraph.edges();
		int edgeCount = 0;

		for (; edges.ok(); edges.next()) {
			String edgeDesc = (String) hyperEdgeMap.get(edges.edge());

			boolean conditionValue = condition
					.condition(new Object[] { edgeDesc });
			if (edgeCount > 0 && conditionValue) {
				notation.append(NotationCompositor.NOTATION_DELIMETER);
			}

			if (conditionValue) {

				Node sourceNode = edges.edge().source();
				Node targetNode = edges.edge().target();

				notation.append(nameMap.get(sourceNode));
				notation.append(NotationCompositor.NOTATION_COMMA);
				notation.append(nameMap.get(targetNode));
				notation.append(NotationCompositor.NOTATION_COMMA);
				notation.append(edgeDesc);

				edgeCount++;
			}

		}

		return notation.toString();
	}

}
