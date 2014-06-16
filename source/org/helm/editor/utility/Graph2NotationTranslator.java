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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.util.GraphHider;
import y.view.Graph2D;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationConstant;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.notationcompositor.NotationCompositor;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.MonomerParser;

/**
 * functions for translate from a graph to its string notation
 * 
 * @author lih25
 */
public class Graph2NotationTranslator {

	public static final String STRUCTURE_ELEMENT = "STRUCTURE";
	public static final String NOTATION_SOURCE_ELEMENT = "NOTATION_SOURCE";
	public static final String TEXT_NOTATION_ELEMENT = "TEXT_NOTATION";
	public static final String CANONICAL_SMILES_ELEMENT = "CANONICAL_SMILES";
	public static final String CANONICAL_NOTATION_ELEMENT = "CANONICAL_NOTATION";
	public static final String STRUCTURE_XML_ELEMENT = "STRUCTURE_XML";
	public static final String NEW_MONOMERS_ELEMENT = "NEW_MONOMERS";
	private static NotationCompositor _notationCompositor = new NotationCompositor();

	/**
	 * update the hyper graph according to current graph
	 * 
	 * @param graph
	 * @param graphManager
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public static void updateHyperGraph(final Graph graph,
			final GraphManager graphManager) throws NotationException,
			MonomerException, IOException, JDOMException {

		// initializeIOHandler the new hyper graph
		Graph hyperGraph = graphManager.getHyperGraph();
		hyperGraph.clear();

		NodeMap hyperNodeNameMap = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE_NAME);

		NodeMap hyperNodeAnotationMap = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE_ANOTATION);

		NodeMap hyperNodeMapPolymerType = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
		NodeMap hyperNodeMapPolymerName = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION);

		hyperGraph.removeDataProvider(NodeMapKeys.EXSMIELS);
		hyperGraph.addDataProvider(NodeMapKeys.EXSMIELS,
				hyperGraph.createNodeMap());
		NodeMap smilesMaps = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.EXSMIELS);

		// go through the graph and generate a new hyper graph
		final List<Node> startingList = graphManager.getStartingNodeList();
		final NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		// a Map that has the position information for each monomer
		NodeMap positionNodeMap = graph.createNodeMap();
		graph.addDataProvider(NodeMapKeys.POSITION, positionNodeMap);

		// a map that is <Node,hyper node> pair
		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		if (parentNodeMap == null) {
			parentNodeMap = graph.createNodeMap();
			graph.addDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE,
					parentNodeMap);
		}

		MonomerInfo monomerInfo = null;

		int rnaCount = 0;
		int peptideCount = 0;
		int chemCount = 0;
		int monomerCount = 0;

		NodeCursor successors = null;

		Node rNode = null;
		Node pNode = null;
		Node baseNode = null;
		Edge edge = null;
		Monomer monomer = null;

		Set<Edge> polyEdgeSet = new HashSet<Edge>();

		// hide branch nodes
		EdgeCursor alledges = graph.edges();
		EdgeMap edgeInfo = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);

		GraphHider gh = new GraphHider(graph);

		for (; alledges.ok(); alledges.next()) {
			Edge e = alledges.edge();
			if (MonomerInfoUtils.isPBranchEdge(e)) {
				gh.hide(e);
				polyEdgeSet.add(e);
			}
		}

		Node hyperNode = null;

		NodeMap hyper2starting = clearNodeMap(hyperGraph,
				NodeMapKeys.HYPERNODE2STARTING_NODE);
		clearNodeMap(graph, NodeMapKeys.NODE2PAIR_NODE);
		clearNodeMap(graph, NodeMapKeys.NODE2STARTING_NODE);
		NodeMap indexMap = clearNodeMap(hyperGraph, NodeMapKeys.HYPERNODE2INDEX);

		int hyperNodeIndex = 0;
		// hyper node index corresponds to the order at which starting nodes
		// were added to the gm's starting node list.
		// this is in turn corresponds to the order of polymer list in the
		// notation
		for (Node startingNode : startingList) {
			// for each starting node, we create a hyper node
			hyperNodeIndex++;
			hyperNode = hyperGraph.createNode();
			hyper2starting.set(hyperNode, startingNode);
			indexMap.setInt(hyperNode, hyperNodeIndex);

			monomerInfo = (MonomerInfo) nodeMap.get(startingNode);
			monomer = GraphUtils.getMonomerDB()
					.get(monomerInfo.getPolymerType())
					.get(monomerInfo.getMonomerID());

			StringBuilder code = new StringBuilder();
			StringBuilder codeWithSmiles = new StringBuilder();
			boolean containsSmiles = false;
			monomerCount = 0;
			String polyType = monomerInfo.getPolymerType();
			String anotation = graphManager.getAnnotation(startingNode);

			hyperNodeAnotationMap.set(hyperNode, anotation);

			hyperNodeMapPolymerType.set(hyperNode, polyType);

			if (polyType.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				Set<Node> visitedRNA = new HashSet<Node>();

				// boolean containsSmiles = false;

				if (MonomerInfoUtils.isPMonomer(startingNode)) {
					visitedRNA.add(startingNode);
				}

				// build an RNA String
				rnaCount++;
				// walk through this RNA sequence and get the notation

				if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
					monomerCount = 1;
					rNode = startingNode;
				} else if (monomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_P)) {
					monomerCount = 1;
					pNode = startingNode;
					positionNodeMap.setInt(startingNode, monomerCount++);

					monomerInfo = (MonomerInfo) nodeMap.get(pNode);

					String monomerString = getMonomerString(monomerInfo);

					code.append(monomerString);

					if (monomer.isAdHocMonomer()) {
						containsSmiles = true;
						codeWithSmiles.append("[" + monomer.getCanSMILES()
								+ "]");
					} else {
						codeWithSmiles.append(monomerString);
					}

					parentNodeMap.set(pNode, hyperNode);
					successors = pNode.successors();
					for (; successors.ok(); successors.next()) {
						monomerInfo = (MonomerInfo) nodeMap.get(successors
								.node());
						monomer = GraphUtils.getMonomerDB()
								.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());
						if (monomerInfo.getPolymerType().equalsIgnoreCase(
								Monomer.NUCLIEC_ACID_POLYMER_TYPE)
								&& monomer.getNaturalAnalog().equalsIgnoreCase(
										Monomer.ID_R)) {
							rNode = successors.node();
							// append seperator if there is a next unit
							code.append(".");
							codeWithSmiles.append(".");
							break;
						}
					}
				} else if (monomer.getMonomerType().equalsIgnoreCase(
						Monomer.BRANCH_MOMONER_TYPE)) {
					monomerCount = 1;
					parentNodeMap.set(startingNode, hyperNode);
					positionNodeMap.setInt(startingNode, monomerCount++);
					if (monomer.isAdHocMonomer()) {
						containsSmiles = true;
						codeWithSmiles.append("([" + monomer.getCanSMILES()
								+ "])");
					} else {
						codeWithSmiles.append("("
								+ getMonomerString(monomerInfo) + ")");
					}
					code.append("(");
					code.append(getMonomerString(monomerInfo));
					code.append(")");
				}

				while ((rNode != null) && !visitedRNA.contains(rNode)) {
					parentNodeMap.set(rNode, hyperNode);
					successors = rNode.successors();
					// reset base node and p node
					baseNode = null;
					pNode = null;
					visitedRNA.add(rNode);

					for (; successors.ok(); successors.next()) {
						if (visitedRNA.contains(successors.node())) {
							pNode = null;
							continue;
						}
						monomerInfo = (MonomerInfo) nodeMap.get(successors
								.node());
						monomer = GraphUtils.getMonomerDB()
								.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());

						if (monomer.getMonomerType().equalsIgnoreCase(
								Monomer.BACKBONE_MOMONER_TYPE)
								&& monomer.getNaturalAnalog().equalsIgnoreCase(
										Monomer.ID_P)) {
							// this should be a p node
							pNode = successors.node();
							parentNodeMap.set(pNode, hyperNode);
							visitedRNA.add(pNode);

						} else if (monomer.getMonomerType().equalsIgnoreCase(
								Monomer.BRANCH_MOMONER_TYPE)) {// base node
							baseNode = successors.node();
							parentNodeMap.set(baseNode, hyperNode);
							if (baseNode.successors().size() > 0) {
								// if there are inter polymer edges connect
								// between base and other polymer
								NodeCursor baseSucc = baseNode.successors();
								for (; baseSucc.ok(); baseSucc.next()) {
									edge = baseNode.getEdgeTo(baseSucc.node());
									polyEdgeSet.add(edge);
								}
							}

						} else { // this is an inter polymer edge between R and
									// some other polymer
							edge = rNode.getEdgeTo(successors.node());
							polyEdgeSet.add(edge);
						}
					}
					// r node
					monomerInfo = (MonomerInfo) nodeMap.get(rNode);
					monomer = GraphUtils.getMonomerDB()
							.get(monomerInfo.getPolymerType())
							.get(monomerInfo.getMonomerID());

					String monomerString = getMonomerString(monomerInfo);
					code.append(monomerString);

					if (monomer.isAdHocMonomer()) {
						containsSmiles = true;
						codeWithSmiles.append("[" + monomer.getCanSMILES()
								+ "]");
					} else {
						codeWithSmiles.append(monomerString);
					}

					positionNodeMap.set(rNode, monomerCount++);
					if (baseNode != null) {
						positionNodeMap.setInt(baseNode, monomerCount++);

						monomerInfo = (MonomerInfo) nodeMap.get(baseNode);
						monomer = GraphUtils.getMonomerDB()
								.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());

						code.append("(");
						code.append(getMonomerString(monomerInfo));
						code.append(")");
						if (monomer.isAdHocMonomer()) {
							containsSmiles = true;
							codeWithSmiles.append("([" + monomer.getCanSMILES()
									+ "])");
						} else {
							codeWithSmiles.append("("
									+ getMonomerString(monomerInfo) + ")");
						}
					}
					if (pNode != null) {
						positionNodeMap.setInt(pNode, monomerCount++);
						parentNodeMap.set(pNode, hyperNode);

						monomerInfo = (MonomerInfo) nodeMap.get(pNode);
						monomer = GraphUtils.getMonomerDB()
								.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());

						monomerString = getMonomerString(monomerInfo);
						code.append(monomerString);
						if (monomer.isAdHocMonomer()) {
							containsSmiles = true;
							codeWithSmiles.append("[" + monomer.getCanSMILES()
									+ "]");
						} else {
							codeWithSmiles.append(monomerString);
						}

						successors = pNode.successors();
						if (successors.ok()) {
							if (successors.size() > 1) {
								throw new NotationException(
										"Phosphate can not have more than one connections");
							} else {
								monomerInfo = (MonomerInfo) nodeMap
										.get(successors.node());
								monomer = GraphUtils.getMonomerDB()
										.get(monomerInfo.getPolymerType())
										.get(monomerInfo.getMonomerID());

								if (monomerInfo
										.getPolymerType()
										.equalsIgnoreCase(
												Monomer.NUCLIEC_ACID_POLYMER_TYPE)
										&& monomer.getNaturalAnalog()
												.equalsIgnoreCase(Monomer.ID_R)) {
									rNode = successors.node();
								} else {
									edge = pNode.getEdge(successors.node());
									// setUpHyperEdge(edge, graph, hyperGraph);
									polyEdgeSet.add(edge);
									rNode = null;
								}
							}
						} else {
							rNode = null;
						}
					} else {
						rNode = null;
					}
					code.append(".");
					codeWithSmiles.append(".");
				}

				if (startingNode.inDegree() > 0) {
					polyEdgeSet.add(startingNode.inEdges().edge());
				}

				if (code.lastIndexOf(".") != -1) {
					code.deleteCharAt(code.lastIndexOf("."));
				}

				if (codeWithSmiles.lastIndexOf(".") != -1) {
					codeWithSmiles
							.deleteCharAt(codeWithSmiles.lastIndexOf("."));
				}
				hyperNodeNameMap.set(hyperNode,
						Monomer.NUCLIEC_ACID_POLYMER_TYPE + rnaCount);

			} else if (polyType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
				peptideCount++;
				monomerCount = 1;
				positionNodeMap.setInt(startingNode, monomerCount++);
				parentNodeMap.set(startingNode, hyperNode);

				// StringBuilder codeWithSmiles = new StringBuilder();

				if (monomer.isAdHocMonomer()) {
					containsSmiles = true;
					codeWithSmiles.append("[" + monomer.getCanSMILES() + "]");
				} else {
					codeWithSmiles.append(getMonomerString(monomerInfo));
				}

				code.append(getMonomerString(monomerInfo));

				successors = startingNode.successors();
				Set<Node> visitedPeptides = new HashSet<Node>();
				visitedPeptides.add(startingNode);

				while (successors != null && successors.ok()) {
					Node currentNode = successors.node();
					monomerInfo = (MonomerInfo) nodeMap.get(currentNode);
					monomer = GraphUtils.getMonomerDB()
							.get(monomerInfo.getPolymerType())
							.get(monomerInfo.getMonomerID());
					if (MonomerInfoUtils.isPeptidePolymer(currentNode)) {

						// cycle
						if (visitedPeptides.contains(currentNode)) {
							polyEdgeSet.add(currentNode.inEdges().edge());
							break;
						}

						visitedPeptides.add(currentNode);

						positionNodeMap.setInt(currentNode, monomerCount++);
						parentNodeMap.set(currentNode, hyperNode);

						monomerInfo = (MonomerInfo) nodeMap.get(currentNode);
						code.append(".");
						code.append(getMonomerString(monomerInfo));

						codeWithSmiles.append(".");
						if (monomer.isAdHocMonomer()) {
							containsSmiles = true;
							codeWithSmiles.append("[" + monomer.getCanSMILES()
									+ "]");
						} else {
							codeWithSmiles
									.append(getMonomerString(monomerInfo));
						}

						successors = currentNode.successors();

						successors.toFirst();
					} else {
						// there is only one edge
						edge = currentNode.edges().edge();
						polyEdgeSet.add(edge);
						successors.next();
					}

				}
				// SM: ad hoc monomers should be exported as inline smiles code
				if (containsSmiles) {
					smilesMaps.set(hyperNode, codeWithSmiles.toString());
				}
				hyperNodeNameMap.set(hyperNode, Monomer.PEPTIDE_POLYMER_TYPE
						+ peptideCount);
				hyperNodeMapPolymerType.set(hyperNode,
						Monomer.PEPTIDE_POLYMER_TYPE);
				hyperNodeMapPolymerName.set(hyperNode, code.toString());

			} else { // chemical structure
				chemCount++;
				monomerCount = 0;
				code.append(monomerInfo.getMonomerID());
				parentNodeMap.set(startingNode, hyperNode);
				positionNodeMap.setInt(startingNode, 1);

				if (monomer.isAdHocMonomer()) {
					smilesMaps.set(hyperNode, monomer.getCanSMILES());
				}

				// TY
				// String c = code.toString();
				// if (c != null && c.length() > 3 && c.startsWith("AM#"))
				// smilesMaps.set(hyperNode, monomer.getCanSMILES());

				hyperNodeNameMap.set(hyperNode, Monomer.CHEMICAL_POLYMER_TYPE
						+ chemCount);
				hyperNodeMapPolymerType.set(hyperNode,
						Monomer.CHEMICAL_POLYMER_TYPE);
				hyperNodeMapPolymerName.set(hyperNode, code.toString());

				EdgeCursor edges = startingNode.edges();
				// setUpHyperEdge(edge, graph, hyperGraph);
				for (; edges.ok(); edges.next()) {
					polyEdgeSet.add(edges.edge());
				}
			}
			hyperNodeMapPolymerName.set(hyperNode, code.toString());
			// SM: ad hoc monomers should be exported as inline smiles code
			if (containsSmiles) {
				smilesMaps.set(hyperNode, codeWithSmiles.toString());
			}

		}

		// unhide branch edges
		gh.unhideAll();

		// set up hyperEdges
		Iterator<Edge> polyEdgeIter = polyEdgeSet.iterator();
		while (polyEdgeIter.hasNext()) {
			edge = polyEdgeIter.next();
			setUpHyperEdge(edge, graph, hyperGraph);
			updatePairMap(edge, graph, hyperGraph);
		}

		updateNodeStartingNodeMap(graph, hyperGraph);
	}

	private static NodeMap clearNodeMap(final Graph graph, Object key) {
		NodeMap nodeMap = (NodeMap) graph.getDataProvider(key);

		if (nodeMap != null) {
			graph.removeDataProvider(key);
		}
		nodeMap = graph.createNodeMap();
		graph.addDataProvider(key, nodeMap);
		return nodeMap;
	}

	private static void updateNodeStartingNodeMap(Graph graph, Graph hyperGraph) {
		NodeMap node2starting = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		NodeMap hyper2starting = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE2STARTING_NODE);

		for (Node n : graph.getNodeArray()) {
			Node hyperNode = (Node) parentNodeMap.get(n);
			Node starting = (Node) hyper2starting.get(hyperNode);
			node2starting.set(n, starting);
		}
	}

	private static void updatePairMap(Edge edge, Graph graph, Graph hyperGraph) {
		NodeMap pairMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PAIR_NODE);
		EdgeMap edgeInfo = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		NodeMap hyper2starting = (NodeMap) hyperGraph
				.getDataProvider(NodeMapKeys.HYPERNODE2STARTING_NODE);

		EditorEdgeInfoData info = (EditorEdgeInfoData) edgeInfo.get(edge);
		if (!info.isPair()) {
			return;
		}

		Node hyperSourceNode = (Node) parentNodeMap.get(edge.source());
		Node hyperTargetNode = (Node) parentNodeMap.get(edge.target());

		Node startingSource = (Node) hyper2starting.get(hyperSourceNode);
		Node startingTarget = (Node) hyper2starting.get(hyperTargetNode);

		pairMap.set(startingSource, startingTarget);
		pairMap.set(startingTarget, startingSource);
	}

	private static void setUpHyperEdge(Edge edge, Graph graph, Graph hyperGraph) {

		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		NodeMap positionNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.POSITION);

		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		EdgeMap hyperEdgeMap = (EdgeMap) hyperGraph
				.getDataProvider(EdgeMapKeys.DESCRIPTION);

		Node hyperSourceNode = (Node) parentNodeMap.get(edge.source());
		Node hyperTargetNode = (Node) parentNodeMap.get(edge.target());

		Edge hyperEdge = hyperSourceNode.getEdge(hyperTargetNode);
		String hyperEdgeInfo = null;
		if (hyperEdge != null) {
			hyperEdgeInfo = (String) hyperEdgeMap.get(hyperEdge);
		}

		EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap.get(edge);
		if (hyperEdge == null
				|| (hyperEdge != null && !hyperEdgeInfo
						.equalsIgnoreCase(edgeInfo.toString()))) {
			hyperEdge = hyperGraph.createEdge(hyperSourceNode, hyperTargetNode);

			// set up the edge description
			StringBuilder edgeDesc = new StringBuilder();

			edgeInfo = (EditorEdgeInfoData) edgeMap.get(edge);

			// source polymer position
			edgeDesc.append(positionNodeMap.getInt(edge.source()));
			edgeDesc.append(":");
			// source monomerInfo attachment
			edgeDesc.append(edgeInfo.getSourceNodeAttachment().getLabel());

			edgeDesc.append("-");
			edgeDesc.append(positionNodeMap.get(edge.target()));
			edgeDesc.append(":");
			edgeDesc.append(edgeInfo.getTargetNodeAttachment().getLabel());

			hyperEdgeMap.set(hyperEdge, edgeDesc.toString());
		}

	}

	/**
	 * get the extended notation
	 * 
	 * @param graphManager
	 * @return notation string
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 * @throws java.io.IOException
	 * @throws org.helm.notation.NotationException
	 * @throws ClassNotFoundException
	 */
	public static String getNewNotation(GraphManager graphManager)
			throws MonomerException, JDOMException, IOException,
			NotationException, ClassNotFoundException {

		return _notationCompositor.getExtendedNotation(graphManager);
	}

