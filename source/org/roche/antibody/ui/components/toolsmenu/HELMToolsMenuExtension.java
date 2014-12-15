/*--
 *
 * @(#) HELMToolsMenuExtension.java
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
package org.roche.antibody.ui.components.toolsmenu;

import javax.swing.JMenu;
import javax.swing.JSeparator;

import org.helm.editor.editor.MacromoleculeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code HELMToolsMenuExtension} This is the extension for the Tools Menu in the {@link MacromoleculeEditor}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: HELMToolsMenuExtension.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class HELMToolsMenuExtension extends JMenu {

  /** */
  private static final long serialVersionUID = 1L;

  private static final int INSERT_POSITION = 4;

  private static final int TOOLS_MENU_INDEX = 1;

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(HELMToolsMenuExtension.class);

  public HELMToolsMenuExtension(MacromoleculeEditor editor) {
    LOG.debug("Injecting Antibody Tools Menu");
    this.setText("Antibody Tools");
    this.add(new JSeparator(JSeparator.HORIZONTAL));

    add(new AntibodyEditorAction(editor));
    add(new AntibodyLoadXmlAction(editor));
    add(new AntibodyDomainsLibraryEditorAction(editor));
    add(new JSeparator(JSeparator.HORIZONTAL));
    add(new RegisterAntibodyAction(editor));
    add(new LoadAntibodyAction(editor));
    add(new JSeparator(JSeparator.HORIZONTAL));
    add(new AntibodyEditorSettings(editor));

    JMenu toolsMenu = (JMenu) editor.getMenuBar().getComponent(TOOLS_MENU_INDEX);
    toolsMenu.add(this, INSERT_POSITION);
    toolsMenu.add(new JSeparator(JSeparator.HORIZONTAL), INSERT_POSITION);

  }

}
