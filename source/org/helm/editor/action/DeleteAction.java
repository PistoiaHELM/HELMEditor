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
package org.helm.editor.action;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import y.algo.GraphConnectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.util.GraphHider;
import y.view.Graph2D;

import org.helm.editor.controller.ModelController;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.GUIBase;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.MonomerInfoUtils;

/**
 * 
 * @author lih25
 */
public class DeleteAction extends AbstractAction {

	private GUIBase editor;

	private String _ownerCode;

	public DeleteAction(GUIBase editor, String ownerCode) {
		super("Delete Selection");

		_ownerCode = ownerCode;

		URL imageURL = GUIBase.class.getResource("resource/Delete16.gif");
		if (imageURL != null) {
			this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
		}
		this.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");

		this.editor = editor;
	}

	public void actionPerformed(ActionEvent e) {

		try {
			Graph2D graph = editor.getView().getGraph2D();
			GraphManager graphManager = editor.getGraphManager();
			EdgeCursor edgeCursor = graph.selectedEdges();
			NodeCursor nodeCursor = graph.selectedNodes();

			EdgeMap edgeMap = (EdgeMap) graph
					.getDataProvider(EdgeMapKeys.EDGE_INFO);

			// Edge edge = null;

			NodeMap nodeMap = (NodeMap) graph
					.getDataProvider(NodeMapKeys.MONOMER_REF);
			NodeMap hyperNodeMap = (NodeMap) graph
					.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
			NodeMap startingNodeMap = (NodeMap) graph
					.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);

			// MonomerInfo sourceMonomerInfo = null;
			// MonomerInfo targetMonomerInfo = null;
			String annotation = null;
			if (edgeCursor.size() > 0) {
				edgeCursor.toFirst();
				for (; edgeCursor.ok(); edgeCursor.next()) {
					Edge edge = edgeCursor.edge();
					// free up the connections
					releaseConnection(edge);

					Node source = edge.source();
					Node sourceStarting = (Node) startingNodeMap.get(source);

					edgeMap = (EdgeMap) graph
							.getDataProvider(EdgeMapKeys.EDGE_INFO);
					EditorEdgeInfoData edgeInfoData = (EditorEdgeInfoData) edgeMap
							.get(edge);

					if (sourceStarting != null
							&& isInSimpleLoop(sourceStarting)
							&& !edgeInfoData.isPair()) {
						graphManager.removeStartingNode(sourceStarting);
					}

					if (!edgeInfoData.isPair() && breaksComponent(edge)) {
						Node starting = edge.target();
						NodeMap startingMap = (NodeMap) graph
								.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
						Node s = (Node) startingMap.get(edge.source());
						int startIndex = graphManager.getIndex(s);
						graphManager.addStartingNode(startIndex + 1, starting);
					}

					graph.removeEdge(edge);

				}
			}

			// if R node is selected for deletion then its base should be
			// deleted as well. So just add unselected
			// branch nodes which are neighbours to selected R nodes
			for (; nodeCursor.ok(); nodeCursor.next()) {
				if (MonomerInfoUtils.isRMonomer(nodeCursor.node())) {
					for (NodeCursor neighbours = nodeCursor.node().neighbors(); neighbours
							.ok(); neighbours.next()) {
						if (MonomerInfoUtils.isBranchMonomer(neighbours.node())) {
							graph.setSelected(neighbours.node(), true);
						}
					}
				}
			}
			nodeCursor = graph.selectedNodes();

			// if a node is being removed, we need to add all its successors to
			// the starting node list
			Node node = null;
			NodeCursor successors = null;

			Node hyperNode1 = null;
			Node hyperNode2 = null;
			int index = -1;

			if (nodeCursor.size() > 0) {
				for (; nodeCursor.ok(); nodeCursor.next()) {
					node = (Node) nodeCursor.node();
					releaseConnections(node);

					if (graphManager.isStartingNode(node)) {
						annotation = graphManager.getAnnotation(node);
						index = graphManager.getIndex(node);
						graphManager.removeStartingNode(node);
					} else {
						Node s = (Node) startingNodeMap.get(node);
						index = graphManager.getIndex(s) + 1;
					}

					successors = node.successors();
					hyperNode1 = (Node) hyperNodeMap.get(node);
					for (; successors.ok(); successors.next()) {
						hyperNode2 = (Node) hyperNodeMap.get(successors.node());
						if (hyperNode1 == hyperNode2) {
							graphManager.addStartingNode(index,
									successors.node());
							graphManager
									.annotate(successors.node(), annotation);
						}
					}
					annotation = null;

					Node starting = (Node) startingNodeMap.get(node);
					if (starting != null && isInSimpleLoop(starting)) {
						graphManager.removeStartingNode(starting);
					}

					// remove the node after update the starting node list
					graph.removeNode(node);
					index = -1;
				}
			}

			Graph2NotationTranslator.updateHyperGraph(graph, graphManager);
			String notation = Graph2NotationTranslator
					.getNewNotation(graphManager);
			ModelController.notationUpdated(notation, _ownerCode);
		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
		}

	}

	private void releaseConnections(Node node) {
		Graph graph = node.getGraph();
		EdgeCursor edgeCursor = node.edges();

		// release connections
		for (; edgeCursor.ok(); edgeCursor.next()) {
			Edge edge = edgeCursor.edge();
			releaseConnection(edge);
		}
	}

	private void releaseConnection(Edge edge) {
		Graph graph = edge.getGraph();
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		EditorEdgeInfoData edgeInfoData = (EditorEdgeInfoData) edgeMap
				.get(edge);
		if (edgeInfoData == null) {
			return;
		}

		MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap
				.get(edge.source());
		MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap
				.get(edge.target());

		sourceMonomerInfo.setConnection(edgeInfoData.getSourceNodeAttachment(),
				false);
		targetMonomerInfo.setConnection(edgeInfoData.getTargetNodeAttachment(),
				false);
	}

	private boolean breaksComponent(Edge e) {
		Graph g = e.getGraph();
		NodeMap hyper = (NodeMap) g
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		NodeMap starting = (NodeMap) g
				.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
		if (hyper.get(e.source()) != hyper.get(e.target())) {
			// edge connects two different components,
			// so need to add another one
			return false;
		}

		GraphHider gh = new GraphHider(g);
		gh.hide(e);

		Node s = (Node) starting.get(e.source());
		NodeList accessibleNodes = GraphConnectivity.getSuccessors(g,
				new NodeList(s), g.N());
		// check if we still can access target node
		gh.unhideAll();
		return !accessibleNodes.contains(e.target());
	}

	private boolean isInSimpleLoop(Node n) {
		if (n.inDegree() == 0) {
			return false;
		}

		NodeList myList = new NodeList(n);
		Graph g = n.getGraph();
		GraphHider gh = new GraphHider(g);

		// hide branch nodes for RNA to make all cycles simple
		for (Node node : g.getNodeArray()) {
			if (MonomerInfoUtils.isBranchMonomer(node)
					&& MonomerInfoUtils.isNucleicAcidPolymer(node)) {
				gh.hide(node);
			}
		}

		for (Edge e : g.getEdgeArray()) {
			if (MonomerInfoUtils.isPBranchEdge(e)) {
				gh.hide(e);
			}
		}

		NodeList neighbours = GraphConnectivity.getNeighbors(n.getGraph(),
				myList, g.N());

		for (Object neigh : neighbours) {
			Node neighN = (Node) neigh;
			if (MonomerInfoUtils.isBackbone(neighN)) {
				if (neighN.degree() != 2) {
					gh.unhideAll();
					return false;
				}
			}
		}

		gh.unhideAll();
		return true;

	}
}