	private static String getMonomerString(final MonomerInfo monomerInfo) {
		StringBuilder result = new StringBuilder();
		String name = monomerInfo.getMonomerID();
		if (name.length() > 1) {
			result.append("[");
			result.append(name);
			result.append("]");
		} else {
			result.append(name);
		}

		return result.toString();
	}

	public static int compareNodeList(final NodeList nodeList1,
			final NodeList nodeList2) throws ListNotComparableException {
		int order = 0;
		if (nodeList1.size() != nodeList2.size()) {
			throw new ListNotComparableException(
					"Two list must be the same length");
		}

		NodeComparator nodeComparator = new NodeComparator(null);

		Node[] nodeArray1 = nodeList1.toNodeArray();
		Node[] nodeArray2 = nodeList2.toNodeArray();
		for (int i = 0; i < nodeList1.size(); i++) {
			order = nodeComparator.compare(nodeArray1[i], nodeArray2[i]);
			if (order != 0) {
				nodeArray1 = null;
				nodeArray2 = null;
				return order;
			}
		}
		nodeArray1 = null;
		nodeArray2 = null;
		return order;

	}

	public static Element getStructureInfoElement(Graph2D graph,
			GraphManager graphManager) throws NotationException,
			MonomerException, IOException, JDOMException, StructureException,
			ListNotComparableException, ClassNotFoundException {
		if (null == graph || graph.isEmpty()) {
			return null;
		}

		// String textNotation = graph2Notation(graph, graphManager);
		String textNotation = getNewNotation(graphManager);
		if (null != textNotation || textNotation.length() > 0) {
			Element structureElement = new Element(STRUCTURE_ELEMENT);

			Element notationSourceElement = new Element(NOTATION_SOURCE_ELEMENT);
			notationSourceElement.setText(NotationConstant.NOTATION_SOURCE);
			structureElement.getChildren().add(notationSourceElement);

			Element textNotationElement = new Element(TEXT_NOTATION_ELEMENT);
			textNotationElement.setText(textNotation);
			structureElement.getChildren().add(textNotationElement);

			boolean containsGenericStruc = containsGenericStructure(graph);
			if (containsGenericStruc) {
				// String canonicalNotation =
				// getCanonicalNotation(textNotation);
				String canonicalNotation = ComplexNotationParser
						.getCanonicalNotation(textNotation);

				Element canNotationElement = new Element(
						CANONICAL_NOTATION_ELEMENT);
				canNotationElement.setText(canonicalNotation);
				structureElement.getChildren().add(canNotationElement);
			} else {
				String canonicalSmiles = ComplexNotationParser
						.getComplexPolymerSMILES(textNotation);
				Element canSmilesElement = new Element(CANONICAL_SMILES_ELEMENT);
				canSmilesElement.setText(canonicalSmiles);
				structureElement.getChildren().add(canSmilesElement);
			}

			String structureXML = MacromoleculeEditor.getGraphXML(graph);
			Element structureXMLElement = new Element(STRUCTURE_XML_ELEMENT);

			CDATA structureXMLcdata = new CDATA(structureXML);
			structureXMLElement.setContent(structureXMLcdata);
			// structureXMLElement.setText(encodedStructureXML);
			structureElement.getChildren().add(structureXMLElement);

			Element newMonomers = getNewMonomersElement(graph);
			if (null != newMonomers) {
				structureElement.getChildren().add(newMonomers);
			}

			return structureElement;
		} else {
			return null;
		}
	}

