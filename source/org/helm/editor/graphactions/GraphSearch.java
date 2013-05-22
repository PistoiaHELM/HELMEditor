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
package org.helm.editor.graphactions;

import java.util.HashMap;

import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.SequenceGraphTools;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.view.Graph2D;

public class GraphSearch {
	
	private GraphAction _currentAction;
	
	public GraphSearch(GraphAction action){
		_currentAction = action;
	}
	
	public Graph2D copyGraphWithCondition(GraphAction action) {
		
		Graph2D currentGraph = (Graph2D) _currentAction.getParageter("graph");				
				NodeMap monomerInfoMap = (NodeMap) currentGraph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		EdgeMap edgeMap = (EdgeMap) currentGraph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		
		Graph2D copyGraph = new Graph2D();
		NodeMap copyGraphMonomerInfo = copyGraph.createNodeMap();
		copyGraph.addDataProvider(NodeMapKeys.MONOMER_REF,
				copyGraphMonomerInfo);
		EdgeMap newEdgeMap = copyGraph.createEdgeMap();
		copyGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, newEdgeMap);
		
		GraphCopier copier = SequenceGraphTools.getGraphCopier(currentGraph);		
		copier.setDataProviderContentCopying(false);
		copier.setEdgeMapCopying(false);
		copier.setNodeMapCopying(false);		
		
		HashMap<Node, Node> nodeDirtyMap = new HashMap<Node, Node>();
		
		EdgeCursor edges = currentGraph.edges();
		for (; edges.ok(); edges.next()) {
			
			EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap.get(edges.edge());
			Edge currentEdge = edges.edge();
			if (!edgeInfo.isPair()) {
				Node sourceNode = currentEdge.source();
				Node targetNode = currentEdge.target();
				
				Node newSourceNode = null;
				MonomerInfo sourceMonomerInfo = null;
				
				if ( _currentAction.nodeCondition(sourceNode) ) {
					if (nodeDirtyMap.containsKey(sourceNode)) {
						newSourceNode = nodeDirtyMap.get(sourceNode);
						sourceMonomerInfo = (MonomerInfo) copyGraphMonomerInfo
								.get(newSourceNode);
					} else {
						newSourceNode = copyGraph.createNode();
						copyGraph.setLabelText(newSourceNode, currentGraph
								.getLabelText(sourceNode));
						nodeDirtyMap.put(sourceNode, newSourceNode);
						sourceMonomerInfo = (MonomerInfo) monomerInfoMap
								.get(sourceNode);
						copyGraphMonomerInfo.set(newSourceNode, sourceMonomerInfo);
					}
				}

				Node newTargetNode = null;
				MonomerInfo targetMonomerInfo = null;
				if (_currentAction.nodeCondition(targetNode)) {
					
					if (nodeDirtyMap.containsKey(targetNode)) {
						newTargetNode = nodeDirtyMap.get(targetNode);
						targetMonomerInfo = (MonomerInfo) copyGraphMonomerInfo
								.get(newTargetNode);
					} else {
						newTargetNode = copyGraph.createNode();
						copyGraph.setLabelText(newTargetNode, currentGraph
								.getLabelText(targetNode));
						nodeDirtyMap.put(targetNode, newTargetNode);
						targetMonomerInfo = (MonomerInfo) monomerInfoMap
								.get(targetNode);
						copyGraphMonomerInfo.set(newTargetNode, targetMonomerInfo);
					}
					
				}
				
				if ( _currentAction.nodeCondition(sourceNode)
						&& _currentAction.nodeCondition(targetNode)) {
					Edge copyingGraphEdge = null;
					copyingGraphEdge = copier.getCopyFactory().copyEdge(
							copyGraph, newSourceNode, newTargetNode,
							currentEdge);
					edgeInfo = (EditorEdgeInfoData) edgeMap.get(currentEdge);
					newEdgeMap.set(copyingGraphEdge, new EditorEdgeInfoData(sourceMonomerInfo
							.getAttachment(edgeInfo.getSourceNodeAttachment()
									.getLabel()), targetMonomerInfo
							.getAttachment(edgeInfo.getTargetNodeAttachment()
									.getLabel())));
				}
			}
		}
		
		// if graph have'nt edges, but have nodes
		Node[] nodeArray = currentGraph.getNodeArray();
		for(int i = 0; i < nodeArray.length; i++){
			if (nodeArray[i].degree() == 0 && _currentAction.nodeCondition(nodeArray[i])){
				Node newNode = copyGraph.createNode();
				copyGraph.setLabelText(newNode, currentGraph.getLabelText(nodeArray[i]));
				nodeDirtyMap.put(nodeArray[i], newNode);
				MonomerInfo sourceMonomerInfo = (MonomerInfo) monomerInfoMap
						.get(nodeArray[i]);
				copyGraphMonomerInfo.set(newNode, sourceMonomerInfo);
			}
		}
				
		return copyGraph;		
	}
	
}
