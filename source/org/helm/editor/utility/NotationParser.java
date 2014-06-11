/**
 * *****************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ****************************************************************************
 */
package org.helm.editor.utility;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphPair;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.monomerui.SimpleElemetFactory;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.ComplexPolymer;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.NucleotideSequenceParser;
import org.helm.notation.tools.SimpleNotationParser;
import org.jdom.JDOMException;

import y.algo.Cycles;
import y.algo.GraphChecker;
import y.base.Edge;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

/**
 * For translate a string notation to a graph
 *
 * @author lih25
 */
public class NotationParser {

    public static final String TOP_LEVEL_DELIMITER_REGEX = "\\$";
    public static final String LIST_LEVEL_DELIMITER_REGEX = "\\|";
    public static final String NODE_LABEL_START_SYMBOL = "{";
    public static final String NODE_LABEL_END_SYMBOL = "}";
    public static final String UNIT_SEPERATION_SYMBOL = "\\.";
    public static final String MONOMER_MODIFICATION_START_SYMBOL = "[";
    public static final String MONOMER_MODIFICATION_END_SYMBOL = "]";
    public static final String BASE_MONOMER_START_SYMBOL = "(";
    public static final String BASE_MONOMER_END_SYMBOL = ")";
    public static final String EDGE_COMPONENT_DELIMITER_REGEX = ",";
    public static final String EDGE_PROPERTY_SEPARATOR_SYMBOL = "-";
    public static final String MONOMER_ATTACHEMENT_SEPARATOR_SYMBOL = ":";

    /**
     * setup the hyper edges and connections between different sequences
     *
     * @param connectionNotation string notation
     * @param isPair if this describes the pairing edges
     * @param graph the original graph, this is also the graph we render
     * @param hyperGraph a hyper graph
     * @param hyperNodeNameMap Map <name, hyperNode>
     * @param nodeCursorMap Map <sequence name, sequence graph>
     * @throws org.helm.notation.NotationException
     */
    private static void getConnection(String connectionNotation, boolean isPair, Graph2D graph, Map<String, NodeCursor> nodeCursorMap) throws NotationException {
        if (connectionNotation == null || connectionNotation.equalsIgnoreCase("")) {
            return;
        }

        Node sourceNode = null;
        Node targetNode = null;

        MonomerInfo sourceMonomerInfo = null;
        MonomerInfo targetMonomerInfo = null;
        Attachment sourceAttachment = null;
        Attachment targetAttachment = null;

        String[] edgeString = connectionNotation.split(LIST_LEVEL_DELIMITER_REGEX);

        NodeMap monomerPositionNodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_POSITION);
        NodeMap monomerInfoNodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        EdgeMap connectionEdgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

