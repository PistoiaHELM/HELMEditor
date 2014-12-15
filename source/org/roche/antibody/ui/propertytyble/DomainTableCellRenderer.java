package org.roche.antibody.ui.propertytyble;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * {@code DomainPropertyCellRenderer}
 * 
 * Cell-Renderer to layout and design the propertyTable a bit.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: DomainTableCellRenderer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class DomainTableCellRenderer implements TableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    if (value == null) {
      value = new TableValueRow();
    }
    TableValueRow element = (TableValueRow) value;
    JLabel result = new JLabel();
    result.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    result.setOpaque(true);
    result.setBackground(Color.WHITE);
    if (column == DomainTableModel.PROPERTY_COLUMN) {
      result.setText(element.getName());
      result.setFont(result.getFont().deriveFont(Font.BOLD));
    }
    else {
      result.setText(element.getValue());
      result.setToolTipText(element.getValue());
    }

    if (element.isTitle()) {
      result.setBackground(Color.LIGHT_GRAY);
    }
    
    if (element.isEditable()) {
      result.setBackground(new Color(0xFFFFE0));
    }
    return result;
  }

}
