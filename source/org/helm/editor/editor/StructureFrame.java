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

import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.StructureException;
import org.helm.notation.model.*;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.tools.StructureParser;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.utility.GraphUtils;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author yuant05
 */
public class StructureFrame extends JFrame {
	private StructurePanel structurePanel = null;
	private MacromoleculeEditor editor = null;
	private JButton button = null;
	private MonomerInfo monomerInfo = null;

	private static int iX = 0;
	private static StructureFrame frame = null;
	private Monomer monomer;

	private StructureFrame() {
		super();

		structurePanel = new StructurePanel();

		button = new JButton();
		button.setText("Save");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
	}

	// Get all R atoms from a given smiles string
	public static List<String> getRAtoms(String smiles) {
		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		int offset = 2;
		int p = smiles.indexOf("|$");
		if (p < 0) {
			p = smiles.indexOf("|r,$");
			offset = 4;
		}
		if (p < 0)
			return list;
		smiles = smiles.substring(p + offset);
		p = smiles.indexOf("$|");
		if (p > 0)
			smiles = smiles.substring(0, p);

		String[] ss = smiles.split(";");
		for (int i = 0; i < ss.length; ++i) {
			String s = ss[i];
			if (s.length() >= 3 && s.charAt(0) == '_' && s.charAt(1) == 'R'
					&& (s.charAt(2) >= '0' && s.charAt(2) <= '9'))
				list.add(s.substring(1));
		}
		return list;
	}

	// look for predefined monomer in smilesDB
	// if no monomer can be found, register the smiles in monomer database as a
	// session temporary record
	public static Monomer getMonomerBySmiles(String smiles, String polymerType,
			MonomerStore storeToAdd, Monomer editedMonomer) {

		MonomerStore combinedMonomerStore = MonomerStoreCache.getInstance()
				.getCombinedMonomerStore();

		Map<String, Monomer> map = combinedMonomerStore
				.getMonomers(polymerType);
		Map<String, Monomer> smilesDB = combinedMonomerStore
				.getSmilesMonomerDB();
		Monomer monomer = null;

		String uniqueSmiles = null;
		try {
			uniqueSmiles = StructureParser.getUniqueExtendedSMILES(smiles);
		} catch (Exception e) {
			uniqueSmiles = smiles;
		}

		// first check if a predefined monomer exists
		if (smilesDB.containsKey(uniqueSmiles)) {
			monomer = smilesDB.get(uniqueSmiles);
		} else {
			// no predefined monomer, then a new should be created
			boolean r1 = false;
			boolean r2 = false;
			boolean r3 = false;
			List<String> rs = getRAtoms(uniqueSmiles);
			for (String r : rs) {
				if (r.compareTo("R1") == 0 && !r1)
					r1 = true;
				else if (r.compareTo("R2") == 0 && !r2)
					r2 = true;
				else if (r.compareTo("R3") == 0 && !r3)
					r3 = true;
				else
					return null;
			}
			if (r3 && (!r1 || !r2) || r2 && !r1 || !r1)
				return null;

			// if not, then create new one

			monomer = new Monomer();
			monomer.setPolymerType(polymerType);
			monomer.setCanSMILES(uniqueSmiles);
			monomer.setName("Dynamic");
			monomer.setAdHocMonomer(true);

			// make sure it assigns a unique alternaeId
			String alternateId = null;
			while (true) {
				// alternateId = "AM#" + (++iX);
				alternateId = SimpleNotationParser.generateNextAdHocMonomerID(
						polymerType, combinedMonomerStore);

				if (!map.containsKey(alternateId))
					break;
			}
			monomer.setAlternateId(alternateId);

			if (polymerType.equals(Monomer.CHEMICAL_POLYMER_TYPE)) {
				monomer.setMonomerType(Monomer.UNDEFINED_MOMONER_TYPE);

			} else if (polymerType.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				monomer.setMonomerType(editedMonomer.getMonomerType());
				monomer.setNaturalAnalog(editedMonomer.getNaturalAnalog());
			} else {
				monomer.setMonomerType(Monomer.BACKBONE_MOMONER_TYPE);
				monomer.setNaturalAnalog(editedMonomer.getNaturalAnalog());
			}

			int index = 0;
			for (String r : rs) {
				Attachment a = new Attachment();
				a.setAlternateId(r + "-H");
				a.setCapGroupName("H");
				a.setCapGroupSMILES("[*][H] |$_" + r + ";$|");
				a.setId(index++);
				a.setLabel(r);
				monomer.addAttachment(a);
			}

			// add the new monomer to the dictionary
			try {
				storeToAdd.addNewMonomer(monomer);
			} catch (Exception e) {
				// JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
				// JOptionPane.WARNING_MESSAGE);
				monomer = null;
			}

			org.helm.editor.utility.MonomerNodeHelper.generateImageFile(
					monomer, true);
		}

		return monomer;
	}

