/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.roche.antibody.ui.propertytyble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.DomainLibraryValues;
import org.roche.antibody.services.AbConst;
import org.roche.antibody.services.ConnectionService;

import com.quattroresearch.antibody.ISingleMutationRead;
import com.quattroresearch.antibody.UnknownMutation;

/**
 * {@code DomainTableModel}
 * 
 * TableModel for Property-Table on the right side of the Antibody Editor
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id$
 */
public class DomainTableModel extends AbstractTableModel {

  /** */
  private static final long serialVersionUID = 1L;

  static final int PROPERTY_COLUMN = 0;

  private ArrayList<TableValueRow> data = new ArrayList<TableValueRow>();

  private String[] columnNames = {"Property", "Description"};

  private Domain curModel;

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data.get(rowIndex);
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (data.get(rowIndex).getName().equalsIgnoreCase(TableDataKeys.USER_LABEL)) {
      curModel.setUserLabel(aValue.toString());
    }

    if (data.get(rowIndex).getName().equalsIgnoreCase(TableDataKeys.USER_COMMENT)) {
      curModel.setUserComment(aValue.toString());
    }
    updateModel();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnNames[columnIndex];
  }

  public void setModel(Domain dom) {
    this.curModel = dom;
    if (dom == null) {
      clearModel();
    } else {
      updateModel();
    }
  }

  /**
   * 
   */
  public void updateModel() {
    Map<String, Object> tableData = getTableData(curModel);

    // --------
    data.clear();
    data.add(new TableValueRow("> CHAIN", true));
    for (String key : TableDataKeys.getChainSectionKeys()) {
      Object value = tableData.get(key);
      data.add(new TableValueRow(key, value == null ? "" : value.toString()));
    }

    data.add(new TableValueRow("> DOMAIN", true));
    for (String key : TableDataKeys.getDomainSectionKeys()) {
      Object value = tableData.get(key);
      boolean isEditable = key.equals(TableDataKeys.USER_LABEL) || key.equals(TableDataKeys.USER_COMMENT);

      // do not add paratope when none was detected
      if (!(key.equals(TableDataKeys.PARATOPE) && value == null)) {
        data.add(new TableValueRow(key, (value == null ? "" : value.toString()), isEditable));
      }
    }

    // Annotations and mutations. Keys may vary!
    data.add(new TableValueRow("> ANNOTATIONS", true));
    for (String key : tableData.keySet()) {
      if (key.startsWith(TableDataKeys.ANNOTATION_PREFIX)) {
        data.add(new TableValueRow(tableData.get(key).toString(), null));
      }
    }
    data.add(new TableValueRow("> MUTATIONS", true));
    for (String key : tableData.keySet()) {
      if (key.startsWith(TableDataKeys.MUTATION_PREFIX)) {
        if (tableData.get(key) != null && tableData.get(key) instanceof ISingleMutationRead) {
          ISingleMutationRead singleMutation = (ISingleMutationRead) tableData.get(key);
          data.add(new TableValueRow(singleMutation.getMutation().getMutationName(), singleMutation.toString()));
        }
      }
    }

    // Unknown mutations
    data.add(new TableValueRow("> UNKNOWN MUTATIONS", true));
    List<UnknownMutation> unknownMutationList = new ArrayList<UnknownMutation>();
    for (String key : tableData.keySet()) {
      if (key.startsWith(TableDataKeys.UNKNOWN_MUTATION_PREFIX)) {
        if (tableData.get(key) != null && tableData.get(key) instanceof UnknownMutation) {
          UnknownMutation unknownMutation = (UnknownMutation) tableData.get(key);
          unknownMutationList.add(unknownMutation);
        }
      }
    }
    // Necessary for displaying in the right order
    Collections.sort(unknownMutationList);
    for (UnknownMutation um : unknownMutationList) {
      data.add(new TableValueRow("", um.toString()));
    }

    // Bridges / Connections / Library values
    data.add(new TableValueRow("> CYS BRIDGES", true));
    for (String key : TableDataKeys.getCysBridgeSectionKeys()) {
      Object value = tableData.get(key);
      data.add(new TableValueRow(key, (value == null ? "" : value.toString())));
    }
    data.add(new TableValueRow("> OTHER BRIDGES", true));
    for (String key : TableDataKeys.getOtherBridgeSectionKeys()) {
      Object value = tableData.get(key);
      data.add(new TableValueRow(key, (value == null ? "" : value.toString())));
    }
    if (curModel.getLibraryValues() != null) {
      data.add(new TableValueRow("> RECOGNIZED DOMAIN FROM LIBRARY", true));
      for (String key : TableDataKeys.getLibrarySectionKeys()) {
        Object value = tableData.get(key);
        data.add(new TableValueRow(key, (value == null ? "" : value.toString())));
      }
    }

    fireTableDataChanged();
  }

  /**
   * Creates a table (Key/value pairs) of the data to show in the properties table for a selected domain.
   * 
   * @param domain
   * @return
   */
  public static Map<String, Object> getTableData(Domain domain) {
    Map<String, Object> tableData = new TreeMap<String, Object>();

    // > CHAIN data
    tableData.put(TableDataKeys.NAME, domain.getPeptide().getName());
    tableData.put(TableDataKeys.SEQUENCE_LENGTH, getStringLength(domain.getPeptide().getSequence()));
    tableData.put(TableDataKeys.SEQUENCE, domain.getPeptide().getSequence());
    tableData.put(TableDataKeys.SEQUENCE, domain.getPeptide().getOriginalSequence());

    // > DOMAIN data
    tableData.put(TableDataKeys.USER_LABEL, domain.getUserLabel());
    tableData.put(TableDataKeys.USER_COMMENT, domain.getUserComment());
    tableData.put(TableDataKeys.START_POS_ON_CHAIN, domain.getStartPosition());
    tableData.put(TableDataKeys.END_POS_ON_CHAIN, domain.getEndPosition());
    tableData.put(TableDataKeys.LENGTH, (domain.getEndPosition() - domain.getStartPosition() + 1));
    tableData.put(TableDataKeys.START_POS_ON_TEMPLATE, domain.getStartTemplatePos());
    tableData.put(TableDataKeys.END_POS_ON_TEMPLATE, domain.getEndTemplatePos());
    if (domain.isCoverageAndIdentityValid()) {
      tableData.put(TableDataKeys.IDENTITY_TO_LIB_DOM, AbConst.PERCENT.format(domain.getIdentity()));
      tableData.put(TableDataKeys.COVERAGE_OF_LIB_DOM, AbConst.PERCENT.format(domain.getCoverage()));
    }
    tableData.put(TableDataKeys.LEADING_SEQUENCE, domain.getPreSubsequence());
    tableData.put(TableDataKeys.SEQUENCE_DE_FACTO, domain.getSequence());
    tableData.put(TableDataKeys.TRAILING_SEQUENCE, domain.getPostSequence());
    if (domain.getParatope() != null) {
      tableData.put(TableDataKeys.PARATOPE, domain.getParatope().toString());
    }

    // XXX: "> ANNOTATIONS" Stored with prefix "ANNOTATION_" in the Library.
    // XXX: "> MUTATIONS" Stored with prefix "MUTATION_" in the Library.
    // XXX: "> UNKNOWN MUTATIONS" Stored with prefix "UNKNOWN_MUTATION_" in the library.
    int counter = 1;
    for (String annotation : domain.getAntibody().getAnnotations()) {
      tableData.put(TableDataKeys.ANNOTATION_PREFIX + (counter++), annotation);
    }
    counter = 1;
    for (ISingleMutationRead singleMutation : domain.getSingleMutations()) {
      tableData.put(TableDataKeys.MUTATION_PREFIX + (counter++), singleMutation);
    }
    counter = 1;
    if (!domain.isVariable()) {
      for (UnknownMutation unknownMutation : domain.getUnknownMutations()) {
        tableData.put(TableDataKeys.UNKNOWN_MUTATION_PREFIX + (counter++), unknownMutation);
      }
    }

    // > CysBridgeData
    tableData.put(TableDataKeys.CYS_POS, domain.getCysteinPositions());
    tableData.put(TableDataKeys.CYS_BRIDGES, ConnectionService.getInstance().getCysteinConnectionsOnly(domain.getConnections()));
    tableData.put(TableDataKeys.FREE_CYS, domain.getFreeCysteinPositions());
    // > OTHER BRIDGES data
    tableData.put(TableDataKeys.GENERAL, ConnectionService.getInstance().getGeneralConnectionsOnly(domain.getConnections()));
    DomainLibraryValues libValues = domain.getLibraryValues();
    if (libValues != null) {
      // > RECOGNIZED DOMAIN FROM LIBRARY data
      tableData.put(TableDataKeys.NAME_IN_LIB, libValues.getName());
      tableData.put(TableDataKeys.SHORT_NAME, libValues.getShortName());
      tableData.put(TableDataKeys.SPECIES, libValues.getSpecies());
      tableData.put(TableDataKeys.HUMANNESS, libValues.getHumanessType());
      tableData.put(TableDataKeys.CHAIN, libValues.getChainType());
      tableData.put(TableDataKeys.DOMAIN_TYPE, libValues.getDomainType());
      tableData.put(TableDataKeys.SEQUENCE_LENGTH_IN_LIB, libValues.getSequenceLength());
      tableData.put(TableDataKeys.CYS_COUNT, libValues.getCysteinCount());
      tableData.put(TableDataKeys.PATTERN, libValues.getCysteinPatterns());
      tableData.put(TableDataKeys.COMMENT, libValues.getComment());
      tableData.put(TableDataKeys.SEQUENCE_TEMPLATE, libValues.getSequence());
    }

    return tableData;
  }

  /**
   * @param sequence
   * @return
   */
  private static Integer getStringLength(String sequence) {
    return sequence == null ? 0 : sequence.length();
  }

  private void clearModel() {
    data.clear();
    fireTableDataChanged();
  }

  /**
   * We make Value cells editable in order to mark and copy the value. but we don't change the value.
   * 
   * {@inheritDoc}
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 1;
  }

}
