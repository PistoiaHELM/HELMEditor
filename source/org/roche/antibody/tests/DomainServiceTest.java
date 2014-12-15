/*--
 *
 * @(#) DomainServiceTest.java
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
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.services.DomainService;

/**
 * {@code DomainServiceTest}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: DomainServiceTest.java 13993 2014-12-12 12:30:53Z schirmb $
 */

public class DomainServiceTest {
  
  private static final int FIRST = 0;
  private static final int SECOND = 1;
  private DomainService ds = DomainService.getInstance();
  
  @Test
  public void testAddDomainFirst() throws Exception {
    String sequenceToTest = "STEFANCAPRILCLARSC";
    String newSequence = "STEFANC";
    Peptide pep = TestSuite.getTestPeptide();
    Domain dom = ds.addAsFirstDomain(newSequence, pep);
    assertTrue(StringUtils.startsWith(dom.getPeptide().getSequence(), newSequence));
    assertEquals("Wrong Sequence in Peptide", sequenceToTest, pep.getSequence());
    assertEquals("Wrong startPosition in Domain", 1, dom.getStartPosition());
    assertEquals("Wrong endPosition in Domain", 7, dom.getEndPosition());
    assertEquals("Wrong Peptide", pep, dom.getPeptide());
    assertEquals("Two domains expected!", 2, pep.getDomains().size());
    assertEquals("Domain is not on first position", dom, pep.getDomains().get(0));
    assertEquals("DomainName should be " + "NN (7AAs)", "NN (7AAs)", dom.getName());
    // we check the index offset of the second domain
    assertEquals(8, pep.getDomains().get(SECOND).getStartPosition());
    assertEquals(18, pep.getDomains().get(SECOND).getEndPosition());
    // we check the connection if indexes were updated
    assertEquals(13, pep.getConnections().get(FIRST).getSourcePosition());
    assertEquals(18, pep.getConnections().get(FIRST).getTargetPosition());
  }

  @Test
  public void testAddDomainLast() throws Exception {
    String sequenceToTest = "APRILCLARSCSTEFANC";
    String newSequence = "STEFANC";
    Peptide pep = TestSuite.getTestPeptide();
    Domain dom = ds.addAsLastDomain(newSequence, pep);
    assertTrue(StringUtils.endsWith(dom.getPeptide().getSequence(), newSequence));
    assertEquals("Wrong Sequence in Peptide", sequenceToTest, pep.getSequence());
    assertEquals("Wrong startPosition in Domain", 12, dom.getStartPosition());
    assertEquals("Wrong endPosition in Domain", 7, dom.getEndPosition());
    assertEquals("Wrong Peptide", pep, dom.getPeptide());
    assertEquals("Two domains expected!", 2, pep.getDomains().size());
    assertEquals("Domain is not on last position", dom, pep.getDomains().get(pep.getDomains().size() - 1));
    assertEquals("DomainName should be " + "NN (7AAs)", "NN (7AAs)", dom.getName());
    // we check the index offset of the second domain
    assertEquals(1, pep.getDomains().get(FIRST).getStartPosition());
    assertEquals(11, pep.getDomains().get(FIRST).getEndPosition());
    // we check the connection if indexes were updated
    assertEquals(6, pep.getConnections().get(FIRST).getSourcePosition());
    assertEquals(11, pep.getConnections().get(FIRST).getTargetPosition());
  }
}
