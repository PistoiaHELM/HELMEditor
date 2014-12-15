/*--
 *
 * @(#) DomainNodeRealizer.java
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
import org.roche.antibody.model.antibody.ChainType;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.services.AbConst;

import y.view.LineType;
import y.view.ShapeNodeRealizer;

/**
 * {@code DomainNodeRealizer} Realizer for a {@link Domain}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: DomainNodeRealizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class DomainNodeRealizer extends ShapeNodeRealizer implements AbstractGraphElementInitializer {

  private static final int DEFAULT_HEIGHT = 40;

  private static final int DEFAULT_WIDTH = 160;

  private static final int HINGE_HEIGHT = 35;

  private static final int HINGE_WIDTH = 140;

  // color
// public static final Color RED_LIGHT = new Color(0xFFE6EE);

  public static final Color RED_HEAVY = new Color(0xFFBDD2);

// public static final Color BLUE_LIGHT = new Color(0xCCDFFF);

  public static final Color BLUE_HEAVY = new Color(0xB0CDFF);

// public static final Color GRAY_LIGHT = new Color(0xDEDCDC);

  public static final Color GRAY_HEAVY = new Color(0xC7C5C5);

  public static final Color[] PARATOPE_COLORS = new Color[] {new Color(0xF3F781),
      new Color(0xA6F58C), new Color(0xC2B28D), new Color(0x9BFACE)};

  boolean initialized = false;

  public DomainNodeRealizer() {
    super();
    initFromMap();
  }

  public DomainNodeRealizer(Domain d) {
    super();
    initFromMap(d);
  }

  public void initFromMap() {
    try {
      Domain domain = (Domain) getNode().getGraph()
          .getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY)
          .get(getNode());
      initFromMap(domain);
    } catch (NullPointerException e) {
      // Not yet ready for initialization
    }
  }

  public void initFromMap(Domain domain) {
    if (domain != null) {
      findFillColor(domain);
      findShape(domain);
      findBorderLine(domain);
      setLabelText(buildLabel(domain));
      findSize(domain);
      initialized = true;
    }
  }

  @Override
  protected void paintNode(Graphics2D graph) {
    // ML 2014-03-21 multiple refreshing errors. Fast workaround needed for demo.
    // TODO restructure refreshing (when label, connections, etc. change)
    // if (!initialized) {
    initFromMap();
    // }
    super.paintNode(graph);
  }

  private void findFillColor(Domain domain) {
    switch (domain.getHumanessType()) {
    case HUMAN:
// if (domain.getChainType().equals(ChainType.HEAVY)) {
        setFillColor(BLUE_HEAVY);
// } else {
// setFillColor(BLUE_LIGHT);
// }
      break;
    case HUMANIZABLE:
// if (domain.getChainType().equals(ChainType.HEAVY)) {
        setFillColor(RED_HEAVY);
// } else {
// setFillColor(RED_LIGHT);
// }
      break;
    case NON_HUMAN:
// if (domain.getChainType().equals(ChainType.HEAVY)) {
        setFillColor(GRAY_HEAVY);
// } else {
// setFillColor(GRAY_LIGHT);
// }
      break;
    default:
      setFillColor(GRAY_HEAVY);
    }

    if (domain.getParatope() != null) {
      setFillColor2(PARATOPE_COLORS[domain.getParatope() - 1]);
    } else {
      setFillColor2(null);
    }
  }

  private void findShape(Domain domain) {
    if (domain.isConstant() || domain.isHinge()) {
      setShapeType(ROUND_RECT);
    }
    if (domain.isUnknownDomainType()) {
      setShapeType(ELLIPSE);
    }
    if (domain.isVariable()) {
      setShapeType(OCTAGON);
    }
  }

  private void findBorderLine(Domain domain) {
    if (domain.getFreeCysteinPositions().size() == 0) {
      this.setLineColor(Color.BLACK);
    } else {
      this.setLineColor(Color.RED);
    }

    if (domain.getChainType() == ChainType.HEAVY) {
      this.setLineType(LineType.LINE_2);
    } else if (domain.getChainType() == ChainType.NONE) {
      this.setLineType(LineType.DOTTED_4);
    } else {
      this.setLineType(LineType.DASHED_2);
    }
  }

  private void findSize(Domain domain) {
    if (domain.isHinge()) {
      setSize(HINGE_WIDTH, HINGE_HEIGHT);
    } else {
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
  }

  private String buildLabel(Domain domain) {
    String result = domain.getUserLabel();
    if (StringUtils.isNotBlank(domain.getUserComment())) {
      result += "\n" + domain.getUserComment();
    }
    return result;
  }

}
