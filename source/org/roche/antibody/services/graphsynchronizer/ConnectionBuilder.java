/*--
 *
 * @(#) ConnectionBuilder.java
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

import org.roche.antibody.model.antibody.Connection;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;

/**
 * {@code ConnectionBuilder}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: ConnectionBuilder.java 13993 2014-12-12 12:30:53Z schirmb $
 */
abstract class ConnectionBuilder<GINSTANCE extends Connection> {

  /**
   * @param helmConnection {@link HELMConnection}
   * @param sequenceMap e.g.: KEY={PEPTIDE|CHEM|RNA} - VALUE= {@link Sequence}
   * @return a specific {@link Connection} in reference to Builder Instance.
   */
  public abstract GINSTANCE build(HELMConnection helmConnection, Map<HELMElement, Sequence> sequenceMap);

}
