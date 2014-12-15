package org.roche.antibody.ui.propertytyble;

/**
 * {@code TableValueRow} Representation of a Table Row in the Properties Table
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: TableValueRow.java 13993 2014-12-12 12:30:53Z schirmb $
 */
class TableValueRow {

  private String name = "";

  private String value = "";

  private boolean isTitle = false;

  private boolean isEditable = false;

  TableValueRow() {

  }

  TableValueRow(String name, String value) {
    this.name = name;
    this.value = value;
  }

  TableValueRow(String name, String value, boolean isEditable) {
    this.name = name;
    this.value = value;
    this.isEditable = isEditable;
  }

  TableValueRow(String name, boolean isTitle) {
    this.name = name;
    this.isTitle = isTitle;
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }

  public boolean isTitle() {
    return this.isTitle;
  }

  public boolean isEditable() {
    return this.isEditable;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setTitle(boolean isTitle) {
    this.isTitle = isTitle;
  }

  public void setEditable(boolean isEditable) {
    this.isEditable = isEditable;
  }

  @Override
  public String toString() {
    return this.value;
  }

}
