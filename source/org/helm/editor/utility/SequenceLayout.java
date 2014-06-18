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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import y.algo.GraphChecker;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.BufferedLayouter;
import y.layout.GraphLayout;
import y.layout.LayoutTool;
import y.layout.transformer.GraphTransformer;
import y.util.GraphHider;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LayoutMorpher;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NotationException;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.layout.StructuresLayoutModule;
import org.helm.editor.layout.primitives.AbstractLayoutPrimitives;
import org.helm.editor.layout.primitives.EditorViewLayoutPrimitives;
import org.helm.editor.layout.utils.DirectionFinder;
import org.helm.editor.layout.utils.LayoutUtils;
import org.helm.notation.model.Monomer;

/**
 * 
 * @author lih25
 */
public class SequenceLayout {

	// distance in vertical direction
	private final static int DISTANCE_V = 60;
	// distance in horizontal direction
	private final static int DISTANCE_H = 60;

	private GraphManager _graphManager;
	private Graph2DView _view;

	private DirectionFinder finder;

	public SequenceLayout(Graph2DView view, GraphManager graphManager) {
		_graphManager = graphManager;
		_view = view;
	}

	public void doLayout() throws MonomerException, IOException, JDOMException,
			NotationException {
		Graph2D graph = _view.getGraph2D();
		_graphManager.clearFlippedSet();

		clearEdgeBends(_view);

		AbstractLayoutPrimitives layoutPrimitives = new EditorViewLayoutPrimitives();

		StructuresLayoutModule layoutModule = new StructuresLayoutModule();
		layoutModule.setLayoutPrimitives(layoutPrimitives);
		layoutModule.start(_view.getGraph2D());

		layoutPrimitives.arrangeNodesVisualisationSettings(_view.getGraph2D());
		layoutPrimitives.arrangeEdgesVisualisationSettings(_view.getGraph2D());

	}

	private void testNucleotideFlip(Node startingNode) throws MonomerException,
			IOException, JDOMException {
		Graph2D graph = _view.getGraph2D();
		NodeList sequence = SequenceGraphTools.getNucleicAcidSequenceNodes(
				startingNode, graph);
		Node finalNode = MonomerInfoUtils.getFinalRNode(sequence.lastNode());

		Node startingNotNucleotide = MonomerInfoUtils
				.getNonNucleotideNeibor(startingNode);
		Node finalNotNucleotide = MonomerInfoUtils
				.getNonNucleotideNeibor(finalNode);

		// flip condition
		boolean needFlip = false;
		if (startingNotNucleotide != null) {
			needFlip = LayoutUtils.scalar(graph.getRealizer(startingNode),
					graph.getRealizer(finalNode),
					graph.getRealizer(startingNotNucleotide));
		} else if (finalNotNucleotide != null) {
			needFlip = !LayoutUtils.scalar(graph.getRealizer(startingNode),
					graph.getRealizer(finalNode),
					graph.getRealizer(finalNotNucleotide));
		}

		if (needFlip) {
			_graphManager.setFlipped(startingNode);
			_graphManager.setFlipped(finalNode);
			graph.setSelected(SequenceGraphTools.addHorizontalChain(sequence)
					.nodes(), true);
			MacromoleculeEditor.flipHorizontal(_view);
		}

		graph.unselectAll();
	}

	private void testPeptideFlip(Node startingNode) throws MonomerException,
			IOException, JDOMException {
		Graph2D graph = _view.getGraph2D();
		NodeList sequence = SequenceGraphTools.getPeptideSequence(startingNode,
				graph);
		Node finalNode = sequence.lastNode();

		// flip condition
		Node startingNonPeptide = MonomerInfoUtils
				.getNonPeptideNeibour(startingNode);
		Node finalNonPeptide = MonomerInfoUtils.getNonPeptideNeibour(finalNode);

		boolean needFlip = false;
		if (startingNonPeptide != null) {
			needFlip = LayoutUtils.scalar(graph.getRealizer(startingNode),
					graph.getRealizer(finalNode),
					graph.getRealizer(startingNonPeptide));
		} else if (finalNonPeptide != null) {
			needFlip = !LayoutUtils.scalar(graph.getRealizer(startingNode),
					graph.getRealizer(finalNode),
					graph.getRealizer(finalNonPeptide));
		}

		if (needFlip) {
			_graphManager.setFlipped(startingNode);
			_graphManager.setFlipped(finalNode);
			graph.setSelected(SequenceGraphTools.addHorizontalChain(sequence)
					.nodes(), true);
			MacromoleculeEditor.flipHorizontal(_view);
		}

		graph.unselectAll();
	}

