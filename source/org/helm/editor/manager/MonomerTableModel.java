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
package org.helm.editor.manager;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.model.Monomer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author zhangtianhong
 */
public class MonomerTableModel extends AbstractTableModel {

	private List<Monomer> monomers;
	private String[] columnNames;
	private String polymerType;
	private String[] polymerTypes;

	public MonomerTableModel(String polymerType) {
		this(polymerType, MonomerStoreCache.getInstance()
				.getCombinedMonomerStore());
	}

	public MonomerTableModel(String polymerType, MonomerStore monomerStore) {
		init(polymerType, monomerStore);
	}

	public int getRowCount() {
		if (monomers == null) {
			return 0;
		} else {
			return monomers.size();

		}
	}

	public int getColumnCount() {
		return columnNames.length;

	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Monomer mo = monomers.get(rowIndex);
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

	public List<Monomer> getMonomerList() {
		return monomers;
	}

	public String[] getPolymerTypes() {
		return polymerTypes;
	}

	private void init(String polymerType, MonomerStore monomerStore) {

		this.polymerType = polymerType;
		columnNames = new String[] { "Symbol", "Natural Analog", "Name",
				"Structure" };

		if (monomerStore == null) {
			return;
		}
		Map<String, Map<String, Monomer>> map = null;
		try {

			map = monomerStore.getMonomerDB();
			// map = MonomerFactory.getInstance().getMonomerDB();
		} catch (Exception ex) {
			Logger.getLogger(MonomerTableModel.class.getName()).log(
					Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(null,
					"Error reading monomer DB document", "Warning",
					JOptionPane.WARNING_MESSAGE);
			map = null;
		}

		monomers = new ArrayList<Monomer>();
		if (null != map) {
			Set<String> typeSet = map.keySet();
			polymerTypes = typeSet.toArray(new String[typeSet.size()]);

			Map<String, Monomer> tmpMap = map.get(polymerType);
			if (null != tmpMap) {
				Set keyset = tmpMap.keySet();
				for (Iterator i = keyset.iterator(); i.hasNext();) {
					String key = (String) i.next();
					Monomer m = tmpMap.get(key);
					monomers.add(m);
				}
			}
		}
	}

}
