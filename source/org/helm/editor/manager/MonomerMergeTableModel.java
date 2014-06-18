package org.helm.editor.manager;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.helm.notation.model.Monomer;

public class MonomerMergeTableModel extends AbstractTableModel {

	private List<Monomer> monomers;
	private String[] columnNames;

	public MonomerMergeTableModel(List<Monomer> monomers) {

		this.monomers = monomers;
		columnNames = new String[] { "Symbol", "Natural Analog", "Name",
				"Structure" };
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		if (this.monomers == null) {
			return 0;
		} else {
			return this.monomers.size();

		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Monomer mo = this.monomers.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return mo.getAlternateId();
		case 1:
			return mo.getNaturalAnalog();
		case 2:
			return mo.getName();
		case 3:
			return mo.getCanSMILES();
		default:
			return "N/A";
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public List<Monomer> getMonomerList() {
		return this.monomers;
	}

}
