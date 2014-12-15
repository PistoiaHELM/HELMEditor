/*--
 *
 * @(#) SequenceService.java
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
package org.roche.antibody.services;

import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.model.antibody.RNA;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.helmnotation.HelmNotationService;
import org.roche.antibody.services.helmnotation.model.HELMChem;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.roche.antibody.services.helmnotation.model.HELMPeptide;
import org.roche.antibody.services.helmnotation.model.HELMRna;

/**
 * {@code SequenceService}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: SequenceService.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class SequenceService {

  private HelmNotationService hs = HelmNotationService.getInstance();

  /** static Singleton instance */
  private static SequenceService instance;

  /** Private constructor for singleton */
  private SequenceService() {
  }

  /** Static getter method for retrieving the singleton instance */
  public synchronized static SequenceService getInstance() {
    if (instance == null) {
      instance = new SequenceService();
    }
    return instance;
  }

  /**
   * converts a {@link Sequence} to a {@link HELMElement}
   * 
   * @param sequence {@link Sequence}
   * @return {@link HELMElement}
   */
  public HELMElement toHELM(Sequence sequence) {
    HELMElement element = null;
    if (sequence instanceof Peptide || sequence instanceof Domain) {
      element = new HELMPeptide();
      element.setSequenceRepresentation(hs.simpleSequenceToSequenceRepresentation(sequence.getSequence()));
    }

    if (sequence instanceof RNA) {
      element = new HELMRna();
      element.setSequenceRepresentation(sequence.getSequence());
    }

    if (sequence instanceof ChemElement) {
      element = new HELMChem();
      element.setSequenceRepresentation(sequence.getSequence());
    }
    return element;
  }
}
