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

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.realizer.MonomerNodeRealizer;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.util.GraphCopier;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

/**
 * generate different kinds of node
 * 
 * @author lih25
 */
public class NodeFactory {

	private static final Color ANOTATION_COLOR = Color.DARK_GRAY;
	public static final int distance = 30;
	public static final int size = 30;
	public static final int centerX = 10;
	public static final int centerY = 10;

	/**
	 * Create a standard graph. Each graph contains 3 nodes, base<-R->P where R
	 * and P are standard monomers
	 * 
	 * @param notation
	 *            : the base monomer id. A, T, C, G, U
	 * @return a graph that represents a notation
	 */

	@Deprecated
	public static Graph2D createNucleictideNodeGraph(String notation)
			throws MonomerException, IOException, JDOMException {

		// a graph is used in here because we need to associate data with every
		// nodes
		Graph2D graph = new Graph2D();
		graph.setDefaultNodeRealizer(new MonomerNodeRealizer());
		NodeMap nodePropertiesNodeMap = graph.createNodeMap();
		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodePropertiesNodeMap);

		EdgeMap edgeMap = graph.createEdgeMap();
		graph.addDataProvider(EdgeMapKeys.EDGE_INFO, edgeMap);

		GraphCopier copier = new GraphCopier(graph.getGraphCopyFactory());
		copier.setDataProviderContentCopying(true);
		copier.setEdgeMapCopying(true);
		copier.setNodeMapCopying(true);

		String baseMonomerID = notation.substring(notation.indexOf("(") + 1,
				notation.indexOf(")"));
		baseMonomerID = baseMonomerID.replace("[", "");
		baseMonomerID = baseMonomerID.replace("]", "");

		Node baseNode = copier.copy(createNucleicAcidBaseNode(baseMonomerID),
				graph).firstNode();
		NodeRealizer baseNodeRealizer = graph.getRealizer(baseNode);
		baseNodeRealizer.setCenter(centerX, centerY + size + distance);

		String r = notation.substring(0, notation.indexOf("("));
		r = r.replace("[", "");
		r = r.replace("]", "");

		Node rNode = copier.copy(
				createNucleicAcidBackboneNode(r, Monomer.ID_R), graph)
				.firstNode();
		NodeRealizer rNodeRealizer = graph.getRealizer(rNode);
		rNodeRealizer.setCenter(centerX, centerY);

		String p = notation.substring(notation.indexOf(")") + 1);
		p = p.replace("[", "");
		p = p.replace("]", "");

		Node pNode = copier.copy(
				createNucleicAcidBackboneNode(p, Monomer.ID_P), graph)
				.firstNode();
		NodeRealizer pNodeRealizer = graph.getRealizer(pNode);
		pNodeRealizer.setCenter(centerX + size + distance, centerY);

		MonomerInfo pKeys = (MonomerInfo) nodePropertiesNodeMap.get(pNode);
		MonomerInfo rKeys = (MonomerInfo) nodePropertiesNodeMap.get(rNode);
		MonomerInfo baseKeys = (MonomerInfo) nodePropertiesNodeMap
				.get(baseNode);

		// r->p
		Edge edge = graph.createEdge(rNode, pNode);
		Attachment sourceAttachment = rKeys
				.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
		Attachment targetAttachment = pKeys
				.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
		rKeys.setConnection(sourceAttachment, true);
		pKeys.setConnection(targetAttachment, true);

		edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment,
				targetAttachment));

		// r->base
		edge = graph.createEdge(rNode, baseNode);

		sourceAttachment = rKeys
				.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
		targetAttachment = baseKeys
				.getAttachment(Attachment.BRANCH_MONOMER_ATTACHEMENT);
		rKeys.setConnection(sourceAttachment, true);
		baseKeys.setConnection(targetAttachment, true);

		edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment,
				targetAttachment));

		return graph;
	}

	/**
	 * Build a (modified) nucleic acid base node, this node should already
	 * registared in the database
	 * 
	 * @param monomerID
	 *            : the name of the base node
	 * @return a graph contains a single (base) node
	 */
	@Deprecated
	public static Graph2D createNucleicAcidBaseNode(String monomerID)
			throws MonomerException, IOException, JDOMException {

		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB =
		 * monomerFactory.getMonomerDB();
		 */
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();

		Graph2D graph = new Graph2D();
		MonomerNodeRealizer baseNodeRealizer = new MonomerNodeRealizer(
				MonomerNodeRealizer.DIAMOND);
		graph.setDefaultNodeRealizer(baseNodeRealizer);

		baseNodeRealizer.setSize(size, size);

		MonomerInfo monomerKeys = new MonomerInfo(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE, monomerID);
		Monomer baseMonomer = monomerDB.get(monomerKeys.getPolymerType()).get(
				monomerKeys.getMonomerID());

		if (baseMonomer == null) {
			throw new MonomerException("Invalid base monomer " + monomerID);
		}

		Node baseNode = graph.createNode();

		Color fillColor = ColorMap.getNucleotidesColor(baseMonomer
				.getNaturalAnalog());
		baseNodeRealizer.setFillColor(fillColor);

		Color textColor = Color.BLACK;
		// was: Color textColor = new Color(23 - fillColor.getRGB());
		// but we want identical view at left panel and at sequence panel, so
		// textColor is always set to black.

		baseNodeRealizer.setLabelText(monomerID);
		baseNodeRealizer.getLabel().setTextColor(textColor);
		baseNodeRealizer.getLabel().setFontSize(calculateFontSize(monomerID));

		graph.setRealizer(baseNode, baseNodeRealizer);

		NodeMap nodePropertiesNodeMap = graph.createNodeMap();
		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodePropertiesNodeMap);
		nodePropertiesNodeMap.set(baseNode, monomerKeys);

		return graph;
	}

	@Deprecated
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

	/**
	 * a dummy base node with no monomer attached to it
	 * 
	 * @param monomerID
	 * @return dummy nucleic acid base node graph
	 */
	public static Graph2D createDummyNucleicAcidBaseNode(String monomerID) {

		Graph2D graph = new Graph2D();
		MonomerNodeRealizer baseNodeRealizer = new MonomerNodeRealizer(
				MonomerNodeRealizer.DIAMOND);
		graph.setDefaultNodeRealizer(baseNodeRealizer);

		final int size = 30;
		baseNodeRealizer.setSize(size, size);

		MonomerInfo monomerKeys = new MonomerInfo(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE, monomerID);

		Node baseNode = graph.createNode();

		Color fillColor = ColorMap.getNucleotidesColor("X");
		baseNodeRealizer.setFillColor(fillColor);

		Color textColor = new Color(23 - fillColor.getRGB());
		baseNodeRealizer.setLabelText(monomerID);
		baseNodeRealizer.getLabel().setTextColor(textColor);
		baseNodeRealizer.getLabel().setFontSize(14);

		graph.setRealizer(baseNode, baseNodeRealizer);

		NodeMap nodePropertiesNodeMap = graph.createNodeMap();
		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodePropertiesNodeMap);
		nodePropertiesNodeMap.set(baseNode, monomerKeys);

		NodeLabel anotationLabel = new NodeLabel("");
		anotationLabel.setModel(NodeLabel.FREE);
		anotationLabel.setFreeOffset(-baseNodeRealizer.getHeight() / 2,
				-baseNodeRealizer.getWidth() / 4);
		anotationLabel.setTextColor(ANOTATION_COLOR);
		anotationLabel
				.setFontSize(baseNodeRealizer.getLabel().getFontSize() + 2);

		baseNodeRealizer.addLabel(anotationLabel);

		return graph;
	}

	/**
	 * create a (modified) nucleic acid backbone node. this monomer should
	 * already registared in the monomer database
	 * 
	 * @param monomerID
	 * @return A graph contains a nucleic acid backbone node
	 */

	@Deprecated
	public static Graph2D createNucleicAcidBackboneNode(String monomerID,
			String naturalAnalog) throws MonomerException, IOException,
			JDOMException {

		final String url = MacromoleculeEditor.class.getResource("resource/")
				.getFile();

		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB = monomerFactory
		 * .getMonomerDB();
		 */
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();

		Graph2D graph = new Graph2D();
		MonomerNodeRealizer backboneNodeRealizer = null;
		if (naturalAnalog.equalsIgnoreCase(Monomer.ID_R)) {
			backboneNodeRealizer = new MonomerNodeRealizer(
					MonomerNodeRealizer.ROUND_RECT);
		} else if (naturalAnalog.equalsIgnoreCase(Monomer.ID_P)) {
			backboneNodeRealizer = new MonomerNodeRealizer(
					MonomerNodeRealizer.ELLIPSE);
		} else {
			backboneNodeRealizer = new MonomerNodeRealizer(
					MonomerNodeRealizer.OCTAGON);
		}

		backboneNodeRealizer.setFillColor(ColorMap
				.getNucleotidesColor(naturalAnalog));
		backboneNodeRealizer.setSize(size, size);

		graph.setDefaultNodeRealizer(backboneNodeRealizer);

		MonomerInfo monomerKeys = new MonomerInfo(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE, monomerID);
		Monomer monomer = monomerDB.get(monomerKeys.getPolymerType()).get(
				monomerKeys.getMonomerID());

		if (monomer == null) {
			throw new MonomerException("Invalid backbone monomer ID "
					+ monomerID);
		}
		Node baseNode = graph.createNode();
		backboneNodeRealizer.setLabelText(monomerID);
		backboneNodeRealizer.setFillColor(ColorMap.getNucleotidesColor(monomer
				.getNaturalAnalog()));
		graph.setRealizer(baseNode, backboneNodeRealizer);

		NodeMap nodePropertiesNodeMap = graph.createNodeMap();
		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodePropertiesNodeMap);
		nodePropertiesNodeMap.set(baseNode, monomerKeys);
		return graph;
	}

	/**
	 * create an (modified) amino acid node
	 * 
	 * @param monomerID
	 * @return amino acid node graph
	 */
	@Deprecated
	public static Graph2D createAminoAcidNode(String monomerID)
			throws MonomerException, IOException, JDOMException {

		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB = monomerFactory
		 * .getMonomerDB();
		 */
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();

		Graph2D graph = new Graph2D();

		MonomerInfo monomerKeys = new MonomerInfo(Monomer.PEPTIDE_POLYMER_TYPE,
				monomerID);
		Monomer monomer = monomerDB.get(monomerKeys.getPolymerType()).get(
				monomerKeys.getMonomerID());
		if (monomer == null) {
			throw new MonomerException("Invalid HELM notation " + monomerID);
		}

		MonomerNodeRealizer nodeRealizer = new MonomerNodeRealizer(
				MonomerNodeRealizer.DIAMOND);
		nodeRealizer.setSize(size, size);
		nodeRealizer.setLabelText(monomerID);
		nodeRealizer.setFillColor(ColorMap.getAminoAcidColor(monomer
				.getNaturalAnalog()));
		nodeRealizer.getLabel().setFontSize(calculateFontSize(monomerID));

		Node node = graph.createNode(nodeRealizer);
		NodeMap nodeMap = graph.createNodeMap();
		nodeMap.set(node, monomerKeys);

		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodeMap);

		return graph;
	}

	/**
	 * create a registared chemical structure node
	 * 
	 * @param monomerID
	 * @return chemcial modifier node graph
	 */
	@Deprecated
	public static Graph2D createChemNode(String monomerID)
			throws MonomerException, IOException, JDOMException {

		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB = monomerFactory
		 * .getMonomerDB();
		 */
		Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
				.getInstance().getCombinedMonomerStore().getMonomerDB();

		MonomerInfo monomerKeys = new MonomerInfo(
				Monomer.CHEMICAL_POLYMER_TYPE, monomerID);
		Monomer monomer = monomerDB.get(monomerKeys.getPolymerType()).get(
				monomerKeys.getMonomerID());
		if (monomer == null) {
			throw new MonomerException("Invalid chemical ID " + monomerID);
		}

		Graph2D graph = new Graph2D();

		MonomerNodeRealizer nodeRealizer = new MonomerNodeRealizer(
				MonomerNodeRealizer.HEXAGON);
		nodeRealizer.setSize(size, size);
		nodeRealizer.setLabelText(monomerID);
		nodeRealizer.getLabel().setFontSize(calculateFontSize(monomerID));

		nodeRealizer.setFillColor(new Color(200, 0, 255)); // was Color.magenta;

		Node node = graph.createNode(nodeRealizer);

		NodeMap nodeMap = graph.createNodeMap();
		nodeMap.set(node, monomerKeys);

		graph.addDataProvider(NodeMapKeys.MONOMER_REF, nodeMap);

		return graph;
	}

	private static int calculateFontSize(String label) {
		int textLength = label.length();
		return (textLength < 4) ? 14 : ((textLength > 5) ? 9 : 10);
	}
}
