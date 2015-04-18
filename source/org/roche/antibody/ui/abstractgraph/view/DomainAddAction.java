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

import java.awt.Dialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.ui.components.AntibodyEditorPane;

/**
 * {@code DomainAddAction}: Action for adding a domain from library to selected domain.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class DomainAddAction extends AbstractAction {

	/** Generated UID */
	private static final long serialVersionUID = 6320593579348992920L;

	private AntibodyEditorPane editor;

	private Domain domain;
	private String selectedSequence;

	public DomainAddAction(AntibodyEditorPane editor, Domain domain) {
		super("Add Domain");
		this.domain = domain;
		this.editor = editor;
	}

	public void setSelectedSequence(String selectedSequence) {
		this.selectedSequence = selectedSequence;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		DomainAddDialog dialog = new DomainAddDialog(editor.getMainEditor()
				.getFrame(), Dialog.ModalityType.APPLICATION_MODAL, this);
		dialog.setLocationRelativeTo(editor.getMainEditor().getFrame());
		dialog.setVisible(true);

		if (selectedSequence != null) {
      try {
        editor.getGraphSyncer().sendToMacroMolecularEditor(domain,
            selectedSequence);
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

}
