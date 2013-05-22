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
package org.helm.editor.editor;

import java.io.IOException;
import java.util.HashMap;

import org.jdom.JDOMException;

import y.algo.GraphChecker;
import y.algo.GraphConnectivity;
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

import org.helm.notation.MonomerException;
import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphManagerInfo;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.notation.model.Monomer;

public class GraphSelection {

	public static GraphData getSelectedGraph(Graph2D graph) {
	
	    GraphData graphData = new GraphData();
	
	    Graph2D selectedGraph = new Graph2D();
	
	    NodeMap monomerInfoMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
	    NodeMap node2starting = (NodeMap) graph.getDataProvider(NodeMapKeys.NODE2STARTING_NODE);
	    
	    EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
	
	    NodeMap newMonomerInfoMap = selectedGraph.createNodeMap();
	
	    GraphCopier copier = SequenceGraphTools.getGraphCopier(graph);
	    MonomerInfo monomerInfo = null;
	
	    EditorEdgeInfoData edgeInfo = null;
	    EdgeCursor edges = graph.edges();
	    Node sourceNode = null;
	    Node targetNode = null;
	    Node newSourceNode = null;
	    Node newTargetNode = null;
	
	    Edge edge;
	    HashMap<Node, Node> nodeDirtyMap = new HashMap<Node, Node>();
	    MonomerInfo sourceMonomerInfo = null;
	    MonomerInfo targetMonomerInfo = null;
	
	    copier.setDataProviderContentCopying(false);
	    copier.setEdgeMapCopying(false);
	    copier.setNodeMapCopying(false);
	    Edge newEdge = null;
	
	    selectedGraph.addDataProvider(NodeMapKeys.MONOMER_REF,
	            newMonomerInfoMap);
	    EdgeMap newEdgeMap = selectedGraph.createEdgeMap();
	    selectedGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, newEdgeMap);
	
	    for (; edges.ok(); edges.next()) {
	
	        edgeInfo = (EditorEdgeInfoData) edgeMap.get(edges.edge());
	        if (!edgeInfo.isPair()) {
	            edge = edges.edge();
	            sourceNode = edge.source();
	            targetNode = edge.target();
	            if (graph.isSelected(sourceNode)) {
	                if (nodeDirtyMap.containsKey(sourceNode)) {
	                    newSourceNode = nodeDirtyMap.get(sourceNode);
	                    sourceMonomerInfo = (MonomerInfo) newMonomerInfoMap.get(newSourceNode);
	                } else {
	                    newSourceNode = selectedGraph.createNode();
	                    selectedGraph.setLabelText(newSourceNode, graph.getLabelText(sourceNode));
	                    // newSourceNode =
	                    // copier.getCopyFactory().copyNode(selectedStructure,
	                    // pNode);
	                    nodeDirtyMap.put(sourceNode, newSourceNode);
	                    sourceMonomerInfo = (MonomerInfo) monomerInfoMap.get(sourceNode);
	                    newMonomerInfoMap.set(newSourceNode, sourceMonomerInfo);
	                }
	            }
	
	            if (graph.isSelected(targetNode)) {
	                if (nodeDirtyMap.containsKey(targetNode)) {
	                    newTargetNode = nodeDirtyMap.get(targetNode);
	                    targetMonomerInfo = (MonomerInfo) newMonomerInfoMap.get(newTargetNode);
	                } else {
	                    newTargetNode = selectedGraph.createNode();
	                    selectedGraph.setLabelText(newTargetNode, graph.getLabelText(targetNode));
	                    // newTargetNode =
	                    // copier.getCopyFactory().copyNode(selectedStructure,
	                    // rNode);
	                    nodeDirtyMap.put(targetNode, newTargetNode);
	                    targetMonomerInfo = (MonomerInfo) monomerInfoMap.get(targetNode);
	                    newMonomerInfoMap.set(newTargetNode, targetMonomerInfo);
	                }
	            }
	
	            if (graph.isSelected(sourceNode)
	                    && graph.isSelected(targetNode)) {
	
	                newEdge = copier.getCopyFactory().copyEdge(
	                        selectedGraph, newSourceNode, newTargetNode,
	                        edge);
	                edgeInfo = (EditorEdgeInfoData) edgeMap.get(edge);
	                EditorEdgeInfoData newData = new EditorEdgeInfoData(sourceMonomerInfo.getAttachment(edgeInfo.getSourceNodeAttachment().getLabel()), targetMonomerInfo.getAttachment(edgeInfo.getTargetNodeAttachment().getLabel()));
	                newData.setIsPair(edgeInfo.isPair());
	                newEdgeMap.set(newEdge, newData);
	            }
	        }
	    }
	
