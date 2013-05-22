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
package org.helm.editor.layout.procedures;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import y.algo.GraphConnectivity;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.LayoutGraph;
import y.util.GraphHider;

import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.primitives.AbstractLayoutPrimitives;
import org.helm.notation.model.Monomer;

public class ComplementaryStructuresLayout extends AbstratStructureLayout {
	@Override
	protected boolean canLayoutCore(LayoutGraph arg0) {
		return true;
	}

	@Override
	protected void doLayoutCore(LayoutGraph graph) {
		DataProvider node2Hipernode = graph.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		Graph hiperGraph = ((Node)node2Hipernode.get(graph.firstNode())).getGraph();
		DataProvider hipernode2Index = hiperGraph.getDataProvider(NodeMapKeys.HYPERNODE2INDEX);
		DataProvider hipernode2PolymerType = hiperGraph.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);

		// hide pair edges and find all sequences 
		GraphHider graphHider = new GraphHider(graph);
		for (Edge edge : graph.getEdgeArray()) {
			DataProvider edgeTypeMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
			if (!((EdgeInfo)edgeTypeMap.get(edge)).isRegular()) {
				graphHider.hide(edge);
			}
		}
		NodeList[] components = GraphConnectivity.connectedComponents(graph);
		graphHider.unhideAll();

		// get the hiper node with the smallest index
		Node smallestIndexHiperNode = (Node)node2Hipernode.get(components[0].firstNode());
		int smallestIndexComponentIndex = hipernode2Index.getInt(smallestIndexHiperNode);
		for (int i = 1; i < components.length; i++) {
			Node hiperNode = (Node)node2Hipernode.get(components[i].firstNode());
			int componentIndex = hipernode2Index.getInt(hiperNode);
			if (smallestIndexComponentIndex > componentIndex) {
				smallestIndexHiperNode = hiperNode;
				smallestIndexComponentIndex = componentIndex;
			}
		}

		// if get(node) == 'true' then sequence is flipped; otherwise - is not 
		Map<Node, Boolean> flippedHyperNodes = new HashMap<Node, Boolean>();
		flippedHyperNodes.put(smallestIndexHiperNode, false);
		// for each sequence this map defines another sequence which the shifting procedure is based on 
		Map<Node, Node> hyperNode2PairedHiperNodeMap = new HashMap<Node, Node>();
		hyperNode2PairedHiperNodeMap.put(smallestIndexHiperNode, null);

		// hyoerNode -> [startingNode, endNode]. 
		// Should be used for intersection definition
		Map<Node, Node[]> hyperNode2StartAndEndNodes = new LinkedHashMap<Node, Node[]>();