        for (int i = 0; i < edgeString.length; i++) {
            //there are only three element in the edgeDesc, the source node, target node and the connection distribtion
            String[] edgeDesc = edgeString[i].split(EDGE_COMPONENT_DELIMITER_REGEX);

            //connectionDesc normally has two elements, the source attachment description and the target attachment description
            // for some special cases (chemical monomer without struccture) it contains only one element
            String[] connectionDesc = edgeDesc[2].split(EDGE_PROPERTY_SEPARATOR_SYMBOL);//"-"

            int position = Integer.valueOf(connectionDesc[0].substring(0, connectionDesc[0].indexOf(MONOMER_ATTACHEMENT_SEPARATOR_SYMBOL)));

            NodeCursor nodeCursor = nodeCursorMap.get(edgeDesc[0]);
            nodeCursor.toFirst();
            for (; nodeCursor.ok(); nodeCursor.next()) {
                if (monomerPositionNodeMap.getInt(nodeCursor.node()) == position) {
                    sourceNode = nodeCursor.node();
                    sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(sourceNode);

                    sourceAttachment = sourceMonomerInfo.getAttachment(
                            connectionDesc[0].substring(connectionDesc[0].indexOf(MONOMER_ATTACHEMENT_SEPARATOR_SYMBOL) + 1));
                    break;
                }
            }

            position = Integer.valueOf(connectionDesc[1].substring(0, connectionDesc[1].indexOf(MONOMER_ATTACHEMENT_SEPARATOR_SYMBOL)));
            nodeCursor = nodeCursorMap.get(edgeDesc[1]);
            nodeCursor.toFirst();

            for (; nodeCursor.ok(); nodeCursor.next()) {
                if (monomerPositionNodeMap.getInt(nodeCursor.node()) == position) {
                    targetNode = nodeCursor.node();
                    targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(targetNode);
                    targetAttachment = targetMonomerInfo.getAttachment(
                            connectionDesc[1].substring(connectionDesc[1].indexOf(MONOMER_ATTACHEMENT_SEPARATOR_SYMBOL) + 1));
                    break;
                }
            }


            if (sourceAttachment != null && !sourceMonomerInfo.isConnected(sourceAttachment) && targetAttachment != null && !targetMonomerInfo.isConnected(targetAttachment)) {
                //pairing node
                if (isPair) {
                    if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(targetMonomerInfo.getPolymerType())) {
                        if (sourceAttachment.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)
                                && targetAttachment.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)) {
                            sourceMonomerInfo.setConnection(sourceAttachment, true);
                            targetMonomerInfo.setConnection(targetAttachment, true);

                            Edge edge = graph.createEdge(sourceNode, targetNode);
                            EditorEdgeInfoData edgeInfo = new EditorEdgeInfoData(sourceAttachment, targetAttachment);
                            connectionEdgeMap.set(edge, edgeInfo);
                        } else {
                            throw new NotationException("two nodes of the same type cannot be connected");
                        }

                    }
                } else {
                    if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)) {
                        //chemical structure nodes are always target nodes
                        sourceMonomerInfo.setConnection(sourceAttachment, true);
                        targetMonomerInfo.setConnection(targetAttachment, true);

                        Edge edge = graph.createEdge(targetNode, sourceNode);
                        EditorEdgeInfoData edgeInfo = new EditorEdgeInfoData(targetAttachment, sourceAttachment);
                        connectionEdgeMap.set(edge, edgeInfo);
                    } else {
                        sourceMonomerInfo.setConnection(sourceAttachment, true);
                        targetMonomerInfo.setConnection(targetAttachment, true);

                        Edge edge = graph.createEdge(sourceNode, targetNode);
                        connectionEdgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));

                        //Check to see if this connection is "backwards" meaning that it created an undirected cycle
                        //     The edge needs to be in the right direction so downstream functionality will work
                        boolean isCyclic = GraphChecker.isCyclic(graph);
                        EdgeList edgeList = Cycles.findAllCycleEdges(graph, false);

                        if (edgeList.isEmpty() == false && isCyclic == false) {
                            //reverse the direction of the edge
                            graph.removeEdge(edge);

                            edge = graph.createEdge(targetNode, sourceNode);
                            connectionEdgeMap.set(edge, new EditorEdgeInfoData(targetAttachment, sourceAttachment));
                        }
                    }
                }
            }

            sourceNode = null;
            targetNode = null;
            sourceMonomerInfo = null;
            targetMonomerInfo = null;
            sourceAttachment = null;
            targetAttachment = null;
        }
    }
    
    public static String transferDynamicChemicalModifiersToMonomers(String notation) throws NotationException, MonomerException, JDOMException, IOException {
    	return transferDynamicChemicalModifiersToMonomers(notation, MonomerStoreCache.getInstance().getCombinedMonomerStore());
    }

    // TY
    public static String transferDynamicChemicalModifiersToMonomers(String notation, MonomerStore monomerStore) throws NotationException, MonomerException, JDOMException, IOException {
    	
    
		notation=ComplexNotationParser.getNotationByReplacingSmiles(notation,monomerStore);
		
       /* int p = notation.indexOf("{");
        while (p > 0) {
            int p2 = notation.indexOf("}", p + 1);
            if (p2 < p) {
                break;
            }

            String smiles = notation.substring(p + 1, p2);
            if (smiles.indexOf("$") > 0) {
                Monomer monomer = org.helm.editor.editor.StructureFrame.getMonomerBySmiles(smiles);
                notation = notation.substring(0, p + 1) + monomer.getAlternateId() + notation.substring(p2);
            }
            p = notation.indexOf("{", p + 1);
        }*/
		
		
		//Match and remove smiles code inside annotation part coming from editor.getSelectedNotation() XHELM-28/XHELM-76
		Pattern p=Pattern.compile("\\$\\$(.+\\$.+)\\$$");
		Matcher m=p.matcher(notation);
		while(m.find()){
		    //System.out.println(m.group(1));
		    notation=notation.replace(m.group(1), "");
		}


  
    	
        return notation;
    }

    public static GraphPair getGraphPair(String notation)
            throws NotationException, MonomerException, IOException, JDOMException, StructureException {
    	return getGraphPair(notation, MonomerStoreCache.getInstance().getCombinedMonomerStore());
    }
    
    /**
     * translate string notation to an (Graph2D, GraphManager) pair
     *
     * @param notation
     * @return a (graph, graphmanager) pair
     * @throws org.helm.notation.NotationException
     */
    public static GraphPair getGraphPair(String notation, MonomerStore monomerStore)
            throws NotationException, MonomerException, IOException, JDOMException, StructureException {

        //validate notation input
        ComplexPolymer cp = ComplexNotationParser.parse(notation, monomerStore);


        // TY
        notation = transferDynamicChemicalModifiersToMonomers(notation, monomerStore);


        ArrayList<String> notationComponentsList = new ArrayList<String>();
        int count = 0;
        int start = 0;
        int end = notation.indexOf("$");

        //populate the list
        while (end != -1) {
            if (start == end) {
                notationComponentsList.add("");
            } else {
                notationComponentsList.add(notation.substring(start, end));
            }
            start = end + 1;
            end = notation.indexOf("$", start);
        }

        //if the list is empty, just return a graph pair with a empty graph and a graph manager        
        // Map<String, Node> hyperNodeNameMap = new HashMap<String, Node>();

        Graph2D graph = new Graph2D();
        GraphManager graphManager = new GraphManager();

        NodeMap monomerInfoNodeMap = graph.createNodeMap();
        NodeMap monomerPositionNodeMap = graph.createNodeMap();

        EdgeMap connectionEdgeMap = graph.createEdgeMap();

        graph.addDataProvider(NodeMapKeys.MONOMER_REF, monomerInfoNodeMap);
        graph.addDataProvider(NodeMapKeys.MONOMER_POSITION, monomerPositionNodeMap);
        graph.addDataProvider(EdgeMapKeys.EDGE_INFO, connectionEdgeMap);
        graph.addDataProvider(NodeMapKeys.NODE2PAIR_NODE, graph.createNodeMap());
        graph.addDataProvider(NodeMapKeys.NODE2STARTING_NODE, graph.createNodeMap());


        //set up the copier
        GraphCopier copier = new GraphCopier(graph.getGraphCopyFactory());

        copier.setDataProviderContentCopying(true);
        copier.setEdgeMapCopying(true);
        copier.setNodeMapCopying(true);

        // all polymers in this notation, <polymerName(RNA1), polymer Graph>
//        String allNodeString = ComplexNotationParser.getAllNodeString(notation);
        List<PolymerNode> polymerNodes = getPolymerList(notation,monomerStore);

        //all nodes belongs to a certain sequence <sequnce name, sequence graph>
        Map<String, NodeCursor> nodeCursorMap = new HashMap<String, NodeCursor>();
        NodeCursor nodeCursor = null;

        //   Node hyperNode = null;

        String polymers = notation.substring(0, notation.indexOf("$"));

        int startIndex = -1;

        Graph2D subgraph = null;
        String polyID = null;
        String polyLable = null;
        String annotation = null;
        for (PolymerNode polyNode : polymerNodes) {
            //add a sequence to the graph
            polyID = polyNode.getId();
            polyLable = polyNode.getLabel();
            annotation = polyNode.getAnotation();
            if (polyID.contains(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                subgraph = loadNucleicAcidMonomerSequence(polyLable);
            } else if (polyID.contains(Monomer.PEPTIDE_POLYMER_TYPE)) {
                subgraph = loadAminoAcidGraph(polyLable);
            } else if (polyID.contains(Monomer.CHEMICAL_POLYMER_TYPE)) {
                subgraph = loadChemicalStructGraph(polyLable);

            }
            nodeCursor = copier.copy(subgraph, graph).nodes();
            nodeCursor.toFirst();

            nodeCursorMap.put(polyID, nodeCursor);

            //hyperNodeNameMap.put(polyID, hyperNode);

            //extract the polymer notation
            startIndex = polymers.indexOf(polyID) + 1;
            startIndex = polymers.indexOf(NODE_LABEL_START_SYMBOL, startIndex) + 1;
            graphManager.addStartingNode(nodeCursor.node());

            if (annotation != null && !annotation.equalsIgnoreCase("")) {
                graphManager.annotate(nodeCursor.node(), annotation);
            }

            subgraph = null;
            polyID = null;
            polyLable = null;
            annotation = null;
        }

        //---------- set up the regular polymer connections ----------------------
        getConnection(notationComponentsList.get(1), false, graph, nodeCursorMap);

        //---------- set up the pairing polymer connections ----------------------
        getConnection(notationComponentsList.get(2), true, graph, nodeCursorMap);

        return new GraphPair(graph, graphManager);
    }

    /**
     *
     * @param monomer the name of the monomer, for modified monomer it will be
     * in the format [name]
     * @return the name with [] stripped
     */
    private static String getMonomerName(String monomerName) {

        if (monomerName.indexOf(MONOMER_MODIFICATION_START_SYMBOL) != -1) {
            String name = monomerName;
            name = name.replace(MONOMER_MODIFICATION_START_SYMBOL, "");
            name = name.replace(MONOMER_MODIFICATION_END_SYMBOL, "");
            return name;
        }

        return monomerName;
    }

    /**
     * Transfer a polymer notation nucleic acid sequence into a graph
     *
     * @param polymerNotation - simplePolymerNotation of format
     * R(A)P.R(G)P.[mR](A)P.R([mA]).P
     * @param withStarting - if we should have a 5' node
     * @return rna graph
     */
    public static Graph2D loadNucleicAcidMonomerSequence(String polymerNotation, boolean withStarting) throws MonomerException, IOException, JDOMException, NotationException, StructureException {

    	MonomerStore monomerStore= MonomerStoreCache.getInstance().getCombinedMonomerStore();
        Graph2D sequenceGraph = new Graph2D();

        NodeMap monomerInfoNodeMap = sequenceGraph.createNodeMap();
        NodeMap monomerPositionNodeMap = sequenceGraph.createNodeMap();
        EdgeMap connectionEdgeMap = sequenceGraph.createEdgeMap();

        sequenceGraph.addDataProvider(NodeMapKeys.MONOMER_REF, monomerInfoNodeMap);
        sequenceGraph.addDataProvider(NodeMapKeys.MONOMER_POSITION, monomerPositionNodeMap);
        sequenceGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, connectionEdgeMap);

        GraphCopier copier = SequenceGraphTools.getGraphCopier(sequenceGraph);

        List<Nucleotide> nucList = SimpleNotationParser.getNucleotideList(polymerNotation,monomerStore);

        Node pNode = null;
        Node baseNode = null;
        Node rNode = null;
        Node preNode = null;
        Node targetNode = null;
        String rName = null;
        String pName = null;
        String baseName = null;
        MonomerInfo sourceMonomerInfo = null;
        MonomerInfo targetMonomerInfo = null;

        Attachment sourceAttachment = null;
        Attachment targetAttachment = null;
        Edge edge = null;

        int position = 1;  //monomer position tracker

        //if there are more than one nuclitide unit in the polymer notation, create a starting node 
        Node firstRNode = null; //the R node of the sequence, if the sequence start with a p node, then firstRNode will be null;
        for (int i = 0; i < nucList.size(); i++) {
            Nucleotide nuc = nucList.get(i);

            //nucleotide contains one monomer, suager and phosphate are the same
            //This is a bug that in the Nucleotide class in the toolkit, it returns both sugar monomer and phosphate monomer, if there is only one monomer
            if (null != nuc.getSugarMonomer(monomerStore) && null != nuc.getPhosphateMonomer(monomerStore)
                    && nuc.getSugarMonomer(monomerStore).getAlternateId().equals(nuc.getPhosphateMonomer(monomerStore).getAlternateId())) {
                //first nucleotide is P
                //las nucleotide is R
                if (i == 0) {
                    pName = nuc.getPhosphateMonomer(monomerStore).getAlternateId();
                    pNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, pName), sequenceGraph).firstNode();
//                    pNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(pName, Monomer.ID_P), sequenceGraph).firstNode();
                } else if (i == nucList.size() - 1) {
                    rName = nuc.getSugarMonomer(monomerStore).getAlternateId();
                    rNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, rName), sequenceGraph).firstNode();
