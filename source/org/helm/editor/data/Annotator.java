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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.util.GraphHider;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.notation.model.Monomer;

public class Annotator {

	private static final Color ANOTATION_COLOR = Color.DARK_GRAY;
	private Map<Node, List<String>> annotationMap = new HashMap<Node, List<String>>();
	private Map<Node, NodeLabel> annotationLabelMap = new HashMap<Node, NodeLabel>();
	private Graph2D graph;
	private GraphManager manager;
	private boolean numberNodes = false;

	private NodeMap labelInfoMap;

	public Annotator(GraphManager manager) {
		this.manager = manager;
	}

	public Annotator(Graph2D graph2D, GraphManager manager) {
		setGraph2D(graph2D);
		this.manager = manager;
	}

	public void annotateBasePosition(final Node startingNode) throws Exception {
		Map<Node, String> annotationMap = manager.getAnnotationMap();
		String annotation = annotationMap.get(startingNode) == null ? ""
				: annotationMap.get(startingNode);
		annotate(startingNode, annotation);
		NodeList nl = SequenceGraphTools
				.getBaseList(startingNode, graph, false);
		boolean peptides = false;
		if (nl == null) {
			nl = SequenceGraphTools.getPeptideSequence(startingNode, graph);
			peptides = true;
		}
		NodeCursor baseCursor = nl.nodes();
		int position = 1;
		NodeLabel positionLabel = null;
		NodeRealizer nr = null;
		for (; baseCursor.ok(); baseCursor.next()) {
			Node currNode = baseCursor.node();
			nr = graph.getRealizer(currNode);
			positionLabel = nr.createNodeLabel();
			positionLabel.setText(String.valueOf(position));
			configAnnotationLabel(positionLabel, peptides);
			nr.addLabel(positionLabel);
			LabelInfo info = getLabelInfo(currNode);
			info.setPositionNumber(position);
			position++;
		}

	}

	private void clearLabels(Node startingNode, Graph2D graph) {
		AbstractBfsIterator iter = new SenseIterator(graph, startingNode);
		while (iter.hasNext()) {
			Node currNode = iter.next();
			NodeRealizer nr = graph.getRealizer(currNode);

			while (nr.labelCount() > 1) {
				NodeLabel l = nr.getLabel(1);
				if (l != null) {
					nr.removeLabel(l);
				}
			}
			get(currNode).clear();
			// annotationLabelMap.remove(startingNode);
		}
	}

	public void annotateAllBasePosition() throws Exception {
		annotationLabelMap.clear();
		GraphHider gh = new GraphHider(graph);
		for (Edge e : graph.getEdgeArray()) {
			if (MonomerInfoUtils.isPBranchEdge(e)) {
				gh.hide(e);
			}
		}

		labelInfoMap = clearDataProvider(NodeMapKeys.LABEL_INFO_MAP);

		for (Node node : manager.getStartingNodeList()) {
			if (MonomerInfoUtils.isNucleicAcidPolymer(node)
					|| MonomerInfoUtils.isPeptidePolymer(node)) {
				clearLabels(node, graph);
				annotateBasePosition(node);
			}
		}
		if (numberNodes) {
			int i = 0;
			for (Node n : graph.getNodeArray()) {
				NodeRealizer nr = graph.getRealizer(n);
				nr.getLabel(0).setText(String.valueOf(i));
				i++;
			}
		}

		gh.unhideAll();
	}

	public void annotate(Node node, String annotation) throws IOException,
			MonomerException, JDOMException {
		List<Node> startingNodeList = manager.getStartingNodeList();
		Map<Node, String> annotationMap = manager.getAnnotationMap();
		// Map<Node, NodeLabel> startingNodeLabelMap =
		// manager.getNodeLabelMap();

		if (annotation == null) {
			return;
		}
		if (startingNodeList.contains(node)) {
			// MonomerFactory monomerFactory = MonomerFactory.getInstance();
			// Map<String, Map<String, Monomer>> monomerDB =
			// monomerFactory.getMonomerDB();
			Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
					.getInstance().getCombinedMonomerStore().getMonomerDB();

			NodeMap nodeMap = (NodeMap) node.getGraph().getDataProvider(
					NodeMapKeys.MONOMER_REF);
			MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(node);

			boolean isFliped = false;
			Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(
					monomerInfo.getMonomerID());
			if (!monomer.getMonomerType().equalsIgnoreCase(
					Monomer.BACKBONE_MOMONER_TYPE)) {
				return;
			}

			annotationMap.put(node, annotation);

			// find the first R node in this sequence.
			Node nodeToAnnotate = null;
			NodeCursor successors = null;
			Node nextNode = null;
			NodeRealizer currentRealizer = null;

			if (!monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)
					&& !monomerInfo.getPolymerType().equalsIgnoreCase(
							Monomer.PEPTIDE_POLYMER_TYPE)) {
				NodeRealizer nrPreNode = ((Graph2D) node.getGraph())
						.getRealizer(node);

				successors = node.successors();
				nextNode = null;
				for (; successors.ok(); successors.next()) {
					nextNode = successors.node();
					monomerInfo = (MonomerInfo) nodeMap.get(nextNode);
					if (monomerInfo.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
						monomer = monomerDB.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());
						if (monomer.getNaturalAnalog().equalsIgnoreCase(
								Monomer.ID_R)) {
							nodeToAnnotate = nextNode;
							currentRealizer = ((Graph2D) nodeToAnnotate
									.getGraph()).getRealizer(nodeToAnnotate);
							if (Double.compare(currentRealizer.getCenterX(),
									nrPreNode.getCenterX()) < 0) {
								isFliped = true;
							}
							break;
						}
					}
				}
			} else {
				nodeToAnnotate = node;
			}

