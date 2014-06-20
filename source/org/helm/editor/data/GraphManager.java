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
package org.helm.editor.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.NodeLabel;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.model.Monomer;

/**
 * Keeps track of how many sequences and chemical structures in the current
 * graph each graph should associate with a graph manager
 * 
 * @author Hongli Li
 */
public class GraphManager {

	/**
	 * list of starting node. it could be a starting node for a sequence or a
	 * chemical structure
	 */
	private LinkedList<Node> startingNodeList;
	private Graph hyperGraph;
	// private boolean needUpdate = true; //if the hyperGraph need to be update
	private Map<Node, String> annotationMap;
	private Map<Node, NodeLabel> startingNodeLabelMap;
	private Set<Node> startingFlippedNodes = new HashSet<Node>();
	private List<DataListener> listeners;
	private Annotator annotator;

	/**
	 * constructor
	 */
	public GraphManager() {
		listeners = new ArrayList<DataListener>();
		startingNodeList = new LinkedList<Node>();
		annotationMap = new HashMap<Node, String>();
		startingNodeLabelMap = new HashMap<Node, NodeLabel>();

		hyperGraph = new Graph2D();
		NodeMap hyperNodeNameMap = hyperGraph.createNodeMap();
		hyperGraph
				.addDataProvider(NodeMapKeys.HYPERNODE_NAME, hyperNodeNameMap);

		NodeMap hyperNodeAnotationMap = hyperGraph.createNodeMap();
		hyperGraph.addDataProvider(NodeMapKeys.HYPERNODE_ANOTATION,
				hyperNodeAnotationMap);

		NodeMap hyperNodeMapPolymerType = hyperGraph.createNodeMap();
		hyperGraph.addDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE,
				hyperNodeMapPolymerType);