	public static String getStructureInfoXML(Graph2D graph,
			GraphManager graphManager) throws NotationException,
			MonomerException, IOException, JDOMException, StructureException,
			ListNotComparableException, ClassNotFoundException {
		Element element = getStructureInfoElement(graph, graphManager);
		if (null != element && element.getChildren().size() > 0) {
			XMLOutputter outputter = new XMLOutputter();
			return outputter.outputString(element);
		} else {
			return null;
		}
	}

	public static boolean containsGenericStructure(Graph2D graph)
			throws MonomerException, IOException, JDOMException {

		final NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		final Node[] nodes = graph.getNodeArray();
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			final MonomerInfo mi = (MonomerInfo) nodeMap.get(node);
			Monomer m = GraphUtils.getMonomerDB().get(mi.getPolymerType())
					.get(mi.getMonomerID());
			if (null != m
					&& m.getPolymerType().equals(Monomer.CHEMICAL_POLYMER_TYPE)
					&& m.getCanSMILES() == null) {
				return true;
			}
		}
		return false;
	}

	public static List<Monomer> getNewMonomers(Graph2D graph)
			throws MonomerException, IOException, JDOMException {
		List<Monomer> monomers = new ArrayList<Monomer>();
		final NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		final Node[] nodes = graph.getNodeArray();
		Monomer m = null;
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			final MonomerInfo mi = (MonomerInfo) nodeMap.get(node);
			m = GraphUtils.getMonomerDB().get(mi.getPolymerType())
					.get(mi.getMonomerID());
			if (m.isNewMonomer()) {
				monomers.add(m);
			}
		}
		return monomers;
	}

	public static Element getNewMonomersElement(Graph2D graph)
			throws MonomerException, IOException, JDOMException {
		List<Monomer> monomers = getNewMonomers(graph);
		if (null != monomers && monomers.size() > 0) {
			Element rootElement = new Element(NEW_MONOMERS_ELEMENT);
			for (int i = 0; i < monomers.size(); i++) {
				Monomer m = monomers.get(i);
				Element el = MonomerParser.getMonomerElement(m);
				rootElement.getChildren().add(el);

			}
			return rootElement;
		} else {
			return null;
		}
	}

	public static String getNewMonomersXML(Graph2D graph)
			throws MonomerException, IOException, JDOMException {
		Element element = getNewMonomersElement(graph);

		if (null != element && element.getChildren().size() > 0) {
			XMLOutputter outputter = new XMLOutputter();
			return outputter.outputString(element);
		} else {
			return null;
		}
	}

	public static int compareEdgeList(EdgeList edgeList1, EdgeList edgeList2)
			throws ListNotComparableException {
		int order = 0;
		if (edgeList1.size() != edgeList2.size()) {
			throw new ListNotComparableException(
					"Two list must be the same length");
		}

		EdgeComparator edgeComparator = new EdgeComparator();

		Edge[] edgeArray1 = edgeList1.toEdgeArray();
		Edge[] edgeArray2 = edgeList2.toEdgeArray();

		for (int i = 0; i < edgeArray1.length; i++) {
			order = edgeComparator.compare(edgeArray1[i], edgeArray2[i]);
			if (order != 0) {
				edgeArray1 = null;
				edgeArray2 = null;
				return order;
			}
		}
		edgeArray1 = null;
		edgeArray2 = null;
		return order;

	}

	// private static String produceNotation(ArrayList<NodeEdgeListPair>
	// notationList, Graph graph) {
	// NodeMap nodeNameMap = (NodeMap)
	// graph.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION);
	// NodeMap nodeTypeMap = (NodeMap)
	// graph.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
	// EdgeMap edgeDescMap = (EdgeMap)
	// graph.getDataProvider(EdgeMapKeys.DESCRIPTION);
	//
	// HashMap<Node, String> nodeIDMap = new HashMap<Node, String>();
	//
	// StringBuilder notationNodeBuilder = new StringBuilder();
	// StringBuilder notationEdgeBuilder = new StringBuilder();
	// StringBuilder notationBuilder = new StringBuilder();
	//
	//
	// int nucleitideID = 1;
	// int pepID = 1;
	// int chemID = 1;
	//
	// NodeList nodeList = null;
	// EdgeList edgeList = null;
	//
	// Node node = null;
	// Edge edge = null;
	// String nodeType = null;
	// String nodeName = null;
	// Collections.sort(notationList, new NodeEdgeListComparator());
	//
	// for (int i = 0; i < notationList.size(); i++) {
	// nodeList = notationList.get(i).getNodeList();
	// edgeList = notationList.get(i).getEdgeList();
	//
	// while (!nodeList.isEmpty()) {
	// node = nodeList.popNode();
	// nodeType = (String) nodeTypeMap.get(node);
	// if (nodeType.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
	// nodeIDMap.put(node, nodeType + nucleitideID++);
	// } else if (nodeType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
	// nodeIDMap.put(node, nodeType + pepID++);
	// } else {
	// nodeIDMap.put(node, nodeType + chemID++);
	// }
	//
	// if (notationNodeBuilder.length() > 0) {
	// notationNodeBuilder.append("|");
	// }
	// notationNodeBuilder.append(nodeIDMap.get(node));
	// notationNodeBuilder.append("{");
	// notationNodeBuilder.append(nodeNameMap.get(node));
	// notationNodeBuilder.append("}");
	// }
	//
	// while (!edgeList.isEmpty()) {
	// edge = edgeList.popEdge();
	// if (notationEdgeBuilder.length() > 0) {
	// notationEdgeBuilder.append("|");
	// }
	// //source node id
	// notationEdgeBuilder.append(nodeIDMap.get(edge.source()));
	// notationEdgeBuilder.append(",");
	// //target node id
	// notationEdgeBuilder.append(nodeIDMap.get(edge.target()));
	// notationEdgeBuilder.append(",");
	//
	// //description
	// notationEdgeBuilder.append(edgeDescMap.get(edge));
	// }
	// }
	//
	// if (notationNodeBuilder.length() > 0) {
	// notationBuilder.append(notationNodeBuilder);
	// }
	// if (notationEdgeBuilder.length() > 0) {
	// notationBuilder.append("$");
	// notationBuilder.append(notationEdgeBuilder);
	// }
	//
	// return notationBuilder.toString();
	//
	// }
	//
}

