/*--
 *
 * @(#) PeptideToMoleculeConnectionBuilder.java
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

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.DomainService;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code PeptideToMoleculeConnectionBuilder}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: GeneralConnectionBuilder.java 13993 2014-12-12 12:30:53Z schirmb $
 */
class GeneralConnectionBuilder extends ConnectionBuilder<GeneralConnection> {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(GeneralConnectionBuilder.class);

  @Override
  public GeneralConnection build(HELMConnection helmConnection, Map<HELMElement, Sequence> sequenceMap) {
    
    int sPos = helmConnection.getSourcePosition();
    int tPos = helmConnection.getTargetPosition();
    Sequence source = sequenceMap.get(helmConnection.getSource());
    Sequence target = sequenceMap.get(helmConnection.getTarget());

    if (source instanceof Domain) {
      Domain dom = (Domain)source;
      sPos = DomainService.getInstance().transformDomainPositionToPeptidePosition(dom, helmConnection.getSourcePosition());
      source = dom.getPeptide();
    }
    
    if (target instanceof Domain) {
      Domain dom = (Domain) target;
      tPos = DomainService.getInstance().transformDomainPositionToPeptidePosition(dom, helmConnection.getTargetPosition());
      target = dom.getPeptide();
    }
    
    GeneralConnection newConn =
        new GeneralConnection(source, target, sPos, tPos, helmConnection.getSourceRest(),
            helmConnection.getTargetRest());
    LOG.debug("New GeneralConnection created: {}", newConn);
    return newConn;
  }

}
