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

import org.helm.notation.NotationConstant;
import org.helm.notation.NucleotideFactory;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.tools.NucleotideSequenceParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author zhangtianhong
 */
public class NucleotideTableModel extends AbstractTableModel {

    private List<Nucleotide> nucleotides;
    private String[] columnNames;
    private String xmlString;

    public NucleotideTableModel(String xmlString) {
        init(xmlString);
    }

    public NucleotideTableModel() {
        init();
    }

    public int getRowCount() {
        if (nucleotides == null) {
            return 0;
        } else {
            return nucleotides.size();

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
        Nucleotide nu = nucleotides.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return nu.getSymbol();
            case 1:
                return nu.getNaturalAnalog();
            case 2:
                return nu.getNotation();
            default:
                return "N/A";
        }
    }

    public List<Nucleotide> getNucleotideList() {
        return nucleotides;
    }
    
    private void init(String xmlString) {
        columnNames = new String[]{
                    "Symbol",
                    "Natural Analog",
                    "Notation"
                };

        Map<String, Map<String, String>> map = null;

        nucleotides = new ArrayList<Nucleotide>();
        if (xmlString != null) {
            this.xmlString = xmlString;
            SAXBuilder builder = new SAXBuilder();
            Document doc;

            try {
                doc = builder.build(xmlString);
                Element rootElement = doc.getRootElement();
                map = NucleotideSequenceParser.getNucleotideTemplates(rootElement);
            } catch (Exception ex) {
                Logger.getLogger(NucleotideTableModel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Error parsing nucleotide template XML document", "Warning", JOptionPane.WARNING_MESSAGE);
                map = null;
            }

        } else {
            this.xmlString = null;
            try {
                map = NucleotideFactory.getInstance().getNucleotideTemplates();
            } catch (Exception ex) {
                Logger.getLogger(NucleotideTableModel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Error reading nucleotide template XML document", "Warning", JOptionPane.WARNING_MESSAGE);
                map = null;
            }
        }
        if (null != map) {
            Map<String, String> helmMap = map.get(NotationConstant.NOTATION_SOURCE);
            Set keyset = helmMap.keySet();
            for (Iterator i = keyset.iterator(); i.hasNext();) {
                String symbol = (String) i.next();
                String notation = helmMap.get(symbol);
                Nucleotide n = new Nucleotide(symbol, notation);
                nucleotides.add(n);
            }
        }

    }

    private void init() {
        init(null);
    }
}

