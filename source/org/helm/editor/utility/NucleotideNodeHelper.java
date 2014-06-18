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

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.xmlparser.data.XmlElement;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.SimpleNotationParser;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.NodeRealizer;

/**
 * 
 * @author ZHANGTIANHONG
 */
public class NucleotideNodeHelper {

	// public static final String NOTATION_DIRECTORY =
	// System.getProperty("user.home") + System.getProperty("file.separator") +
	// ".notation";
	// public static final String IMAGE_DIRECTORY =
	// MonomerFactory.NOTATION_DIRECTORY + System.getProperty("file.separator")
	// + "images" ;
	public static final String NUCLEOTIDE_DIRECTORY = MonomerNodeHelper.IMAGE_DIRECTORY
			+ System.getProperty("file.separator") + "NUCLEOTIDE";

	public static String getTooltip(XmlElement element) {
		String notation = element.getNotation();
		String imageFilePath = getStructureImageFilePath(element);
		return getTooltip(notation, imageFilePath);
	}

	public static String getTooltip(Node node) {
		String notation = getNotation(node);
		String imageFilePath = getStructureImageFilePath(node);
		return getTooltip(notation, imageFilePath);
	}

	public static String getTooltip(String notation, String imageFilePath) {
		String tooltip = "";
		if (null == notation)
			return tooltip;

		if (null == imageFilePath)
			return tooltip;

		File imageFile = new File(imageFilePath);
		generateImageFile(imageFilePath, notation);

		if (imageFile.exists()) {
			tooltip = "<html><body>Nucleotide Notation: " + notation
					+ "<br><img src = \"file:" + imageFilePath
					+ "\"></body></html>";
		}

		return tooltip;
	}

	private static void generateImageFile(String imageFilePath, String notation) {
		try {
			File notationDir = new File(MonomerFactory.NOTATION_DIRECTORY);
			if (!notationDir.exists()) {
				notationDir.mkdir();
			}

			File imageDir = new File(MonomerNodeHelper.IMAGE_DIRECTORY);
			if (!imageDir.exists()) {
				imageDir.mkdir();
			}

			File nucDir = new File(NUCLEOTIDE_DIRECTORY);
			if (!nucDir.exists()) {
				nucDir.mkdir();
			}

			File structureImageFile = new File(imageFilePath);
			if (!structureImageFile.exists()) {
				saveImage(notation, imageFilePath);
			} else {
				long lastTime = structureImageFile.lastModified();
				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - lastTime;
				if (elapsedTime > MonomerNodeHelper.MAX_HOURS_BETWEEN_IMAGE_REFRESH * 60 * 60 * 1000) {
					saveImage(notation, imageFilePath);
				}
			}

		} catch (Exception ex) {
			Logger.getLogger(NucleotideNodeHelper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public static String getNotation(Node node) {
		Graph2D graph = (Graph2D) node.getGraph();
		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION);
		return (String) nodeMap.get(node);
	}

	public static String getStructureImageFilePath(XmlElement element) {
		String fileName = element.getName();
		fileName = fileName + ".png";
		return NUCLEOTIDE_DIRECTORY + System.getProperty("file.separator")
				+ fileName;
	}

	public static String getStructureImageFilePath(Node node) {
		Graph2D graph = (Graph2D) node.getGraph();
		NodeRealizer nr = graph.getRealizer(node);
		String fileName = nr.getLabelText();
		fileName = fileName + ".png";
		return NUCLEOTIDE_DIRECTORY + System.getProperty("file.separator")
				+ fileName;
	}

	private static void saveImage(String notation, String imageFilePath)
			throws NotationException, MonomerException, StructureException,
			JDOMException, IOException {
		String complexNotation = SimpleNotationParser
				.getComplextNotationForRNA(notation);
		String smiles = ComplexNotationParser
				.getComplexPolymerSMILES(complexNotation);
		Molecule mol = MolImporter.importMol(smiles);
		Image image = (Image) mol.toObject("image");
		new SaveAsPNG(image, imageFilePath);
	}
}