			if (nodeToAnnotate != null) {
				currentRealizer = ((Graph2D) nodeToAnnotate.getGraph())
						.getRealizer(nodeToAnnotate);
				NodeLabel label;

				// test if this sequence has been fliped

				if (monomerInfo.getPolymerType().equalsIgnoreCase(
						Monomer.PEPTIDE_POLYMER_TYPE)) {
					label = createLabelNode(annotation, currentRealizer, "n");
					isFliped = manager.isFlipped(nodeToAnnotate);
				} else {
					label = createLabelNode(annotation, currentRealizer, "5'");
				}

				if (graph != null) {
					getLabelInfo(nodeToAnnotate).setTerminalLabel(
							label.getText());
				}

				successors = nodeToAnnotate.successors();
				for (; successors.ok(); successors.next()) {
					nextNode = successors.node();
					monomerInfo = (MonomerInfo) nodeMap.get(nextNode);
					if (monomerInfo.getPolymerType().equalsIgnoreCase(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
						monomer = monomerDB.get(monomerInfo.getPolymerType())
								.get(monomerInfo.getMonomerID());
						if (monomer.getMonomerType().equalsIgnoreCase(
								Monomer.BACKBONE_MOMONER_TYPE)) {
							NodeRealizer nrNext = ((Graph2D) nodeToAnnotate
									.getGraph()).getRealizer(successors.node());
							if (Double.compare(nrNext.getCenterX(),
									currentRealizer.getCenterX()) < 0) {
								isFliped = true;

							}
							break;
						}
					}
				}

				if (isFliped) {
					label.setPosition(NodeLabel.SE);
				} else {
					label.setPosition(NodeLabel.NW);
				}

				currentRealizer.addLabel(label);
				annotationLabelMap.put(nodeToAnnotate, label);
			}
		}

	}

	public void setGraph2D(Graph2D graph2D) {
		this.graph = graph2D;
		labelInfoMap = clearDataProvider(NodeMapKeys.LABEL_INFO_MAP);
	}

	public void setManager(GraphManager manager) {
		this.manager = manager;
	}

	public NodeLabel getLabel(Node node) {
		return annotationLabelMap.get(node);
	}

	public void reset() {
		annotationLabelMap.clear();
		annotationMap.clear();
	}

	public void removeLabel(Node node) {
		annotationLabelMap.remove(node);
	}

	public void numberNodes(boolean numberNodes) {
		this.numberNodes = numberNodes;
	}

	private NodeLabel createLabelNode(String annotation, NodeRealizer nrRNode,
			String type) {
		NodeLabel label = null;
		label = nrRNode.createNodeLabel();
		label.setModel(NodeLabel.EIGHT_POS);

		label.setText(type + " " + annotation);
		label.setBackgroundColor(Color.YELLOW);
		return label;
	}

	private List<String> get(Node node) {
		List<String> result = annotationMap.get(node);
		if (result == null) {
			result = new ArrayList<String>();
			annotationMap.put(node, result);
		}
		return result;
	}

	private NodeMap clearDataProvider(Object key) {
		NodeMap map = (NodeMap) graph.getDataProvider(key);

		if (map != null) {
			graph.removeDataProvider(key);
		}

		map = graph.createNodeMap();
		graph.addDataProvider(key, map);
		return map;
	}

	private LabelInfo getLabelInfo(Node viewNode) {
		LabelInfo info = (LabelInfo) labelInfoMap.get(viewNode);
		if (info == null) {
			info = new LabelInfo();
			labelInfoMap.set(viewNode, info);
		}
		return info;
	}

	public static NodeLabel configAnnotationLabel(NodeLabel annotationLabel,
			boolean isPeptide) {
		annotationLabel.setModel(NodeLabel.EIGHT_POS);
		if (isPeptide) {
			annotationLabel.setPosition(NodeLabel.N);
		} else {
			annotationLabel.setPosition(NodeLabel.NW);
		}

		annotationLabel.setTextColor(ANOTATION_COLOR);

		return annotationLabel;
	}
}
