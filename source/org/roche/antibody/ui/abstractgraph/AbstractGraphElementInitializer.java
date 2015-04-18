/*--
 *
 * @(#) AbstractGraphElementInitializer.java
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


/**
 * {@code AbstractGraphElementInitializer} should be implemented by all Realizers used to visualize an antibody graph.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:clemens.wrzodek@roche.com">Clemens Wrzodek</a>
 * @version $Id: AbstractGraphElementInitializer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public interface AbstractGraphElementInitializer {
  /**
   * Setup the element (shape, color, line type, etc.)
   */
  public void initFromMap();
}
