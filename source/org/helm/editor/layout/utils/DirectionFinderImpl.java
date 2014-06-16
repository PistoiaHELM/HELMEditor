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
package org.helm.editor.layout.utils;

import java.util.ArrayList;
import java.util.List;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;

import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.SequenceGraphTools;

public class DirectionFinderImpl implements DirectionFinder {

	private GraphManager manager;
	private Graph2D graph;

	public DirectionFinderImpl(GraphManager manager, Graph2D graph2D) {
		this.manager = manager;
		this.graph = graph2D;
	}

	// TODO: should return direction in angle. Now true = from left to right,
	// false = right to left
	public boolean getDirection(Node dockNode) {
		boolean result;
		try {
			result = SequenceGraphTools.isLastNucleicacidBackbone(dockNode)
					|| SequenceGraphTools.isLastPeptideSequenceNode(dockNode,
							graph);
			if (isInAntiSence(dockNode)) {
				result = !result;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (manager.isFlipped(dockNode)) {
			result = !result;
		}

		return result;
	}

	private List<Node> getPaired(Node startNode) {
		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		Graph hyperGraph = manager.getHyperGraph();
		EdgeMap hyperEdgeDescMap = (EdgeMap) hyperGraph
				.getDataProvider(EdgeMapKeys.DESCRIPTION);
		List<Node> startingNodeList = manager.getStartingNodeList();
		List<Node> result = new ArrayList<Node>();

		Node hyperNode1 = (Node) parentNodeMap.get(startNode);
		for (Node aStartingNodeList : startingNodeList) {
			Node hyperNode2 = (Node) parentNodeMap.get(aStartingNodeList);
			Edge hyperEdge = hyperNode1.getEdge(hyperNode2);
			if (hyperEdge != null) {
				String edgeDesc = (String) hyperEdgeDescMap.get(hyperEdge);
				if (edgeDesc.contains("pair")) {
					result.add(aStartingNodeList);
				}
			}
		}
		return result;
	}

	public boolean isInAntiSence(Node startNode) {
		List<Node> paired = getPaired(startNode);
		if (paired.isEmpty()) {
			return false;
		}
		Node complementary = paired.get(0);
		return graph.getRealizer(startNode).getCenterY() > graph.getRealizer(
				complementary).getCenterY();

	}
}
