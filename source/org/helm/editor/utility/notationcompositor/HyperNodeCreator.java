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

import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;

import org.helm.editor.data.NodeMapKeys;

public class HyperNodeCreator implements NotationCreator {

	private static final int HYPER_GRAPH_POSITION = 0;
	private static final int NAME_MAP_POSITION = 1;

	@SuppressWarnings("unchecked")
	public String createNotationPart(Object[] args) {

		Graph hyperGraph = (Graph) args[HYPER_GRAPH_POSITION];
		Map<Node, String> nameMap = (Map<Node, String>) args[NAME_MAP_POSITION];

		StringBuilder notation = new StringBuilder();

		notation.append(NotationCompositor.NOTATION_PART_ENDING);

		NodeMap notationMap = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE_ANOTATION);
		String anotation;
		int i = 0;
		for (Node hyperNode : nameMap.keySet()) {
			anotation = null;
			anotation = (String) notationMap.get(hyperNode);
			if (anotation != null && !anotation.equalsIgnoreCase("")) {

				notation.append(nameMap.get(hyperNode));
				notation.append(NotationCompositor.NOTATION_BEGINING);
				notation.append(anotation);
				notation.append(NotationCompositor.NOTATION_ENDING);

				notation.append(NotationCompositor.NOTATION_DELIMETER);

			}
		}

		String finalNotation = notation.toString();
		if (finalNotation.endsWith(NotationCompositor.NOTATION_DELIMETER)) {
			finalNotation = finalNotation.substring(0,
					finalNotation.length() - 1);
		}

		return finalNotation;

	}

}
