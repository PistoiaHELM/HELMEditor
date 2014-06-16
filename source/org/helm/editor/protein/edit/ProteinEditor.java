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
 * SingleProteinInputPanel.java
 *
 * Created on Dec 6, 2010, 8:53:10 PM
 */
package org.helm.editor.protein.edit;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.editor.utility.ExceptionHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import org.jdom.JDOMException;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * 
 * @author ZHANGTIANHONG
 */
public class ProteinEditor extends javax.swing.JPanel {

	public static final int ANNOTATION_TABLE_ROW_COUNT = 10;
	public static final int CONNECTION_TABLE_ROW_COUNT = 6;
	public static final int AMINO_ACID_COUNT_PER_BLOCK = 10;
	public static final String SEQUENCE_SEPARATOR_REGEX = "\\$";
	public static final String PREFIX_CHAIN = "Chain ";

	public ProteinEditor() {
		initComponents();
		customInit();
	}

	private void customInit() {
		connectionTableModel = new PeptideConnectionTableModel();
		connectionTableModel.setupEmptyData(CONNECTION_TABLE_ROW_COUNT);
		connectionTable.setModel(connectionTableModel);

		JComboBox connectionComboBox = new JComboBox(
				PeptideConnection.VALID_BOND_TYPES);
		DefaultCellEditor connectionEditor = new DefaultCellEditor(
				connectionComboBox);
		connectionTable.getColumnModel()
				.getColumn(PeptideConnectionTableModel.BOND_TYPE_COLUMN_INDEX)
				.setCellEditor(connectionEditor);

		annotationTableModel = new SequenceAnnotationTableModel();
		annotationTableModel.setupEmptyData(ANNOTATION_TABLE_ROW_COUNT);
		annotationTable.setModel(annotationTableModel);

		JComboBox annotationComboBox = new JComboBox(
				SequenceAnnotation.VALID_ANNOTATIONS);
		DefaultCellEditor annotationEditor = new DefaultCellEditor(
				annotationComboBox);
		annotationTable
				.getColumnModel()
				.getColumn(SequenceAnnotationTableModel.ANNOTATION_COLUMN_INDEX)
				.setCellEditor(annotationEditor);

		chainComboBox = new JComboBox(new String[] { "" });
		DefaultCellEditor chainEditor = new DefaultCellEditor(chainComboBox);
		annotationTable.getColumnModel()
				.getColumn(SequenceAnnotationTableModel.CHAIN_COLUMN_INDEX)
				.setCellEditor(chainEditor);
		connectionTable
				.getColumnModel()
				.getColumn(
						PeptideConnectionTableModel.SOURCE_CHAIN_COLUMN_INDEX)
				.setCellEditor(chainEditor);
		connectionTable
				.getColumnModel()
				.getColumn(
						PeptideConnectionTableModel.TARGET_CHAIN_COLUMN_INDEX)
				.setCellEditor(chainEditor);

		sequenceTextArea.getDocument().addDocumentListener(
				new DocumentListener() {
					public void insertUpdate(DocumentEvent e) {
						chainChange();
					}

					public void removeUpdate(DocumentEvent e) {
						chainChange();
					}

					public void changedUpdate(DocumentEvent e) {
						chainChange();
					}
				});

		connectionTableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				fireOnChange();
			}
		});

		annotationTableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				fireOnChange();
			}
		});
	}

	private void chainChange() {
		cacheSequence();
		for (int i = chainComboBox.getItemCount() - 1; i < sequences.size(); ++i)
			chainComboBox.addItem(ProteinEditor.PREFIX_CHAIN + (i + 1));

		int nc = 0;
		int na = 0;
		for (int i = chainComboBox.getItemCount() - 1; i > sequences.size(); --i) {
			chainComboBox.removeItemAt(i);
			nc += connectionTableModel.removeChain(i);
			na += annotationTableModel.removeChain(i);
		}

		if (nc > 0)
			connectionTable.updateUI();
		if (na > 0)
			annotationTable.updateUI();

		fireOnChange();
	}

	public void fireOnChange() {
		if (!notFiringOnChange) {
			for (int i = 0; i < onchanges.size(); ++i)
				onchanges.get(i).actionPerformed(
						new ActionEvent(this, 0, "Data Cahnged"));
		}
	}

	public void addOnChangeListener(ActionListener listener) {
		onchanges.add(listener);
	}

	public void setNotation(String notation) {
		if (null == notation || notation.length() == 0) {
			notFiringOnChange = true;
			sequenceTextArea.setText("");
			connectionTableModel.setupEmptyData(CONNECTION_TABLE_ROW_COUNT);
			annotationTableModel.setupEmptyData(ANNOTATION_TABLE_ROW_COUNT);
			notFiringOnChange = false;
			return;
		}

		notFiringOnChange = true;
		try {
			ComplexProtein cp = ComplexProtein.convert(notation);
			List<String> sequences = cp.getSequences();
			if (null != sequences && !sequences.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String seq : sequences) {
					if (sb.length() > 0) {
						sb.append(ComplexProtein.SEQUENCE_SEPARATOR_SYMBOL);
						sb.append("\n");
					}
					sb.append(toBlockedSequence(seq));
					sb.append("\n");
				}
				sequenceTextArea.setText(sb.toString());

				List<PeptideConnection> conList = cp.getConnections();
				if (null != conList && !conList.isEmpty()) {
					if (conList.size() >= CONNECTION_TABLE_ROW_COUNT) {
						connectionTableModel.setData(conList);
					} else {
						List<PeptideConnection> l = new ArrayList<PeptideConnection>();
						l.addAll(conList);
						for (int i = 0; i < CONNECTION_TABLE_ROW_COUNT
								- conList.size(); i++) {
							l.add(new PeptideConnection());
						}
						connectionTableModel.setData(l);
					}
				} else {
					connectionTableModel
							.setupEmptyData(CONNECTION_TABLE_ROW_COUNT);
				}

				List<SequenceAnnotation> annList = cp.getAnnotations();
				if (null != annList && !annList.isEmpty()) {
					if (annList.size() >= ANNOTATION_TABLE_ROW_COUNT) {
						annotationTableModel.setData(annList);
					} else {
						List<SequenceAnnotation> l = new ArrayList<SequenceAnnotation>();
						l.addAll(annList);
						for (int i = 0; i < ANNOTATION_TABLE_ROW_COUNT
								- annList.size(); i++) {
							l.add(new SequenceAnnotation());
						}
						annotationTableModel.setData(l);
					}
				} else {
					annotationTableModel
							.setupEmptyData(ANNOTATION_TABLE_ROW_COUNT);
				}

			} else {
				sequenceTextArea.setText("");
				connectionTableModel.setupEmptyData(CONNECTION_TABLE_ROW_COUNT);
				annotationTableModel.setupEmptyData(ANNOTATION_TABLE_ROW_COUNT);
			}
		} catch (Exception ex) {
			Logger.getLogger(ProteinEditor.class.getName()).log(Level.SEVERE,
					null, ex);
			ExceptionHandler.handleException(ex);
		}
		notFiringOnChange = false;
	}

	public String getNotation() throws NotationException, IOException,
			MonomerException, JDOMException {
		ComplexProtein cp = new ComplexProtein();
		cp.setSequences(getSequences());

		if (annotationTable.getCellEditor() != null) {
			annotationTable.getCellEditor().stopCellEditing();
		}
		cp.setAnnotations(annotationTableModel
				.getPopulatedSeqeuenceAnnotations());

		if (connectionTable.getCellEditor() != null) {
			connectionTable.getCellEditor().stopCellEditing();
		}
		cp.setConnections(connectionTableModel.getPopulatedConnections());
		return cp.getNotation();
	}

	public List<String> getSequences() {
		return sequences;
	}

	void cacheSequence() {
		sequences.clear();

		String text = sequenceTextArea.getText();
		String[] tokens = text.split(SEQUENCE_SEPARATOR_REGEX);
		for (String token : tokens) {
			String seq = token.replaceAll("\\s", "");
			if (seq.length() > 0) {
				sequences.add(seq);
			}
		}
	}

	private String toBlockedSequence(String sequence) {
		char[] chars = sequence.toCharArray();
		StringBuilder sb = new StringBuilder();
		boolean modStart = false;
		int count = 0;
		for (char c : chars) {
			String singleLetter = String.valueOf(c);
			if (singleLetter
					.equals(ComplexProtein.START_MODIFICATION_DECORATOR)) {
				modStart = true;
			} else if (singleLetter
					.equals(ComplexProtein.END_MODIFICATION_DECORATOR)) {
				modStart = false;
				count++;
			} else {
				if (!modStart) {
					count++;
				}
			}

			sb.append(singleLetter);

			if (count == AMINO_ACID_COUNT_PER_BLOCK) {
				sb.append(" ");
				count = 0;
			}
		}

		return sb.toString();
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

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		connectionScroll = new javax.swing.JScrollPane();
		connectionTable = new javax.swing.JTable();
		jPanel3 = new javax.swing.JPanel();
		sequenceScroll = new javax.swing.JScrollPane();
		sequenceTextArea = new javax.swing.JTextArea();
		jPanel4 = new javax.swing.JPanel();
		annotationScroll = new javax.swing.JScrollPane();
		annotationTable = new javax.swing.JTable();
		legnedScroll = new javax.swing.JScrollPane();
		legendTextPane = new javax.swing.JTextPane();

		setLayout(new java.awt.BorderLayout());

		jPanel2.setPreferredSize(new java.awt.Dimension(500, 414));
		jPanel2.setRequestFocusEnabled(false);

		connectionScroll.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Connections"));
		connectionScroll.setViewportView(connectionTable);

		org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(connectionScroll,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 708,
				Short.MAX_VALUE));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(connectionScroll,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122,
				Short.MAX_VALUE));

		sequenceScroll.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Sequences"));

		sequenceTextArea.setColumns(20);
		sequenceTextArea.setFont(new java.awt.Font("Courier New", 0, 13));
		sequenceTextArea.setLineWrap(true);
		sequenceTextArea.setRows(5);
		sequenceTextArea
				.setToolTipText("<html>Enter single letter protein sequence here<p>\nUse $ to separate sequences<p>\nUse square bracket [ ] to enclose modified amino acid<p>\nWhite space will be ignored</html>");
		sequenceScroll.setViewportView(sequenceTextArea);

		annotationScroll.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Annotations"));
		annotationScroll.setViewportView(annotationTable);

		legendTextPane.setBackground(new java.awt.Color(204, 204, 204));
		legendTextPane.setEditable(false);
		legendTextPane
				.setText("$   Sequence Separator\n[ ]  Modified Amino Acid");
		legnedScroll.setViewportView(legendTextPane);

		org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(
				jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING, legnedScroll,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140,
						Short.MAX_VALUE)
				.add(annotationScroll,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140,
						Short.MAX_VALUE));
		jPanel4Layout
				.setVerticalGroup(jPanel4Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel4Layout
										.createSequentialGroup()
										.add(annotationScroll,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												311, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(legnedScroll,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												55,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
				.setHorizontalGroup(jPanel3Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel3Layout
										.createSequentialGroup()
										.add(sequenceScroll,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												562, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jPanel4,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
		jPanel3Layout.setVerticalGroup(jPanel3Layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.add(sequenceScroll,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 372,
						Short.MAX_VALUE));

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 708,
						Short.MAX_VALUE)
				.add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(org.jdesktop.layout.GroupLayout.TRAILING,
								jPanel1Layout
										.createSequentialGroup()
										.add(jPanel3,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jPanel2,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												122,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		add(jPanel1, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents
		// Variables declaration - do not modify//GEN-BEGIN:variables

	private javax.swing.JScrollPane annotationScroll;
	private javax.swing.JTable annotationTable;
	private javax.swing.JScrollPane connectionScroll;
	private javax.swing.JTable connectionTable;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JTextPane legendTextPane;
	private javax.swing.JScrollPane legnedScroll;
	private javax.swing.JScrollPane sequenceScroll;
	private javax.swing.JTextArea sequenceTextArea;
	// End of variables declaration//GEN-END:variables
	private PeptideConnectionTableModel connectionTableModel;
	private SequenceAnnotationTableModel annotationTableModel;
	private List<ActionListener> onchanges = new ArrayList<ActionListener>();
	private boolean notFiringOnChange = false;
	private JComboBox chainComboBox = null;
	private List<String> sequences = new ArrayList<String>();;
}
