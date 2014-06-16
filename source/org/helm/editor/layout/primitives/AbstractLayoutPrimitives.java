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
package org.helm.editor.layout.primitives;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import y.algo.GraphChecker;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.BufferedLayouter;
import y.layout.LayoutGraph;
import y.layout.LayoutTool;
import y.layout.transformer.GraphTransformer;
import y.util.GraphHider;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

import org.helm.editor.componentPanel.sequenceviewpanel.EdgeType;
import org.helm.editor.data.AbstractEdgeInfo;
import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.layout.metrics.IViewMetrics;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;

public abstract class AbstractLayoutPrimitives {
	public static final double EPS = 0.001;

	public abstract void layoutSequence(LayoutGraph graph, Node sequenceStart,
			Node sequenceEnd, boolean directOrder);

	public abstract void layoutLoop(LayoutGraph graph, Node loopStart,
			Node loopEnd, boolean layoutStartingNode);

	public abstract void arrangeNodesVisualisationSettings(Graph2D graph);

	public abstract void arrangeEdgesVisualisationSettings(Graph2D graph);

	public abstract Node getRNode(Node baseNode);

	public NodeList getSequenceNodes(Node startingNode, Node lastNode,
			Graph graph, boolean directOrder) {
		NodeList selectedList = new NodeList();
		Set<Node> visited = new HashSet<Node>();

		Queue<Node> nodesQueue = new LinkedList<Node>();
		nodesQueue.offer(startingNode);
		Node currentNode = null;
		while (nodesQueue.size() > 0) {
			currentNode = nodesQueue.poll();
			selectedList.add(currentNode);
			visited.add(currentNode);
			if (currentNode.equals(lastNode)) {
				break;
			}

			NodeCursor nodeCursor = directOrder ? currentNode.successors()
					: currentNode.predecessors();
			for (; nodeCursor.ok(); nodeCursor.next()) {
				Edge edge = currentNode.getEdge(nodeCursor.node());
				if (!(MonomerInfoUtils.isBranchEdge(edge) && isPeptidePolymer(currentNode))
						&& !MonomerInfoUtils.isPair(edge)
						&& !visited.contains(nodeCursor.node())) {
					nodesQueue.offer(nodeCursor.node());
				}
			}
		}

		// add branch and p nodes to the selected sequence is it is a nucleic
		// acid polymer
		if (lastNode != null
				&& MonomerInfoUtils.isNucleicAcidPolymer(startingNode)) {
			for (NodeCursor successors = currentNode.successors(); successors
					.ok(); successors.next()) {
				Edge edge = currentNode.getEdge(successors.node());
				if (!(MonomerInfoUtils.isBranchEdge(edge) && isPeptidePolymer(currentNode))
						&& !MonomerInfoUtils.isPair(edge)
						&& !visited.contains(successors.node())) {
					selectedList.add(successors.node());
				}
			}
		}

		return selectedList;
	}

	public Node getStartingNode(Node node) {
		if (GraphChecker.isCyclic(node.getGraph())) {
			return node;
		}

		NodeList curNodeToStartingNodeList = getSequenceNodes(node, null,
				node.getGraph(), false);
		return curNodeToStartingNodeList.lastNode();
	}

	public Node getEndNode(Node node) {
		if (GraphChecker.isCyclic(node.getGraph())) {
			return node;
		}

		NodeList curNodeToEndNodeList = getSequenceNodes(node, null,
				node.getGraph(), true);
		return curNodeToEndNodeList.lastNode();
	}

	/**
	 * @param startingNode
	 *            starting node in the sequence of nodes where loop is placed
	 * @param graph
	 * @return start and end loop nodes
	 */
	public abstract Node[] getLoopBounds(Node startingNode, LayoutGraph graph);

	public abstract IViewMetrics getViewMetrics();

	// ////////////////////////////////
	// Chem nodes layout primitives //
	// ////////////////////////////////
	/**
	 * @return x = dX, y = dY
	 */
	public abstract Point getChemNodesFloatongSequenceLayoutMetrics();

	public abstract Point getChemNodesDockedSequenceLayoutMetrics();

	public abstract int getChemNodesYLayoutStart(LayoutGraph graph,
			Set<Node> layoutedNodes);

