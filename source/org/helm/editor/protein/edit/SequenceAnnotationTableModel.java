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
package org.helm.editor.protein.edit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SequenceAnnotationTableModel extends AbstractTableModel {

	public static final int CHAIN_COLUMN_INDEX = 0;
	public static final int ANNOTATION_COLUMN_INDEX = 1;
	private String[] columnNames = { "Chain", "Annotation" };
	private final List<SequenceAnnotation> data = new ArrayList<SequenceAnnotation>();

	public SequenceAnnotationTableModel() {
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return data.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int removeChain(int k) {
		int n = 0;
		for (int i = data.size() - 1; i >= 0; --i) {
			if (data.get(i).getId() == k) {
				data.remove(i);
				++n;
			}
		}
		return n;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		SequenceAnnotation annotation = data.get(rowIndex);
		switch (columnIndex) {

		case 0:
			if (annotation.getId() > 0) {
				return ProteinEditor.PREFIX_CHAIN
						+ String.valueOf(annotation.getId());
			} else {
				return "";
			}
		case 1:
			return annotation.getAnnotation();
		default:
			return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		SequenceAnnotation annotation = data.get(rowIndex);
		switch (columnIndex) {
		case 0:
			try {
				String s = (String) aValue;
				int pos = s.startsWith(ProteinEditor.PREFIX_CHAIN) ? Integer
						.parseInt(s.substring(ProteinEditor.PREFIX_CHAIN
								.length())) : 0;
				if (pos < 0) {
					pos = 0;
				}
				annotation.setId(pos);
			} catch (NumberFormatException nfe) {
			}
			break;
		case 1:
			annotation.setAnnotation((String) aValue);
			break;
		}
	}

	public synchronized void setData(List<SequenceAnnotation> newData) {
		data.clear();
		for (int i = 0; i < newData.size(); i++) {
			data.add(newData.get(i));
		}
		fireTableDataChanged();
	}

	public synchronized void setupEmptyData(int count) {
		data.clear();
		for (int i = 0; i < count; i++) {
			data.add(new SequenceAnnotation());
		}
		fireTableDataChanged();
	}

	public synchronized List<SequenceAnnotation> getPopulatedSeqeuenceAnnotations() {
		List<SequenceAnnotation> l = new ArrayList<SequenceAnnotation>();
		for (SequenceAnnotation ann : data) {
			if (null != ann.getAnnotation() && ann.getAnnotation().length() > 0
					&& ann.getId() > 0) {
				l.add(ann);
			}
		}
		return l;
	}

	public synchronized void clear() {
		data.clear();
		fireTableDataChanged();
	}
}
