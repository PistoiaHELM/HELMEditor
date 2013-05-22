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

import java.util.Map;

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.layout.LayoutGraph;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.notation.MonomerFactory;
import org.helm.editor.componentPanel.sequenceviewpanel.EdgeType;
import org.helm.editor.componentPanel.sequenceviewpanel.SViewEdgeInfo;
import org.helm.editor.data.DataException;
import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.utils.DirectionFinderImpl;
import org.helm.notation.model.Monomer;

public class MonomerInfoUtils {

    public static Node getNonPeptideNeibour(Node node) {
        for (Node n : GraphUtils.getNeighbours(node)) {
            if (!isPeptidePolymer(n)) {
                return n;
            }
        }

        return null;
    }
    
    // TODO: merge with getNonPeptideNeibour
    public static Node getNonNucleotideNeibor(Node node) {
        for (Node n : GraphUtils.getNeighbours(node)) {
            if (!isNucleicAcidPolymer(n)) {
                return n;
            }
        }

        return null;
    }
    
    public static boolean isInterPolymer(Edge e) {
    	Node s = e.source();
    	Node t = e.target();
    	
    	String sType = getPolymerType(s);
    	String tType = getPolymerType(t);
    	
    	if (sType == null) {
    		return (tType != null);
    	}
    	
    	return !sType.equals(tType); 
    }
    
    public static String getPolymerType(Node node) {
    	MonomerInfo info = getMonomerInfo(node);
    	return info != null ? info.getPolymerType() : null;
    }

    public static Node getFinalRNode(Node node) {
        if (isRMonomer(node)) {
        	return node;
        }
    	for (Node n : GraphUtils.getNeighbours(node)) {
            if (isRMonomer(n)) {
                return n;
            }
        }

        return null;
    }

