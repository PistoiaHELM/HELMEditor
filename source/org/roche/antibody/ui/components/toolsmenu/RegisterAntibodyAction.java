package org.roche.antibody.ui.components.toolsmenu;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.ui.components.AntibodyEditorAccess;

import com.quattroresearch.antibody.RegisterAntibodyDialog;

/**
 * Starts Antibody Registration Dialog.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class RegisterAntibodyAction extends AbstractAction {

  /** Generated UID */
  private static final long serialVersionUID = 8349196639441490984L;

  private MacromoleculeEditor editor;

  private RegisterAntibodyDialog registerDialog;

  public RegisterAntibodyAction(MacromoleculeEditor editor) {
    super("Register Antibody ...");

    this.editor = editor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Preferences prefs = PreferencesService.getInstance().getUserPrefs();
    boolean isAllLibsMaster = prefs.getBoolean(
        PreferencesService.USE_MASTER_DOMAIN_LIB, false);
    isAllLibsMaster = isAllLibsMaster && prefs.getBoolean(
        PreferencesService.USE_MASTER_MUTATION_LIB, false);
    isAllLibsMaster = isAllLibsMaster && prefs.getBoolean(
        PreferencesService.USE_MASTER_AUTOCONNECTOR_CONFIG, false);
    if (!isAllLibsMaster) {
      JOptionPane.showMessageDialog(editor.getFrame(), "Registration is only allowed when master libraries were used."
          + System.getProperty("line.separator") + "Please change the library settings accordingly.", "Registration forbidden", JOptionPane.ERROR_MESSAGE);
    }
    else if (AntibodyEditorAccess.getInstance().getAntibodyEditorPane() == null
        || AntibodyEditorAccess.getInstance().getAntibodyEditorPane().getAntibody() == null) {
      JOptionPane.showMessageDialog(editor.getFrame(), "There is no antibody to register.", "No antibody found", JOptionPane.ERROR_MESSAGE);
    } else {
      if (registerDialog == null) {
        registerDialog = new RegisterAntibodyDialog(editor, true);
        registerDialog.setLocationRelativeTo(editor.getFrame());
      }
      registerDialog.setVisible(true);
      registerDialog = null;
    }
  }
}
