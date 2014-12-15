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
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.services.AbstractGraphService;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.ui.abstractgraph.DomainNodeRealizer;
import org.roche.antibody.ui.components.AntibodyEditorPane;

import y.base.Node;

import com.quattroresearch.antibody.DomainDetectionStandalone;

/**
 * {@code DomainAnnotationAction}: action for re-blasting the selected domain.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 */
public class DomainAnnotationAction extends AbstractAction {

  /** Generated UID */
  private static final long serialVersionUID = -8894318297121202684L;

  private AntibodyEditorPane _editor;

  private Domain _domain;

  public DomainAnnotationAction(AntibodyEditorPane editor, Domain domain) {
    super("Annotate Domain");
    this._domain = domain;
    this._editor = editor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      annotateDomain(_editor, _domain);
    } catch (FileNotFoundException e1) {
      JOptionPane.showMessageDialog(_editor.getParent(), "Please check the filepath in the Antibody Editor Settings", "Domain definition file not found", JOptionPane.ERROR_MESSAGE);

    }
  }

  /**
   * Annotates a given domain in given editor. Because it is a static method, you may use it without to instantiate an
   * Action.
   * 
   * @param editor antibody editor
   * @param domain domain to annotate
   * @throws FileNotFoundException
   */
  public static void annotateDomain(AntibodyEditorPane editor, Domain domain) throws FileNotFoundException {
    ArrayList<String> name = new ArrayList<String>(1);
    ArrayList<String> sequence = new ArrayList<String>(1);
    name.add(domain.getName());
    sequence.add(domain.getSequence());

    // ML 2014-03-26: Switched to domain reannotation instead of domain
    // switching
    DomainDetectionStandalone domainDetection = new DomainDetectionStandalone(
        name, sequence, PreferencesService.getInstance()/* , null */);
    try {
      domainDetection.makeBlastDatabase();
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(editor, "Please check the filepath in the Antibody Editor Settings", "Domain definition file not found", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (Exception e) {
      JOptionPane.showMessageDialog(editor, "Creating blast database failed: " + e.getMessage(), "Blast failed", JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      domainDetection.annotateDomain(domain, domain.getPeptide().getSequence());
    } catch (Exception e) {
      JOptionPane.showMessageDialog(editor, "Annotation of domain '" + domain.getName() + "' failed: " + e.getMessage(), "Blast failed", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // ML 2014-03-14: relabel the node and repaint the graph
    Node changedNode = AbstractGraphService
        .findNodeBySequence(editor.getAbstractGraph(), domain);
    DomainNodeRealizer realizer = (DomainNodeRealizer) editor
        .getAbstractGraph().getRealizer(changedNode);
    realizer.initFromMap(domain);

    realizer.repaint();
  }
}