	private void layoutChemNodes(GraphManager graphManager, Graph2D graph) {
		// ChemSequenceHolder holder = graphManager.getChemSequenceHolder();
		//
		// if (holder != null) {
		// finder = new DirectionFinderImpl(graphManager, graph);
		//
		// ChemSequencesLayouter layouter = new ChemSequencesLayouter(holder,
		// finder, graph);
		//
		// layouter.setupMetrics(new ViewMetrics(_view));
		//
		// for (Node n : graph.getNodeArray()) {
		// if (!MonomerInfoUtils.isChemicalModifierPolymer(n)) {
		// layouter.addLayotedNode(n);
		// }
		// }
		//
		// layouter.layout();
		// }
	}

	public DirectionFinder getDirectionFinder() {
		return finder;
	}

	/**
	 * test if a sequence has been fliped
	 * 
	 * @param startingNode
	 * @param graph
	 * @return
	 * @throws org.helm.notation.MonomerException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	private static boolean isNucleotideSequenceFliped(Node startingNode,
			Graph2D graph) throws MonomerException, MonomerException,
			IOException, JDOMException {
		NodeRealizer nr1 = graph.getRealizer(startingNode);
		NodeRealizer nr2 = null;
		NodeCursor succ = startingNode.successors();
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB =
		// MonomerFactory.getInstance().getMonomerDB();
		for (; succ.ok(); succ.next()) {
			MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(succ
					.node());
			Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());
			if (monomer != null
					&& monomer.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BACKBONE_MOMONER_TYPE)) {
				nr2 = graph.getRealizer(succ.node());
				break;
			}
		}

		if (nr2 != null) {
			if (nr2.getCenterX() > nr1.getCenterX()) {
				return false;
			} else {
				return true;
			}

		} else {
			return false;
		}

	}

	private void layoutPeptideSequence(Node startingNode, Graph2D graph,
			double centerX, double centerY, NodeMap nodeMap,
			Map<String, Map<String, Monomer>> monomerDB) {
		int horizontalIndex = 0;
		NodeRealizer nodeRealizer = graph.getRealizer(startingNode);
		nodeRealizer.setCenter(horizontalIndex * DISTANCE_H, centerY);
		horizontalIndex++;

		Node currentNode = startingNode;
		NodeCursor neighbors = currentNode.successors();
		Node succNode = null;
		MonomerInfo monomerInfo = null;
		Monomer monomer = null;

		while (neighbors.ok()) {
			for (; neighbors.ok(); neighbors.next()) {
				succNode = neighbors.node();
				monomerInfo = (MonomerInfo) nodeMap.get(succNode);
				nodeRealizer = graph.getRealizer(succNode);
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());

				if (monomer.getPolymerType().equalsIgnoreCase(
						Monomer.PEPTIDE_POLYMER_TYPE)) {
					nodeRealizer.setCenter(horizontalIndex * DISTANCE_H
							+ centerX, centerY);
					currentNode = succNode;
				}
			}

			if (currentNode != null) {
				horizontalIndex++;
				neighbors = currentNode.successors();
			} else {
				break;
			}

			succNode = null;
			currentNode = null;
		}

	}

	private static void layoutNucleotideSequence(Node startingNode,
			Graph2D graph, double centerY, NodeMap nodeMap,
			Map<String, Map<String, Monomer>> monomerDB) {
		int horizontalIndex = 0;
		NodeRealizer nodeRealizer = graph.getRealizer(startingNode);
		Monomer monomer = null;
		MonomerInfo monomerInfo = null;
		NodeCursor neighbors = null;
		Node currentNode = null;
		Node succNode = null;
		nodeRealizer.setCenter(horizontalIndex * DISTANCE_H, centerY);
		horizontalIndex++;

		neighbors = startingNode.successors();
		neighbors.toFirst();

		// takes care of the anotation label
		NodeLabel anotateLabel = null;
		if (nodeRealizer.labelCount() >= 2) {
			anotateLabel = nodeRealizer.getLabel(1);
		}

		if (anotateLabel != null) {
			anotateLabel.setModel(NodeLabel.EIGHT_POS);
			anotateLabel.setPosition(NodeLabel.NW);
		}

		Set<Node> visited = new HashSet<Node>();

		while (neighbors.size() != 0) {
			currentNode = null; // reset

			for (; neighbors.ok(); neighbors.next()) {
				// System.out.println("[SEQ LAYOUT] Succ Node: " + succNode);

				succNode = neighbors.node();
				monomerInfo = (MonomerInfo) nodeMap.get(succNode);
				nodeRealizer = graph.getRealizer(succNode);
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());
				if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					nodeRealizer.setCenter(horizontalIndex * DISTANCE_H,
							centerY);
					currentNode = succNode;
					if (monomer.getNaturalAnalog().equalsIgnoreCase(
							Monomer.ID_R))
						visited.add(currentNode);
					// System.out.println("[SEQ LAYOUT] Succ Node: " +
					// succNode);

				} else if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BRANCH_MOMONER_TYPE)) {
					nodeRealizer.setCenter((horizontalIndex - 1) * DISTANCE_H,
							centerY + DISTANCE_V);
				}

			}
			if (currentNode == null) {
				break;
			} else {
				horizontalIndex++;
				neighbors = currentNode.successors();
				if ((neighbors != null) && neighbors.ok()
						&& visited.contains(neighbors.node())) {
					break;
				}
			}

		}
	}

	/**
	 * given a base node, find its R node
	 * 
	 * @param baseNode
	 * @param monomerNodeMap
	 * @param parentNodeMap
	 * @param monomerDB
	 * @return
	 */
	private static Node getRNode(Node baseNode, NodeMap monomerNodeMap,
			NodeMap parentNodeMap, Map<String, Map<String, Monomer>> monomerDB) {
		NodeCursor predecessors = baseNode.predecessors();
		Node rNode = null;
		MonomerInfo monomerInfo = null;
		Monomer monomer = null;
		for (; predecessors.ok(); predecessors.next()) {
			monomerInfo = (MonomerInfo) monomerNodeMap.get(predecessors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());
			if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
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

	/**
	 * clear all bends of all edges
	 * 
	 * @param view
	 */
	private static void clearEdgeBends(Graph2DView view) {
		EdgeRealizer er = null;
		Graph2D graph = view.getGraph2D();
		EdgeCursor edges = graph.edges();
		for (; edges.ok(); edges.next()) {
			er = graph.getRealizer(edges.edge());
			er.clearBends();
		}

		return;
	}

	/**
	 * rotate the sequence starting with the starting startingNode 180 degree
	 * 
	 * @param view
	 * @param startingNode
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	private static void rotate180(Graph2DView view, Node startingNode)
			throws MonomerException, IOException, JDOMException {
		Graph2D graph = view.getGraph2D();

		// calculate the geometric info of the rotated graph
		GraphTransformer graphTransformer = new GraphTransformer();
		graphTransformer.setOperation(GraphTransformer.ROTATE);
		graphTransformer.setRotationAngle(180);

		GraphHider graphHider = new GraphHider(graph);
		preFlip(graphHider, graph, startingNode);

		GraphLayout layout = (new BufferedLayouter(graphTransformer))
				.calcLayout(graph);
		// apply the geometric info in an animated fashion
		LayoutMorpher morpher = new LayoutMorpher(view, layout);
		AnimationObject easedLM = AnimationFactory
				.createEasedAnimation(morpher);

		// fix the annotation
		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		NodeRealizer nr = null;
		MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(startingNode);
		if (monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			Monomer monomer = monomerDB.get(Monomer.NUCLIEC_ACID_POLYMER_TYPE)
					.get(monomerInfo.getMonomerID());
			if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
				nr = graph.getRealizer(startingNode);
				if (nr.labelCount() > 1) {
					NodeLabel anotateLabel = nr.getLabel(1);
					anotateLabel.setPosition(NodeLabel.SE);
				}

			} else if (monomer.getNaturalAnalog()
					.equalsIgnoreCase(Monomer.ID_P)) {
				NodeCursor successor = startingNode.successors();
				for (; successor.ok(); successor.next()) {
					monomerInfo = (MonomerInfo) nodeMap.get(successor.node());
					monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
							monomerInfo.getMonomerID());
					if (monomerInfo.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)
							&& monomer.getMonomerType().equalsIgnoreCase(
									Monomer.BACKBONE_MOMONER_TYPE)
							&& monomer.getNaturalAnalog().equalsIgnoreCase(
									Monomer.ID_R)) {
						nr = graph.getRealizer(successor.node());
						if (nr.labelCount() > 1) {
							NodeLabel anotateLabel = nr.getLabel(1);
							anotateLabel.setPosition(NodeLabel.SE);
						}

						break;
					}

				}
			}
		}
		new AnimationPlayer().animate(easedLM);
		graph.unselectAll();
		postFlip(graphHider);
	}

	/**
	 * select the corresponding nodes and edges
	 * 
	 * @param graphHider
	 * @param graph
	 * @param startingNode
	 */
	private static void preFlip(GraphHider graphHider, Graph2D graph,
			Node startingNode) throws MonomerException, IOException,
			JDOMException {
		EdgeCursor edges = null;
		graph.unselectAll();
		graph.setSelected(
				SequenceGraphTools.getNucleicAcidSequenceNodes(startingNode,
						graph).nodes(), true);
		NodeCursor nodes = graph.nodes();
		for (; nodes.ok(); nodes.next()) {
			if (!graph.isSelected(nodes.node())) {
				edges = nodes.node().edges();
				for (; edges.ok(); edges.next()) {
					// if both the source and target startingNode is not
					// selected, hide it
					if (!graph.isSelected(edges.edge().source())
							&& !graph.isSelected(edges.edge().target())) {
						graphHider.hide(edges.edge());
					}

				}
				graphHider.hide(nodes.node());
			}

		}
	}

	private static void postFlip(GraphHider graphHider) {
		graphHider.unhideAll();
	}

	/**
	 * a helper class that could shift a DNA/RNA sequence to make the pairing
	 * layout nicely
	 * 
	 * @param view
	 *            : the display of the current graph
	 * @param startingNode
	 *            : all node after startingNode (include the startingNode) will
	 *            be shifted
	 * @param yshift
	 *            : the y direction shift distance in pixel
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 * @throws java.io.IOException
	 */
	private static void shiftSubgraph(Graph2DView view, Node startingNode,
			int yshift) throws MonomerException, JDOMException, IOException {

		Graph2D graph = view.getGraph2D();
		NodeList nodeList = SequenceGraphTools.getNucleicAcidSequenceNodes(
				startingNode, graph);
		NodeCursor sequenceNodes = nodeList.nodes();
		MonomerInfo monomerInfo = null;
		Monomer monomer = null;
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		EdgeCursor edges = null;
		EditorEdgeInfoData edgeInfo = null;

		for (; sequenceNodes.ok(); sequenceNodes.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap
					.get(sequenceNodes.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			// for starting startingNode 5', the monomer is null (5')
			if (monomer != null
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BRANCH_MOMONER_TYPE)) {
				edges = sequenceNodes.node().edges();
				for (; edges.ok(); edges.next()) {
					edgeInfo = (EditorEdgeInfoData) edgeMap.get(edges.edge());
					if (edgeInfo.isPair()) {
						NodeRealizer sourceNodeRealizer = graph
								.getRealizer(sequenceNodes.node());
						NodeRealizer targetNodeRealizer = null;
						if (sequenceNodes.node() != edges.edge().target()) {
							targetNodeRealizer = graph.getRealizer(edges.edge()
									.target());
						} else {
							targetNodeRealizer = graph.getRealizer(edges.edge()
									.source());
						}
						// if the source startingNode and target startingNode is
						// not on the same vertical line

						if (sourceNodeRealizer.getCenterX() != targetNodeRealizer
								.getCenterX()) {
							LayoutTool.moveSubgraph(graph, nodeList.nodes(),
									targetNodeRealizer.getCenterX()
											- sourceNodeRealizer.getCenterX(),
									yshift);
							break;

						}

					}
				}
			}
		}

	}

	private static double getRadius(Node raNode, Node rpNode,
			NodeMap monomerInfoMap, Map<String, Map<String, Monomer>> monomerDB) {

		NodeCursor successors = raNode.successors();
		MonomerInfo monomerInfo = null;
		Monomer monomer = null;
		int count = 0;
		Node nextNode = null;
		// int nodeDistance = NodeFactory.distance;

		while (successors != null && successors.ok()) {
			for (; successors.ok(); successors.next()) {
				monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
						.node());
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());
				if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					nextNode = successors.node();
					break;

				}

			}
			if (nextNode != rpNode) {
				successors = nextNode.successors();
				count++;

			} else {
				break;
			}

		}

