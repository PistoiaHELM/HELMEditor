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
package org.helm.editor.action;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.model.MoleculeInfo;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.ExtinctionCoefficientCalculator;
import org.helm.notation.tools.StructureParser;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * 
 * @author zhangtianhong
 */
public class MoleculePropertyAction extends AbstractAction {

	private MacromoleculeEditor editor;
	private NumberFormat nf = new DecimalFormat("#0.00");

	public MoleculePropertyAction(MacromoleculeEditor editor) {
		super("Molecule Properties");
		this.editor = editor;
	}

	public void actionPerformed(ActionEvent e) {
		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JComponent message = createDisplay();
				JOptionPane.showMessageDialog(editor.getFrame(), message,
						"Molecular Properties", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private JComponent createDisplay() {
		String notation = editor.getNotation();
		if (null == notation || notation.trim().length() == 0) {
			return new JLabel(
					"There is no structure to calculate molecular properties");
		}

		String mf = "N/A";
		String mw = "N/A";
		String em = "N/A";
		String ec = "N/A";
		String ecHeader = "Ext. Coefficient";

		String smiles = null;
		try {
			smiles = ComplexNotationParser.getComplexPolymerSMILES(notation,
					editor.getMonomerStore());
		} catch (Exception ex) {
			Logger.getLogger(MoleculePropertyAction.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		if (null != smiles) {
			try {
				MoleculeInfo mi = StructureParser.getMoleculeInfo(smiles);
				mf = mi.getMolecularFormula();
				mw = nf.format(mi.getMolecularWeight());
				em = nf.format(mi.getExactMass());
			} catch (Exception ex) {
				Logger.getLogger(MoleculePropertyAction.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		try {
			ecHeader = "Ext. Coefficient ("
					+ ExtinctionCoefficientCalculator.getInstance().getUnit(
							ExtinctionCoefficientCalculator.RNA_UNIT_TYPE)
					+ ")";
			float extc = ExtinctionCoefficientCalculator.getInstance()
					.calculateFromComplexNotation(notation,
							ExtinctionCoefficientCalculator.RNA_UNIT_TYPE);
			ec = nf.format(extc);
		} catch (Exception ex) {
			Logger.getLogger(MoleculePropertyAction.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		String[] columns = { "Property", "Value" };
		String[][] data = { { "Molecular Formula", mf },
				{ "Moleular Weight", mw }, { "Exact Mass", em },
				{ ecHeader, ec } };
		TableModel model = new DefaultTableModel(data, columns);
		JTable table = new JTable(model);

		// align column header text to the left
		TableCellRenderer renderer = table.getTableHeader()
				.getDefaultRenderer();
		JLabel label = (JLabel) renderer;
		label.setHorizontalAlignment(JLabel.LEFT);

		// set viewport size based on content
		Dimension scrollSize = new Dimension(
				table.getPreferredScrollableViewportSize().width, data.length
						* table.getRowHeight());
		table.setPreferredScrollableViewportSize(scrollSize);
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(table);

		return scroll;
	}
}