//                    rNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(rName, Monomer.ID_R), sequenceGraph).firstNode();
                }
            } else {

                //parse each unit to get the name for p, r and base

                if (nuc.getSugarMonomer(monomerStore) != null) {
                    rName = nuc.getSugarMonomer(monomerStore).getAlternateId();
                    rNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, rName), sequenceGraph).firstNode();
//                    rNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(rName, Monomer.ID_R), sequenceGraph).firstNode();
                    if (null == firstRNode) {
                        firstRNode = rNode;
                    }

                    //dzhelezov: fix for sP starting node, notation toolkit identifies it as sugar monomer 
//                    if (rName.contains("P")) {
//                        pName = rName;
//                        pNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(rName, Monomer.ID_P), sequenceGraph).firstNode();
//
//                    } else {
//                        rNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(rName, Monomer.ID_R), sequenceGraph).firstNode();
//                        if (null == firstRNode) {
//                            firstRNode = rNode;
//                        }
//                    }
                }

                if (nuc.getBaseMonomer(monomerStore) != null) {
                    baseName = nuc.getBaseMonomer(monomerStore).getAlternateId();
                    baseNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, baseName), sequenceGraph).firstNode();
//                    baseNode = copier.copy(NodeFactory.createNucleicAcidBaseNode(baseName), sequenceGraph).firstNode();
                }

                if (nuc.getPhosphateMonomer(monomerStore) != null) {
                    pName = nuc.getPhosphateMonomer(monomerStore).getAlternateId();
                    pNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, pName), sequenceGraph).firstNode();
