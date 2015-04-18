package org.roche.antibody.ui.components.toolsmenu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.helm.editor.editor.MacromoleculeEditor;

import com.quattroresearch.antibody.LoadAntibodyFromBackendDialog;

/**
 * Starts Antibody Loading Dialog.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class LoadAntibodyAction extends AbstractAction {

  /** Generated UID */
  private static final long serialVersionUID = 8349196639441490984L;

  private MacromoleculeEditor editor;

  private LoadAntibodyFromBackendDialog registerDialog;

  public LoadAntibodyAction(MacromoleculeEditor editor) {
    super("Load Antibody ...");

    this.editor = editor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (registerDialog == null) {
      registerDialog = new LoadAntibodyFromBackendDialog(editor, true);
      registerDialog.setLocationRelativeTo(editor.getFrame());
    }
    registerDialog.setVisible(true);
    registerDialog = null;
  }
}
