/*--
 *
 * @(#) DomainEditAction.java
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
package org.roche.antibody.ui.abstractgraph.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.ui.components.AntibodyEditorPane;

/**
 * {@code DomainEditAction}: action for editing the selected domain.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: DomainEditAction.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class DomainEditAction extends AbstractAction {

  /** */
  private static final long serialVersionUID = 1L;

  private AntibodyEditorPane editor;

  private Domain domain;

  public DomainEditAction(AntibodyEditorPane editor, Domain domain) {
    super("Edit Domain");
    this.domain = domain;
    this.editor = editor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      editor.getGraphSyncer().sendToMacroMolecularEditor(domain);
    } catch (Exception e1) {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(editor, "Syncronization failed: "
          + e1.getMessage(), "Syncronization failed",
          JOptionPane.ERROR_MESSAGE);
    }
    editor.getSequenceEditorExtensionMenu().setEnabled(true);

    editor.updateGraphLayout();
  }

}
