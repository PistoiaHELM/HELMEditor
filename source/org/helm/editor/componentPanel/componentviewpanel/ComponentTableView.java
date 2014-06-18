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
package org.helm.editor.componentPanel.componentviewpanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdom.JDOMException;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewLayout;
import org.helm.editor.renderer.HELMGraphTableCellRenderer;
import org.helm.editor.renderer.PeptideTableCellRenderer;
import org.helm.editor.renderer.RNATableCellRenderer;
import org.helm.editor.utility.JTableRowResizer;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComboBox;

/**
 * 
 * @author lih25
 */
public class ComponentTableView extends JPanel {

	public static final int NON_SEQUENCE_COLUMN_WIDTH = 150;
	public static final int INDEX_COLUMN_WIDHT = 70;
	public static final int SEQUENCE_COLUMN_WIDTH = 200;
	private JTable table;
	private SequenceTableModel tableModel;
	private JComboBox comboBox;
	public static final String GENERIC_SEQUENCE = "Generic Sequence";
	public static final String NUCLEIC_ACID_SEQUENCE = "Nucleic Acid Sequence";
	public static final String PROTEIN_SEQUENCE = "Protein Sequence";
	public static final String[] STRUCTURE_DISPLAY_TYPES = { GENERIC_SEQUENCE,
			NUCLEIC_ACID_SEQUENCE, PROTEIN_SEQUENCE };

	public ComponentTableView() {
		super();
		JPanel displayTypePanel = createStructureDisplayTypePanel();
		JScrollPane tableScroll = createTableScrollPane();
		setLayout(new BorderLayout());
		add(displayTypePanel, BorderLayout.NORTH);
		add(tableScroll, BorderLayout.CENTER);

		configureStructureRenderer();
	}

	private JPanel createStructureDisplayTypePanel() {
		JLabel label = new JLabel("Structure Display Type: ");
		Dimension labelDim = label.getPreferredSize();
		Dimension boxDim = new JComboBox().getPreferredSize();
		Dimension dim = new Dimension((int) labelDim.getWidth() + 20,
				(int) boxDim.getHeight());
		comboBox = new JComboBox(STRUCTURE_DISPLAY_TYPES);
		comboBox.setPreferredSize(dim);
		comboBox.setMaximumSize(dim);
		comboBox.setMinimumSize(dim);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configureStructureRenderer();
				tableModel.fireTableDataChanged();
			}
		});

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalStrut(5));
		box.add(label);
		box.add(Box.createHorizontalStrut(5));
		box.add(comboBox);
		box.add(Box.createHorizontalGlue());

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(5, 5));
		p.add(box);
		return p;
	}

	private JScrollPane createTableScrollPane() {
		tableModel = new SequenceTableModel();

		table = new JTable(tableModel);

		new JTableRowResizer(table);
		table.setRowHeight(50);

		TableColumnModel tcm = table.getColumnModel();
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setPreferredWidth(INDEX_COLUMN_WIDHT);
		col.setMaxWidth(INDEX_COLUMN_WIDHT);
		col = table.getColumnModel().getColumn(1);
		col.setPreferredWidth(NON_SEQUENCE_COLUMN_WIDTH);

		for (int i = 3; i < 6; i++) {
			col = tcm.getColumn(i);
			col.setPreferredWidth(NON_SEQUENCE_COLUMN_WIDTH);
		}

		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

	private void configureStructureRenderer() {
		String structureDisplayType = (String) comboBox.getSelectedItem();
		TableColumn strucCol = table.getColumnModel().getColumn(2);
		strucCol.setPreferredWidth(SEQUENCE_COLUMN_WIDTH);

		if (NUCLEIC_ACID_SEQUENCE.equalsIgnoreCase(structureDisplayType)) {
			RNATableCellRenderer cellRenderer = new RNATableCellRenderer();
			cellRenderer
					.setDisplayMode(RNATableCellRenderer.LETTER_DISPLAY_MODE);
			strucCol.setCellRenderer(cellRenderer);
		} else if (PROTEIN_SEQUENCE.equalsIgnoreCase(structureDisplayType)) {
			PeptideTableCellRenderer cellRenderer = new PeptideTableCellRenderer();
			strucCol.setCellRenderer(cellRenderer);
		} else {
			HELMGraphTableCellRenderer cellRenderer = new HELMGraphTableCellRenderer(
					HELMGraphTableCellRenderer.MINIMAL_GAP_VIEW_TYPE,
					SequenceViewLayout.LEFT_ALIGNMENT);
			strucCol.setCellRenderer(cellRenderer);
		}
	}

	private synchronized void init(String notation) {
		tableModel.init(notation);
	}

	public synchronized void reset() {
		tableModel.clear();
	}

	public synchronized void setNotation(String notation)
			throws NotationException, MonomerException, IOException,
			JDOMException, StructureException, ClassNotFoundException {

		if (null == notation || notation.length() == 0) {
			reset();
			return;
		}

		init(notation);

		repaint();
	}
}