	private void save() {
		String smiles = structurePanel.getSmilesEx();
		// salts should be allowed (XHELM-75)
		// simple verification
		/*
		 * if (smiles.indexOf(".") > 0) { String msg =
		 * "The structure has to be in one fragment.";
		 * JOptionPane.showMessageDialog(editor.getFrame(), msg, "Error",
		 * JOptionPane.WARNING_MESSAGE); return; }
		 */

		MonomerStore store = MonomerStoreCache.getInstance().getMonomerStore(
				this.monomer);

		// return the monomer corresponding to the smiles
		Monomer monomer = getMonomerBySmiles(smiles,
				this.monomer.getPolymerType(), store, this.monomer);
		if (monomer == null) {
			String msg = "Connection points R atoms not defined correctly.  Please use R1, R2 and R3 only.";
			JOptionPane.showMessageDialog(editor.getFrame(), msg, "Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// set up attachment points
		for (Attachment a1 : monomerInfo.getUsedAttachmentList()) {
			boolean f = false;
			for (Attachment a2 : monomer.getAttachmentList()) {
				if (a1.getLabel().compareTo(a2.getLabel()) == 0) {
					if (a1.getAlternateId().compareTo(a2.getAlternateId()) != 0) {
						a1.setAlternateId(a2.getAlternateId());
						a1.setCapGroupName(a2.getCapGroupName());
						a1.setCapGroupSMILES(a2.getCapGroupSMILES());
					}
					f = true;
					break;
				}
			}
			if (!f) {
				String msg = "Connection point " + a1.getLabel()
						+ " is not defined";
				JOptionPane.showMessageDialog(editor.getFrame(), msg, "Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}

		monomerInfo.setMonomerID(monomer.getAlternateId());
		editor.onDropCompleteEvent(null);

		this.hide();
	}

	private void setMonomer(Monomer monomer) {
		if (monomer == null)
			return;

		String s = "";
		if (monomer.getMolfile() != null) {
			s = monomer.getMolfile();
		} else if (monomer.getCanSMILES() != null) {
			s = monomer.getCanSMILES();
		}

		this.monomer = monomer;
		structurePanel.setEditMode(true);
		structurePanel.setMol(s);

	}

	public static JFrame showDialog(MacromoleculeEditor editor,
			MonomerInfo monomerInfo) {
		Monomer monomer = null;
		try {
			monomer = monomerInfo == null ? null : GraphUtils.getMonomerDB()
					.get(monomerInfo.getPolymerType())
					.get(monomerInfo.getMonomerID());
		} catch (Exception e) {
		}

		if (monomer == null)
			// support peptide and chem
			// || !(monomerInfo.getPolymerType().equals("CHEM") ||
			// monomerInfo.getPolymerType().equals("PEPTIDE")))
			return null;

		if (frame == null) {
			frame = new StructureFrame();
			frame.setTitle("Structure Editor");
			frame.setDefaultCloseOperation(HIDE_ON_CLOSE);

			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(frame.structurePanel,
					BorderLayout.CENTER);

			frame.getContentPane().add(frame.button, BorderLayout.SOUTH);
			frame.pack();
		}

		frame.editor = editor;
		frame.monomerInfo = monomerInfo;
		frame.setMonomer(monomer);

		frame.setVisible(true);
		Dimension d = frame.getSize();
		if (d.width < 600)
			d.width = 600;
		if (d.height < 400)
			d.height = 400;
		frame.setSize(d);
		return frame;
	}
}
