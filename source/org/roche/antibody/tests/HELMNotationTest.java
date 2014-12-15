/*--
 *
 * @(#) HELMNotationTest.java
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
package org.roche.antibody.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.roche.antibody.services.helmnotation.model.HELMPeptide;
import org.roche.antibody.services.helmnotation.model.HELMRna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code HELMNotationTest}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: HELMNotationTest.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class HELMNotationTest {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(HELMNotationTest.class);

  @Test
  public void testEqualConnection() {
    HELMElement rna = new HELMRna();
    HELMPeptide pep = new HELMPeptide("ABCDEFGHIJKLMN");
    HELMConnection conSourceTarget = new HELMConnection(rna, 1, "R1", pep, 3, "R3");
    HELMConnection conTargetSource = new HELMConnection(pep, 3, "R3", rna, 1, "R1");
    assertEquals("Connections are not equal", conSourceTarget, conTargetSource);
    assertEquals("Hash is different!", conSourceTarget.hashCode(), conTargetSource.hashCode());
  }

}
