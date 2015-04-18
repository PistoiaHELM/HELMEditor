/*--
 *
 * @(#) GeneralConnectionEdgeRealizer.java
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

import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.services.AbConst;

import y.base.DataProvider;
import y.view.GenericEdgeRealizer;

/**
 * Extended realizer class representing the edges connecting all domains within antibody chain (non-cystein connection)
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * 
 * @version $Id: GeneralConnectionEdgeRealizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class GeneralConnectionEdgeRealizer extends GenericEdgeRealizer implements AbstractGraphElementInitializer {
	
	/**
	 * Constructor
	 */
	public GeneralConnectionEdgeRealizer() {
    initFromMap();
	}

  @Override
  public void paint(Graphics2D gfx) {
    initFromMap();
    super.paint(gfx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initFromMap() {
    try {
      setLineColor(Color.BLUE);

      DataProvider cysBridgeMap = getEdge().getGraph().getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
      GeneralConnection bridge = (GeneralConnection) cysBridgeMap.get(getEdge());
      super.setLabelText(bridge.getSourcePosition() + ":" + bridge.getSourceRest() + "-" + bridge.getTargetPosition()
          + ":" + bridge.getTargetRest());

    } catch (Exception e) {
      // Not yet ready for initialization
    }
  }
}
