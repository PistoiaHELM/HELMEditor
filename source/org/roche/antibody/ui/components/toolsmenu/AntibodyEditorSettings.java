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
package org.roche.antibody.ui.components.toolsmenu;

import java.awt.Dialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ToolTipManager;

import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.ui.components.AntibodySettingsDialog;

/**
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * @author Stefan Zilch - BridgingIT GmbH
 * 
 */
public class AntibodyEditorSettings extends AbstractAction {

  /** */
  private static final long serialVersionUID = 3915001522538066691L;

  private MacromoleculeEditor editor;

  public AntibodyEditorSettings(MacromoleculeEditor editor) {
    super("Antibody Editor Settings");
    this.editor = editor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    AntibodySettingsDialog dialog = null;
    int oldDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
    try {
      ToolTipManager.sharedInstance().setDismissDelay(6000);
      dialog =
        new AntibodySettingsDialog(editor.getFrame(), Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setLocationRelativeTo(editor.getFrame());
    dialog.setVisible(true);
    } catch (NullPointerException ex) {
      // no dialog on error --> error handling inside
    } finally {
      ToolTipManager.sharedInstance().setDismissDelay(oldDismissDelay);
    }
  }

}