	    // if graph have'nt edges, but have nodes
	    Node[] nodeArray = graph.getNodeArray();
	    for (int i = 0; i < nodeArray.length; i++) {
	        if (nodeArray[i].degree() == 0 && graph.isSelected(nodeArray[i])) {
	            Node newNode = selectedGraph.createNode();
	            selectedGraph.setLabelText(newNode, graph.getLabelText(nodeArray[i]));
	            nodeDirtyMap.put(nodeArray[i], newNode);
	            sourceMonomerInfo = (MonomerInfo) monomerInfoMap.get(nodeArray[i]);
	            newMonomerInfoMap.set(newNode, sourceMonomerInfo);
	        }
	    }
	
	
	    graphData.setGraph(selectedGraph);
	    graphData.setMonomerInfo(monomerInfo);
	    graphData.setNewMonomerInfoMap(newMonomerInfoMap);
	
	    return graphData;
	}

	public static GraphManagerInfo getSelectedGraphManager(GraphData graphData)
	        throws IOException, MonomerException, JDOMException {
	
	    GraphManagerInfo graphManagerInfo = new GraphManagerInfo();
	
	    GraphManager selectedGraphManager = new GraphManager();
	
	    //MonomerInfo monomerInfo = graphData.getMonomerInfo();
	    NodeMap newMonomerInfoMap = graphData.getNewMonomerInfoMap();
	    
	    Graph graph = graphData.getGraph();
	    
	    Node newNode = null;
	    Monomer monomer = null;
	    NodeList specialBases = new NodeList();
	    
	    //GraphCopier gc = new GraphCopier();
	    //Graph temp = gc.copy(graph, nodes);
	    
	    GraphHider gh = new GraphHider(graph);
	    
	    //hide pairings and chem edges
		EdgeMap edgeMap = (EdgeMap)graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
		for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
			Edge e = ec.edge();
			EdgeInfo einfo = (EdgeInfo)edgeMap.get(e);
			if (einfo.isPair()) {
				gh.hide(ec.edge());
			}
			if (MonomerInfoUtils.isChemicalModifierPolymer(e.source()) ||
				MonomerInfoUtils.isChemicalModifierPolymer(e.target())) {
				gh.hide(e);
			}
			if (MonomerInfoUtils.isPBranchEdge(e)) {
				gh.hide(e);
			}
		}
	
		for (NodeCursor nodes = graph.nodes(); nodes.ok(); nodes.next()) {
	        newNode = nodes.node();
	        MonomerInfo monomerInfo = (MonomerInfo) newMonomerInfoMap.get(nodes.node());
	        monomer = GraphUtils.getMonomerDB().get(monomerInfo.getPolymerType()).get(
	                monomerInfo.getMonomerID());
	        if (newNode.inDegree() == 0) {
	            if (monomer != null
	                    && MonomerInfoUtils.isBranchMonomer(monomer)) {
	                specialBases.add(newNode);
	            } else {
	                selectedGraphManager.addStartingNode(nodes.node());
	            }
	        } 
	             
	    }
		
		NodeList[] components = GraphConnectivity.connectedComponents(graph);
		
		for (NodeList component : components) {
			GraphCopier gc = new GraphCopier();
			Graph temp = gc.copy(graph, component.nodes());
			if (GraphChecker.isCyclic(temp)) {
				selectedGraphManager.addStartingNode(component.firstNode());
			}
		}
		
		gh.unhideAll();
		
	    graphManagerInfo.setGraphManager(selectedGraphManager);
	    graphManagerInfo.setSpecialBases(specialBases);
	
	    return graphManagerInfo;
	}

}
