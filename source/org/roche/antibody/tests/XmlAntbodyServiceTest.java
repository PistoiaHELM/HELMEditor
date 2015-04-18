/*--
 *
 * @(#) XmlAntbodyServiceTest.java
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.roche.antibody.model.antibody.AntibodyContainer;
import org.roche.antibody.services.antibody.AntibodyService;
import org.roche.antibody.services.helmnotation.HelmNotationService;
import org.roche.antibody.services.xml.XmlAntibodyService;

/**
 * {@code XmlAntbodyServiceTest}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: XmlAntbodyServiceTest.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class XmlAntbodyServiceTest {

  private XmlAntibodyService xmlService = XmlAntibodyService.getInstance();

  private HelmNotationService helmService = HelmNotationService.getInstance();

  private AntibodyService abService = AntibodyService.getInstance();

  private File xmlFile = new File("test-antibody.xml");

  private String xmlString;

  @Test
  public void testMarshalAndUnmarshal() throws Exception {
    AntibodyContainer container = new AntibodyContainer();
    container.setAntibody(TestSuite.getTestAntibody());
    container.setHelmCode(helmService.toHELMString(abService.toHELM(TestSuite.getTestAntibody())));
    xmlService.marshal(container, xmlFile);
    assertTrue("XML-File was not created!", xmlFile.exists());

    AntibodyContainer abContainer = xmlService.unmarshal(xmlFile);
    assertNotNull("Antibody was null after unmarshal", abContainer.getAntibody());
    assertFalse("HELMCODE was null after unmarshal", StringUtils.isBlank(abContainer.getHelmCode()));
  }

  @Test
  public void testMarshalAndUnmarshalString() throws Exception {
    AntibodyContainer container = new AntibodyContainer();
    container.setAntibody(TestSuite.getTestAntibody());
    container.setHelmCode(helmService.toHELMString(abService.toHELM(TestSuite.getTestAntibody())));
    xmlString = xmlService.marshal(container);
    assertTrue("XML was not created!", xmlString != null);

    AntibodyContainer abContainer = xmlService.unmarshal(xmlString);
    assertNotNull("Antibody was null after unmarshal", abContainer.getAntibody());
    assertFalse("HELMCODE was null after unmarshal", StringUtils.isBlank(abContainer.getHelmCode()));
  }

}