    public static boolean isPeptidePolymer(Node node) {
    	MonomerInfo info = getMonomerInfo(node);
        if (info == null) {
            return false;
        }
        return info.getPolymerType().equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE);
    }
    
    public static boolean isPeptidePolymer(Monomer m) {
        
        return m.getPolymerType().equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE);
    }

    public static boolean isNucleicAcidPolymer(Node node) {
        MonomerInfo info = getMonomerInfo(node);
        if (info == null) {
            return false;
        }
        return info.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
    }

    public static boolean isChemicalModifierPolymer(Node node) {
        MonomerInfo info = getMonomerInfo(node);
        if (info == null) {
            return false;
        }
        return info.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE);
    }
    
    public static boolean isChemicalModifierPolymer(Monomer monomer) {
        if (monomer == null) {
            return false;
        }
        return monomer.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE);
    }

    public static boolean isBackbone(Node node) {
        Monomer monomer = getMonomer(node);
        return isBackbone(monomer);
    }

	public static boolean isBackbone(Monomer monomer) {
		if (monomer == null) {
            throw new DataException("Monomer info is null");
        }
        return monomer.getMonomerType().equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE);
	}

    public static Object getMonomerInfo(Node node, Object key) {
        Graph graph = node.getGraph();
        // TODO: why graph is null
        if (graph == null) {
            return null;
        }
        DataProvider map = graph.getDataProvider(key);
        return map.get(node);
    }

    public static MonomerInfo getMonomerInfo(Node node) {
        return (MonomerInfo) getMonomerInfo(node, NodeMapKeys.MONOMER_REF);
    }

    public static String getMonomerID(Node node) {
        MonomerInfo info = getMonomerInfo(node);
        return info.getMonomerID();
    }

    public static String getMonomerLabelText(Node node, Graph2D graph) {
        NodeRealizer realizer = graph.getRealizer(node);
        return realizer.getLabelText();
    }

    public static Monomer getMonomer(Node node) {
        return getMonomer(node, NodeMapKeys.MONOMER_REF);
    }

    public static Monomer getMonomer(Node node, Object key) {
        Map<String, Map<String, Monomer>> monomerDB = null;
        try {
            monomerDB = MonomerFactory.getInstance().getMonomerDB();
        } catch (Exception e) {
            throw new DataException(e);
        }
        Object monomerInfo = getMonomerInfo(node, key);
        if (monomerInfo == null) {
            return null;
        }

        if (monomerInfo instanceof String) {
            Monomer result = new Monomer();
            result.setNaturalAnalog((String) monomerInfo);
            result.setMonomerType(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
            return result;
        }

        MonomerInfo monomerInfoFinal = (MonomerInfo) monomerInfo;
        return monomerDB.get(monomerInfoFinal.getPolymerType()).get(monomerInfoFinal.getMonomerID());
    }

    public static boolean isBranchMonomer(Node node) {
        Monomer monomer = getMonomer(node);
        if (monomer == null) {
            return false;
        }
        return isBranchMonomer(monomer);
    }
    
    public static boolean isBranchMonomer(Monomer monomer) {
        return monomer.getMonomerType().equalsIgnoreCase(Monomer.BRANCH_MOMONER_TYPE);
    }
    

    public static boolean isRMonomer(Node node) {
        return isRMonomer(getMonomer(node));
    }
    
    public static boolean isRMonomer(Monomer monomer) {
        if (monomer == null) {
            return false;
        }
        if (monomer.getNaturalAnalog() == null) {
            return false;
        }
        return monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R);
    }

    public static boolean isPMonomer(Node node) {
        Monomer monomer = getMonomer(node);
        return isPMonomer(monomer);
    }

	private static boolean isPMonomer(Monomer monomer) {
		if (monomer == null) {
            return false;
        }
        return monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P);
	}

    public static boolean notModifiedMonomer(Node node) {
        Monomer monomer = getMonomer(node);
        if (monomer == null) {
            return false;
        }
        return !monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_X);
    }

    public static String getNaturalAnalog(Node node) {
        Monomer monomer = getMonomer(node);
        if (monomer == null) {
            throw new DataException("No monomer associated with node " + node.toString());
        }
        return monomer.getNaturalAnalog();
    }

    public static boolean isEndNode(Node source) {
        boolean result = false;
        try {
            result = SequenceGraphTools.isLastNucleicacidBackbone(source);
            result = result || SequenceGraphTools.isLastPeptideSequenceNode(source, source.getGraph());
            result = result || SequenceGraphTools.isFirstNucleicacidBackbone(source);
            result = result || SequenceGraphTools.isFirstPeptideSequenceNode(source, source.getGraph());
        } catch (Exception e) {
            throw new DataException(e);
        }
        return result;
    }

    public static boolean isInAntiSense(Node startingNode, Graph2D graph, GraphManager manager) {
        DirectionFinderImpl dirFinder = new DirectionFinderImpl(manager, graph);
        return dirFinder.isInAntiSence(startingNode);
    }

    public static boolean isPair(Edge edge) {
        Graph graph = edge.getGraph();
        DataProvider edgeMap = graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
        EdgeInfo data = (EdgeInfo) edgeMap.get(edge);
        if (data == null) {
            return false;
        }
        return data.isPair();
    }
    
    public static boolean isAnnotation(String text) {
        return text.contains("5'") || text.contains("n");
    }
    
    public static boolean is5Node(Node node, Graph2D graph) {
        return getAnnotation(node, graph).contains("5'");
    }
    
    public static String getAnnotation(Node node, Graph2D graph) {
        if (node == null) {
            return "";
        }
        
        NodeRealizer r = graph.getRealizer(node);
        if (r == null) {
            return "";
        }
        
        NodeLabel l = r.getLabel();
        if (l == null) {
            return "";
        }
        
        return l.getText();
    }
    
    public static boolean isPBranchEdge(Edge e) {
    	if (!isPeptidePolymer(e.source()) || !isPeptidePolymer(e.target())) {
    		return false;
    	}
    	return isBranchEdge(e);
    }
    
    public static boolean isBranchEdge(Edge e) {
    	Graph g = e.getGraph();
    	DataProvider info = g.getDataProvider(EdgeMapKeys.EDGE_INFO);
    	if (info == null) {
    		throw new IllegalArgumentException("Graph does not contain edge info");
    	}
    	EdgeInfo einfo = (EdgeInfo)info.get(e);
    	return einfo.isPBranchBackbone() || einfo.isPBranchBranch();
    }

    public static boolean isBranchToBackboneEdge(Edge e) {
    	Graph g = e.getGraph();
    	DataProvider info = g.getDataProvider(EdgeMapKeys.EDGE_INFO);
    	if (info == null) {
    		throw new IllegalArgumentException("Graph does not contain edge info");
    	}
    	EdgeInfo einfo = (EdgeInfo)info.get(e);
    	return einfo.isPBranchBackbone();
    }

    public static boolean isBranchToBranchEdge(Edge e) {
    	Graph g = e.getGraph();
    	DataProvider info = g.getDataProvider(EdgeMapKeys.EDGE_INFO);
    	if (info == null) {
    		throw new IllegalArgumentException("Graph does not contain edge info");
    	}
    	EdgeInfo einfo = (EdgeInfo)info.get(e);
    	return einfo.isPBranchBranch();
    }

    public static boolean isPInterSequencrEdge(Edge e) {
    	Graph g = e.getGraph();
    	NodeMap node2hypernode = (NodeMap)g.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
    	if (node2hypernode == null) {
    		throw new IllegalArgumentException("Graph does not contain edge info");
    	}
    	return !node2hypernode.get(e.source()).equals(node2hypernode.get(e.target())) && isPBranchEdge(e);
    }
    
    
    /**
     * test if a sequence has been flipped
     *
     * @param startingNode
     * @param graph
     * @return boolean
     */
    public static boolean isFliped(Node startingNode, Graph2D graph) {
        NodeRealizer nr1 = graph.getRealizer(startingNode);
        NodeRealizer nr2 = null;
        NodeCursor succ = startingNode.successors();
        
        if (succ.ok()) {
            nr2 = graph.getRealizer(succ.node());
        }

        if (nr2 != null && nr1.getY() == nr2.getY()) {
            if (nr2.getCenterX() < nr1.getCenterX()) {
                return true;
            } 
        } 
        
        return false;        
    }

    public static boolean matches(Node node, String id, String type) {
        MonomerInfo info = getMonomerInfo(node);
        String monomerId = getMonomerID(node);
        return (info != null) && (id != null) && type.equals(info.getPolymerType()) && (monomerId.equals(id));
    }

    public static NodeList getSequenceNodes(Node startingNode, LayoutGraph graph, boolean includeChem) {
        NodeList nodeList = new NodeList();
        Node currentNode = startingNode;
        nodeList.add(currentNode);
        NodeCursor successors = currentNode.successors();
        Edge edge = null;
        EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

        EdgeType edgeType = null;
        Node preNode = currentNode;
        
        while (successors.ok()) {
            for (; successors.ok(); successors.next()) {
                edge = currentNode.getEdge(successors.node());
                edgeType = ((SViewEdgeInfo) edgeMap.get(edge)).getType();

                boolean chemNodesCond = includeChem && (edgeType == EdgeType.CHEM);
                if ((edgeType == EdgeType.REGULAR)
                        || (edgeType == EdgeType.MODIFIED_P) || chemNodesCond) {
                    currentNode = successors.node();
                    nodeList.add(currentNode);
                    break;
                }
            }
            
            if (currentNode != preNode) {
                successors = currentNode.successors();
                preNode = currentNode;
            }
        }

        return nodeList;
    }
    
    public static NodeList getSequenceNodes(Node startingNode, LayoutGraph graph){
    	return getSequenceNodes(startingNode, graph, false);    	
    }
    
    public static NodeCursor getSequenceCursor(Node startingNode, Graph2D graph){
    	return getSequenceNodes(startingNode, graph, false).nodes();
    }
    
    public static NodeCursor getSequenceCursor(Node startingNode, Graph2D graph, boolean includeChem){
    	return getSequenceNodes(startingNode, graph, includeChem).nodes();    	
    }
    
    public static boolean isChainHaveChemMod(Graph2D graph){
    	
    	for (Node node : graph.getNodeArray()){
    		if (node.toString().toLowerCase().equals("peg2")){
    			return true;
    		}
    	}
    	    	    
    	return false;
    } 

    public static NodeList getSequenceNodes(Node startingNode, Node end, LayoutGraph graph) {
        NodeList nodeList = new NodeList();
        Node currentNode = startingNode;
        nodeList.add(currentNode);
        NodeCursor successors = currentNode.successors();
        Edge edge = null;
        EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

        Node preNode = currentNode;

        EdgeType edgeType = null;
        while (successors.ok()) {
            for (; successors.ok(); successors.next()) {
                edge = currentNode.getEdge(successors.node());
                edgeType = ((SViewEdgeInfo) edgeMap.get(edge)).getType();
                if ((edgeType != EdgeType.PAIR)
                        && (edgeType != EdgeType.CHEM)) {
                    currentNode = successors.node();
                    nodeList.add(currentNode);
                    break;
                }
            }
            if (currentNode == end) {
                break;
            }
            if (currentNode != preNode) {
                successors = currentNode.successors();
                preNode = currentNode;
            }
        }

        return nodeList;
    }
    
} 

