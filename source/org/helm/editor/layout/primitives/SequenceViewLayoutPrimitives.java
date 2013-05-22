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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Set;

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.util.GraphHider;
import y.view.Arrow;
import y.view.Drawable;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.Port;

import org.helm.editor.componentPanel.LegendPanel;
import org.helm.editor.componentPanel.sequenceviewpanel.EdgeType;
import org.helm.editor.componentPanel.sequenceviewpanel.LabelConstructor;
import org.helm.editor.componentPanel.sequenceviewpanel.SViewEdgeInfo;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewModel;
import org.helm.editor.componentPanel.sequenceviewpanel.ViewMetrics;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.layout.metrics.IViewMetrics;
import org.helm.editor.layout.metrics.SequenceViewViewMetrics;
import org.helm.editor.layout.utils.LayoutUtils;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Attachment;

public class SequenceViewLayoutPrimitives extends AbstractLayoutPrimitives {
	private static final String SVIEW_ARROW_TYPE_T_SHORT = "SVIEW_T_SHORT";
	private static final Font SIMPLE_FONT = new Font("Helvetica", Font.PLAIN, 22);
	private static final Font ITALIC_FONT = new Font("Helvetica", Font.ITALIC, 22);	
	
	protected SequenceViewViewMetrics layoutMetrics = null;
	private LabelConstructor labelConstructor = null;

	public SequenceViewLayoutPrimitives(
			LabelConstructor labelConstructor,
			ViewMetrics baseViewMetrics) {
		this.layoutMetrics = new SequenceViewViewMetrics(baseViewMetrics);
		this.labelConstructor = labelConstructor;

		Arrow.addCustomArrow(SVIEW_ARROW_TYPE_T_SHORT, new Drawable() {
			public Rectangle getBounds() {
				return new Rectangle(1, 7);
			}
			public void paint(Graphics2D g) {
				
				Stroke s = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawLine(0, -3, 0, 3);
				g.setStroke(s);
			}
		});
	}

	public void layoutSequence(LayoutGraph graph, Node sequenceStart, Node sequenceEnd, boolean directOrder) {
		int horizontalIndex = 0;

		NodeList nodeList = getSequenceNodes(sequenceStart, sequenceEnd, graph, true);
		for (NodeCursor cursor = nodeList.nodes(); cursor.ok(); cursor.next()) {
			graph.setSize(cursor.node(), layoutMetrics.getNodeSize(), layoutMetrics.getNodeSize());
			graph.setCenter(cursor.node(), 
					horizontalIndex * layoutMetrics.getHDistanceExt() + layoutMetrics.getXStep(), 
					layoutMetrics.getYStep());
			horizontalIndex++;
		}
	}

