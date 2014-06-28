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
/*
 * ADCEditor2.java
 *
 * Created on Oct 21, 2011, 1:19:52 PM
 */
package org.helm.editor.adc;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import org.helm.notation.NotationException;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.protein.edit.ProteinEditor;

import chemaxon.marvin.beans.MSketchPane;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.helm.editor.utility.ExceptionHandler;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.model.*;

/**
 * 
 * @author YUANT05
 */
public class ADCEditor extends javax.swing.JPanel implements ActionListener {

	/** Creates new form ADCEditor2 */
	public ADCEditor() {
		initComponents();

		antibodyConnectionPanel.setLayout(new BorderLayout());

		antibody = new ProteinEditor();
		antibodyConnectionPanel.add(antibody, BorderLayout.CENTER);

		structure = new MSketchPane();
		panelStructure.setLayout(new BorderLayout());
		panelStructure.add(structure, BorderLayout.CENTER);

		connection = new ConnectionEditor();
		antibodyConnectionPanel.add(connection, BorderLayout.SOUTH);

		viewer = new ADCTabViewer(this);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(viewer, BorderLayout.CENTER);
		viewer.enableParsingNotation(true);

		connection.addOnChangeListener(this);
		antibody.addOnChangeListener(this);
		structure
				.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent ev) {
						String name = ev.getPropertyName();
						if (name != null && name.equals("mol") && !updating)
							actionPerformed(null);
					}
				});
	}

	public void actionPerformed(ActionEvent e) {
		if (!updating)
			viewer.updateView();
	}

	// generate HELM notation
	public String getNotation() {
		try {
			String smiles = structure.getMol("cxsmiles");
			String sequence = antibody.getNotation();
			String conn = connection.getConnection();
			if (sequence == null || sequence.length() == 0) {
				if (smiles == null || smiles.length() == 0)
					return "";
				else
					return "CHEM1{" + smiles + "}$$$$";
			}

			if (smiles == null || smiles.length() == 0)
				return sequence;

			String r = "?";
			List<String> Rs = org.helm.editor.editor.StructureFrame
					.getRAtoms(smiles);
			if (Rs != null && Rs.size() > 1)
				return "ERROR: The Structure part should have only one R atom, and has to be named as R1.";
			else if (Rs != null && Rs.size() == 1)
				r = Rs.get(0);
			String connstr = getConnStr(sequence, conn, r);

			int p = sequence.indexOf("$");
			return sequence.substring(0, p) + "|CHEM1{" + smiles + "}$"
					+ connstr + sequence.substring(p + 1);
		} catch (java.lang.Exception e) {
			return "ERROR: " + e.getMessage();
		}
	}

	private String getConnStr(String sequence, String conn, String r)
			throws NotationException {
		if (sequence == null || sequence.length() == 0)
			return "";

		String ret = "";
		// int p = sequence.indexOf("$");
		// String[] ss = sequence.replace("|", ",").split(",");
		// for (int i = 0; i < ss.length; ++i) {
		// String s = ss[i];
		// p = s.indexOf("{");
		// if (ret.length() > 0)
		// ret += "+";
		// ret += s.substring(0, p);
		// }

		// use NotationToolkit
		List<PolymerNode> pnl = ComplexNotationParser
				.getPolymerNodeList(sequence);
		for (PolymerNode pn : pnl) {
			if (ret.length() > 0)
				ret += PolymerEdge.NODE_CONCATENATOR;

			ret += pn.getId();
		}
		if (conn == null || conn.length() == 0)
			return ret + ",CHEM1," + PolymerEdge.GENERIC_EDGE_KEY + ":?-1:" + r;
		return ret + ",CHEM1," + PolymerEdge.GENERIC_EDGE_KEY + ":" + conn
				+ "-1:" + r;
	}

	public void setNotation(String notation) {
		updating = true;
		String[] ss = parseNotation(notation);
		antibody.setNotation(ss == null ? null : ss[0]);
		structure.setMol(ss == null ? null : ss[1]);
		connection.setConnection(ss == null ? null : ss[2]);
		updating = false;
	}

	// Split HELM notation into three parts: Antibody, Structure, Connection
	public static String[] parseNotation(String notation) {
		if (notation == null || notation.length() == 0
				|| notation.startsWith("ERROR"))
			return null;

		String ab = "";
		String drug = "";
		String connect = "";

		try {
			// Map<String, Monomer> map = MonomerStoreCache.getInstance()
			// .getCombinedMonomerStore().getMonomerDB().get("CHEM");
			Map<String, Monomer> map = org.helm.notation.MonomerFactory
					.getInstance().getMonomerDB().get("CHEM");
			ComplexPolymer cp = ComplexNotationParser.parse(notation);

			List<PolymerNode> nodes = cp.getPolymerNodeList();
			for (int i = 0; i < nodes.size(); ++i) {
				PolymerNode n = nodes.get(i);
				String type = n.getType();
				// only PEPTIDE and CHEM are allowed for ADC
				if (type.equals("PEPTIDE")) {
					if (ab.length() > 0)
						ab += "|";
					ab += n.getId() + "{" + n.getLabel() + "}";
				} else if (type.equals("CHEM")) {
					if (drug.length() > 0)
						throw new Exception(
								"This notation doesn't look like a valid ADC, because more than on chemical component found");
					Monomer mon = (Monomer) map.get(n.getLabel());
					drug = mon.getCanSMILES();
				} else {
					throw new Exception(
							"This notation doesn't look like a valid ADC, because component type *"
									+ type + "* found");
				}
			}

			// Remove CHEM from the connection part to make Antibody connection
			String ss = "";
			List<PolymerEdge> edges = cp.getPolymerEdgeList();
			for (int i = 0; i < edges.size(); ++i) {
				PolymerEdge e = edges.get(i);
				if (e.getSourceNode().startsWith("CHEM")
						|| e.getTargetNode().startsWith("CHEM")) {
					connect = e.getEdgeNotation();
				} else {
					if (ss.length() > 0)
						ss += "|";
					ss += e.getEdgeNotation();
				}
			}
			ab += "$" + ss + "$";

			ss = "";
			List<PolymerEdge> pairs = cp.getBasePairList();
			for (int i = 0; i < pairs.size(); ++i) {
				if (ss.length() == 0)
					ss += "|";
				ss += pairs.get(i).getEdgeNotation();
			}
			ab += ss + "$";

			ss = "";
			Map<String, String> annotations = cp.getPolymerNodeAnnotationMap();
			Iterator<String> it = annotations.keySet().iterator();
			while (it.hasNext()) {
				String a = it.next();
				if (ss.length() > 0)
					ss += "|";
				ss += a + "{" + annotations.get(a) + "}";
			}
			ab += ss + "$";

		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
			return null;
		}

		String[] ret = new String[3];
		ret[0] = ab.equals("$$$$") ? null : ab;
		ret[1] = drug;
		ret[2] = connect;
		return ret;
	}

	public String getAntibodyNotation() {
		try {
			return antibody.getNotation();
		} catch (java.lang.Exception e) {
			return null;
		}
	}

	public String getDrugStructure() {
		return structure.getMol("mol");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		topBottomSplit = new javax.swing.JSplitPane();
		topPanel = new javax.swing.JPanel();
		leftRightSplit = new javax.swing.JSplitPane();
		antibodyConnectionPanel = new javax.swing.JPanel();
		panelStructure = new javax.swing.JPanel();
		bottomPanel = new javax.swing.JPanel();

		setPreferredSize(new java.awt.Dimension(700, 600));

		topBottomSplit.setDividerLocation(500);
		topBottomSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		topBottomSplit.setOneTouchExpandable(true);

		leftRightSplit.setDividerLocation(400);
		leftRightSplit.setOneTouchExpandable(true);

		org.jdesktop.layout.GroupLayout antibodyConnectionPanelLayout = new org.jdesktop.layout.GroupLayout(
				antibodyConnectionPanel);
		antibodyConnectionPanel.setLayout(antibodyConnectionPanelLayout);
		antibodyConnectionPanelLayout
				.setHorizontalGroup(antibodyConnectionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING).add(0,
								399, Short.MAX_VALUE));
		antibodyConnectionPanelLayout
				.setVerticalGroup(antibodyConnectionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING).add(0,
								497, Short.MAX_VALUE));

		leftRightSplit.setLeftComponent(antibodyConnectionPanel);

		org.jdesktop.layout.GroupLayout panelStructureLayout = new org.jdesktop.layout.GroupLayout(
				panelStructure);
		panelStructure.setLayout(panelStructureLayout);
		panelStructureLayout.setHorizontalGroup(panelStructureLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 226, Short.MAX_VALUE));
		panelStructureLayout.setVerticalGroup(panelStructureLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 497, Short.MAX_VALUE));

		leftRightSplit.setRightComponent(panelStructure);

		org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(
				topPanel);
		topPanel.setLayout(topPanelLayout);
		topPanelLayout.setHorizontalGroup(topPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(leftRightSplit,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632,
				Short.MAX_VALUE));
		topPanelLayout.setVerticalGroup(topPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(leftRightSplit,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 499,
				Short.MAX_VALUE));

		topBottomSplit.setTopComponent(topPanel);

		org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(
				bottomPanel);
		bottomPanel.setLayout(bottomPanelLayout);
		bottomPanelLayout.setHorizontalGroup(bottomPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 632, Short.MAX_VALUE));
		bottomPanelLayout.setVerticalGroup(bottomPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 82, Short.MAX_VALUE));

		topBottomSplit.setRightComponent(bottomPanel);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(topBottomSplit,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 634,
				Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(topBottomSplit,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588,
				Short.MAX_VALUE));
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel antibodyConnectionPanel;
	private javax.swing.JPanel bottomPanel;
	private javax.swing.JSplitPane leftRightSplit;
	private javax.swing.JPanel panelStructure;
	private javax.swing.JSplitPane topBottomSplit;
	private javax.swing.JPanel topPanel;
	// End of variables declaration//GEN-END:variables

	private ProteinEditor antibody;
	private MSketchPane structure;
	private ConnectionEditor connection;
	private ADCTabViewer viewer;
	private boolean updating = false;
}
