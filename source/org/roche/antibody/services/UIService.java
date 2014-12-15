/*--
 *
 * @(#) UIService.java
 *
 * Copyright 2014 by Roche Diagnostics GmbH,
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
package org.roche.antibody.services;

import java.awt.Component;
import java.awt.Dialog;

import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.ui.components.AntibodyEditorPane;

import com.quattroresearch.antibody.AntibodyFindDialog;

/**
 * {@code UIService}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: UIService.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class UIService {


  /** static Singleton instance */
  private static UIService instance;

  /** Private constructor for singleton */
  private UIService() {
  }

  /** Static getter method for retrieving the singleton instance */
  public synchronized static UIService getInstance() {
    if (instance == null) {
      instance = new UIService();
    }
    return instance;
  }


  /**
   * We add or replace the Antibody View Editor in the tab pane of the main editor window
   * 
   * @param editor - main window
   * @param findDialog - can be null, if user loads Antibody via XML functionality.
   * @return created {@link AntibodyEditorPane}
   */
  public AntibodyEditorPane addAntibodyViewEditor(MacromoleculeEditor editor, AntibodyFindDialog findDialog) {
    AntibodyEditorPane viewDialog = new AntibodyEditorPane(editor, Dialog.ModalityType.APPLICATION_MODAL, findDialog);
      Component curCmp;
      int tabCount = editor.getTabbedSequenceViewPanel().getTabbedPane().getComponentCount();
      for (int index = 0; index < tabCount; index++) {
        curCmp =
            editor.getTabbedSequenceViewPanel().getTabbedPane().getComponentAt(index);
        if (curCmp instanceof AntibodyEditorPane) {
          editor.getTabbedSequenceViewPanel().getTabbedPane().setComponentAt(index,
              viewDialog);
          editor.getTabbedSequenceViewPanel().getTabbedPane().setSelectedComponent(viewDialog);
        return viewDialog;
        }
      }
      editor.getTabbedSequenceViewPanel().getTabbedPane()
          .add("Antibody View Editor", viewDialog);
    editor.getTabbedSequenceViewPanel().getTabbedPane().setSelectedComponent(viewDialog);
    return viewDialog;
  }

}