//                    pNode = copier.copy(NodeFactory.createNucleicAcidBackboneNode(pName, Monomer.ID_P), sequenceGraph).firstNode();
                }
            }

            rName = null;
            pName = null;
            baseName = null;

            //update the position info
            if (rNode != null) {
                monomerPositionNodeMap.setInt(rNode, position++);
            }

            if (baseNode != null) {
                monomerPositionNodeMap.setInt(baseNode, position++);
            }

            if (pNode != null) {
                monomerPositionNodeMap.setInt(pNode, position++);
            }
            //make connections -----------------------------
            //make connections r->base
            if (rNode != null && baseNode != null) {

                sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(rNode);
                targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(baseNode);
                sourceAttachment = sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
                targetAttachment = targetMonomerInfo.getAttachment(Attachment.BRANCH_MONOMER_ATTACHEMENT);
                if (!sourceMonomerInfo.isConnected(sourceAttachment) && !targetMonomerInfo.isConnected(targetAttachment)) {
                    sourceMonomerInfo.setConnection(sourceAttachment, true);
                    targetMonomerInfo.setConnection(targetAttachment, true);
                    edge = sequenceGraph.createEdge(rNode, baseNode);
                    connectionEdgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
                }

            }

            //make connections r->p
            if (rNode != null && pNode != null) {
                sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(rNode);
                targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(pNode);

                sourceAttachment = sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);

                targetAttachment = targetMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);

                if (!sourceMonomerInfo.isConnected(sourceAttachment) && !targetMonomerInfo.isConnected(targetAttachment)) {
                    edge = sequenceGraph.createEdge(rNode, pNode);
                    sourceMonomerInfo.setConnection(sourceAttachment, true);
                    targetMonomerInfo.setConnection(targetAttachment, true);

                    connectionEdgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
                }

            }

            //connecting this current unit with the previous one
            if (preNode != null) {
                sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(preNode);
                if (rNode != null) {
                    targetNode = rNode;
                    targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(rNode);
                } else if (pNode != null) {
                    targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(pNode);
                    targetNode = pNode;
                }

                sourceAttachment = sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
                targetAttachment = targetMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);

                if (!sourceMonomerInfo.isConnected(sourceAttachment) && !targetMonomerInfo.isConnected(targetAttachment)) {
                    sourceMonomerInfo.setConnection(sourceAttachment, true);
                    targetMonomerInfo.setConnection(targetAttachment, true);

                    edge = sequenceGraph.createEdge(preNode, targetNode);
                    connectionEdgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));

                }

            }

            preNode = pNode; //update the connection node to the next nucleic acid unit
            if (firstRNode != null && withStarting) {
                NodeRealizer rRealizer = sequenceGraph.getRealizer(firstRNode);
                NodeLabel anotationLabel = null;
                if (rRealizer.labelCount() >= 2) {
                    anotationLabel = rRealizer.getLabel(1);
                } else {
                    anotationLabel = rRealizer.createNodeLabel();
                    rRealizer.addLabel(anotationLabel);
                }
                anotationLabel.setModel(NodeLabel.EIGHT_POS);
                anotationLabel.setPosition(NodeLabel.NW);
                anotationLabel.setText("5'");
                anotationLabel.setBackgroundColor(Color.YELLOW);

            }

            pNode = null;
            rNode = null;
            baseNode = null;
        }

        return sequenceGraph;

    }

    public static Graph2D loadNucleicAcidMonomerSequence(String polymerNotation) throws MonomerException, JDOMException, NotationException, IOException, StructureException {
        return loadNucleicAcidMonomerSequence(polymerNotation, true);
    }

    /**
     * get the sequence graph from notation like "ACCGU"
     *
     * @param sequenceNotation
     * @return a graph that represent this notation
     * @throws org.helm.notation.MonomerException
     * @throws java.io.IOException
     * @throws org.jdom.JDOMException
     */
    private static Graph2D loadAminoAcidGraph(String sequenceNotation) throws MonomerException, IOException, JDOMException {

        /*MonomerFactory monomerFactory = MonomerFactory.getInstance();
        Map<String, Map<String, Monomer>> monomerDB = monomerFactory.getMonomerDB();*/

        //setting up the sequence graph
        Graph2D sequenceGraph = new Graph2D();
        sequenceGraph.addDataProvider(NodeMapKeys.MONOMER_REF, sequenceGraph.createNodeMap());
        sequenceGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, sequenceGraph.createEdgeMap());
        sequenceGraph.addDataProvider(NodeMapKeys.MONOMER_POSITION, sequenceGraph.createNodeMap());

        //initializing the copier and set the right behavior
        GraphCopier copier = new GraphCopier(sequenceGraph.getGraphCopyFactory());

        copier.setDataProviderContentCopying(true);
        copier.setEdgeMapCopying(true);
        copier.setNodeMapCopying(true);


        String[] ncArray = sequenceNotation.split(UNIT_SEPERATION_SYMBOL);

        int distance = 120;
        int centerX = 50;
        int centerY = 50;
        Node sourceNode = null;
        Node targetNode = null;
        Attachment sourceAttachment = null;
        Attachment targetAttachment = null;

        MonomerInfo sourceMonomerInfo = null;
        MonomerInfo targetMonomerInfo = null;
        Edge edge = null;

        NodeMap nodeMap = (NodeMap) sequenceGraph.getDataProvider(NodeMapKeys.MONOMER_REF);
        NodeMap positionMap = (NodeMap) sequenceGraph.getDataProvider(NodeMapKeys.MONOMER_POSITION);
        EdgeMap edgeMap = (EdgeMap) sequenceGraph.getDataProvider(EdgeMapKeys.EDGE_INFO);

        NodeRealizer nodeRealizer = null;

        int position = 1;
        if (ncArray.length > 0) {
            for (int i = 0; i < ncArray.length; i++) {
                sourceNode = sequenceGraph.lastNode();
                if (ncArray[i].contains(MONOMER_MODIFICATION_START_SYMBOL)) {
                    ncArray[i] = ncArray[i].substring(ncArray[i].indexOf(MONOMER_MODIFICATION_START_SYMBOL) + 1, ncArray[i].indexOf(MONOMER_MODIFICATION_END_SYMBOL));
                }

                targetNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.PEPTIDE_POLYMER_TYPE, ncArray[i]), sequenceGraph).lastNode();
                positionMap.setInt(targetNode, position++);
                nodeRealizer = sequenceGraph.getRealizer(targetNode);
                nodeRealizer.setCenter(centerX + distance * i, centerY);
                if (sourceNode != null && targetNode != null) {
                    sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
                    targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);
                    sourceAttachment = sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
                    targetAttachment = targetMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);