	private void preFlip(GraphHider graphHider, LayoutGraph graph,
			Node startingNode, Node end) {
		EdgeCursor edges = null;
		HashSet<Node> selected = new HashSet<Node>();
		NodeList nodeList = getSequenceNodes(startingNode, end, graph, true);
		NodeCursor selectedNodes = nodeList.nodes();
		for (; selectedNodes.ok(); selectedNodes.next()) {
			selected.add(selectedNodes.node());
		}

		NodeCursor nodes = graph.nodes();
		for (; nodes.ok(); nodes.next()) {
			if (!selected.contains(nodes.node())) {
				edges = nodes.node().edges();
				for (; edges.ok(); edges.next()) {
					// if both the source and target startingNode is not
					// selected, hide it
					if (!selected.contains(edges.edge().source())
							&& !selected.contains(edges.edge().target())) {
						graphHider.hide(edges.edge());
					}
				}
				graphHider.hide(nodes.node());
			}
		}
	}

	private void postFlip(GraphHider graphHider) {
		graphHider.unhideAll();
	}

	/**
	 * rotate the sequence starting with the starting startingNode 180 degree
	 * 
	 * @param startingNode
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public void rotate180(Node startingNode, Node end, LayoutGraph graph) {
		// calculate the geometric info of the rotated graph
		GraphTransformer graphTransformer = new GraphTransformer();
		graphTransformer.setOperation(GraphTransformer.ROTATE);
		graphTransformer.setRotationAngle(180);

		GraphHider graphHider = new GraphHider(graph);
		preFlip(graphHider, graph, startingNode, end);
		(new BufferedLayouter(graphTransformer)).doLayout(graph);
		postFlip(graphHider);
	}

	public void setFlipState(NodeCursor nodeCursor, boolean isFlipped) {
		DataProvider node2LabelInfo = nodeCursor.node().getGraph()
				.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);

		// TODO remove it
		if (node2LabelInfo == null)
			return;

		for (; nodeCursor.ok(); nodeCursor.next()) {
			LabelInfo labelInfo = (LabelInfo) node2LabelInfo.get(nodeCursor
					.node());
			if (labelInfo != null) {
				labelInfo.setFlipped(isFlipped);
			}
		}
	}

	public void setFlipState(Node node, boolean isFlipped) {
		DataProvider node2LabelInfo = node.getGraph().getDataProvider(
				NodeMapKeys.LABEL_INFO_MAP);
		LabelInfo labelInfo = (LabelInfo) node2LabelInfo.get(node);
		if (labelInfo != null) {
			labelInfo.setFlipped(isFlipped);
		}
	}

	public void shiftSubgraph(LayoutGraph graph, Node startingNode, Node end,
			Node parentHyperNode, boolean isFlipped) {
		DataProvider node2Hipernode = graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		DataProvider edgeMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
		NodeList nodeList = getSequenceNodes(startingNode, end, graph, true);

		for (NodeCursor sequenceNodes = nodeList.nodes(); sequenceNodes.ok(); sequenceNodes
				.next()) {
			for (EdgeCursor edges = sequenceNodes.node().edges(); edges.ok(); edges
					.next()) {
				EdgeInfo edgeInfo = (EdgeInfo) edgeMap.get(edges.edge());
				if (edgeInfo.isPair() || !edgeInfo.isRegular()
						&& isPeptidePolymer(nodeList.firstNode())) {
					Node sourceNode = sequenceNodes.node();
					Node targetNode = null;
					if (sequenceNodes.node() != edges.edge().target()) {
						targetNode = edges.edge().target();
					} else {
						targetNode = edges.edge().source();
					}

					// we should shift sequence in compliance with appropriate
					// sequence which has already been laid out.
					// If parentHyperNode is null then shifted node and the
					// sequence
					// that is being shifted are the parts of one component
					Node targetHiperNode = (Node) node2Hipernode
							.get(targetNode);
					if (parentHyperNode == null
							|| parentHyperNode.equals(targetHiperNode)) {
						LayoutTool
								.moveSubgraph(
										graph,
										nodeList.nodes(),
										graph.getCenterX(targetNode)
												- graph.getCenterX(sourceNode),
										isFlipped ? isPeptideBranchEdge(edgeInfo) ? getViewMetrics()
												.getShiftForFlippedPeptideSequence()
												: getViewMetrics()
														.getShiftForFlippedNucleotideSequence()
												: 0);
						return;
					}
				}
			}
		}
	}

	public void verticalShiftSubgraph(LayoutGraph graph, Node startingNode,
			Node end, boolean isFlipped) {

		NodeList nodeList = getSequenceNodes(startingNode, end, graph, true);
		boolean isPeptide = isPeptidePolymer(startingNode);
		double shiftStep = isPeptide ? getViewMetrics()
				.getShiftForFlippedPeptideSequence() : getViewMetrics()
				.getShiftForFlippedNucleotideSequence();

		LayoutTool.moveSubgraph(graph, nodeList.nodes(), 0,
				isFlipped ? shiftStep : -shiftStep);
	}

	private boolean isPeptideBranchEdge(EdgeInfo edgeInfo) {
		return (edgeInfo.getType().equals(EdgeType.BRANCH_BACKBONE) || edgeInfo
				.getType().equals(EdgeType.BRANCH_BRANCH));
	}

	protected Node[] getLoopBounds(NodeList baseList, LayoutGraph graph) {
		// determine the type of polymer
		DataProvider node2Hipernode = graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		Node hyperNode = (Node) node2Hipernode.get(baseList.firstNode());
		DataProvider hyperNodeMapPolymerType = hyperNode.getGraph()
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
		String polyType = (String) hyperNodeMapPolymerType.get(hyperNode);

		// the last base startingNode before the hairpin loop
		Node aBaseNode = null;
		// the other base startingNode that aBaseNode is pairing with
		Node pBaseNode = null;

		EdgeInfo edgeInfo = null;
		Map<Node, Integer> basePositionMap = new HashMap<Node, Integer>();

		// setup the base position map
		for (int i = 0; i < baseList.size(); i++) {
			basePositionMap.put((Node) baseList.get(i), i + 1);
		}

		DataProvider edgeMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

		// find the last base startingNode before the hairpin loop
		for (int i = 0; i < baseList.size(); i++) {
			Node node = (Node) baseList.get(i);
			for (EdgeCursor edgeCursor = node.edges(); edgeCursor.ok(); edgeCursor
					.next()) {
				edgeInfo = (EdgeInfo) edgeMap.get(edgeCursor.edge());
				if (edgeInfo.isPair()
						|| !edgeInfo.isRegular()
						&& polyType
								.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
					Node neighbour = null;
					if (node == edgeCursor.edge().source()) {
						neighbour = edgeCursor.edge().target();
					} else {
						neighbour = edgeCursor.edge().source();
					}

					if (basePositionMap.get(node) < basePositionMap
							.get(neighbour)) {
						pBaseNode = neighbour;
						aBaseNode = node;
					} else {
						break;
					}
				}
			}
		}

		Node[] toReturn = new Node[2];
		toReturn[0] = aBaseNode;
		toReturn[1] = pBaseNode;

		return toReturn;
	}

	public boolean isPeptidePolymer(Node node) {
		// determine the type of polymer
		DataProvider node2Hipernode = node.getGraph().getDataProvider(
				NodeMapKeys.NODE2PARENT_HYPERNODE);
		Node hyperNode = (Node) node2Hipernode.get(node);
		DataProvider hyperNodeMapPolymerType = hyperNode.getGraph()
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
		String polyType = (String) hyperNodeMapPolymerType.get(hyperNode);

		return polyType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE);
	}

	public boolean isNucleotidePolymer(Node node) {
		// determine the type of polymer
		DataProvider node2Hipernode = node.getGraph().getDataProvider(
				NodeMapKeys.NODE2PARENT_HYPERNODE);
		Node hyperNode = (Node) node2Hipernode.get(node);
		DataProvider hyperNodeMapPolymerType = hyperNode.getGraph()
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
		String polyType = (String) hyperNodeMapPolymerType.get(hyperNode);

		return polyType.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
	}

	public boolean isChemicalPolymer(Node node) {
		// determine the type of polymer
		DataProvider node2Hipernode = node.getGraph().getDataProvider(
				NodeMapKeys.NODE2PARENT_HYPERNODE);
		Node hyperNode = (Node) node2Hipernode.get(node);
		DataProvider hyperNodeMapPolymerType = hyperNode.getGraph()
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
		String polyType = (String) hyperNodeMapPolymerType.get(hyperNode);

		return polyType.equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE);
	}

	/**
	 * The method builds path for edges between chem nodes and backbone nodes if
	 * they are not neighbors in layout
	 * 
	 * @param graph
	 * @param e
	 */
	public void arrangeChemEdgePath(Graph2D graph, Edge e) {
		NodeRealizer sr = graph.getRealizer(e.source());
		NodeRealizer tr = graph.getRealizer(e.target());
		EdgeRealizer er = graph.getRealizer(e);

		// if s and t nodes are neighbours on the same horisontal line
		if (Math.abs(sr.getCenterY() - tr.getCenterY()) < EPS
				&& Math.abs(sr.getCenterX() - tr.getCenterX()) <= getViewMetrics()
						.getChemNodeToBackboneNodeDistance()) {
			return;
		}

		// if s and t nodes are neighbours on the same vertical line
		DataProvider edgeInfoDP = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
		AbstractEdgeInfo edgeInfo = (AbstractEdgeInfo) edgeInfoDP.get(e);
		boolean isBranchConnection = false;
		if (isChemicalPolymer(e.source())) {
			isBranchConnection = edgeInfo
					.getTargetNodeAttachment()
					.getLabel()
					.equalsIgnoreCase(
							Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
		} else {
			isBranchConnection = edgeInfo
					.getSourceNodeAttachment()
					.getLabel()
					.equalsIgnoreCase(
							Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
		}
		if (isBranchConnection
				&& Math.abs(sr.getCenterX() - tr.getCenterX()) < EPS
				&& Math.abs(sr.getCenterY() - tr.getCenterY()) <= getViewMetrics()
						.getShiftForFlippedPeptideSequence()) {
			return;
		}

		// //////////////////////////////////////
		// The case of complex edge path
		// /////////////////////////////////////

		// determines the side where edge path should be built
		boolean isSourceTheMostLeftNode = true;
		boolean isSourceStandAloneNode = true;
		double sx = graph.getRealizer(e.source()).getCenterX();
		double sy = graph.getRealizer(e.source()).getCenterY();
		for (NodeCursor cursor = e.source().neighbors(); cursor.ok(); cursor
				.next()) {
			double cnY = graph.getRealizer(cursor.node()).getCenterY();
			if (Math.abs(cnY - sy) < EPS)
				isSourceStandAloneNode = false;

			double cnX = graph.getRealizer(cursor.node()).getCenterX();
			if (!e.target().equals(cursor.node()) && cnX < sx) {
				isSourceTheMostLeftNode = false;
				break;
			}
		}

		boolean isTargetTheMostLeftNode = true;
		boolean isTargetStandAloneNode = true;
		double tx = graph.getRealizer(e.target()).getCenterX();
		double ty = graph.getRealizer(e.target()).getCenterY();
		for (NodeCursor cursor = e.target().neighbors(); cursor.ok(); cursor
				.next()) {
			double cnY = graph.getRealizer(cursor.node()).getCenterY();
			if (Math.abs(cnY - ty) < EPS)
				isTargetStandAloneNode = false;

			double cnX = graph.getRealizer(cursor.node()).getCenterX();
			if (!e.source().equals(cursor.node()) && cnX < tx) {
				isTargetTheMostLeftNode = false;
				break;
			}
		}

		// the case of stand alone chem mod connected via branch edge
		if (isTargetStandAloneNode) {
			isTargetTheMostLeftNode = isSourceTheMostLeftNode;
		}
		if (isSourceStandAloneNode) {
			isSourceTheMostLeftNode = isTargetTheMostLeftNode;
		}

		double upper = (sr.getCenterY() < tr.getCenterY()) ? sr.getCenterY()
				: tr.getCenterY();
		double mostLeft = (sr.getCenterX() < tr.getCenterX()) ? sr.getCenterX()
				: tr.getCenterX();
		double mostRight = (sr.getCenterX() > tr.getCenterX()) ? sr
				.getCenterX() : tr.getCenterX();
		if (isSourceTheMostLeftNode && isTargetTheMostLeftNode) {
			if (isBranchConnection) {
				er.addPoint(sr.getCenterX(), tr.getCenterY());
			} else {
				er.addPoint(mostLeft
						- getViewMetrics().getChemEdgePathHorisontalStep(),
						sr.getCenterY());
				er.addPoint(mostLeft
						- getViewMetrics().getChemEdgePathHorisontalStep(),
						tr.getCenterY());
			}
		} else if (!isSourceTheMostLeftNode && !isTargetTheMostLeftNode) {
			if (isBranchConnection) {
				er.addPoint(sr.getCenterX(), tr.getCenterY());
			} else {
				er.addPoint(mostRight
						+ getViewMetrics().getChemEdgePathHorisontalStep(),
						sr.getCenterY());
				er.addPoint(mostRight
						+ getViewMetrics().getChemEdgePathHorisontalStep(),
						tr.getCenterY());
			}
		} else if (isTargetTheMostLeftNode) {
			if (isBranchConnection) {
				er.addPoint(sr.getCenterX(), upper
						- getViewMetrics().getChemEdgePathVerticalStep());
			} else {
				er.addPoint(mostRight
						+ getViewMetrics().getChemEdgePathHorisontalStep(),
						sr.getCenterY());
				er.addPoint(mostRight
						+ getViewMetrics().getChemEdgePathHorisontalStep(),
						upper - getViewMetrics().getChemEdgePathVerticalStep());
			}

			er.addPoint(mostLeft
					- getViewMetrics().getChemEdgePathHorisontalStep(), upper
					- getViewMetrics().getChemEdgePathVerticalStep());
			er.addPoint(mostLeft
					- getViewMetrics().getChemEdgePathHorisontalStep(),
					tr.getCenterY());
		} else if (isSourceTheMostLeftNode) {
			if (isBranchConnection) {
				er.addPoint(sr.getCenterX(), upper
						- getViewMetrics().getChemEdgePathVerticalStep());
			} else {
				er.addPoint(mostLeft
						- getViewMetrics().getChemEdgePathHorisontalStep(),
						sr.getCenterY());
				er.addPoint(mostLeft
						- getViewMetrics().getChemEdgePathHorisontalStep(),
						upper - getViewMetrics().getChemEdgePathVerticalStep());
			}

			er.addPoint(mostRight
					+ getViewMetrics().getChemEdgePathHorisontalStep(), upper
					- getViewMetrics().getChemEdgePathVerticalStep());
			er.addPoint(mostRight
					+ getViewMetrics().getChemEdgePathHorisontalStep(),
					tr.getCenterY());
		}
	}

	// public abstract void add5label(Graph2D graph, Node startingNode, String
	// terminalLabel, boolean isComplimentary);
	// public void add5label(Graph2D graph) {
	// DataProvider labelInfoMap =
	// graph.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
	//
	// // 5 and n labels
	// for (Node node : graph.getNodeArray()) {
	// LabelInfo labelInfo = (LabelInfo)labelInfoMap.get(node);
	// String terminalLabel = labelInfo.getTerminalLabel();
	// if (terminalLabel != null &&
	// MonomerInfoUtils.isAnnotation(terminalLabel)) {
	// boolean isFlipped = labelInfo.isFlipped();
	// add5label(graph, node, terminalLabel, isFlipped);
	// }
	// }
	//
	//
	// NodeRealizer nr = graph.getRealizer(startingNode);
	// NodeLabel label = nr.createNodeLabel();
	//
	// label.setModel(NodeLabel.FREE);
	// // label.setModel(NodeLabel.EIGHT_POS);
	// double complMulX = 1;
	// double complMulY = 1;
	// if (isComplimentary) {
	// complMulX = -1.5;
	// complMulY = -1;
	//
	// // label.setPosition(NodeLabel.SE);
	// } else {
	// // label.setPosition(NodeLabel.NW);
	// }
	// label.setText(terminalLabel);
	//
	// int modStartLabelStep = 0;
	// // if (isSenceOrAntiSence(labelText) && isComplimentary==false){
	// // modStartLabelStep = layoutMetrics.getModifiedStartLabelStep();
	// // }
	//
	// label.setOffset(complMulX * ( layoutMetrics.getXStartLabelOffset() +
	// modStartLabelStep), complMulY* layoutMetrics.getYStartLabelOffset());
	//
	// label.setBackgroundColor(Color.yellow);
	//
	// label.setFontSize(layoutMetrics.getLabelFontSize());
	// // label.setBackgroundColor(sourcelabel.getBackgroundColor());
	//
	// nr.addLabel(label);
	// }
}
