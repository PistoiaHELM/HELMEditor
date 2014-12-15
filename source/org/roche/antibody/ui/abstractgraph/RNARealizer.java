/*--
 *
 * @(#) RNARealizer.java
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

import org.apache.commons.lang.StringUtils;
import org.roche.antibody.model.antibody.RNA;
import org.roche.antibody.services.AbConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import y.view.ShapeNodeRealizer;

/**
 * {@code RNARealizer}
 * 
 * Realizer for {@link RNA} Elements
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: RNARealizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class RNARealizer extends ShapeNodeRealizer implements AbstractGraphElementInitializer {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(RNARealizer.class);

  private static final int DEFAULT_HEIGHT = 20;

  private static final int DEFAULT_WIDTH = 80;

  boolean initialized = false;

  public RNARealizer() {
    super();
    initFromMap();
  }

  public void initFromMap() {
    try {
      setShapeType(PARALLELOGRAM);
      setFillColor(Color.GREEN);
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      RNA mol = (RNA) getNode().getGraph()
          .getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY)
          .get(getNode());
      if (mol != null) {
        // JF 2014-06-16: Changed label to number of nucleid acids + bp (base pair)
        int countNucleidAcids = StringUtils.countMatches(mol.getName(), "(");
        StringBuilder sb = new StringBuilder();
        sb.append("NN (");
        sb.append(countNucleidAcids);
        sb.append("bp)");
        setLabelText(sb.toString());
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
