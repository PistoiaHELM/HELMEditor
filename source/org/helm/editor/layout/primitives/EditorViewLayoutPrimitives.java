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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Set;

import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.view.Arrow;
import y.view.Drawable;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.layout.metrics.EditorViewViewMetrics;
import org.helm.editor.layout.metrics.IViewMetrics;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.NodeFactory;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.notation.model.Attachment;

public class EditorViewLayoutPrimitives extends AbstractLayoutPrimitives {
	private static final String EVIEW_ARROW_TYPE_T_SHORT = "EVIEW_T_SHORT";
	private EditorViewViewMetrics viewMetrics = null;

	public EditorViewLayoutPrimitives() {
		viewMetrics = new EditorViewViewMetrics();

		Arrow.addCustomArrow(EVIEW_ARROW_TYPE_T_SHORT, new Drawable() {
			public Rectangle getBounds() {
				return new Rectangle(3, 9);
			}

			public void paint(Graphics2D g) {

				Stroke s = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawLine(-1, -4, -1, 4);
				g.setStroke(s);
			}
		});
	}

	public void layoutLoop(LayoutGraph graph, Node loopStart, Node loopEnd,
			boolean layoutStartingNode) {
		NodeList loopNodesList = getSequenceNodes(loopStart, loopEnd, graph,
				true);

		// pop start and end nodes from the list
		loopNodesList.popNode();
		if (loopEnd == null) {
			// if it is a circle then the starting node equals ending node
			loopEnd = loopStart;
		} else {
			NodeCursor nodeCursor = loopEnd.successors();
			for (; nodeCursor.ok(); nodeCursor.next()) {
				loopNodesList.remove(nodeCursor.node());
			}
			loopNodesList.remove(loopEnd);
		}
		int count = 0;
		for (NodeCursor loopNodes = loopNodesList.nodes(); loopNodes.ok(); loopNodes
				.next()) {
			if (MonomerInfoUtils.isBackbone(loopNodes.node())) {
				count++;
			}
		}

		// Y distance between ra and rp;
		// double distancePA = rpRealizer.getCenterY() -
		// raRealizer.getCenterY();
		double distance = NodeFactory.distance * 2 + NodeFactory.size
				+ EditorViewViewMetrics.DISTANCE_H;
		double theta = 2 * Math.PI / (count + 4);
		double r = distance * 0.5 / Math.sin(theta * 1.5);
		r *= 1.1;

		double distancePA = graph.getCenterY(loopEnd)
				- graph.getCenterY(loopStart);

		// root (r^2 - (distancePA /2)^2)
		double centerX = Math.signum(distancePA)
				* Math.pow((r * r - (distancePA * 0.5) * (distancePA * 0.5)),
						0.5);
		if (centerX != Double.NaN) {
			centerX = graph.getCenterX(loopStart) + centerX;
		} else {
			centerX = graph.getCenterX(loopStart) + r;
		}

		// the center of the loop circle
		YPoint center = new YPoint(centerX, graph.getCenterY(loopStart)
				+ (distancePA * 0.5));

		// the degree of loopStart relative to the horizontal line, counter
		// clockwise
		double asin = distancePA * 0.5 / r;
		if (Double.compare(asin, 1) > 0) {
			asin = 1;
		}

		if (distancePA > 0) {
			theta = Math.asin(asin);
		} else if (distancePA < 0) {
			theta = Math.PI - Math.asin(asin);
		} else {
			theta = 0.;
		}
		// the angle distance between two neighbor nodes
		double d_theta = (2 * Math.PI - 2 * Math.abs(Math.asin(asin)))
				/ (count + 1);

		double current_theta = theta;

		// position for backbone node
		double x = 0;
		double y = 0;
		// position for base node
		double baseX = 0;
		double baseY = 0;
		// omit first branch node
		boolean isFirstBranch = true;
		for (NodeCursor loopNodes = loopNodesList.nodes(); loopNodes.ok(); loopNodes
				.next()) {
			if (MonomerInfoUtils.isBackbone(loopNodes.node())) {
				current_theta = current_theta + d_theta;

				// get the location of this node
				x = center.x - r * Math.cos(current_theta);
				y = center.y - r * Math.sin(current_theta);
				graph.setCenter(loopNodes.node(), x, y);

				if (MonomerInfoUtils.isRMonomer(loopNodes.node())) {
					baseX = center.x
							- (NodeFactory.distance + NodeFactory.size + r)
							* Math.cos(current_theta);
					baseY = center.y
							- (NodeFactory.distance + NodeFactory.size + r)
							* Math.sin(current_theta);
				}
			} else if (MonomerInfoUtils.isBranchMonomer(loopNodes.node())) {
				if (isFirstBranch) {
					isFirstBranch = false;
					continue;
				}
				graph.setCenter(loopNodes.node(), baseX, baseY);
			}
		}

		// for circular layout
		if (layoutStartingNode) {
			graph.setCenter(loopStart, center.x - r * Math.cos(theta), center.y
					- r * Math.sin(theta));
			NodeCursor successors = loopStart.successors();
			for (; successors.ok(); successors.next()) {
				setFlipState(successors.node(), Math.sin(current_theta) < 0);
				if (MonomerInfoUtils.isBranchMonomer(successors.node())) {
					baseX = center.x
							- (NodeFactory.distance + NodeFactory.size + r)
							* Math.cos(theta);
					baseY = center.y
							- (NodeFactory.distance + NodeFactory.size + r)
							* Math.sin(theta);
					graph.setCenter(successors.node(), baseX, baseY);
					break;
				}
			}
		}
	}

