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
package org.helm.editor.componentPanel.sequenceviewpanel;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.editor.data.ComponentIterator;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Attachment;

public class ViewElementsConstructor implements GraphElementConstructor {

	
	private Graph2D _graph;
	private SequenceViewModel _viewModel;
	private ViewMetrics _layoutMetrics;

	public ViewElementsConstructor(Graph2D graph, SequenceViewModel viewModel,
			ViewMetrics layoutMetrics) {
		_graph = graph;
		_viewModel = viewModel;
		_layoutMetrics = layoutMetrics;
	}

	public void setGraph(Graph2D graph) {
		_graph = graph;
	}

	public void setMetrics(ViewMetrics layoutMetrics) {
		_layoutMetrics = layoutMetrics;
	}

	public Node createNode(String label, int nodeType) {

		switch (nodeType) {
		case SIMPLE_NODE:
			return createChemNode(label, _graph);

		case CHEM_NODE:
			return createChemNode(label, _graph);

		case ERROR_NODE:
			return createErrorNode(label, _graph);
		}

		return null;
	}

	public void createEdge(Edge currentEdge, Attachment sourceAttachment, Attachment targetAttachment, int edgeType) {
		switch (edgeType) {
		case PAIR_EDGE:
			addPairEdge(currentEdge, sourceAttachment, targetAttachment);
			break;

		case BACKBONE_BRANCH_EDGE:
			addBackboneToBranchEdge(currentEdge, sourceAttachment, targetAttachment);
			break;

		case BRANCH_BRANCH_EDGE:
			addBranchToBranchEdge(currentEdge, sourceAttachment, targetAttachment);
			break;

		case CHEM_EDGE:
			addChemEdge(currentEdge, sourceAttachment, targetAttachment);
			break;
		default: addSimpleEdge(currentEdge);

		}
	}

	public Object realize(Graph2D graph, Object realizedObject,
			int realizationType) {
		switch (realizationType) {
		case REALIZE_CHEM_EDGE:
			realizeChemEdge(graph, (Edge) realizedObject, null, null);
			break;
		case REALIZE_NEIBOURS:
			return realizeNeigbour(graph, (Node) realizedObject);
		}

		return null;
	}

	public void setUpViewModel(SequenceViewModel viewModel) {
		_viewModel = viewModel;
	}

	private Node createNode(String label, Graph2D graph) {
		Node node = graph.createNode();

		NodeRealizer nodeRealizer = graph.getRealizer(node);
		nodeRealizer.getLabel().setText(label);

		return node;
	}

	private Node createChemNode(String label, Graph2D graph) {
		Node chem = createNode(label, graph);
		NodeRealizer nodeRealizer = graph.getRealizer(chem);
		nodeRealizer.setTransparent(false);

		NodeLabel nl = nodeRealizer.getLabel(0);
		//		nodeRealizer.setSize(_layoutMetrics.getChemNodeSize(), _layoutMetrics.getChemNodeSize());
		nodeRealizer.setSize(nl.getBox().getWidth()+10, nl.getBox().getHeight());
		nl.setFontSize(_layoutMetrics.getChemNodeFontSize());
		nl.setFontStyle(Font.BOLD);
		nl.setModel(NodeLabel.INTERNAL);
		nl.setPosition(NodeLabel.CENTER);
		nl.setAlignment(NodeLabel.ALIGN_CENTER);
		nl.setTextColor(Color.BLACK);

		return chem;
	}

	private Node createErrorNode(String label, Graph2D graph) {
		Node node = createNode(label, graph);

		NodeRealizer nodeRealizer = graph.getRealizer(node);
		nodeRealizer.setTransparent(true);
		nodeRealizer.setLineColor(Color.white);

		NodeLabel nl = nodeRealizer.getLabel(0);
		nl.setFontSize(_layoutMetrics.getLabelFontSize()+15);
		nl.setFontStyle(Font.PLAIN);
		nl.setModel(NodeLabel.INTERNAL);
		nl.setPosition(NodeLabel.CENTER);
		nl.setAlignment(NodeLabel.ALIGN_LEFT);
		nl.setTextColor(Color.cyan);

		nodeRealizer.setSize(nl.getBox().getWidth(), nl.getBox().getHeight());

		return node;
	}

	private void realizeChemEdge(Graph2D graph, Edge newEdge, Attachment sourceAttachment, Attachment targetAttachment) {

		EdgeMap edgeTypeMap = (EdgeMap) graph
		.getDataProvider(EdgeMapKeys.EDGE_INFO);

		EdgeRealizer er = graph.getRealizer(newEdge);
		er.setLineColor(Color.BLACK);
		er.setLineType(LineType.LINE_1);
		edgeTypeMap.set(newEdge, new SViewEdgeInfo(EdgeType.CHEM, sourceAttachment, targetAttachment));
	}

