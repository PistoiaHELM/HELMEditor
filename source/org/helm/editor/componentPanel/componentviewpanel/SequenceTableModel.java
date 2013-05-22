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

import org.helm.editor.utility.ExceptionHandler;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.ExtinctionCoefficientCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

public class SequenceTableModel extends AbstractTableModel {

    private static String ecColumn = "Ext. Coefficient";

    static {
        try {
            ecColumn = "Ext. Coefficient (" + ExtinctionCoefficientCalculator.getInstance().getUnit(ExtinctionCoefficientCalculator.RNA_UNIT_TYPE) + ")";
        } catch (Exception ex) {
        }
    }
    
    private String[] columnNames = {"Component", "Component Type", "Component Structure",
        "Molecular Weight", "Molecular Formula", ecColumn};
    private final List<SequenceTableDataModel> data = new ArrayList<SequenceTableDataModel>();

    public SequenceTableModel(String notation) {
        init(notation);
    }

    public SequenceTableModel() {
    }

    public void init(String notation) {
        try {
            String[] compNotations = ComplexNotationParser.decompose(notation);
            List<SequenceTableDataModel> list = new ArrayList<SequenceTableDataModel>();
            for (int rowNumber = 1; rowNumber<=compNotations.length; rowNumber++) {
                SequenceTableDataModel dataModel = SequenceTableDataModel.createSequenceTableDataModel(compNotations[rowNumber-1]);
                dataModel.setAnnotation(String.valueOf(rowNumber));
                list.add(dataModel);
            }
            setData(list);

        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
            Logger.getLogger(SequenceTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } 
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

    public Object getValueAt(int rowIndex, int columnIndex) {
        SequenceTableDataModel dataModel = data.get(rowIndex);
        return dataModel.getFiled(columnIndex);
    }

    public synchronized void setData(List<SequenceTableDataModel> newData) {
        synchronized (data) {
            data.clear();
            for (int i = 0; i < newData.size(); i++) {
                data.add(newData.get(i));
            }
        }

        fireTableDataChanged();
    }

    public void clear() {
        synchronized (data) {
            data.clear();
        }

        fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