		Queue<Node> visitedHiperNodes = new LinkedList<Node>();
		visitedHiperNodes.offer(smallestIndexHiperNode);
		while (visitedHiperNodes.size() > 0) {
			Node currentHiperNode = visitedHiperNodes.poll();
			boolean isCurrectFlipped = flippedHyperNodes.get(currentHiperNode).booleanValue();

			// to keep the order of components handled we need to process them in the index increasing order
			NodeList neighboursList = new NodeList(currentHiperNode.neighbors());
			int neighboursListSize = neighboursList.size();
			for (int i = 0; i < neighboursListSize; i++) {
				// select hyper node
				Node hiperNode = neighboursList.firstNode();
				int hiperNodeIndex = hipernode2Index.getInt(hiperNode);
				for (NodeCursor nodeCursor = neighboursList.nodes(); nodeCursor.ok(); nodeCursor.next()) {
					if (hipernode2Index.getInt(nodeCursor.node()) < hiperNodeIndex) {
						hiperNode = nodeCursor.node();
						hiperNodeIndex = hipernode2Index.getInt(hiperNode);
					}
				}
				neighboursList.remove(hiperNode);

				// Process hyper node
				// omit chem modifiers hyper nodes
				if (Monomer.CHEMICAL_POLYMER_TYPE.equals(hipernode2PolymerType.get(hiperNode))) {
					continue;
				}
				if (!flippedHyperNodes.containsKey(hiperNode)) {
					visitedHiperNodes.offer(hiperNode);
					flippedHyperNodes.put(hiperNode, !isCurrectFlipped);
					hyperNode2PairedHiperNodeMap.put(hiperNode, currentHiperNode);
				}
			}

			// get corresponding component
			NodeList component = null;
			for (NodeList curComponent : components) {
				Node startingHiperNode = (Node)node2Hipernode.get(curComponent.firstNode());
				if (startingHiperNode.equals(currentHiperNode)) {
					component = curComponent;
					break;
				}
			}
			// get start and end points
			Node startingNode = layoutPrimitives.getStartingNode(component.firstNode());
			Node endNode = layoutPrimitives.getEndNode(component.lastNode());
			hyperNode2StartAndEndNodes.put(currentHiperNode, new Node[] {startingNode, endNode});

			// layout procedure
			layoutPrimitives.layoutSequence(graph, startingNode, null, true);
			if (isCurrectFlipped && !layoutPrimitives.isPeptidePolymer(startingNode)) {
				layoutPrimitives.rotate180(startingNode, null, graph);
			}
			layoutPrimitives.setFlipState(component.nodes(), isCurrectFlipped);

			Node parentHyperNode = hyperNode2PairedHiperNodeMap.get(currentHiperNode);
			if (parentHyperNode != null) {
				layoutPrimitives.shiftSubgraph(graph, startingNode, null, parentHyperNode, isCurrectFlipped);				
			}

			// check intersection and do a shift if needed			
			handleIntersections(graph, currentHiperNode, flippedHyperNodes, hyperNode2StartAndEndNodes);
		}
	}

	private void handleIntersections(
			LayoutGraph graph,
			Node currentHiperNode,
			Map<Node, Boolean> flippedHyperNodes,
			Map<Node, Node[]> hyperNode2StartAndEndNodes) {

		boolean isCurFlipped = flippedHyperNodes.get(currentHiperNode).booleanValue();
		Node[] curStartEndNodes = hyperNode2StartAndEndNodes.get(currentHiperNode);

		boolean ifSequencesIntersect;
		do {
			ifSequencesIntersect = false;
			for (Node hyperNode : hyperNode2StartAndEndNodes.keySet()) {
				// check the side (flipped or not)
				boolean isFlipped = flippedHyperNodes.get(hyperNode).booleanValue();
				// there is no need to check intersection with the same sequence or 
				// with sequences from another side
				if (hyperNode.equals(currentHiperNode) || isFlipped != isCurFlipped) {
					continue;
				}

				Node[] startEndNodes = hyperNode2StartAndEndNodes.get(hyperNode);
				ifSequencesIntersect = crosses(graph, isCurFlipped,
						curStartEndNodes[0], curStartEndNodes[1],
						startEndNodes[0], startEndNodes[1]);
				if (ifSequencesIntersect) {
					break;
				}
			}
			// do a shift
			if (ifSequencesIntersect) {
				layoutPrimitives.verticalShiftSubgraph(graph, curStartEndNodes[0], curStartEndNodes[1], isCurFlipped);
			}
		} while (ifSequencesIntersect);
	}

	private boolean crosses(LayoutGraph graph, boolean isFlipped,
			Node startNode1, Node endNode1,
			Node startNode2, Node endNode2) {

		if (Math.abs(graph.getCenterY(startNode1) - graph.getCenterY(startNode2)) < AbstractLayoutPrimitives.EPS) {
			if (isFlipped && !layoutPrimitives.isPeptidePolymer(startNode1)) {
				return (graph.getCenterX(startNode1) >= graph.getCenterX(endNode2) &&
						graph.getCenterX(endNode1) <= graph.getCenterX(startNode2));
			} else {
				return (graph.getCenterX(endNode1) >= graph.getCenterX(startNode2) &&
						graph.getCenterX(startNode1) <= graph.getCenterX(endNode2));
			}
		}			
		return false;
	}

}