	private Node realizeNeigbour(Graph2D graph, Node node) {
		Iterator<Node> iterator = new ComponentIterator(graph, node);
		for (; iterator.hasNext();) {
			Node next = iterator.next();

			if (_viewModel.getEditorViewMap().containsKey(next) && !MonomerInfoUtils.isChemicalModifierPolymer(next)) {
				Node mapped = _viewModel.getViewNode(next);
				if (MonomerInfoUtils.is5Node(mapped, _graph)) {
					continue;
				}

				_viewModel.putViewNode(node, mapped);
				return mapped;
			}

		}

		// this should never happen. We're doing this for safety
		Node result = createNode(MonomerInfoUtils.getMonomerID(node),
				ViewElementsConstructor.SIMPLE_NODE);

		_viewModel.putViewNode(node, result);

		return result;
	}

	private void addChemEdge(Edge currentEdge, Attachment sourceAttachment, Attachment targetAttachment) {
		Edge newEdge = addSimpleEdge(currentEdge);
		realizeChemEdge(_graph, newEdge, sourceAttachment, targetAttachment);
	}

	private Edge addSimpleEdge(Edge currentEdge) {
		Node sourceFrom = currentEdge.source();
		Node from = _viewModel.getViewNode(sourceFrom);

		if (from == null) {
			from = realizeNeigbour(_graph, sourceFrom);
		}

		Node sourceTo = currentEdge.target();
		Node to = _viewModel.getViewNode(sourceTo);
		if (to == null) {
			to = realizeNeigbour(_graph, sourceTo);
		}

		Edge newEdge = _graph.createEdge(from, to);
		return newEdge;
	}

	private void addPairEdge(Edge currentEdge, Attachment sourceAttachment, Attachment targetAttachment) {

		EdgeMap edgeTypeMap = (EdgeMap) _graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

		Node sourceNode = _viewModel.getViewNode(currentEdge.source());
		Node targetNode = _viewModel.getViewNode(currentEdge.target());

		Edge newEdge = _graph.createEdge(sourceNode, targetNode);
		edgeTypeMap.set(newEdge, new SViewEdgeInfo(EdgeType.PAIR, sourceAttachment, targetAttachment));


		sourceNode = getViewStartingNode(newEdge.source(), _graph);
		targetNode = getViewStartingNode(newEdge.target(), _graph);

		_viewModel.addComplentaryViewNodes(sourceNode, targetNode);
	}


	private void addBranchToBranchEdge(Edge currentEdge, Attachment sourceAttachment, Attachment targetAttachment) {
		EdgeMap edgeTypeMap = (EdgeMap) _graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

		Node sourceNode = _viewModel.getViewNode(currentEdge.source());
		Node targetNode = _viewModel.getViewNode(currentEdge.target());

		Edge newEdge = _graph.createEdge(sourceNode, targetNode);
		edgeTypeMap.set(newEdge, new SViewEdgeInfo(EdgeType.BRANCH_BRANCH, sourceAttachment, targetAttachment));

		sourceNode = getViewStartingNode(newEdge.source(), _graph);
		targetNode = getViewStartingNode(newEdge.target(), _graph);

		_viewModel.addComplentaryViewNodes(sourceNode, targetNode);
	}

	private void addBackboneToBranchEdge(Edge currentEdge, Attachment sourceAttachment, Attachment targetAttachment) {
		EdgeMap edgeTypeMap = (EdgeMap) _graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

		Node sourceNode = _viewModel.getViewNode(currentEdge.source());
		Node targetNode = _viewModel.getViewNode(currentEdge.target());

		Edge newEdge = _graph.createEdge(sourceNode, targetNode);

		edgeTypeMap.set(newEdge, new SViewEdgeInfo(EdgeType.BRANCH_BACKBONE, sourceAttachment, targetAttachment));

		sourceNode = getViewStartingNode(newEdge.source(), _graph);
		targetNode = getViewStartingNode(newEdge.target(), _graph);

		_viewModel.addComplentaryViewNodes(sourceNode, targetNode);
	}

	private Node getViewStartingNode(Node node, Graph2D view) {
		NodeMap startingNodeMap = (NodeMap) _graph.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
		return (Node)startingNodeMap.get(node);			
	}

	/**
	 * get the starting node of the sequence in where the current node is
	 * sitting in
	 * 
	 * @deprecated use getViewStartingNode
	 * @param node
	 * @param graph
	 * @return
	 */
	@Deprecated
	private Node getStartingNode(Node node, Graph2D graph) {

		if (_viewModel.getStartingViewNodeList().contains(node)) {
			return node;
		} else {
			NodeCursor pres = node.predecessors();
			Edge edge;
			Node currentNode = node;

			EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
			Node preNode = currentNode;
			while (pres.ok()) {
				for (; pres.ok(); pres.next()) {
					edge = currentNode.getEdge(pres.node());

					EdgeType edgeType = ((SViewEdgeInfo) edgeMap.get(edge)).getType();
					if ((edgeType == EdgeType.REGULAR)
							|| (edgeType == EdgeType.MODIFIED_P)) {
						currentNode = pres.node();
						break;
					}
				}
				if (currentNode != preNode) {
					pres = currentNode.predecessors();
					preNode = currentNode;
				} else {
					break;
				}

			}
			if (_viewModel.getStartingViewNodeList().contains(currentNode)) {
				return currentNode;
			} else {
				return null;
			}
		}
	}

}