	public void layoutLoop(LayoutGraph graph, Node loopStart, Node loopEnd, boolean layoutStartingNode) {
		NodeList nodeList = getSequenceNodes(loopStart, loopEnd, graph, true);
		// if (loopEnd == null) then it is a circle
		int count = nodeList.size();//(loopEnd == null) ? nodeList.size() - 1 : nodeList.size(); 
		double r = layoutMetrics.getVDistanceInt() * 0.5 / Math.sin(Math.PI / count);

		// if it is a circle then the starting node equals ending node
		if (loopEnd == null) {
			loopEnd = loopStart;
		}
		// rewrite later
		double distancePA = graph.getCenterY(loopEnd) - graph.getCenterY(loopStart);

		// root (r^2 - (distancePA /2)^2)
		double centerX = Math.signum(distancePA) * Math.pow((r * r - (distancePA * 0.5) * (distancePA * 0.5)), 0.5);
		if (centerX != Double.NaN) {
			centerX = graph.getCenterX(loopStart) + centerX;
		} else {
			centerX = graph.getCenterX(loopStart) + r;
		}

		// the center of the loop circle
		YPoint center = new YPoint(centerX, graph.getCenterY(loopStart) + (distancePA * 0.5));

		// remove the first and the last node in this list
		nodeList.popNode();
		if (loopEnd != loopStart)
			nodeList.remove(nodeList.size() - 1);
		// the number of nodes in between
		NodeCursor nodes = nodeList.nodes(); //nodeList.nodes();

		// the degree of loopStart relative to the horizontal line, counter
		// clockwise
		double asin = distancePA * 0.5 / r;
		if (Double.compare(asin, 1) > 0) {
			asin = 1;
		}

		double theta;
		if (distancePA > 0) 
			theta = Math.asin(asin);
		else if (distancePA < 0) 
			theta = Math.PI - Math.asin(asin);
		else
			theta = 0.;
		// the angle distance between two neighbor nodes
		double d_theta = (2 * Math.PI) / count;

		double current_theta = theta;

		// node position
		double x = 0;
		double y = 0;
		nodes.toFirst();

		if (layoutStartingNode) {
			x = center.x - r * Math.cos(current_theta);
			y = center.y - r * Math.sin(current_theta);
			graph.setCenter(loopStart, x, y);
		}

		for (; nodes.ok(); nodes.next()) {
			if (nodes.node().equals(loopStart) || nodes.node().equals(loopEnd)) {
				continue;
			}
			current_theta = current_theta + d_theta;

			// get the location of this node
			x = center.x - r * Math.cos(current_theta);
			y = center.y - r * Math.sin(current_theta);

			setFlipState(nodes.node(), Math.sin(current_theta) < 0);

			graph.setCenter(nodes.node(), x, y);
			graph.setSize(nodes.node(), layoutMetrics.getNodeSize(), layoutMetrics.getNodeSize());
		}
	}

	public void arrangeNodesVisualisationSettings(Graph2D graph) {
		DataProvider nodeMap = graph.getDataProvider(SequenceViewModel.MODIFICATION_COUNT);
		DataProvider labelInfoMap = graph.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);

		for (Node node : graph.getNodeArray()) {
			if (MonomerInfoUtils.isChemicalModifierPolymer(node)) {
				NodeRealizer realizer = graph.getRealizer(node);
				realizer.setTransparent(false);
				realizer.setFillColor(Color.WHITE);

				NodeLabel nl = realizer.getLabel(0);
				realizer.setSize(nl.getBox().getWidth()+10, nl.getBox().getHeight());
				nl.setFontSize(layoutMetrics.getChemNodeFontSize());
				nl.setFontStyle(Font.BOLD);
				nl.setModel(NodeLabel.INTERNAL);
				nl.setPosition(NodeLabel.CENTER);
				nl.setAlignment(NodeLabel.ALIGN_CENTER);
				nl.setTextColor(Color.BLACK);
			} else {
				NodeRealizer nodeRealizer = graph.getRealizer(node);
				nodeRealizer.setTransparent(true);
				nodeRealizer.setLineColor(Color.white);

				NodeLabel nlable = null;
				nlable = nodeRealizer.getLabel(0);
				nlable.setFontSize(layoutMetrics.getFontSize());
				nlable.setFontStyle(Font.BOLD);
				nlable.setModel(NodeLabel.INTERNAL);
				nlable.setPosition(NodeLabel.CENTER);
				nlable.setTextColor(Color.BLACK);

				if (!nlable.getText().contains("5'") && !nlable.getText().contains("P")) {
					switch (nodeMap.getInt(node)) {
					case 0:
						nlable.setFont(SIMPLE_FONT);				
						nlable.setTextColor(LegendPanel.NO_MODIFICATION_COLOR);
						break;
					case 1:
						nlable.setFont(ITALIC_FONT);
						nlable.setTextColor(LegendPanel.ONE_MODIFICATION_COLOR);
						break;
					case 2:
						nlable.setFont(ITALIC_FONT);				
						nlable.setTextColor(LegendPanel.TWO_MODIFICATION_COLOR);
						break;
					default:
						nlable.setFont(ITALIC_FONT);
						nlable.setTextColor(LegendPanel.NO_MODIFICATION_COLOR);
						break;
					}

					if (nlable.getText().equalsIgnoreCase("X")) {
						nlable.setFont(ITALIC_FONT);				
					}

					LabelInfo labelInfo = (LabelInfo)labelInfoMap.get(node);
					// node index
					if (nodeRealizer.labelCount() >= 2) {
						nlable = nodeRealizer.getLabel(1);
					} else {
						nlable = nodeRealizer.createNodeLabel();
						nodeRealizer.addLabel(nlable);
					}
					nlable.setTextColor(new Color(0, 100, 150));
					if (labelInfo.getPositionNumber() != 0) {
						nlable.setText("" + labelInfo.getPositionNumber());	
					} else {
						nlable.setText("");
					}
					double xOffset = layoutMetrics.getNumberLabelXOffset();
					int numSymb = nlable.getText().length();
					if (numSymb > 1){
						xOffset -= 3 * numSymb; 
					}
					boolean isFlipped = labelInfo.isFlipped();
					nlable.setOffset(xOffset, ((isFlipped) ? -1 : 1) * layoutMetrics.getNumberLabelYOffset());
				}
			}
		}