	public Node[] getLoopBounds(Node startingNode, LayoutGraph graph) {
		NodeList nodeList = null;

		if (MonomerInfoUtils.isPeptidePolymer(startingNode)) {
			nodeList = getSequenceNodes(startingNode, null, graph, true);
		} else {
			// get the base startingNode list of this sequence in order
			try {
				nodeList = SequenceGraphTools.getBaseList(startingNode, graph,
						false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return getLoopBounds(nodeList, graph);
	}

	/**
	 * given a base node, find its R node
	 * 
	 * @param baseNode
	 * @return node
	 */
	public Node getRNode(Node baseNode) {
		if (MonomerInfoUtils.isPeptidePolymer(baseNode)) {
			return baseNode;
		}

		DataProvider parentNodeMap = baseNode.getGraph().getDataProvider(
				NodeMapKeys.NODE2PARENT_HYPERNODE);

		NodeCursor predecessors = baseNode.predecessors();
		Node rNode = null;
		for (; predecessors.ok(); predecessors.next()) {
			if (MonomerInfoUtils.isRMonomer(predecessors.node())) {
				// double check if they belong to the same paprent hyper
				// startingNode
				if (parentNodeMap.get(predecessors.node()) == parentNodeMap
						.get(baseNode)) {
					// we got our R startingNode;
					rNode = predecessors.node();
					break;

				}
			}
		}
		return rNode;
	}

	@Override
	public void layoutSequence(LayoutGraph graph, Node sequenceStart,
			Node sequenceEnd, boolean directOrder) {
		NodeList nodeList = getSequenceNodes(sequenceStart, sequenceEnd, graph,
				true);
		int horizontalIndex = 0;
		int lastRNodeIndex = 0;

		for (NodeCursor nodeCursor = nodeList.nodes(); nodeCursor.ok(); nodeCursor
				.next()) {
			Node node = nodeCursor.node();
			if (MonomerInfoUtils.isBackbone(node)) {
				graph.setCenter(node, horizontalIndex
						* EditorViewViewMetrics.DISTANCE_H, 0);
				if (MonomerInfoUtils.isRMonomer(node))
					lastRNodeIndex = horizontalIndex;
				horizontalIndex++;
			} else if (MonomerInfoUtils.isBranchMonomer(node)) {
				graph.setCenter(node, lastRNodeIndex
						* EditorViewViewMetrics.DISTANCE_H,
						EditorViewViewMetrics.DISTANCE_V);
			}
		}
	}

	@Override
	public void arrangeEdgesVisualisationSettings(Graph2D graph) {
		DataProvider edgeInfoMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
		for (EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
			EdgeRealizer edgeRealizer = graph.getRealizer(edges.edge());

			if (MonomerInfoUtils.isChemicalModifierPolymer(edges.edge()
					.source())
					|| MonomerInfoUtils.isChemicalModifierPolymer(edges.edge()
							.target())) {
				arrangeChemEdgePath(graph, edges.edge());
			}

			if (MonomerInfoUtils.isPair(edges.edge())) {
				edgeRealizer.setLineType(LineType.DOTTED_3);
				edgeRealizer.setLineColor(Color.BLUE);
			} else {
				edgeRealizer.setLineType(LineType.LINE_1);
				edgeRealizer.setLineColor(Color.BLACK);

				EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeInfoMap
						.get(edges.edge());
				Attachment sourceAttachment = edgeInfo
						.getSourceNodeAttachment();
				Attachment targetAttachment = edgeInfo
						.getTargetNodeAttachment();

				if (MonomerInfoUtils.isPeptidePolymer(edges.edge().source())
						&& sourceAttachment.getLabel().equalsIgnoreCase(
								Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
					edgeRealizer.setSourceArrow(Arrow
							.getCustomArrow(EVIEW_ARROW_TYPE_T_SHORT));
				}
				if (MonomerInfoUtils.isPeptidePolymer(edges.edge().target())
						&& targetAttachment.getLabel().equalsIgnoreCase(
								Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
					edgeRealizer.setTargetArrow(Arrow
							.getCustomArrow(EVIEW_ARROW_TYPE_T_SHORT));
				}
			}
		}
	}

	@Override
	public void arrangeNodesVisualisationSettings(Graph2D graph) {
		DataProvider labelInfoMap = graph
				.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
		if (labelInfoMap == null) {
			return;
		}
		// 5 and n labels
		for (Node node : graph.getNodeArray()) {
			LabelInfo labelInfo = (LabelInfo) labelInfoMap.get(node);
			if (labelInfo == null)
				continue;
			String terminalLabel = labelInfo.getTerminalLabel();
			if (terminalLabel != null
					&& MonomerInfoUtils.isAnnotation(terminalLabel)) {
				boolean isFlipped = labelInfo.isFlipped()
						&& !isPeptidePolymer(node);
				add5label(graph, node, terminalLabel, isFlipped);
			}
		}
	}

	public void add5label(Graph2D graph, Node startingNode,
			String terminalLabel, boolean isComplimentary) {
		NodeRealizer nr = graph.getRealizer(startingNode);
		if (nr.labelCount() <= 1) {
			return;
		}

		NodeLabel label = nr.getLabel(1);

		label.setModel(NodeLabel.EIGHT_POS);
		label.setPosition(isComplimentary ? NodeLabel.SE : NodeLabel.NW);
		label.setBackgroundColor(Color.yellow);
	}

	// @Override
	// public void arrangeNodesVisualisationSettings(Graph2D graph) {
	// TODO: after Annotator removing here will be a mechanism for labeling
	// DataProvider labelInfoMap =
	// graph.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
	// if (labelInfoMap == null) {
	// return;
	// }
	//
	// // 5 and n labels
	// for (Node node : graph.getNodeArray()) {
	// LabelInfo labelInfo = (LabelInfo)labelInfoMap.get(node);
	// if (labelInfo == null)
	// continue;
	//
	// // add position number label
	// if (labelInfo.getPositionNumber() > 0) {
	// NodeRealizer nr = graph.getRealizer(node);
	// NodeLabel nl = nr.createNodeLabel();
	// nl.setModel(NodeLabel.EIGHT_POS);
	// if(isPeptidePolymer(node)) {
	// nl.setPosition(NodeLabel.N);
	// } else {
	// nl.setPosition(NodeLabel.NW);
	// }
	// nl.setTextColor(Color.DARK_GRAY);
	// nl.setText("" + labelInfo.getPositionNumber());
	// }
	//
	// // add terminal label
	// String terminalLabel = labelInfo.getTerminalLabel();
	// if (terminalLabel != null &&
	// MonomerInfoUtils.isAnnotation(terminalLabel)) {
	// boolean isFlipped = labelInfo.isFlipped() && !isPeptidePolymer(node);
	//
	// NodeRealizer nr = graph.getRealizer(node);
	// NodeLabel nl = nr.createNodeLabel();
	// nl.setModel(NodeLabel.EIGHT_POS);
	// nl.setPosition(isFlipped ? NodeLabel.SE : NodeLabel.NW);
	// nl.setText(terminalLabel);
	// nl.setBackgroundColor(Color.yellow);
	// }
	// }
	// }

	@Override
	public IViewMetrics getViewMetrics() {
		return viewMetrics;
	}

	// ////////////////////////////////
	// Chem nodes layout primitives //
	// ////////////////////////////////
	/**
	 * @return x = dX, y = dY
	 */
	public Point getChemNodesFloatongSequenceLayoutMetrics() {
		return new Point(viewMetrics.getHDistanceInt(),
				viewMetrics.getVDistanceExt());
	}

	public Point getChemNodesDockedSequenceLayoutMetrics() {
		return new Point(viewMetrics.getHDistanceInt(),
				viewMetrics.getVDistanceExt());
	}

	public int getChemNodesYLayoutStart(LayoutGraph graph,
			Set<Node> layoutedNodes) {
		Node lowest = null;
		for (Node node : layoutedNodes) {
			if (lowest == null) {
				lowest = node;
				continue;
			}
			if (graph.getCenterY(lowest) < graph.getCenterY(node)) {
				lowest = node;
				continue;
			}
		}

		int yBound = 0;
		if (lowest != null) {
			yBound = (int) graph.getCenterY(lowest);
		}

		yBound += viewMetrics.getVDistanceExt();

		return yBound;
	}
}
