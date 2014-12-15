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
package org.roche.antibody.ui.filechooser;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.roche.antibody.services.PreferencesService;

/**
 * Abstract file reader for various file input/output associated with HELM Antibody extension
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * 
 */
public class AntibodyFileChooser extends JFileChooser {

  /** */
  private static final long serialVersionUID = 2671018958219737872L;

  // supported files
  protected FileNameExtensionFilter helm = new FileNameExtensionFilter("HELM Notation (*.helm)", "helm");

  protected FileNameExtensionFilter txt = new FileNameExtensionFilter("Text File (*txt)", "txt");

  protected FileNameExtensionFilter gp = new FileNameExtensionFilter("GP File (*.gp)", "gp");

  protected FileNameExtensionFilter graphml = new FileNameExtensionFilter("GraphML File (*.graphml)", "graphml");

  protected FileNameExtensionFilter jpeg = new FileNameExtensionFilter("JPEG/JPG (*.jpg)", "jpg");

  protected FileNameExtensionFilter png = new FileNameExtensionFilter("PNG (*.png)", "png");

  protected FileNameExtensionFilter gif = new FileNameExtensionFilter("GIF (*.gif)", "gif");

  protected FileNameExtensionFilter csv = new FileNameExtensionFilter("CSV (*.csv)", "csv");

  protected FileNameExtensionFilter fa = new FileNameExtensionFilter("FASTA File (*.fa)", "fa");

  protected FileNameExtensionFilter abXml = new FileNameExtensionFilter("Antibody-XML (*.xml)", "xml");
  
  protected PreferencesService prefService = PreferencesService.getInstance();

  public static final String XML_EXTENSION = ".xml";

  public AntibodyFileChooser() {
    setCurrentDirectory(getLastDirectory());
  }
  
  public String getLastDirectoryProperty() {
    return PreferencesService.LAST_FILE_FOLDER;
  };
  
  public File getLastDirectory() {
    return new File(prefService.getUserPrefs().get(getLastDirectoryProperty(), System.getProperty("user.home")));
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JFileChooser#approveSelection()
   */
  @Override
  public void approveSelection() {
    File f = getSelectedFile();
    if (f.exists() && getDialogType() == SAVE_DIALOG) {
      int result =
          JOptionPane.showConfirmDialog(this, "File already exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
      switch (result) {
      case JOptionPane.YES_OPTION:
        super.approveSelection();
        prefService.getUserPrefs().put(getLastDirectoryProperty(), f.getParent());
        return;
      case JOptionPane.NO_OPTION:
        return;
      case JOptionPane.CLOSED_OPTION:
        return;
      case JOptionPane.CANCEL_OPTION:
        cancelSelection();
        return;
      }
    }
    prefService.getUserPrefs().put(getLastDirectoryProperty(), f.getParent());
    super.approveSelection();
  }

}