		NodeMap hyperNodeMapPolymerName = hyperGraph.createNodeMap();
		hyperGraph.addDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION,
				hyperNodeMapPolymerName);

		EdgeMap hyperEdgeMap = hyperGraph.createEdgeMap();
		hyperGraph.addDataProvider(EdgeMapKeys.DESCRIPTION, hyperEdgeMap);

		annotator = new Annotator(this);
	}

	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
	}

	/**
	 * annotate a nucleic acid sequence
	 * 
	 * @param node
	 *            - the starting node of a sequence
	 * @param annotation
	 *            - sense or antisense
	 */
	public void annotate(Node node, String annotation) throws IOException,
			MonomerException, JDOMException {
		if (annotation != null) {
			annotator.annotate(node, annotation);
		}
	}

	public Annotator getAnnotator() {
		return annotator;
	}

	public List<Node> getStartingNodeList() {
		return startingNodeList;
	}

	/**
	 * add a node to the starting node list. annotate it when nessesary
	 * 
	 * @param node
	 * @throws java.io.IOException
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 */
	public void addStartingNode(Node node) throws IOException,
			MonomerException, JDOMException {
		if (!startingNodeList.contains(node)) {
			startingNodeList.add(node);
			// annotate(node, "");
			// needUpdate = true;
		}

	}

	/**
	 * get the index of a node in the starting node list
	 * 
	 * @param node
	 * @return index
	 */
	public int getIndex(Node node) {
		return startingNodeList.indexOf(node);
	}

	/**
	 * insert a node in the starting node list at certain position
	 * 
	 * @param index
	 * @param node
	 * @throws java.io.IOException
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 */
	public void addStartingNode(int index, Node node) throws IOException,
			MonomerException, JDOMException {
		if (!startingNodeList.contains(node)) {
			startingNodeList.add(index, node);
		}
	}

	/**
	 * remove a node from the starting node list if it exits
	 * 
	 * @param node
	 * @return boolean - true or false
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 * @throws java.io.IOException
	 */
	public boolean removeStartingNode(Node node) throws MonomerException,
			JDOMException, IOException {
		if (annotationMap.containsKey(node)) {
			annotationMap.remove(node);
		}
		annotator.removeLabel(node);
		startingFlippedNodes.remove(node);
		return startingNodeList.remove(node);

	}

	public boolean isStartingNode(Node node) {
		return startingNodeList.contains(node);
	}

	public Graph getHyperGraph() {
		return hyperGraph;
	}

	public void reset() {
		startingNodeList.clear();
		annotationMap.clear();
		listeners.clear();
		if (hyperGraph != null) {
			hyperGraph.clear();
		}
		startingFlippedNodes.clear();
		annotator.reset();
		startingNodeLabelMap.clear();
	}

	/**
	 * sort the starting node list according to the connection and also node
	 * properties it will put RNA/DNA node first and then for each paired up
	 * RNA, they will be put next to each other
	 * 
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws org.jdom.JDOMException
	 * @throws java.io.IOException
	 */
	public void sortStartingNodeList() throws NotationException,
			MonomerException, JDOMException, IOException {

		if (startingNodeList == null || startingNodeList.isEmpty()) {
			return;
		}

		Graph2D graph = (Graph2D) startingNodeList.get(0).getGraph();

		// if (needUpdate) {
		// Graph2NotationTranslator.updateHyperGraph(graph, this);
		// }

		Collections.sort(startingNodeList, new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				final Graph2D graph = (Graph2D) o1.getGraph();

				// TODO: why graph is null
				if (graph == null) {
					return -1;
				}
				NodeMap monomerInfoMap = (NodeMap) graph
						.getDataProvider(NodeMapKeys.MONOMER_REF);
				MonomerInfo monomerInfo1 = (MonomerInfo) monomerInfoMap.get(o1);
				MonomerInfo monomerInfo2 = (MonomerInfo) monomerInfoMap.get(o2);

				if (monomerInfo1.getPolymerType().equalsIgnoreCase(
						monomerInfo2.getPolymerType())) {
					// if both polymers are nucleotide, if they have anotation
					// such as sense and antisense, put sense strand first
					if (monomerInfo1.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
						String anotation1 = null;
						String anotation2 = null;
						if (graph.getRealizer(o1).labelCount() >= 2) {
							anotation1 = graph.getRealizer(o1).getLabel(1)
									.getText();
						}

						if (graph.getRealizer(o2).labelCount() >= 2) {
							anotation2 = graph.getRealizer(o2).getLabel(1)
									.getText();
						}
						// if two anotations are the same
						if (anotation1 != null && anotation2 != null
								&& anotation1.equalsIgnoreCase(anotation2)) {
							return 0;
							// if node o1's anotation is sense, put o1 first
						} else if (anotation1 != null
								&& anotation1
										.equalsIgnoreCase(MacromoleculeEditor.SENSE)) {
							return -1;
							// if node o2's anotation is sense, put o2 first
						} else if (anotation2 != null
								&& anotation2
										.equalsIgnoreCase(MacromoleculeEditor.SENSE)) {
							return 1;
							// otherwise, return 0
						} else if (anotation1 != null
								&& anotation1
										.equalsIgnoreCase(MacromoleculeEditor.ANTISENSE)) {
							return 1;
						} else if (anotation2 != null
								&& anotation2
										.equalsIgnoreCase(MacromoleculeEditor.ANTISENSE)) {
							return -1;
						} else {
							return 0;
						}

					} else {
						return 0;
					}
				} else if (monomerInfo1.getPolymerType().equalsIgnoreCase(
						Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
					return -1;
				} else if (monomerInfo1.getPolymerType().equalsIgnoreCase(
						Monomer.CHEMICAL_POLYMER_TYPE)) {
					return 1;
				} else if (monomerInfo1.getPolymerType().equalsIgnoreCase(
						Monomer.PEPTIDE_POLYMER_TYPE)) {
					if (monomerInfo2.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
						return 1;
					} else if (monomerInfo2.getPolymerType().equalsIgnoreCase(
							Monomer.CHEMICAL_POLYMER_TYPE)) {
						return -1;
					} else {
						return 0;

					}
				} else {
					return 0;
				}
			}
		});

		NodeMap parentNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		EdgeMap hyperEdgeDescMap = (EdgeMap) hyperGraph
				.getDataProvider(EdgeMapKeys.DESCRIPTION);
		NodeMap monomerInfoMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo monomerInfo = null;

		// from hyper node to node
		Map<Node, Node> hyperNode2Node = new HashMap<Node, Node>();
		Node hyperNode = null;
		for (Node node : startingNodeList) {
			hyperNode = (Node) parentNodeMap.get(node);
			hyperNode2Node.put(hyperNode, node);
		}

		String desc = null;
		EdgeCursor hyperEdgeCursor;
		Node startingNode;
		int size = startingNodeList.size();
		Node node2 = null;
		int index = 0;

		for (int i = 0; i < size; i++) {
			startingNode = startingNodeList.get(i);
			hyperNode = (Node) parentNodeMap.get(startingNode);
			monomerInfo = (MonomerInfo) monomerInfoMap.get(startingNode);
			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				// test in edges
				hyperEdgeCursor = hyperNode.inEdges();
				for (; hyperEdgeCursor.ok(); hyperEdgeCursor.next()) {
					desc = (String) hyperEdgeDescMap
							.get(hyperEdgeCursor.edge());
					if (desc.contains("pair")) {
						node2 = hyperNode2Node.get(hyperEdgeCursor.edge()
								.source());
						index = startingNodeList.indexOf(node2);
						if (index > i) {
							startingNodeList.remove(index);
							startingNodeList.add(i + 1, node2);
							i++;
							// there is one and only one RNA sequence
							break;
						}
					}

				}

				// for out edges
				hyperEdgeCursor = hyperNode.outEdges();
				for (; hyperEdgeCursor.ok(); hyperEdgeCursor.next()) {
					desc = (String) hyperEdgeDescMap
							.get(hyperEdgeCursor.edge());
					if (desc.contains("pair")) {
						node2 = hyperNode2Node.get(hyperEdgeCursor.edge()
								.target());
						index = startingNodeList.indexOf(node2);
						if (index > i) {
							startingNodeList.remove(index);
							startingNodeList.add(i + 1, node2);
							i++;
							// there is one and only one RNA sequence
							break;
						}
					}

				}
			}

		}
	}

	public ChemSequenceHolder getChemSequenceHolder() {
		if (startingNodeList == null || startingNodeList.isEmpty()) {
			return EmptyChemSequenceHolder.getInstance();
		}
		Graph2D graph = (Graph2D) startingNodeList.get(0).getGraph();
		ChemSequenceHolderImpl holder = new ChemSequenceHolderImpl(graph);
		for (Node node : startingNodeList) {
			holder.pushNode(node);
		}
		return holder;
	}

	/**
	 * get the annotation for a sequence starting with startingNode
	 * 
	 * @param startingNode
	 * @return annotation of starting node
	 */
	public String getAnnotation(Node startingNode) {
		String annotationText = "";
		if (startingNodeList.contains(startingNode)) {
			annotationText = annotationMap.get(startingNode);
		}

		return annotationText;
	}

	public Map<Node, String> getAnnotationMap() {
		return annotationMap;
	}

	public boolean isFlipped(Node node) {

		if (startingFlippedNodes == null || startingFlippedNodes.isEmpty()) {
			return false;
		}

		return startingFlippedNodes.contains(node);
	}

	public void setFlipped(Node node) {
		startingFlippedNodes.add(node);
	}

	public void clearFlippedSet() {
		startingFlippedNodes.clear();
	}
}
