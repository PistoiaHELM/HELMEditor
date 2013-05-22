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
package org.helm.editor.monomerui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom.JDOMException;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.realizer.MonomerNodeRealizer;
import org.helm.editor.utility.ColorMap;
import org.helm.editor.utility.NotationParser;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.ShapedXmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlElement;
import org.helm.notation.model.Monomer;

import java.awt.Color;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import y.base.Node;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Factory for one node  
 * @author Alexander Makarov
 */
public class SimpleElemetFactory {

    private static Map<String, Map<String, Monomer>> monomerDB;
    private static final double DEFAULT_NODE_SIZE = 30;
    private static SimpleElemetFactory instance;
    private static final Logger log = Logger.getLogger(SimpleElemetFactory.class.toString());

    private SimpleElemetFactory() throws MonomerException, IOException, JDOMException {
        MonomerFactory monomerFactory = MonomerFactory.getInstance();
        monomerDB = monomerFactory.getMonomerDB();
    }
    private final static Map<String, Byte> SHAPE_MAP = new HashMap<String, Byte>() {

        {
            put("No", null);
            put("Circle", MonomerNodeRealizer.ELLIPSE);
            put("Rectangle", MonomerNodeRealizer.ROUND_RECT);
            put("Rhomb", MonomerNodeRealizer.DIAMOND);
            put("Hexagon", MonomerNodeRealizer.HEXAGON);
        }
    };

    /**
     * An instance of singeltone class 
     * @return instance
     * @throws MonomerException
     * @throws IOException
     * @throws JDOMException
     */
    public static SimpleElemetFactory getInstance() throws MonomerException, IOException, JDOMException {
        if (instance == null) {
            instance = new SimpleElemetFactory();
        }

        return instance;
    }

    /**
     * Construct node by type, shape and data model
     * @param <T>
     * @param type
     * @param nodeShape
     * @param element
     * @return xml element
     * @throws MonomerException
     * @throws IOException
     * @throws JDOMException
     */
    public <T extends XmlElement> Graph2D createNode(String type, String nodeShape, T element)
            throws MonomerException, IOException, JDOMException {

        if (element.getNotation() == null) {
            return createSimpleNode(type, nodeShape, element);
        }

        return createComposedNode(type, element);
    }

