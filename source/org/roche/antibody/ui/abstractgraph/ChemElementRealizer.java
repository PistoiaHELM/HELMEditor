/*--
 *
 * @(#) ChemElementRealizer.java
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
package org.roche.antibody.ui.abstractgraph;

import java.awt.Color;
import java.awt.Graphics2D;

import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.services.AbConst;

import y.view.ShapeNodeRealizer;

/**
 * {@code ChemElementRealizer}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: ChemElementRealizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class ChemElementRealizer extends ShapeNodeRealizer implements AbstractGraphElementInitializer {

  private static final int DEFAULT_HEIGHT = 20;

  private static final int DEFAULT_WIDTH = 80;

  boolean initialized = false;

  public ChemElementRealizer() {
    super();
    initFromMap();
  }

  public void initFromMap() {

    try {

      setShapeType(OCTAGON);
      setFillColor(Color.YELLOW);
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      ChemElement mol = (ChemElement) getNode().getGraph()
          .getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY)
          .get(getNode());
      if (mol != null) {
        setLabelText(mol.getName());
        initialized = true;
      }

    } catch (NullPointerException e) {
      // Not yet ready for initialization
    }
  }

  @Override
  protected void paintNode(Graphics2D graph) {
    if (!initialized) {
      initFromMap();
    }
    super.paintNode(graph);
  }
}