/**
 * 
 * @author LIH25
 */
class NodeEdgeListPair {

	private NodeList nodeList = null;
	private EdgeList edgeList = null;

	public NodeEdgeListPair(NodeList nodeList, EdgeList edgeList) {
		this.nodeList = new NodeList();
		this.nodeList.addAll(nodeList);
		this.edgeList = new EdgeList();
		this.edgeList.addAll(edgeList);
	}

	public EdgeList getEdgeList() {
		return edgeList;
	}

	public NodeList getNodeList() {
		return nodeList;
	}

	public int getSize() {
		return (nodeList.size() + edgeList.size());
	}
}

class NodeEdgeListComparator implements Comparator<NodeEdgeListPair> {

	public int compare(NodeEdgeListPair p1, NodeEdgeListPair p2) {
		int order = p1.getSize() - p2.getSize();
		if (order != 0) {
			return order;
		} else {
			try {

				order = Graph2NotationTranslator.compareNodeList(
						p1.getNodeList(), p2.getNodeList());
				if (order != 0) {
					return order;
				} else {
					return Graph2NotationTranslator.compareEdgeList(
							p1.getEdgeList(), p2.getEdgeList());
				}

			} catch (ListNotComparableException ex) {
				Logger.getLogger(NodeEdgeListComparator.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			return Integer.MAX_VALUE;
		}

	}
}
