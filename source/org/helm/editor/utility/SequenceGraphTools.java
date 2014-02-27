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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import y.algo.GraphConnectivity;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.util.GraphHider;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.utils.LayoutUtils;
import org.helm.editor.monomerui.SimpleElemetFactory;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.NucleotideSequenceParser;

/**
 * 
 * @author lih25
 */
public class SequenceGraphTools {

	public static final LineType INSERTION_LINE_TYPE = LineType.LINE_3;

	/**
	 * Minimal distance between two paired bases should be more than 4. But
	 * there are "P" nodes between "R" nodes and steps from bases to sugars
	 * should be taken into account so the distance should exceed (4R + 5P) +
	 * 2(base-R) = 11 nodes
	 */
	public static final int MINIMAL_DISTANCE_PAIRED_NODES = 11;

	/**
	 * Minimal distance between two backbones to be connected with each other.
	 * Should take into account that there could be pair edges between
	 * neighbours. So minimal distance is 4R + 4P + 2(R-base) + 1()base-base) =
	 * 11 nodes
	 */
	public static final int MINIMAL_NODES_DISTANCE = 10;

	/**
	 * Returns the combined monomer db, containing external (XHELM) monomers
	 * together with the internal monomers.
	 * 
	 * @return combined monomer db
	 */
	private static Map<String, Map<String, Monomer>> getMonomerDB() {
		return MonomerStoreCache.getInstance().getCombinedMonomerStore()
				.getMonomerDB();
	}

	/**
	 * Giving the starting backboneNode of a nuclitide sequence, return the base
	 * list in order, from 5' to 3'
	 * 
	 * @param startingNode
	 * @param graph
	 * @return a list of base node
	 */
	public static NodeList getBaseList(Node startingNode, Graph graph,
			boolean reverse) throws MonomerException, IOException,
			JDOMException {

		Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();

		NodeList nodeList = new NodeList();

		DataProvider monomerInfoMap = graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap
				.get(startingNode);
		Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
				monomerInfo.getMonomerID());
		Node pNode = null;
		Node rNode = null;

		NodeCursor successors = null;

