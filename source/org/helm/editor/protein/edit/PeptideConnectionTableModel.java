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

public class PeptideConnectionTableModel extends AbstractTableModel {

    public static final int BOND_TYPE_COLUMN_INDEX = 0;
     public static final int SOURCE_CHAIN_COLUMN_INDEX = 1;
      public static final int TARGET_CHAIN_COLUMN_INDEX = 3;
    private String[] columnNames = {"Bond Type", "Source Chain", "Source AA #", "Target Chain", "Target AA #"};
    private final List<PeptideConnection> data = new ArrayList<PeptideConnection>();

    public PeptideConnectionTableModel() {
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
            if (data.get(i).getSourceSequenceIndex() == k || data.get(i).getTargetSequenceIndex() == k) {
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
        PeptideConnection con = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return con.getBondType();
            case 1:
                if (con.getSourceSequenceIndex() > 0) {
                    return ProteinEditor.PREFIX_CHAIN + String.valueOf(con.getSourceSequenceIndex());
                } else {
                    return "";
                }
            case 2:
                if (con.getSourceAminoAcidIndex() > 0) {
                    return String.valueOf(con.getSourceAminoAcidIndex());
                } else {
                    return "";
                }

            case 3:
                if (con.getTargetSequenceIndex() > 0) {
                    return ProteinEditor.PREFIX_CHAIN + String.valueOf(con.getTargetSequenceIndex());
                } else {
                    return "";
                }
            case 4:
                if (con.getTargetAminoAcidIndex() > 0) {
                    return String.valueOf(con.getTargetAminoAcidIndex());
                } else {
                    return "";
                }
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PeptideConnection con = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                con.setBondType((String) aValue);
                break;
            case 1:
                try {
                    String s = (String)aValue;
                    int pos = s.startsWith(ProteinEditor.PREFIX_CHAIN) ? Integer.parseInt(s.substring(ProteinEditor.PREFIX_CHAIN.length())) : 0;
                    if (pos > 0) {
                        con.setSourceSequenceIndex(pos);
                    } else {
                        con.setSourceSequenceIndex(0);
                    }
                } catch (NumberFormatException nfe) {
                    con.setSourceSequenceIndex(0);
                }
                break;

            case 2:
                try {
                    int pos = Integer.parseInt((String) aValue);
                    if (pos > 0) {
                        con.setSourceAminoAcidIndex(pos);
                    } else {
                        con.setSourceAminoAcidIndex(0);
                    }
                } catch (NumberFormatException nfe) {
                    con.setSourceAminoAcidIndex(0);
                }
                break;

            case 3:
                try {
                    String s = (String)aValue;
                    int pos = s.startsWith(ProteinEditor.PREFIX_CHAIN) ? Integer.parseInt(s.substring(ProteinEditor.PREFIX_CHAIN.length())) : 0;
                    if (pos > 0) {
                        con.setTargetSequenceIndex(pos);
                    } else {
                        con.setTargetSequenceIndex(0);
                    }
                } catch (NumberFormatException nfe) {
                    con.setTargetSequenceIndex(0);
                }
                break;

            case 4:
                try {
                    int pos = Integer.parseInt((String) aValue);
                    if (pos > 0) {
                        con.setTargetAminoAcidIndex(pos);
                    } else {
                        con.setTargetAminoAcidIndex(0);
                    }
                } catch (NumberFormatException nfe) {
                    con.setTargetAminoAcidIndex(0);
                }
                break;
        }
    }

    public synchronized void setData(List<PeptideConnection> newData) {
        data.clear();
        for (int i = 0; i < newData.size(); i++) {
            data.add(newData.get(i));
        }
        fireTableDataChanged();
    }

    public synchronized void setupEmptyData(int count) {
        data.clear();
        for (int i = 0; i < count; i++) {
            data.add(new PeptideConnection());
        }
        fireTableDataChanged();
    }

    public synchronized List<PeptideConnection> getPopulatedConnections() {
        List<PeptideConnection> l = new ArrayList<PeptideConnection>();
        for (PeptideConnection con : data) {
            if (null != con.getBondType() && con.getBondType().length() > 0
                    && con.getSourceSequenceIndex() > 0 && con.getSourceAminoAcidIndex() > 0
                    && con.getTargetSequenceIndex() > 0 && con.getTargetAminoAcidIndex() > 0) {
                l.add(con);
            }
        }
        return l;
    }

    public synchronized void clear() {
        data.clear();
        fireTableDataChanged();
    }
}
