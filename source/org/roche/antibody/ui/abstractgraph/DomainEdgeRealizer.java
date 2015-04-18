/*--
 *
 * @(#) DomainEdgeRealizer.java
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

import java.awt.Graphics2D;

import y.view.GenericEdgeRealizer;
import y.view.LineType;

/**
 * Extended realizer class representing the edges connecting all domains within antibody chain (non-cystein connection)
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author Stefan Zilch - BridgingIT GmbH
 * @version $Id: DomainEdgeRealizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class DomainEdgeRealizer extends GenericEdgeRealizer implements AbstractGraphElementInitializer {

  public DomainEdgeRealizer() {
    super();
    initFromMap();
  }

  @Override
  public void paint(Graphics2D gfx) {
    super.paint(gfx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initFromMap() {
    this.setLineType(LineType.LINE_2);
  }

}
