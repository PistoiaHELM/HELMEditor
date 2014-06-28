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

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.notation.model.Monomer;

import java.awt.Image;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import y.base.Node;
import y.base.NodeMap;

/**
 * 
 * @author ZHANGTIANHONG
 */
public class MonomerNodeHelper {

	// public static final String NOTATION_DIRECTORY =
	// System.getProperty("user.home") + System.getProperty("file.separator") +
	// ".notation";
	public static final String IMAGE_DIRECTORY = MonomerFactory.NOTATION_DIRECTORY
			+ System.getProperty("file.separator") + "images";
	public static final long MAX_HOURS_BETWEEN_IMAGE_REFRESH = 48;

	public static String getTooltip(Monomer monomer) {
		String tooltip = "";
		String imageFilePath = getStructureImageFile(monomer);
		File imageFile = new File(imageFilePath);

		generateImageFile(monomer, false);

		if (imageFile.exists()) {
			String title = monomer.getAlternateId() + ":"
					+ monomer.getNaturalAnalog() + ":" + monomer.getName();
			tooltip = "<html><body>" + title + "<br><img src = \"file:"
					+ imageFilePath + "\"></body></html>";
		}
		return tooltip;
	}

	public static String getTooltip(Node node) {
		return getTooltip(node2monomer(node));
	}

	public static Monomer node2monomer(Node node) {
		try {
			NodeMap nodeMap = (NodeMap) node.getGraph().getDataProvider(
					NodeMapKeys.MONOMER_REF);
			MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(node);
			Monomer monomer = MonomerStoreCache.getInstance()
					.getCombinedMonomerStore().getMonomerDB()
					.get(monomerInfo.getPolymerType())
					.get(monomerInfo.getMonomerID());
			// Monomer monomer =
			// MonomerFactory.getInstance().getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
			return monomer;
		} catch (Exception ex) {
			Logger.getLogger(MonomerNodeHelper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return null;
	}

	public static String getStructureImageDirectory(Monomer monomer) {
		return IMAGE_DIRECTORY + System.getProperty("file.separator")
				+ monomer.getPolymerType();
	}

	public static String getStructureImageFile(Monomer monomer) {
		String fileName = monomer.getAlternateId();
		if (null != monomer.getName()) {
			fileName = fileName + "-" + monomer.getName();
		}

		// tooltip html is not happy with #
		fileName = fileName.replace("#", "_") + ".png";
		return getStructureImageDirectory(monomer)
				+ System.getProperty("file.separator") + fileName;
	}

	public static void generateImageFile(Monomer monomer,
			boolean forceregenerate) {
		try {
			File notationDir = new File(MonomerFactory.NOTATION_DIRECTORY);
			if (!notationDir.exists()) {
				notationDir.mkdir();
			}

			File imageDir = new File(IMAGE_DIRECTORY);
			if (!imageDir.exists()) {
				imageDir.mkdir();
			}

			if (monomer != null) {
				String structureImageDirectoryString = getStructureImageDirectory(monomer);
				File structureImageDir = new File(structureImageDirectoryString);
				if (!structureImageDir.exists()) {
					structureImageDir.mkdir();
				}

				String structureImageFileString = getStructureImageFile(monomer);
				File structureImageFile = new File(structureImageFileString);
				if (!structureImageFile.exists() || forceregenerate) {
					saveImage(monomer, structureImageFileString);
				} else {
					long lastTime = structureImageFile.lastModified();
					long currentTime = System.currentTimeMillis();
					long elapsedTime = currentTime - lastTime;
					if (elapsedTime > MAX_HOURS_BETWEEN_IMAGE_REFRESH * 60 * 60 * 1000) {
						saveImage(monomer, structureImageFileString);
					}
				}

			}

		} catch (Exception ex) {
			Logger.getLogger(MonomerNodeHelper.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	private static void saveImage(Monomer monomer, String filePath)
			throws MolFormatException {
		Molecule mol = null;
		if (monomer.getMolfile() != null) {
			mol = MolImporter.importMol(monomer.getMolfile());
		} else if (monomer.getCanSMILES() != null) {
			mol = MolImporter.importMol(monomer.getCanSMILES());
		}

		if (mol != null) {
			Image image = (Image) mol.toObject("image");
			new SaveAsPNG(image, filePath);
		}
	}
}
