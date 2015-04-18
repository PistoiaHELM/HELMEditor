/*--
 *
 * @(#) NodePopupMode.java
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
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.AbConst;
import org.roche.antibody.services.AbstractGraphService;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import y.base.Node;
import y.view.PopupMode;

/**
 * {@code NodePopupMode}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @author <a href="mailto:erdmann@quattro-research.com">Marco Erdmann</a>, quattro research GmbH
 * 
 * @version $Id: GraphPopupMode.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class GraphPopupMode extends PopupMode {

  /** The Logger for this class */
  @SuppressWarnings("unused")
private static final Logger LOG = LoggerFactory.getLogger(GraphPopupMode.class);

  private AntibodyEditorPane editor;

  public GraphPopupMode(AntibodyEditorPane editor) {
    this.editor = editor;
  }

  @Override
  public JPopupMenu getNodePopup(Node node) {
    Sequence seq = (Sequence) node.getGraph().getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY).get(node);
    JPopupMenu popup = null;
    if (seq instanceof Domain) {
      popup = new JPopupMenu();
	  popup.add(new DomainAddAction(editor, (Domain) seq));
      popup.add(new DomainEditAction(editor, (Domain) seq));
      popup.add(new DomainAnnotationAction(editor, (Domain) seq));
    }

    return popup;
  }

  @Override
  public JPopupMenu getPaperPopup(double x, double y) {
    JPopupMenu graphContextMenu = new JPopupMenu();
    JMenuItem item = new JMenuItem("Back to Domain Recognition");
    if (editor.getFindDialog() == null || !editor.getIsBackToDomainEditorEnabled()) {
      item.setEnabled(false);
    }
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        editor.getFindDialog().setVisible(true);
      }
    });
    graphContextMenu.add(item);

    item = new JMenuItem("Reset Layout");
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
// editor.updateGraphLayout();
        editor.setModel(AntibodyEditorAccess.getInstance().getAntibodyEditorPane().getAntibody());
      }
    });
    graphContextMenu.add(item);
    item = new JMenuItem("Reset Cystein Bridges");
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        AbstractGraphService.getInstance().resetCysteinBridges(editor.getAbstractGraph());
        editor.getAbstractGraph().updateViews();
      }
    });
    graphContextMenu.add(item);
    item = new JMenuItem("Reset Cystein Bridges + Layout");
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        AbstractGraphService.getInstance().resetCysteinBridges(editor.getAbstractGraph());
        editor.updateGraphLayout();
        editor.getAbstractGraph().updateViews();
      }
    });
    graphContextMenu.add(item);

    return graphContextMenu;
  }

}
