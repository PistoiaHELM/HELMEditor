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

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.layout.LayoutGraph;

import org.helm.editor.data.AbstractEdgeInfo;
import org.helm.editor.data.ChemSequenceHolder;
import org.helm.editor.data.ChemSequenceHolderImpl;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.data.NodeSequence;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.layout.utils.AbstractSequenceLayouter;
import org.helm.editor.layout.utils.DockedSequenceLayouter;
import org.helm.editor.layout.utils.LayoutMetrics;
import org.helm.editor.layout.utils.LinearSequenceLayouter;
import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Attachment;

public class ChemModifiersLayout extends AbstratStructureLayout {
	private static final double EPS = 1e-100;

	protected Set<NodeSequence> sequences;
	protected ChemSequenceHolder holder;
	protected Set<Node> docks;
	protected LayoutGraph graph;
	protected Set<Node> layoutedNodes;

	@Override
	protected boolean canLayoutCore(LayoutGraph arg0) {
		return true;
	}

	@Override
	protected void doLayoutCore(LayoutGraph graph) {
	}
	
	public void doLayout(LayoutGraph graph) {
		init(graph);

		if (sequences.isEmpty()) {
			return;
		}

		if (docks != null && !docks.isEmpty()) {
			layoutDockedSequences();
		}

		layoutFloatingSequences();
	}
	
	private void init(LayoutGraph graph) {
		this.holder = getChemSequenceHolder(graph);
		this.graph = graph;
		this.sequences = holder.getSequences();

		this.docks = new HashSet<Node>();
		this.docks.addAll(holder.getDockedNodes());
		
		layoutedNodes = new HashSet<Node>();
		for (Node n : graph.getNodeArray()) {
			if (!MonomerInfoUtils.isChemicalModifierPolymer(n)) {
				layoutedNodes.add(n);
			}
		}
	}
	
    private ChemSequenceHolder getChemSequenceHolder(LayoutGraph graph) {
    	ChemSequenceHolderImpl holder = new ChemSequenceHolderImpl(graph);
    	for (Node node : graph.getNodeArray()) {
    		holder.pushNode(node);
    	}
    	return holder;
    }

	private boolean getDirection(Node node) {
		Set<Node> neighbours = GraphUtils.getNeighbours(node);
		double xCord = graph.getCenterX(node);
		for (Node neighbour : neighbours) {
			if (!layoutedNodes.contains(neighbour)) {
				continue;
			}
			if (graph.getCenterX(neighbour) > xCord) {
				return false;
			}
		}
		
		return true;
	}

	private void layoutDockedSequences() {
		// flip information provider
		DataProvider labelInfoDP = graph.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
		DataProvider edgeInfoDP = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
		
		LinkedList<Node> sortedDocks = new LinkedList<Node>();
		sortedDocks.addAll(docks);
		Collections.sort(sortedDocks, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;
				if (Math.abs(graph.getCenterY(o1) - graph.getCenterY(o2)) < EPS) {
					if (Math.abs(graph.getCenterX(o1) - graph.getCenterX(o2)) < EPS) {
						return 0;
					}
					return Double.compare(graph.getCenterX(o1), graph.getCenterX(o2));
				}
				return Double.compare(graph.getCenterY(o1), graph.getCenterY(o2));
			}
		});

		Point pMetrics = layoutPrimitives.getChemNodesDockedSequenceLayoutMetrics();
		LayoutMetrics metrics = new LayoutMetrics(pMetrics.x, pMetrics.y);
		while (!sortedDocks.isEmpty()) {
			Node dock = sortedDocks.poll();
			AbstractSequenceLayouter layouter = null;
			
			// dock point detection
			Node chemNode = holder.getConnectedSequences(dock).get(0).getNodes().get(0);
			Edge e = dock.getEdge(chemNode);
			AbstractEdgeInfo edgeInfo = (AbstractEdgeInfo)edgeInfoDP.get(e);
			boolean isBranchConnection = false;
			if (e.source().equals(dock)) {
				isBranchConnection = edgeInfo.getSourceNodeAttachment().getLabel().equalsIgnoreCase(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);	
			} else {
				isBranchConnection = edgeInfo.getTargetNodeAttachment().getLabel().equalsIgnoreCase(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
			}
			
			Point dockPoint = new Point((int) graph.getCenterX(dock), (int) graph.getCenterY(dock));
			if (isBranchConnection) {
				boolean isFlipped = ((LabelInfo)labelInfoDP.get(dock)).isFlipped();
				double shift = layoutPrimitives.getViewMetrics().getShiftForFlippedPeptideSequence();
				dockPoint.y += (isFlipped) ? shift : -shift; 

				layouter = new LinearSequenceLayouter(dockPoint.x, dockPoint.y);
			} else {
				layouter = new DockedSequenceLayouter(dockPoint, getDirection(dock));				
			}
			
			layouter.setMetrics(metrics);
			for (NodeSequence sequence : holder.getConnectedSequences(dock)) {
				if (layoutedNodes.contains(sequence.getStartNode())) {
					continue;
				}

				layouter.layout(graph, sequence);

				sequences.remove(sequence);
				layoutedNodes.addAll(sequence.getNodes());
			}
		}
	}

	private void layoutFloatingSequences() {

		if ((graph == null) || (graph.getNodeArray().length == 0)) {
			return;
		}

		int xCenter = 0;
		int yCenter = layoutPrimitives.getChemNodesYLayoutStart(graph, layoutedNodes);

		Point pMetrics = layoutPrimitives.getChemNodesFloatongSequenceLayoutMetrics();
		LayoutMetrics metrics = new LayoutMetrics(pMetrics.x, pMetrics.y);
		for (NodeSequence sequence : sequences) {
			if (layoutedNodes.contains(sequence.getStartNode())) {
				continue;
			}
			AbstractSequenceLayouter layouter = new LinearSequenceLayouter(xCenter, yCenter);
			layouter.setMetrics(metrics);
			layouter.layout(graph, sequence);
			layoutedNodes.addAll(sequence.getNodes());

			yCenter += layoutPrimitives.getViewMetrics().getVDistanceExt();
		}
	}
}
