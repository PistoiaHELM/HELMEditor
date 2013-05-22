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

import java.util.LinkedList;
import java.util.List;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.NodeRealizer;

import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.utility.MonomerInfoUtils;

public abstract class AbstractSequenceTransformer implements SequenceTransformer {

	protected LinkedList<Node> viewSequence = new LinkedList<Node>();
	protected NodeMap viewModificationsCount;
	protected NodeMap viewNodesLabelsMap;
	protected NodeMap viewNodeLabelInfoMap;
	protected NodeMap viewNode2HyperNode;
	protected NodeMap viewNodeStartingNodeMap;
	protected NodeMap monomerRefNodeMap;
	protected EdgeMap viewEdgeTypes;
	protected SequenceViewModel viewModel;

	protected Graph2D view;
	protected Graph editor;

	protected void init(SequenceViewModel viewModel) {
		view = viewModel.getGraph();
		viewModificationsCount = (NodeMap) view
				.getDataProvider(SequenceViewModel.MODIFICATION_COUNT);
		viewNodesLabelsMap = (NodeMap) view
				.getDataProvider(SequenceViewModel.LABELS_MAP);
		viewNodeLabelInfoMap = (NodeMap) view
				.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
		viewEdgeTypes = (EdgeMap) view
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		viewNodeStartingNodeMap = (NodeMap)view
				.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
		viewNode2HyperNode = (NodeMap)view.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		//hyperNode2Starting = (NodeMap)view.getDataProvider(NodeMapKeys.HYPERNODE2STARTING_NODE);
		
		monomerRefNodeMap = (NodeMap)view.getDataProvider(NodeMapKeys.MONOMER_REF);
		
		this.viewModel = viewModel;
	}
	
	public List<Node> transform(Node editorStartingNode, SequenceViewModel viewModel) {
		init(viewModel);
		editor = editorStartingNode.getGraph();
		buildViewSequence(editorStartingNode);
		fillNodeMaps(editorStartingNode);
		fillLabelMaps();
		return viewSequence;
	}
	
	/**
	 * In this method mapped view sequence should be constructed and viemodel
	 * map view -> editor and edtior -> view should be filled
	 * 
	 * @param editorStartingNode sequence starting node in the editor graph
	 */
	protected abstract void buildViewSequence(Node editorStartingNode);
	
	/**
	 * Here all dataproviders with label-sprecific data should be filled
	 */
	protected abstract void fillLabelMaps();

	protected void fillNodeMaps(Node editorStartingNode) {
		Node viewStartingNode = viewModel.getViewNode(editorStartingNode);
		viewModel.addStrartingViewNode(viewStartingNode);
		Node hyperNode = getHyperNode(editorStartingNode);
		for (Node n : viewSequence) {
			viewNodeStartingNodeMap.set(n, viewStartingNode);
			viewNode2HyperNode.set(n, hyperNode);
		}
	}

	private Node getHyperNode(Node editorNode) {
		Graph g = editorNode.getGraph();
		NodeMap map = (NodeMap)g.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		return (Node)map.get(editorNode);
	}

	protected LabelInfo getLabelInfo(Node viewNode) {
		LabelInfo info = (LabelInfo)viewNodeLabelInfoMap.get(viewNode);
		if (info == null) {
			info = new LabelInfo();
			viewNodeLabelInfoMap.set(viewNode, info);
		}
		return info;
	}

	protected LabelInfo getEditorLabelInfo(Node editorNode) {
		NodeMap editorInfoMap = (NodeMap)editor.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
		if (editorInfoMap == null) {
			return null;
		}
		return (LabelInfo)editorInfoMap.get(editorNode);
	}

	protected Node createNode(String label, Graph2D graph) {
		Node node = graph.createNode();
		NodeRealizer nodeRealizer = graph.getRealizer(node);
		nodeRealizer.getLabel().setText(label);
		
		return node;
	}

	protected Node getSingleSuccessor(Node n) {
		EdgeCursor edgeCursor = n.outEdges();
		Graph g = n.getGraph();
		EdgeMap type = (EdgeMap)g.getDataProvider(EdgeMapKeys.EDGE_INFO);
		
		for (; edgeCursor.ok(); edgeCursor.next()) {
			Edge e = edgeCursor.edge();
			EdgeInfo info = (EdgeInfo)type.get(e);
			if (info.isRegular() && !MonomerInfoUtils.isInterPolymer(e)) {
				return e.opposite(n);
			}
		}
		
		return null;
			
	}
	
	protected Edge getSingleOutEdge(Node n) {
		EdgeCursor edgeCursor = n.outEdges();
		Graph g = n.getGraph();
		EdgeMap type = (EdgeMap)g.getDataProvider(EdgeMapKeys.EDGE_INFO);
		
		for (; edgeCursor.ok(); edgeCursor.next()) {
			Edge e = edgeCursor.edge();
			EdgeInfo info = (EdgeInfo)type.get(e);
			if (info.isRegular()) {
				return e;
			}
		}
		
		return null;
			
	}

	protected Node getSinglePredecessor(Node n) {
		NodeCursor pred = n.predecessors(); 
		if (pred.ok()) {
			return pred.node(); 
		} else {
			return null;
		}	
	}
	

}