//                        if (!sourceAttachment.isConnected() && !targetAttachment.isConnected()) {
                    if (!sourceMonomerInfo.isConnected(sourceAttachment) && !targetMonomerInfo.isConnected(targetAttachment)) {
                        edge = sequenceGraph.createEdge(sourceNode, targetNode);
                        edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
                        sourceMonomerInfo.setConnection(sourceAttachment, true);
                        targetMonomerInfo.setConnection(targetAttachment, true);
                    }
                }
            }
        }

        return sequenceGraph;



    }

    private static Graph2D loadChemicalStructGraph(String chemID) throws MonomerException, IOException, JDOMException {
        Graph2D chemGraph = SimpleElemetFactory.getInstance().createMonomerNode(Monomer.CHEMICAL_POLYMER_TYPE, chemID);
        chemGraph.addDataProvider(NodeMapKeys.MONOMER_REF, chemGraph.createNodeMap());
        chemGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, chemGraph.createEdgeMap());
        NodeMap nodeMap = chemGraph.createNodeMap();
        chemGraph.addDataProvider(NodeMapKeys.MONOMER_POSITION, nodeMap);
        nodeMap.setInt(chemGraph.firstNode(), 1);

        return chemGraph;
    }

    /**
     * parse an extended polymner notation to a list of simple sequence
     * notations
     *
     * @param complexNotation
     * @return a list of simple notations that contains every polymer
     */
    public static ArrayList<String> getAllSimplePolymerNotation(String complexNotation) throws NotationException, StructureException, MonomerException, JDOMException, IOException {


        List<PolymerNode> polymerNodeList = ComplexNotationParser.getPolymerNodeList(ComplexNotationParser.getAllNodeString(complexNotation));
//               polymerNodeList = ComplexNotationParser.getPolymerNodeList(ComplexNotationParser.getAllNodeString(complexNotation));
        ArrayList<String> simpleSequenceNotationList = new ArrayList<String>(polymerNodeList.size());

        for (PolymerNode polymerNode : polymerNodeList) {
            if (polymerNode.getType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)) {
                simpleSequenceNotationList.add(polymerNode.getLabel());
            } else if (polymerNode.getType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                simpleSequenceNotationList.add(getSimpleNucleotideSequenceNotation(polymerNode.getLabel()));
            } else if (polymerNode.getType().equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
                simpleSequenceNotationList.add(getSimplePeptideSequenceNotation(polymerNode.getLabel()));
            } else {
                simpleSequenceNotationList.add(polymerNode.getLabel());
            }
        }
        return simpleSequenceNotationList;



    }

    /**
     * translate a nucleotide sequence's polymer notation like "R(A)P.R(C)P" to
     * a simple sequence notation as "AC"
     *
     * @param simplePolymerNotation
     * @return simple sequence notation
     */
    public static String getSimpleNucleotideSequenceNotation(String simplePolymerNotation) {
        StringBuilder sb = new StringBuilder();

        String[] nucleotides = simplePolymerNotation.split("\\.");
        int start = 0;
        int end = 0;
        for (int i = 0; i < nucleotides.length; i++) {
            if (nucleotides[i].contains("(")) { //has base
                start = nucleotides[i].indexOf("(");
                end = nucleotides[i].indexOf(")");
                if (nucleotides[i].contains("[")) {
                    if (start < nucleotides[i].indexOf("[", start) && end > nucleotides[i].indexOf("]", start)) {
                        start = start + 1;
                        end = end - 1;
                    }
                }

                sb.append(nucleotides[i].subSequence(start + 1, end));

            } else {//no base
                if (nucleotides[i].indexOf("[") > 0) { //has both P node and R node, the P node has been modified
                    sb.append("X");
                } else if (nucleotides[i].indexOf("[") == 0) { //has R node and R has been modified
                    if (nucleotides[i].indexOf("]") < nucleotides[i].length() - 1) {//also has a P node
                        sb.append("X");
                    }
                }

            }
        }
        return sb.toString();
    }

    /**
     * translate a peptide sequence's polymer notation like "A.A.A.A" to a
     * simple sequence notation as "AAAA"
     *
     * @param simplePolymerNotation
     * @return simple sequence notation
     */
    public static String getSimplePeptideSequenceNotation(final String simplePolymerNotation) {
        String simpleNotation = simplePolymerNotation.replace("\\.", ""); //remove all seperators
        //remove modification charactors
        simpleNotation = simplePolymerNotation.replace("[", "");
        simpleNotation = simplePolymerNotation.replace("]", "");
        return simpleNotation;
    }

    /**
     * breaks a complex notation string into polymer list, the polymer could be
     * a nucleotide sequence, a peptide sequence or a chemical linker
     *
     * @param complexNotation : the string notation for the whole structure
     * @return a list of polymers that is in this structure
     * @throws org.helm.notation.NotationException
     */
    public static List<PolymerNode> getPolymerList(String complexNotation,MonomerStore monomerStore) throws NotationException, StructureException, MonomerException, JDOMException, IOException {
        List<PolymerNode> list = ComplexNotationParser.getPolymerNodeList(ComplexNotationParser.getAllNodeString(complexNotation),monomerStore);
        String anotationString = ComplexNotationParser.getAllNodeLabelString(complexNotation);
        if (anotationString != null && !anotationString.equalsIgnoreCase("")) {
            String[] anotations = anotationString.split(ComplexNotationParser.LIST_LEVEL_DELIMITER_REGEX);
            int index = 0;
            String nodeID = null;
            String anotation = null;
            //<NodeID, anotation> map, where nodeID is RNA1, CHEM1, RNA2 etc.
            HashMap<String, String> anotationMap = new HashMap<String, String>();
            for (int i = 0; i < anotations.length; i++) {
                index = anotations[i].indexOf(ComplexNotationParser.NODE_LABEL_START_SYMBOL);
                nodeID = anotations[i].substring(0, index);
                anotation = anotations[i].substring(index + 1, anotations[i].indexOf(ComplexNotationParser.NODE_LABEL_END_SYMBOL));
                anotationMap.put(nodeID, anotation);
            }

            for (PolymerNode node : list) {
                node.setAnotation(anotationMap.get(node.getId()));
            }
        }
        return list;
    }

    /**
     * generate a standard notation for a given nucleotide sequence such as
     * "AAAA"
     *
     * @param sequence
     * @return standard complicate notation
     * @throws org.helm.notation.NotationException
     * @throws org.jdom.JDOMException
     * @throws java.io.IOException
     */
    public static String getNucleotideSequenceNotation(String sequence) throws NotationException, JDOMException, IOException {

        //translate the regular string notation like "AAAA" to a polymer notation "R(A)P.R(A)P.R(A)P.R(A)P"
        ArrayList<Nucleotide> nucleotideList = (ArrayList<Nucleotide>) NucleotideSequenceParser.getNormalList(sequence);
//            Map<String, Map<String, String>> nucleotideTemplate = NucleotideFactory.getInstance().getNucleotideTemplates();
        StringBuilder polymerNotation = new StringBuilder();
        polymerNotation.append(Monomer.NUCLIEC_ACID_POLYMER_TYPE);
        polymerNotation.append("1{");
//            String template = nucleotideTemplate.keySet().iterator().next();
        for (int i = 0; i < nucleotideList.size(); i++) {
            if (i > 0) {
                polymerNotation.append(".");
            }
            polymerNotation.append(nucleotideList.get(i).getNotation());
        }
        polymerNotation.append("}$$$$");
        return polymerNotation.toString();


    }

    public static Graph2D createNucleotideGraph(String nucleotideNotation) throws MonomerException, IOException, JDOMException {

        //a graph is used in here because we need to associate data with every nodes
        Graph2D graph = new Graph2D();
        NodeMap nodePropertiesNodeMap = graph.createNodeMap();
        graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodePropertiesNodeMap);

        EdgeMap edgeMap = graph.createEdgeMap();
        graph.addDataProvider(EdgeMapKeys.EDGE_INFO, edgeMap);

        GraphCopier copier = new GraphCopier(graph.getGraphCopyFactory());
        copier.setDataProviderContentCopying(true);
        copier.setEdgeMapCopying(true);
        copier.setNodeMapCopying(true);

        Nucleotide nuc = new Nucleotide();
        nuc.setNotation(nucleotideNotation);

        Monomer sMonomer = nuc.getSugarMonomer();
        Monomer pMonomer = nuc.getPhosphateMonomer();
        Monomer bMonomer = nuc.getBaseMonomer(MonomerStoreCache.getInstance().getCombinedMonomerStore());

        Node sNode = null;
        Node pNode = null;
        Node bNode = null;

        MonomerInfo sMonomerInfo = null;
        MonomerInfo pMonomerInfo = null;
        MonomerInfo bMonomerInfo = null;

        if (null != sMonomer) {
            sNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, sMonomer.getAlternateId()), graph).firstNode();
            sMonomerInfo = (MonomerInfo) nodePropertiesNodeMap.get(sNode);
        }

        if (null != pMonomer) {
            pNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, pMonomer.getAlternateId()), graph).firstNode();
            pMonomerInfo = (MonomerInfo) nodePropertiesNodeMap.get(pNode);
        }

        if (null != bMonomer) {
            bNode = copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE, bMonomer.getAlternateId()), graph).firstNode();
            bMonomerInfo = (MonomerInfo) nodePropertiesNodeMap.get(bNode);
        }

        //s-p edge
        if (null != sNode && null != pNode) {
            Edge edge = graph.createEdge(sNode, pNode);
            Attachment sourceAttachment = sMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
            Attachment targetAttachment = pMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
            sMonomerInfo.setConnection(sourceAttachment, true);
            pMonomerInfo.setConnection(targetAttachment, true);
            edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
        }

        //s-b edge
        if (null != sNode && null != bNode) {
            Edge edge = graph.createEdge(sNode, bNode);
            Attachment sourceAttachment = sMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
            Attachment targetAttachment = bMonomerInfo.getAttachment(Attachment.BRANCH_MONOMER_ATTACHEMENT);
            sMonomerInfo.setConnection(sourceAttachment, true);
            pMonomerInfo.setConnection(targetAttachment, true);
            edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
        }

        return graph;
    }

    public static String removeChemMonomerBracket(String complexNotation) {
        if (null == complexNotation || complexNotation.isEmpty()) {
            return complexNotation;
        }

        try {
            if (ComplexNotationParser.validateNotationFormat(complexNotation)) {
                String allNodeString = ComplexNotationParser.getAllNodeString(complexNotation);
                String[] tokens = allNodeString.split("}\\|");
                StringBuilder sb = new StringBuilder();

                if (tokens.length > 1) {
                    for (int i = 0; i < tokens.length; i++) {
                        String token = tokens[i];
                        if (token.startsWith(Monomer.CHEMICAL_POLYMER_TYPE)) {
                            token = removeBracketFromLabel(token);
                        }

                        if (sb.length() > 0) {
                            sb.append("}|");
                        }
                        sb.append(token);
                    }

                } else {
                    String token = tokens[0];
                    if (token.startsWith(Monomer.CHEMICAL_POLYMER_TYPE)) {
                        token = removeBracketFromLabel(token);
                    }
                    sb.append(token);
                }

                return complexNotation.replace(allNodeString, sb.toString());
            }
        } catch (NotationException ignore) {
        }
        return complexNotation;
    }

    private static String removeBracketFromLabel(String chemPolymerNodeString) {
        int start = chemPolymerNodeString.indexOf("{");
        int end = chemPolymerNodeString.indexOf("}");
        String label;
        if (end <= 1) {
            label = chemPolymerNodeString.substring(start + 1);
        } else {
            label = chemPolymerNodeString.substring(start + 1, end);
        }

        String newLabel = label;
        if (newLabel.startsWith("[") && !newLabel.startsWith("[*]")) {
            newLabel = newLabel.substring(1);
        }

        if (newLabel.endsWith("]")) {
            newLabel = newLabel.substring(0, newLabel.length() - 1);
        }

        return chemPolymerNodeString.replace(label, newLabel);
    }

    public static String addChemMonomerBracket(String complexNotation) {
        if (null == complexNotation || complexNotation.isEmpty()) {
            return complexNotation;
        }

        try {
            if (ComplexNotationParser.validateNotationFormat(complexNotation)) {
                String allNodeString = ComplexNotationParser.getAllNodeString(complexNotation);
                String[] tokens = allNodeString.split("}\\|");
                StringBuilder sb = new StringBuilder();

                if (tokens.length > 1) {
                    for (int i = 0; i < tokens.length; i++) {
                        String token = tokens[i];
                        if (token.startsWith(Monomer.CHEMICAL_POLYMER_TYPE)) {
                            token = addBracketToLabel(token);
                        }

                        if (sb.length() > 0) {
                            sb.append("}|");
                        }
                        sb.append(token);
                    }

                } else {
                    String token = tokens[0];
                    if (token.startsWith(Monomer.CHEMICAL_POLYMER_TYPE)) {
                        token = addBracketToLabel(token);
                    }
                    sb.append(token);
                }

                return complexNotation.replace(allNodeString, sb.toString());
            }
        } catch (NotationException ignore) {
        }
        return complexNotation;
    }

    private static String addBracketToLabel(String chemPolymerNodeString) {
        int start = chemPolymerNodeString.indexOf("{");
        int end = chemPolymerNodeString.indexOf("}");
        String label;
        if (end <= 1) {
            label = chemPolymerNodeString.substring(start + 1);
        } else {
            label = chemPolymerNodeString.substring(start + 1, end);
        }

        String newLabel = label;
        if (label.startsWith("[")) {
            if (label.startsWith("[*]")) {
                newLabel = "[" + newLabel;
            }
        } else {
            newLabel = "[" + newLabel;
        }

        if (!newLabel.endsWith("]")) {
            newLabel = newLabel + "]";
        }

        return chemPolymerNodeString.replace(label, newLabel);
    }
}