		// Y distance between ra and rp;
		// double distancePA = rpRealizer.getCenterY() -
		// raRealizer.getCenterY();
		double distancePA = NodeFactory.distance * 2 + NodeFactory.size
				+ DISTANCE_H;

		double theta = 2 * Math.PI / (count + 4);

		double r = distancePA * 0.5 / Math.sin(theta * 1.5);

		return r * 1.1;
	}

	/**
	 * put nodes between raNode and rpNode (exclusively) on a circle
	 * 
	 * @param raNode
	 * @param rpNode
	 * @param monomerInfoMap
	 * @param monomerDB
	 */
	private static void layoutCycle(double r, Node rNode,
			NodeMap monomerInfoMap, Map<String, Map<String, Monomer>> monomerDB) {

		layoutLoop(r, rNode, rNode, monomerInfoMap, monomerDB);

		// layout acid
		NodeCursor successors = rNode.successors();
		Graph2D graph = (Graph2D) rNode.getGraph();
		NodeRealizer nodeRealizer1 = graph.getRealizer(rNode);
		MonomerInfo monomerInfo;
		Monomer monomer = null;

		for (; successors.ok(); successors.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap.get(successors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			NodeRealizer nodeRealizer2 = graph.getRealizer(successors.node());
			if (monomer.getMonomerType().equalsIgnoreCase(
					Monomer.BRANCH_MOMONER_TYPE)) {
				double baseX = nodeRealizer1.getCenterX()
						- (NodeFactory.distance + NodeFactory.size);
				double baseY = nodeRealizer1.getCenterY();
				nodeRealizer2.setCenter(baseX, baseY);
				break;
			}
		}
	}

	/**
	 * put nodes between raNode and rpNode (exclusively) on a circle
	 * 
	 * @param raNode
	 * @param rpNode
	 * @param monomerInfoMap
	 * @param monomerDB
	 */
	private static void layoutLoop(double r, Node raNode, Node rpNode,
			NodeMap monomerInfoMap, Map<String, Map<String, Monomer>> monomerDB) {

		// Node preNode = raNode;
		NodeCursor successors = raNode.successors();
		MonomerInfo monomerInfo;

		Monomer monomer = null;
		Graph2D graph = (Graph2D) raNode.getGraph();
		NodeRealizer nodeRealizer1 = graph.getRealizer(raNode);
		NodeRealizer nodeRealizer2 = graph.getRealizer(rpNode);
		double distancePA = nodeRealizer2.getCenterY()
				- nodeRealizer1.getCenterY();

		// root (r^2 - (distancePA /2)^2)
		double centerX = Math.pow((r * r - (distancePA * 0.5)
				* (distancePA * 0.5)), 0.5);
		if (centerX != Double.NaN) {
			centerX = nodeRealizer1.getCenterX() + centerX;
		} else {
			centerX = nodeRealizer1.getCenterX() + r;
		}

		// the center of the loop circle
		YPoint center = new YPoint(centerX, nodeRealizer1.getCenterY()
				+ (distancePA * 0.5));

		Node nextNode = null;

		// the number of nodes in between
		int count = 0;

		while (successors != null && successors.ok()) {
			for (; successors.ok(); successors.next()) {
				monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
						.node());
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());
				if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					nextNode = successors.node();
					break;

				}

			}
			if (nextNode != rpNode) {
				successors = nextNode.successors();
				count++;

			} else {
				break;
			}

		}

		// the degree of raNode relative to the horizontal line, counter
		// clockwise
		double asin = distancePA * 0.5 / r;
		if (Double.compare(asin, 1) > 0) {
			asin = 1;
		}

		double theta = Math.asin(asin);
		// the angle distance between two neighbor nodes
		double d_theta = (2 * Math.PI - (2 * theta)) / (count + 1);

		double current_theta = theta;

		// position for backbone node
		double x = 0;
		double y = 0;
		successors = raNode.successors();
		// position for base node
		double baseX = 0;
		double baseY = 0;
		count = 0;
		while (successors.ok()) {
			for (; successors.ok(); successors.next()) {
				monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
						.node());
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());
				nodeRealizer2 = graph.getRealizer(successors.node());
				if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					nextNode = successors.node();
					current_theta = current_theta + d_theta;

					// get the location of this node
					x = center.x - r * Math.cos(current_theta);
					y = center.y - r * Math.sin(current_theta);
					nodeRealizer2.setCenter(x, y);

					if (monomer.getNaturalAnalog().equalsIgnoreCase(
							Monomer.ID_R)) {
						baseX = center.x
								- (NodeFactory.distance + NodeFactory.size + r)
								* Math.cos(current_theta);
						baseY = center.y
								- (NodeFactory.distance + NodeFactory.size + r)
								* Math.sin(current_theta);
					}

				} else if (count > 0
						&& monomer.getMonomerType().equalsIgnoreCase(
								Monomer.BRANCH_MOMONER_TYPE)) {
					nodeRealizer2.setCenter(baseX, baseY);
				}

			}
			if (nextNode != rpNode) {
				successors = nextNode.successors();
				count++;

			} else {
				break;
			}
		}
	}

}
