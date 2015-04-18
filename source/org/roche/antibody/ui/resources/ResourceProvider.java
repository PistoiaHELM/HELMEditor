/*--
 *
 * @(#) ResourceProvider.java
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
package org.roche.antibody.ui.resources;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ResourceProvider} This class is responsible for serving resource url of images located in the same package.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: ResourceProvider.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class ResourceProvider {

  public static final String DISK_IMAGE = "disk.png";

  public static final String OPEN_FOLDER = "open-folder.png";

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(ResourceProvider.class);
  
  private static ResourceProvider instance;
  
  private ResourceProvider() {
  }
  
  public synchronized static ResourceProvider getInstance() {
    if (instance == null) {
      instance = new ResourceProvider();
    }
    return instance;
  }

  public URL get(String resourceName) {
    return getClass().getResource(resourceName);
  }

}
