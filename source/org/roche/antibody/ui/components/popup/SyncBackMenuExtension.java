/*--
 *
 * @(#) SequenceEditorExtensionMenu.java
 *
 * Copyright 2013 by Roche Diagnostics GmbH,
 * Nonnenwald 2, DE-82377 Penzberg, Germany
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Roche Diagnostics GmbH ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Roche Diagnostics GmbH.
 *
 */
package org.roche.antibody.ui.components.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.helm.editor.controller.ModelController;
import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.ui.components.AntibodyEditorPane;

/**
 * {@code SequenceEditorExtensionMenu}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: SyncBackMenuExtension.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class SyncBackMenuExtension extends JMenuItem implements ActionListener {

  /** */
  private static final long serialVersionUID = 1L;

  private MacromoleculeEditor mainEditor;

  private AntibodyEditorPane abEditor;

  public SyncBackMenuExtension(final MacromoleculeEditor mainEditor, final AntibodyEditorPane abEditor) {
    this.mainEditor = mainEditor;
    this.abEditor = abEditor;
    this.setText("Sync with Antibody View");
    this.setEnabled(false);
    this.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    boolean submitChange = false;

    try {
      submitChange = abEditor.getGraphSyncer().syncBackToAntibody(mainEditor.getNotation());
    } catch (FileNotFoundException e1) {
      JOptionPane.showMessageDialog(abEditor, "Please check the filepath in the Antibody Editor Settings", "Domain definition file not found", JOptionPane.ERROR_MESSAGE);
    }
    if (submitChange) {
      ModelController.notationUpdated("", mainEditor.getOwnerCode());
      this.setEnabled(false);
    }
  }

}