    private <T extends XmlElement> Graph2D createComposedNode(String type, T element)
            throws MonomerException, IOException, JDOMException {

        Graph2D graph = new Graph2D();

        Node fn;
        Graph2D inner;
        NodeRealizer fnr;
        HierarchyManager hm = graph.getHierarchyManager();

        String notation = element.getNotation();

        String base = notation.substring(notation.indexOf("(") + 1, notation.indexOf(")"));
        base = base.replaceAll("\\[|\\]", "");
        Monomer baseMonomer = monomerDB.get(Monomer.NUCLIEC_ACID_POLYMER_TYPE).get(base);

        if (hm == null) {
            hm = new HierarchyManager(graph);
        }

        NodeMap notationMap = (NodeMap) graph.getDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION);
        if (notationMap == null) {
            notationMap = graph.createNodeMap();
            graph.addDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION,
                    notationMap);
        }

        // a folder containing a graph to represent the given nucleotide
        fn = hm.createFolderNode(graph);
        inner = (Graph2D) hm.getInnerGraph(fn);
        GraphCopier copier = SequenceGraphTools.getGraphCopier(inner);
        copier.copy(NotationParser.createNucleotideGraph(notation), inner);
        notationMap.set(fn, notation);

        // finally tell the realizer to display the inner graph
        fnr = graph.getRealizer(fn);

        String elementName = element.getName();
        fnr.setLabelText(elementName);

        fnr.getLabel().setVisible(false);
        fnr.getLabel().setFontSize(calculateFontSize(elementName));
        fnr.setTransparent(true);
        fnr.setFillColor(ColorMap.getNucleotidesColor(baseMonomer.getNaturalAnalog()));

        // add monomer info to node
        MonomerInfo monomerKeys = new MonomerInfo(type, element.getName());

        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        if (nodeMap == null) {
            nodeMap = graph.createNodeMap();
            nodeMap.set(fn, monomerKeys);
            graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodeMap);
        }

        if (fnr instanceof GroupNodeRealizer) {
            ((GroupNodeRealizer) fnr).setInnerGraphDisplayEnabled(true);
        }

        return graph;
    }

    private <T extends XmlElement> Graph2D createSimpleNode(String type, String shape, T element)
            throws MonomerException {
        String monomerId = element.getName();
        type = type.toUpperCase();

        Monomer monomer = monomerDB.get(type).get(monomerId);
        MonomerInfo monomerKeys = new MonomerInfo(type, monomerId);
        if (monomer == null) {
            log.log(Level.WARNING, "No data found for monomer id " + monomerId);
            return null;
        }

        byte nodeShape = getNodeShage(shape);
        MonomerNodeRealizer nodeRealizer = new MonomerNodeRealizer(nodeShape);

        nodeRealizer.setSize(DEFAULT_NODE_SIZE, DEFAULT_NODE_SIZE);
        nodeRealizer.setLabelText(monomerId);
        nodeRealizer.getLabel().setFontSize(calculateFontSize(monomerId));
        nodeRealizer.setFillColor(element.getBackgroundColor());

        Graph2D graph = new Graph2D();
        Node node = graph.createNode(nodeRealizer);
        NodeMap nodeMap = graph.createNodeMap();
        nodeMap.set(node, monomerKeys);

        graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodeMap);
        return graph;
    }

    private Graph2D createMonomerNode(String polymerType, String monomerId, String shape, Color backgroundColor) throws MonomerException {
        Monomer monomer = monomerDB.get(polymerType).get(monomerId);
        if (monomer == null) {
            log.log(Level.WARNING, "No data found for monomer id " + monomerId + " in " + polymerType);
            return null;
        }

        byte nodeShape = getNodeShage(shape);
        MonomerNodeRealizer nodeRealizer = new MonomerNodeRealizer(nodeShape);


        nodeRealizer.setSize(DEFAULT_NODE_SIZE, DEFAULT_NODE_SIZE);
        nodeRealizer.setLabelText(monomerId);
        nodeRealizer.getLabel().setFontSize(calculateFontSize(monomerId));
        nodeRealizer.setFillColor(backgroundColor);

        MonomerInfo monomerInfo = new MonomerInfo(polymerType, monomerId);

        Graph2D graph = new Graph2D();
        Node node = graph.createNode(nodeRealizer);
        NodeMap nodeMap = graph.createNodeMap();
        nodeMap.set(node, monomerInfo);

        graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodeMap);
        return graph;
    }

    public Graph2D createMonomerNode(String polymerType, String monomerId) throws MonomerException {
        Monomer monomer = monomerDB.get(polymerType).get(monomerId);
        if (monomer == null) {
            log.log(Level.WARNING, "No data found for monomer id " + monomerId + " in " + polymerType);
            return null;
        }

        ShapedXmlFragment shapedXmlFragment = getShapedXmlFragment(polymerType, monomerId);
        if (shapedXmlFragment == null) {
            shapedXmlFragment = new ShapedXmlFragment(monomerId, null);
        }
        return createMonomerNode(polymerType, monomerId, shapedXmlFragment.getShape(), shapedXmlFragment.getBackgroundColor());

    }

    private ShapedXmlFragment getShapedXmlFragment(String polymerType, String monomerId) {
        UIConstructor uiConstructor = null;
        try {
            uiConstructor = UIConstructor.getInstance();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Monomer UI not initialized yet");
            return null;
        }
        
        Iterator<Polymer> it = uiConstructor.getUITemplateManager().getPolymerIntertor();
        while (it.hasNext()) {
            Polymer p = it.next();
            if (p.getName().equalsIgnoreCase(polymerType)) {
                ShapedXmlFragment result = p.getXmlFragmentByName(monomerId);
                if (null != result) {
                    return result;
                }
            }
        }
        return null;

    }

    private byte getNodeShage(String nodeShape) {
        Byte shape = SHAPE_MAP.get(nodeShape);

        if (shape != null) {
            return shape.byteValue();
        }

        return MonomerNodeRealizer.DIAMOND;
    }

    private static int calculateFontSize(String label) {
        int textLength = label.length();
        return (textLength < 4) ? 14 : ((textLength > 5) ? 9 : 10);
    }
}