		// 5 and n labels		
		for (Node node : graph.getNodeArray()) {
			LabelInfo labelInfo = (LabelInfo)labelInfoMap.get(node);
			String terminalLabel = labelInfo.getTerminalLabel();
			if (terminalLabel != null && MonomerInfoUtils.isAnnotation(terminalLabel)) {
				boolean isFlipped = labelInfo.isFlipped() && !isPeptidePolymer(node); 
				add5label(graph, node, terminalLabel, isFlipped);
			}
		}
	}

	public void add5label(Graph2D graph, Node startingNode, String terminalLabel, boolean isComplimentary) {
		NodeRealizer nr = graph.getRealizer(startingNode);
		NodeLabel label = nr.createNodeLabel();

		label.setModel(NodeLabel.FREE);
		double complMulX = 1;
		double complMulY = 1;
		if (isComplimentary) {
			complMulX = -1.5;
			complMulY = -1;
		} 
		label.setText(terminalLabel);
		int modStartLabelStep = 0;
		if (!isComplimentary && isSenceOrAntiSence(startingNode, terminalLabel)){
			modStartLabelStep = layoutMetrics.getModifiedStartLabelStep();			
		}

		label.setOffset(complMulX * ( layoutMetrics.getXStartLabelOffset() + modStartLabelStep), complMulY* layoutMetrics.getYStartLabelOffset());
		label.setBackgroundColor(Color.yellow);
		label.setFontSize(layoutMetrics.getLabelFontSize());

		nr.addLabel(label);
	}

	private boolean isSenceOrAntiSence(Node startingNode, String text){
		return ((text.equals("5' ss") || text.equals("5' as")) 
				&& isNucleotidePolymer(startingNode))
		|| ((text.equals("n hc") || text.equals("n lc")) 
				&& isPeptidePolymer(startingNode));
	}
	
	public void arrangeEdgesVisualisationSettings(Graph2D graph) {
		GraphHider graphHider = new GraphHider(graph);
		EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

		// chem node edges preprocess (depends on the whole graph structure so there should not be hidden edges)
		for (EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
			EdgeType edgeType = ((SViewEdgeInfo) edgeMap.get(edges.edge())).getType();
			if (edgeType == EdgeType.CHEM) {
				arrangeChemEdgePath(graph, edges.edge());
			}
		}
		
		for (EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
			EdgeType edgeType = ((SViewEdgeInfo) edgeMap.get(edges.edge())).getType();
			Edge e = edges.edge();
			EdgeRealizer er = graph.getRealizer(e);

			// arrange edge arrows and cut size
			if (edgeType == EdgeType.PAIR) {
				er.setLineType(LineType.DOTTED_2);
				er.setLineColor(Color.BLUE);
				LayoutUtils.cutEdge(graph, e, 0, 0);
			} else if (edgeType == EdgeType.BRANCH_BACKBONE || 
					edgeType == EdgeType.BRANCH_BRANCH ||
					edgeType == EdgeType.CHEM) {
				double cutSourceSize = layoutMetrics.getChemEdgeOffset();
				double cutTargetSize = layoutMetrics.getChemEdgeOffset();
				
				DataProvider edgeInfoMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
				SViewEdgeInfo edgeInfo = (SViewEdgeInfo)edgeInfoMap.get(edges.edge());
				Attachment sourceAttachment = edgeInfo.getSourceNodeAttachment();
				Attachment targetAttachment = edgeInfo.getTargetNodeAttachment();
				if (!MonomerInfoUtils.isChemicalModifierPolymer(e.source()) &&
						sourceAttachment.getLabel().equalsIgnoreCase(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
					er.setSourceArrow(Arrow.getCustomArrow(SVIEW_ARROW_TYPE_T_SHORT));
					cutSourceSize = 0;
					if (graph.getCenterX(e.target()) == graph.getCenterX(e.source())) {
						cutTargetSize = 0;
					}
				} 
				if (!MonomerInfoUtils.isChemicalModifierPolymer(e.target()) &&
						targetAttachment.getLabel().equalsIgnoreCase(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
					er.setTargetArrow(Arrow.getCustomArrow(SVIEW_ARROW_TYPE_T_SHORT));
					cutTargetSize = 0;
					if (graph.getCenterX(e.target()) == graph.getCenterX(e.source())) {
						cutSourceSize = 0;
					}
				}
				
				LayoutUtils.cutEdge(graph, e, cutSourceSize, cutTargetSize);
			}

			// arrange edge view
			if (edgeType == EdgeType.REGULAR) {
				graphHider.hide(edges.edge());
			} else if (edgeType == EdgeType.MODIFIED_P) {
				renderModifiedEdge(e, graph);
			} 
		}

		// TODO
		if (!graph.isEmpty()) {
			labelConstructor.addLabelsToSequenceImproved(graph);
		}
		//labelConstructor.addLabelsToSequence(graph.firstNode(), graph.lastNode(), graph, false);
	}

	private void renderModifiedEdge(Edge edge, Graph2D graph) {
		Node source = edge.source();
		Node target = edge.target();

		NodeRealizer sourceRealizer = graph.getRealizer(source);
		NodeRealizer targetRealizer = graph.getRealizer(target);

		EdgeRealizer edgeRealizer = graph.getRealizer(edge);
		edgeRealizer.setLineColor(LegendPanel.MODIFIED_P_COLOR);
		edgeRealizer.setLineType(LineType.LINE_4);


		double dx = targetRealizer.getCenterX() - sourceRealizer.getCenterX();
		double dy = targetRealizer.getCenterY() - sourceRealizer.getCenterY();
		double alpha = LayoutUtils.calculateAngle(dx, dy);

		double yOffset = layoutMetrics.getXOffset() * Math.sin(alpha);
		double xOffset = layoutMetrics.getXOffset() * Math.cos(alpha);

		Port sourcePort = edgeRealizer.getSourcePort();
		sourcePort.setOffsets(xOffset, yOffset);

		Port targetPort = edgeRealizer.getTargetPort();
		targetPort.setOffsets(-xOffset, -yOffset);				
	}

	public Node[] getLoopBounds(Node startingNode, LayoutGraph graph) {
		NodeList nodeList = getSequenceNodes(startingNode, null, graph, true);
		return getLoopBounds(nodeList, graph);
	}

	@Override
	public Node getRNode(Node baseNode) {
		return baseNode;
	}

	@Override
	public IViewMetrics getViewMetrics() {
		return layoutMetrics;
	}



	//////////////////////////////////
	// Chem nodes layout primitives //
	//////////////////////////////////
	/**
	 * @return x = dX, y = dY
	 */
	 public Point getChemNodesFloatongSequenceLayoutMetrics() {
		return new Point(layoutMetrics.getHDistanceInt(), layoutMetrics.getVDistanceExt());
	}

	public Point getChemNodesDockedSequenceLayoutMetrics() {
		return new Point(layoutMetrics.getHDistanceInt(), layoutMetrics.getVDistanceExt());
	}

	public int getChemNodesYLayoutStart(LayoutGraph graph, Set<Node> layoutedNodes) {
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

		yBound += layoutMetrics.getVDistanceExt();

		return yBound;
	}
}
