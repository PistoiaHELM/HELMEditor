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

import javax.swing.JMenuItem;

import org.helm.editor.controller.ModelController;
import org.helm.editor.editor.MacromoleculeEditor;

/**
 * {@code SequenceEditorExtensionMenu} This action cancels the editing of a Domain in the {@link MacromoleculeEditor}
 * and resets the view.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: SequenceEditorExtensionMenu.java 221 2013-12-17 08:13:49Z zilchs $
 */
public class CancelABEditingMenuExtension extends JMenuItem implements ActionListener {

  /** */
  private static final long serialVersionUID = 1L;
  
  private  MacromoleculeEditor mainEditor;

  private JMenuItem syncBackItem;

  public CancelABEditingMenuExtension(final MacromoleculeEditor mainEditor, JMenuItem syncBackItem) {
    this.mainEditor = mainEditor;
    this.syncBackItem = syncBackItem;
    this.setText("Cancel Domain Editing");
    this.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ModelController.notationUpdated("", mainEditor.getOwnerCode());
    syncBackItem.setEnabled(false);
  }

}
