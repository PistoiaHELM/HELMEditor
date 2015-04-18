/*--
 *
 * @(#) CysteinConnectionBuilder.java
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
package org.roche.antibody.services.graphsynchronizer;

import java.util.Map;

import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.DomainService;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code CysteinConnectionBuilder}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: CysteinConnectionBuilder.java 13993 2014-12-12 12:30:53Z schirmb $
 */
class CysteinConnectionBuilder extends ConnectionBuilder<CysteinConnection> {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(CysteinConnectionBuilder.class);

  /**
   * 
   * {@inheritDoc}
   */
  @Override
  public CysteinConnection build(HELMConnection helmConnection, Map<HELMElement, Sequence> sequenceMap) {
    Domain source = (Domain) sequenceMap.get(helmConnection.getSource());
    Domain target = (Domain) sequenceMap.get(helmConnection.getTarget());
    int sourcePos = 0;
    int targetPos = 0;

      sourcePos = DomainService.getInstance().transformDomainPositionToPeptidePosition(source, helmConnection.getSourcePosition());
    targetPos =
        DomainService.getInstance().transformDomainPositionToPeptidePosition(target, helmConnection.getTargetPosition());

    CysteinConnection conn =
        new CysteinConnection(sourcePos, targetPos, source.getPeptide(), target.getPeptide());
    LOG.debug("CysteinConnection created: {}", conn);
    return conn;
  }
}