		Set<Node> visited = new HashSet<Node>();
		Set<Node> bases = new HashSet<Node>();

		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return null;
		}

		if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
			successors = startingNode.successors();
			if (successors.ok()) {
				successors = successors.node().successors();
			} else {
				// empty base list
				return nodeList;
			}
		} else if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
			visited.add(startingNode);
			successors = startingNode.successors();
		} else if (monomer.getMonomerType().equalsIgnoreCase(
				Monomer.BRANCH_MOMONER_TYPE)) {
			nodeList.add(startingNode);
			return nodeList;
		}

		while (successors != null && successors.ok() && successors.size() > 0) {
			// successors.toFirst();
			pNode = null;
			for (; successors.ok(); successors.next()) {
				monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
						.node());
				if (monomerInfo.getPolymerType().equalsIgnoreCase(
						Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
					monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
							monomerInfo.getMonomerID());
					if (monomer != null
							&& monomer.getMonomerType().equalsIgnoreCase(
									Monomer.BRANCH_MOMONER_TYPE)) {
						if (bases.contains(successors.node())) {
							continue;
						}
						bases.add(successors.node());
						if (reverse) {
							nodeList.addFirst(successors.node());
						} else {
							nodeList.add(successors.node());
						}
					} else if (monomer != null
							&& monomer.getNaturalAnalog().equalsIgnoreCase(
									Monomer.ID_P)) {
						pNode = successors.node();
					}
				}
			}
			if (pNode != null) {
				successors = pNode.successors();
				if (successors.ok()) {
					monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
							.node());
					monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
							monomerInfo.getMonomerID());
					if (monomerInfo.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)
							&& monomer.getNaturalAnalog().equalsIgnoreCase(
									Monomer.ID_R)
							&& !visited.contains(successors.node())) {
						rNode = successors.node();
						visited.add(rNode);
						successors = rNode.successors();
					} else {
						break;
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return nodeList;

	}

	/**
	 * Given a peptide sequence starting backboneNode, return the whole sequence
	 * 
	 * @param startingNode
	 * @param graph
	 * @return a list of peptide sequence node
	 */
	public static NodeList getPeptideSequence(Node startingNode, Graph graph)
			throws MonomerException, IOException, JDOMException {

		GraphHider gh = new GraphHider(graph);
		for (Edge e : graph.getEdgeArray()) {
			if (MonomerInfoUtils.isPBranchEdge(e)) {
				gh.hide(e);
			}
		}

		// Map<String, Map<String, Monomer>> monomerDB =
		// MonomerFactory.getInstance().getMonomerDB();

		NodeList nodeList = new NodeList();

		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap
				.get(startingNode);

		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.PEPTIDE_POLYMER_TYPE)) {
			return null;
		}

		NodeCursor successors = startingNode.successors();
		nodeList.add(startingNode);
		Node currentNode = null;
		Set<Node> visited = new HashSet<Node>();
		visited.add(startingNode);
		while (successors.ok() && !visited.contains(successors.node())) {
			for (; successors.ok(); successors.next()) {
				monomerInfo = (MonomerInfo) monomerInfoMap.get(successors
						.node());
				if (monomerInfo.getPolymerType().equalsIgnoreCase(
						Monomer.PEPTIDE_POLYMER_TYPE)) {
					currentNode = successors.node();
					visited.add(currentNode);
					nodeList.add(currentNode);
					successors = currentNode.successors();
					break;
				}

			}

		}

		gh.unhideAll();

		return nodeList;

	}

	/**
	 * set up a graph copier for a given graph
	 * 
	 * @param graph
	 * @return a GraphCopier object
	 */
	public static GraphCopier getGraphCopier(Graph graph) {
		GraphCopier copier = new GraphCopier(graph.getGraphCopyFactory());
		copier.setDataProviderContentCopying(true);
		copier.setEdgeMapCopying(true);
		copier.setNodeMapCopying(true);

		return copier;
	}

	public static NodeList addHorizontalChain(NodeList sequence) {
		NodeList result = new NodeList(sequence);
		Node start = result.firstNode();
		Node end = result.lastNode();
		Graph2D graph = (Graph2D) start.getGraph();

		Node leftNeigbour = LayoutUtils.getHorizontalSuccessor(false, start,
				graph);
		while (leftNeigbour != null) {
			result.addFirst(leftNeigbour);
			leftNeigbour = LayoutUtils.getHorizontalSuccessor(false,
					leftNeigbour, graph);
		}

		Node rightNeigbour = LayoutUtils.getHorizontalSuccessor(true, end,
				graph);
		while (rightNeigbour != null) {
			result.addLast(rightNeigbour);
			rightNeigbour = LayoutUtils.getHorizontalSuccessor(true,
					rightNeigbour, graph);
		}
		return result;
	}

	/**
	 * remove the 3' phosphate monomer (if there is one) in a nucleotide
	 * sequence
	 * 
	 * @param startingNode
	 * @return true or false
	 */
	public static boolean removeLastPhosphate(Node startingNode)
			throws MonomerException, IOException, JDOMException {
		final Graph2D graph = (Graph2D) startingNode.getGraph();
		final NodeMap monomerNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		final Map<String, Monomer> rnaMonomerMap = getMonomerDB().get(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE);
		// final Map<String, Monomer> rnaMonomerMap =
		// MonomerFactory.getInstance()
		// .getMonomerDB().get(Monomer.NUCLIEC_ACID_POLYMER_TYPE);

		MonomerInfo monomerInfo = (MonomerInfo) monomerNodeMap
				.get(startingNode);
		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return false;
		}

		// NodeCursor successors = startingNode.successors();
		Node node = getTheLastBackboneNuclicacidNode(startingNode);
		Monomer monomer = null;
		if (node != null) {
			monomerInfo = (MonomerInfo) monomerNodeMap.get(node);

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				monomer = rnaMonomerMap.get(monomerInfo.getMonomerID());
				if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
					Edge edge = node.firstInEdge();
					EdgeMap edgeMap = (EdgeMap) graph
							.getDataProvider(EdgeMapKeys.EDGE_INFO);
					EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap
							.get(edge);
					NodeMap nodeMap = (NodeMap) graph
							.getDataProvider(NodeMapKeys.MONOMER_REF);

					MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap
							.get(edge.source());
					MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap
							.get(edge.target());

					sourceMonomerInfo.setConnection(
							edgeInfo.getSourceNodeAttachment(), false);
					targetMonomerInfo.setConnection(
							edgeInfo.getTargetNodeAttachment(), false);

					graph.removeNode(node);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * find the last backbone backboneNode in a nucleicacid sequence starting
	 * with the starting backboneNode
	 * 
	 * @param startingNode
	 * @return the last backbone nucleic acid node
	 */
	public static Node getTheLastBackboneNuclicacidNode(Node startingNode)
			throws MonomerException, IOException, JDOMException {
		final Graph2D graph = (Graph2D) startingNode.getGraph();
		final NodeMap monomerNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		final Map<String, Monomer> rnaMonomerMap = getMonomerDB().get(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE);
		// final Map<String, Monomer> rnaMonomerMap =
		// MonomerFactory.getInstance()
		// .getMonomerDB().get(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
		Node backboneNode = null;
		MonomerInfo monomerInfo = (MonomerInfo) monomerNodeMap
				.get(startingNode);

		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return null;
		} else {
			if (rnaMonomerMap.get(monomerInfo.getMonomerID()).getMonomerType()
					.equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE)) {
				backboneNode = startingNode;
			}
		}

		NodeCursor successors = startingNode.successors();
		Node node = null;

		// traverse the sequence and get the last backbone backboneNode
		while (successors != null && successors.ok() && successors.size() != 0) {
			node = successors.node();
			monomerInfo = (MonomerInfo) monomerNodeMap.get(node);

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				if (rnaMonomerMap.get(monomerInfo.getMonomerID())
						.getMonomerType()
						.equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE)) {
					backboneNode = node;
					successors = node.successors();
				} else {
					successors.next();
				}
			} else {
				break;
			}
		}
		return backboneNode;
	}

	public static boolean pairable(MonomerInfo sourceMonomerInfo,
			MonomerInfo targetMonomerInfo) throws MonomerException,
			IOException, JDOMException {
		Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		Monomer sourceNodeMonomer = monomerDB.get(
				sourceMonomerInfo.getPolymerType()).get(
				sourceMonomerInfo.getMonomerID());
		Monomer targetNodeMonomer = monomerDB.get(
				targetMonomerInfo.getPolymerType()).get(
				targetMonomerInfo.getMonomerID());

		return targetNodeMonomer.getNaturalAnalog() != null
				&& sourceNodeMonomer.getNaturalAnalog() != null
				&& NucleotideSequenceParser.complementMap.get(sourceNodeMonomer
						.getNaturalAnalog()) != null
				&& NucleotideSequenceParser.complementMap.get(
						sourceNodeMonomer.getNaturalAnalog()).contains(
						targetNodeMonomer.getNaturalAnalog());
	}

	// Check if the distance between two nodes is less than specified (4)
	public static boolean checkDistance(Node node1, Node node2, Graph graph,
			boolean ifPairEdgeAdded) {
		GraphHider graphHider = null;
		if (ifPairEdgeAdded) {
			// hide pair edges and find all sequences
			graphHider = new GraphHider(graph);
			for (Edge edge : graph.getEdgeArray()) {
				DataProvider edgeTypeMap = graph
						.getDataProvider(EdgeMapKeys.EDGE_INFO);
				if (((EdgeInfo) edgeTypeMap.get(edge)).isPair()) {
					graphHider.hide(edge);
				}
			}
		}

		NodeList neighbours = GraphConnectivity.getNeighbors(graph,
				new NodeList(node1),
				ifPairEdgeAdded ? MINIMAL_DISTANCE_PAIRED_NODES
						: MINIMAL_NODES_DISTANCE);

		if (ifPairEdgeAdded) {
			graphHider.unhideAll();
		}

		// if neighbours contains node2 then the distance is less than
		// acceptable
		return !neighbours.contains(node2);
	}

	/**
	 * appending a phosphate node at the end/begining of a sequence
	 * 
	 * @param node
	 *            : the last/first backbone R node
	 * @return a new node with phosphare
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public static Node appendPhosphate(Node node) throws MonomerException,
			IOException, JDOMException {

		Graph2D graph = (Graph2D) node.getGraph();
		GraphCopier copier = SequenceGraphTools.getGraphCopier(graph);

		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo oldNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(node);
		Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		Monomer monomer = monomerDB.get(oldNodeMonomerInfo.getPolymerType())
				.get(oldNodeMonomerInfo.getMonomerID());

		if (!monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
			return null;
		}

		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		// add the new phosphate
		NodeCursor newNodes = copier
				.copy(SimpleElemetFactory.getInstance().createMonomerNode(
						Monomer.NUCLIEC_ACID_POLYMER_TYPE, Monomer.ID_P), graph)
				.nodes();

		Node newNode = newNodes.node();
		if (newNode == null) {
			return null;
		}

		MonomerInfo newNodeMonomerInfo = (MonomerInfo) monomerInfoMap
				.get(newNode);
		NodeRealizer newNodeRealizer = graph.getRealizer(newNode);
		NodeRealizer oldNodeRealizer = graph.getRealizer(node);
		if (isLastNucleicacidBackbone(node)) {
			Attachment sourceAttachment = oldNodeMonomerInfo
					.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
			Attachment targetAttachment = newNodeMonomerInfo
					.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);

			if (sourceAttachment != null
					&& !oldNodeMonomerInfo.isConnected(sourceAttachment)) {
				Edge newEdge = graph.createEdge(node, newNode);
				oldNodeMonomerInfo.setConnection(sourceAttachment, true);
				newNodeMonomerInfo.setConnection(targetAttachment, true);
				edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment,
						targetAttachment));
			}
			newNodeRealizer.setCenter(oldNodeRealizer.getCenterX()
					+ NodeFactory.distance, oldNodeRealizer.getCenterY());

		} else if (isFirstNucleicacidBackbone(node)) {
			Attachment sourceAttachment = newNodeMonomerInfo
					.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
			Attachment targetAttachment = oldNodeMonomerInfo
					.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
			if (sourceAttachment != null
					&& !oldNodeMonomerInfo.isConnected(targetAttachment)) {
				Edge newEdge = graph.createEdge(newNode, node);
				oldNodeMonomerInfo.setConnection(targetAttachment, true);
				newNodeMonomerInfo.setConnection(sourceAttachment, true);
				edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment,
						targetAttachment));
			}
			newNodeRealizer.setCenter(oldNodeRealizer.getCenterX()
					- NodeFactory.distance, oldNodeRealizer.getCenterY());
		}

		return newNode;
	}

	public static boolean isLastNucleicacidBackbone(Node node)
			throws MonomerException, IOException, JDOMException {
		Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();

		Graph2D graph = (Graph2D) node.getGraph();

		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		if (monomerInfoMap == null) {
			return false;
		}

		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(node);
		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return false;
		}

		Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
				monomerInfo.getMonomerID());
		if (!monomer.getMonomerType().equalsIgnoreCase(
				Monomer.BACKBONE_MOMONER_TYPE)) {
			return false;
		}

		NodeCursor successors = node.successors();
		for (; successors.ok(); successors.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap.get(successors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BACKBONE_MOMONER_TYPE)) {
				return false;
			}

		}

		return true;
	}

	public static boolean isFirstNucleicacidBackbone(Node node)
			throws MonomerException, IOException, JDOMException {
		Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		Graph2D graph = (Graph2D) node.getGraph();
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(node);
		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return false;
		}

		Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
				monomerInfo.getMonomerID());
		if (!monomer.getMonomerType().equalsIgnoreCase(
				Monomer.BACKBONE_MOMONER_TYPE)) {
			return false;
		}

		NodeCursor predecessors = node.predecessors();
		for (; predecessors.ok(); predecessors.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap.get(predecessors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BACKBONE_MOMONER_TYPE)) {
				return false;
			}

		}
		return true;
	}

	/**
	 * test if the given node is the starting node of a peptide sequence
	 * 
	 * @param startingNode
	 * @param graph
	 * @return true or false
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public static boolean isFirstPeptideSequenceNode(Node startingNode,
			Graph graph) throws MonomerException, IOException, JDOMException {
		final Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// final Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();

		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap
				.get(startingNode);
		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.PEPTIDE_POLYMER_TYPE)) {
			return false;
		}

		Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
				monomerInfo.getMonomerID());
		if (!monomer.getMonomerType().equalsIgnoreCase(
				Monomer.BACKBONE_MOMONER_TYPE)) {
			return false;
		}

		NodeCursor predecessors = startingNode.predecessors();
		for (; predecessors.ok(); predecessors.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap.get(predecessors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.PEPTIDE_POLYMER_TYPE)
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BACKBONE_MOMONER_TYPE)) {
				return false;
			}

		}
		return true;
	}

	/**
	 * test if the given monomer is the last peptide sequence node
	 * 
	 * @param node
	 * @param graph
	 * @return true or false
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public static boolean isLastPeptideSequenceNode(Node node, Graph graph)
			throws MonomerException, IOException, JDOMException {
		final Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();

		// final Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(node);
		if (!monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.PEPTIDE_POLYMER_TYPE)) {
			return false;
		}

		Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
				monomerInfo.getMonomerID());
		if (!monomer.getMonomerType().equalsIgnoreCase(
				Monomer.BACKBONE_MOMONER_TYPE)) {
			return false;
		}

		NodeCursor successors = node.successors();
		for (; successors.ok(); successors.next()) {
			monomerInfo = (MonomerInfo) monomerInfoMap.get(successors.node());
			monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());

			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.PEPTIDE_POLYMER_TYPE)
					&& monomer.getMonomerType().equalsIgnoreCase(
							Monomer.BACKBONE_MOMONER_TYPE)) {
				return false;
			}

		}
		return true;
	}

	/**
	 * for the giving starting startingNode, return the whole sequence
	 * 
	 * @param startingNode
	 * @param graph
	 * @return a list of nucleic acid sequence node
	 */
	public static NodeList getNucleicAcidSequenceNodes(Node startingNode,
			Graph2D graph) throws MonomerException, IOException, JDOMException {

		final Map<String, Map<String, Monomer>> monomerDB = getMonomerDB();
		// final Map<String, Map<String, Monomer>> monomerDB = MonomerFactory
		// .getInstance().getMonomerDB();
		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		NodeList selectedList = new NodeList();
		NodeCursor successors = startingNode.successors();
		Node succNode = null;
		Node currentNode = null;

		MonomerInfo monomerInfo = null;
		Monomer monomer = null;
		successors.toFirst();
		boolean isEnd = false;

		selectedList.add(startingNode);
		Set<Node> visited = new HashSet<Node>();

		while (successors != null && successors.size() != 0) {
			currentNode = null;
			for (; successors.ok(); successors.next()) {
				succNode = successors.node();
				monomerInfo = (MonomerInfo) nodeMap.get(succNode);
				monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
						monomerInfo.getMonomerID());
				if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					selectedList.add(succNode);
					currentNode = succNode;
					if (monomer.getNaturalAnalog().equalsIgnoreCase(
							Monomer.ID_R))
						visited.add(currentNode);
				} else if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BRANCH_MOMONER_TYPE)) {
					selectedList.add(succNode);
				}
			}
			if (currentNode != null) {
				successors = currentNode.successors();
				if ((successors != null) && successors.ok()
						&& visited.contains(successors.node())) {
					break;
				}
			} else {
				break;
			}
		}
		return selectedList;

	}

	public static boolean isChemicalModifier(Node node)
			throws MonomerException, IOException, JDOMException {
		if (node == null)
			return false;

		Graph2D graph = (Graph2D) node.getGraph();
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(node);
		return monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.CHEMICAL_POLYMER_TYPE);
	}

}
