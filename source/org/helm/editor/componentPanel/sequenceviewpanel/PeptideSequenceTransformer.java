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

import java.util.HashSet;
import java.util.Set;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;

import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.utility.MonomerInfoUtils;

public class PeptideSequenceTransformer extends AbstractSequenceTransformer {

	private Set<Node> visited = new HashSet<Node>();
	private Set<Edge> edges = new HashSet<Edge>();
	
	@Override
	protected void buildViewSequence(Node editorStartingNode) {
		Node currentEditorNode = editorStartingNode;
		while (currentEditorNode != null) {
			/// if simple cycle
			if (visited.contains(currentEditorNode)) {
				break;
			}
			
			createNode(currentEditorNode);
			visited.add(currentEditorNode);
			
			EdgeCursor c = currentEditorNode.outEdges();
			for (;c.ok(); c.next()) {
				edges.add(c.edge());
			}
			
			currentEditorNode = getSingleSuccessor(currentEditorNode);
			
		}
		
		// build edges 
		EdgeMap mapType = (EdgeMap)editor.getDataProvider(EdgeMapKeys.EDGE_INFO);
		for (Edge e : edges) {
			if (visited.contains(e.source()) && visited.contains(e.target())) {
				EditorEdgeInfoData infoData = ((EditorEdgeInfoData)mapType.get(e));
				createEdge(viewModel.getViewNode(e.source()), 
						viewModel.getViewNode(e.target()), infoData);
			}
		}
	}

	private Node createNode(Node editorNode) {
		Node viewNode = createNode(MonomerInfoUtils.getNaturalAnalog(editorNode), 
				view);
		viewModel.putViewNode(editorNode, viewNode);
		viewModificationsCount.set(viewNode, 
				MonomerInfoUtils.getMonomer(editorNode).isModified() ? 1 : 0);
		viewSequence.add(viewNode);
		return viewNode;
	}
	
	private Edge createEdge(Node viewSource, Node viewTarget, EditorEdgeInfoData infoData) {
		Edge e = view.createEdge(viewSource, viewTarget);

		EdgeType type = infoData.getType();
		if (type == null) {
			type = EdgeType.REGULAR;
		}

		viewEdgeTypes.set(e, new SViewEdgeInfo(type, infoData.getSourceNodeAttachment(), infoData.getTargetNodeAttachment()));
		return e;
	}
	
	@Override
	protected void fillLabelMaps() {
		for (Node viewNode : viewSequence) {
			LabelInfo viewInfo = getLabelInfo(viewNode);
			for (Node editorNode : viewModel.getSourceNodes(viewNode)) {
				LabelInfo editorInfo = getEditorLabelInfo(editorNode);
				viewInfo.setPositionNumber(editorInfo.getPositionNumber());
				viewInfo.setTerminalLabel(editorInfo.getTerminalLabel());
			}
		}
	}

}
