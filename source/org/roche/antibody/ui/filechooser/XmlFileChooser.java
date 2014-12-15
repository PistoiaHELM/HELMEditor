/*--
 *
 * @(#) XmlFileSaver.java
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
package org.roche.antibody.ui.filechooser;

import org.roche.antibody.services.PreferencesService;


/**
 * {@code XmlFileSaver}
 * 
 * Dialog for opening and saving antibody Files.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: XmlFileChooser.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class XmlFileChooser extends AntibodyFileChooser {

  /** */
  private static final long serialVersionUID = 7388933607935271135L;

  public XmlFileChooser() {
    super();
    setFileFilter(abXml);
  }
  
  @Override
  public String getLastDirectoryProperty() {
    return PreferencesService.LAST_XML_FOLDER;
  }

}
