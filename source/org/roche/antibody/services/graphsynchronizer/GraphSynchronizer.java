/*--
 *
 * @(#) GraphSynchronizer.java
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

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.configuration.Configuration;
import org.helm.editor.controller.ModelController;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.PeptideSequenceParser;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.tools.StructureParser;
import org.roche.antibody.model.antibody.Antibody;
import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.model.antibody.Connection;
import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.model.antibody.RNA;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.AbstractGraphService;
import org.roche.antibody.services.ConnectionService;
import org.roche.antibody.services.DomainService;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.services.SequenceService;
import org.roche.antibody.services.antibody.AntibodyService;
import org.roche.antibody.services.helmnotation.HELM;
import org.roche.antibody.services.helmnotation.HelmNotationService;
import org.roche.antibody.services.helmnotation.model.HELMChem;
import org.roche.antibody.services.helmnotation.model.HELMCode;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.roche.antibody.services.helmnotation.model.HELMPeptide;
import org.roche.antibody.services.helmnotation.model.HELMRna;
import org.roche.antibody.ui.abstractgraph.view.DomainAnnotationAction;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import y.base.NodeMap;
import y.layout.hierarchic.GivenLayersLayerer;

import com.quattroresearch.blastws.BlastSearchParams;
import com.quattroresearch.blastws.BlastSearchService;

/**
 * {@code GraphSynchronizer} The Magic begins here ... :) This class is responsible for synchronizing the state between
 * the MacromolecularEditor and the AntibodyEditor. This service is STATEFUL, assure that each time you start a
 * synchroni
 * 
 * me = MacromolecularEditor
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * @version $Id: GraphSynchronizer.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class GraphSynchronizer {

  private static final String NEW_PEPTIDE_DEFAULT_NAME = "Peptide";

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory
      .getLogger(GraphSynchronizer.class);

  private static final String CBLOCK_MONOMER_NAME = "C-BLOCK";

  private static final String CBLOCK_MONOMER_SMILES = "C[*] |$;_R1$|";

  private static final String CBLOCK_MOLFILE =
      "H4sIAAAAAAAAAI2OsQ7CQAxD93yFpbL2lOS4XjNTxFSEOrAzdunQge8n10qlCAYsD5YdPYWA/jE/xwkQlSxRcmTtsIkIUB8B3vktM8NdmdnvUMeQmqSlV0/xWBIHXxknfCJ+e6FoaBPnlWI52Y4yVH9S5Ptt6oHhclvaMkJKc752RAcXvQDuchVTDAEAAA==";

  private static final String NBLOCK_MONOMER_NAME = "N-BLOCK";

  private static final String NBLOCK_MONOMER_SMILES = CBLOCK_MONOMER_SMILES;

  private static final String NBLOCK_MOLFILE =
      "H4sIAAAAAAAAAI2OsQ7CQAxD93yFJbpySnI9QmaKOhVVHdgZWRgY+H5yrQRFMGB5sOzoKQQMl/vjegNExSSLqWmHl4gAjRHgld9yd5yVmeMO25zKrmjtNVJua+IUK+OAT8RvzxRN+8K2UNyKryjT5k+KfL9NAzD149zWEVKb46kjakL0BMIa+YcMAQAA";

  private static final String CYSBLOCK_MONOMER_NAME = "CYS-BLOCK";

  private static final String CYSBLOCK_MONOMER_SMILES = CBLOCK_MONOMER_SMILES;

  private static final String CYSBLOCK_MOLFILE =
      "H4sIAAAAAAAAAI2OsQ7CQAxD93yFpbL2lOS4XjNTxFSEOrAzdunQge8n10qlCAYsD5YdPYWA/jE/xwkQlSxRchTtsIkIUB8B3vktM8NdmdnvUMeQmqSlV0/xWBIHXxknfCJ+e6FoaBPnlWI52Y4yVH9S5Ptt6oHhclvaMkJKc752RAcXvQCfYEm6DAEAAA==";

  private static final String BLOCKS_MONOMER_TYPE = Monomer.UNDEFINED_MOMONER_TYPE;

  private static final String BLOCKS_POLYMER_TYPE = Monomer.PEPTIDE_POLYMER_TYPE;

  /** The MacromolecularEditor of HELM Editor */
  private MacromoleculeEditor me;

  private AntibodyEditorPane abEditor;

  private String helmSentToEditor;

  private Antibody ab;

  private Domain activeDomain;

  private List<Connection> handledConnections = new ArrayList<Connection>();

  private List<Connection> handledInterDomainConnections = new ArrayList<Connection>();

  private List<Sequence> handledSequences = new ArrayList<Sequence>();

  private DomainService domainService = DomainService.getInstance();

  private ConnectionService connectionService = ConnectionService
      .getInstance();

  private SequenceService seqService = SequenceService.getInstance();

  private int deletedConnectionCount;

  private int createdConnectionCount;

  /**
   * if we add a blocker we count the number of block. in order to check if the user removed a blocker during edit phase
   */
  private int blockerCount;

  /**
   * if we edit a domain, we have to add logical blocker to the n- and c-terminals. if we sync the domain back, we have
   * to remove the blocker. so we store them here.
   */
  List<String> blocksToRemove = new ArrayList<String>();

  private Configuration appPrefs;

  public GraphSynchronizer(MacromoleculeEditor mainEditor,
      AntibodyEditorPane abEditor, Antibody ab) {
    this.me = mainEditor;
    this.abEditor = abEditor;
    this.ab = ab;
    appPrefs = PreferencesService.getInstance().getApplicationPrefs();
    blocksToRemove.add(appPrefs.getString(PreferencesService.C_BLOCKER));
    blocksToRemove.add(appPrefs.getString(PreferencesService.N_BLOCKER));
    blocksToRemove.add(appPrefs.getString(PreferencesService.CYS_BLOCKER));
  }

  /**
   * 
   * @param domain
   * @throws Exception
   */
  public void sendToMacroMolecularEditor(Domain domain) throws Exception {
    sendToMacroMolecularEditor(domain, null);
  }

  /***
   * Registers a block monomer in local helm editor store and returns success.
   * 
   * @param monomerName
   * @param smiles
   * @param molfile
   * @return number of registered block monomers
   * @throws Exception
   */
  private int registerBlockMonomerInLocalStore(String monomerName, String smiles, String molfile)
      throws Exception {
    MonomerFactory factory = MonomerFactory.getInstance();
    MonomerStore store = factory.getMonomerStore();

    if (!store.getMonomerDB().get("CHEM").containsKey(monomerName)) {
      chemaxon.struc.Molecule mol = StructureParser.getMolecule(smiles);
      Monomer m = new Monomer(Monomer.CHEMICAL_POLYMER_TYPE,
          Monomer.UNDEFINED_MOMONER_TYPE, "", monomerName);
      m.setMolfile(molfile);
      m.setName(monomerName);
      m.setCanSMILES(smiles);
      if (monomerName.equals(CBLOCK_MONOMER_NAME)) {
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        Attachment attachment = new Attachment("R1", "H");
        attachment.setAlternateId("R1-H");
        attachmentList.add(attachment);
        m.setAttachmentList(attachmentList);
      } else if (monomerName.equals(NBLOCK_MONOMER_NAME)) {
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        Attachment attachment = new Attachment("R1", "OH");
        attachment.setAlternateId("R1-OH");
        attachmentList.add(attachment);
        m.setAttachmentList(attachmentList);
      } else if (monomerName.equals(CYSBLOCK_MONOMER_NAME)) {
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        Attachment attachment = new Attachment("R1", "X");
        attachment.setAlternateId("R1-X");
        attachmentList.add(attachment);
        m.setAttachmentList(attachmentList);
      }
      store.addMonomer(m, true);

      factory.saveMonomerCache();
      return 1;
    }

    return 0;
  }

  /**
   * @param domain
   * @param additionalSequence A sequence that should be added to the panel as well.
   * @throws Exception
   */
  public void sendToMacroMolecularEditor(Domain domain,
      String additionalSequence) throws Exception {
    int registered = registerBlockMonomerInLocalStore(CBLOCK_MONOMER_NAME, CBLOCK_MONOMER_SMILES, CBLOCK_MOLFILE);
    registered += registerBlockMonomerInLocalStore(NBLOCK_MONOMER_NAME, NBLOCK_MONOMER_SMILES, NBLOCK_MOLFILE);
    registered += registerBlockMonomerInLocalStore(CYSBLOCK_MONOMER_NAME, CYSBLOCK_MONOMER_SMILES, CYSBLOCK_MOLFILE);
    if (registered > 0) {
      LOG.debug(registered + " BLOCK monomers were registered. Polymer panels will be updated.");
      me.updatePolymerPanels();
    }

    this.activeDomain = domain;
    HELMCode code = buildHelm(domain);
    addBlocker(code, domain);
    String helmNotation = HelmNotationService.getInstance().toHELMString(
        code);

    this.helmSentToEditor = helmNotation;

    ModelController.notationUpdated(helmNotation, me.getOwnerCode());

    // //
    if (additionalSequence != null) {
      try {
        String existingNotation = this.me.getNotation();
        String simpleNotation = PeptideSequenceParser
            .getNotation(additionalSequence);
        String complexNotation = SimpleNotationParser
            .getComplextNotationForPeptide(simpleNotation);
        String newNotation = null;
        if (existingNotation != null
            && existingNotation.trim().length() > 0) {
          newNotation = ComplexNotationParser
              .getCombinedComlexNotation(existingNotation,
                  complexNotation);
        } else
          newNotation = complexNotation;
        me.synchronizeZoom();
        ModelController.notationUpdated(newNotation, me.getOwnerCode());
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this.abEditor,
            "Adding the domain failed.");
        e.printStackTrace();
      }
    }
  }

  private HELMCode buildHelm(Domain activeDomain) {
    HELMCode code = new HELMCode();
    Deque<Sequence> sequencesToHandle = new ArrayDeque<Sequence>();
    handledConnections.clear();
    handledInterDomainConnections.clear();
    handledSequences.clear();
    sequencesToHandle.offer(activeDomain);
    Map<Sequence, HELMElement> helmElemMap = new HashMap<Sequence, HELMElement>();
    while (sequencesToHandle.isEmpty() == false) {
      Sequence seqToHandle = sequencesToHandle.poll();
      Sequence seqForConnectionCheck = seqToHandle;

      if (handledSequences.contains(seqToHandle)) {
        continue;
      } else {
        handledSequences.add(seqToHandle);
      }

      if (seqToHandle instanceof Domain) {
        activeDomain = (Domain) seqToHandle;
        HELMElement pep = seqService.toHELM(activeDomain);
        code.addHELMElement(pep);
        helmElemMap.put(activeDomain.getPeptide(), pep);
        seqForConnectionCheck = activeDomain.getPeptide();
      }

      for (Connection con : seqToHandle.getConnections()) {
        if (handledConnections.contains(con)) {
          continue;
        }
        if (con instanceof GeneralConnection) {
          HELMConnection helmCon = null;
          if (con.getSource() == seqForConnectionCheck
              && con.getTarget() == seqForConnectionCheck) {
            HELMElement element = seqService.toHELM(seqToHandle);
            code.addHELMElement(element);
            helmCon = connectionService.createConnection(con,
                element, element);
          } else {
            HELMElement source = helmElemMap.get(con.getSource());
            if (source == null) {
              source = seqService.toHELM(con.getSource());
              helmElemMap.put(con.getSource(), source);
              code.addHELMElement(source);
              sequencesToHandle.push(con.getSource());
            }

            HELMElement target = helmElemMap.get(con.getTarget());
            if (target == null) {
              target = seqService.toHELM(con.getTarget());
              helmElemMap.put(con.getTarget(), target);
              code.addHELMElement(target);
              sequencesToHandle.push(con.getTarget());
            }
            helmCon = connectionService.createConnection(con,
                source, target);
          }
          code.addHELMConnection(helmCon);
          handledConnections.add(con);
        }
        if (con instanceof CysteinConnection
            && connectionService.isIntraDomainConnection(con)) {
          HELMConnection helmCon = connectionService
              .createConnection(con,
                  helmElemMap.get(activeDomain.getPeptide()),
                  helmElemMap.get(activeDomain.getPeptide()));
          handledConnections.add(con);
          code.addHELMConnection(helmCon);
        }
        if (con instanceof CysteinConnection
            && !connectionService.isIntraDomainConnection(con)) {
          handledInterDomainConnections.add(con);
        }
      }
    }
    return code;
  }

  /**
   * Syncs the changes back to the antibody and returns whether changes should be submitted (and the macromolecule
   * editor cleared)
   * 
   * @param helmCode
   * @return true when macromolecule editor should be cleared and the antibody synced back to abEditor. False when sync
   *         should be cancelled for further edit.
   * @throws FileNotFoundException
   */
  public boolean syncBackToAntibody(String helmCode)
      throws FileNotFoundException {
    Map<HELMElement, Sequence> helmToSequence = new HashMap<HELMElement, Sequence>();
    HELMCode code = HelmNotationService.getInstance().toHELMCode(helmCode);
    List<HELMElement> handledElements = new ArrayList<HELMElement>();
    deletedConnectionCount = 0;
    createdConnectionCount = 0;

    // do not change anything, when molecule did not change
    try {
      if (ComplexNotationParser.getCanonicalNotation(helmSentToEditor)
          .equals(ComplexNotationParser
              .getCanonicalNotation(helmCode))) {
        return true;
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }

    int validationOption = validate(code);
    if (validationOption == JOptionPane.YES_OPTION) {
      removeBlocker(code);
      deleteOldSequencesAndConnections();
      handleActiveDomain(code, helmToSequence, handledElements);
      handleNewSequences(code, helmToSequence, handledElements);
      handleNewConnections(code, helmToSequence);

      // align non-domain Sequences, after they were created and connected
      AbstractGraphService.alignNonDomainSequences(
          abEditor.getAbstractGraph(),
          ab,
          (NodeMap) abEditor.getAbstractGraph().getDataProvider(
              GivenLayersLayerer.LAYER_ID_KEY));
      abEditor.updateLayoutHints();
      abEditor.updateGraphLayout();
    } else {
      //
      if (validationOption == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (validationOption == JOptionPane.NO_OPTION) {
        return true;
      }
    }

    try {
      DomainAnnotationAction.annotateDomain(AntibodyEditorAccess
          .getInstance().getAntibodyEditorPane(), activeDomain);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    // Show the user an appropriate message when the connection count
    // changed.
    int connectionCountChange = deletedConnectionCount
        - createdConnectionCount;
    if (connectionCountChange > 0) {
      String message = String
          .format("%d %s removed, because automated reconfiguration is not possible.",
              connectionCountChange,
              connectionCountChange > 1 ? "connections"
                  : "connection");
      JOptionPane
          .showMessageDialog(this.abEditor, message,
              "Connection count changed",
              JOptionPane.INFORMATION_MESSAGE);
    } else if (connectionCountChange < 0) {
      String message = String.format(
          "%d %s additional connections added.",
          -connectionCountChange,
          connectionCountChange < -1 ? "connections" : "connection");
      JOptionPane
          .showMessageDialog(this.abEditor, message,
              "Connection count changed",
              JOptionPane.INFORMATION_MESSAGE);
    }

    return true;
  }

  private void deleteOldSequencesAndConnections() {
    for (Connection con : handledConnections) {
      if (con instanceof CysteinConnection) {
        deletedConnectionCount++;
      }
      ab.removeConnection(con);
      AbstractGraphService.removeConnection(abEditor.getAbstractGraph(),
          con);

    }
    for (Connection con : handledInterDomainConnections) {
      if (con instanceof CysteinConnection) {
        deletedConnectionCount++;
      }
      ab.removeConnection(con);
      AbstractGraphService.removeConnection(abEditor.getAbstractGraph(),
          con);
    }
    for (Sequence seq : handledSequences) {
      if (seq != activeDomain) {
        AntibodyService.getInstance().removeSequence(seq, ab);
        AbstractGraphService.removeSequence(
            abEditor.getAbstractGraph(), seq);
      }
    }
  }

  private void handleActiveDomain(HELMCode code,
      Map<HELMElement, Sequence> helmToSequence,
      List<HELMElement> handledElemetns) throws FileNotFoundException {

    // we handle the activeDomain and their connections
    HELMElement elemOfActiveDomain = HelmNotationService.getInstance()
        .getHELMElementByName("PEPTIDE1", code);
    if (elemOfActiveDomain != null) {
      HELMPeptide curPeptide = (HELMPeptide) elemOfActiveDomain;
      helmToSequence.put(curPeptide, activeDomain);
      handledElemetns.add(curPeptide);
      Domain dom = DomainService.getInstance().identifyNewDomain(
          activeDomain, curPeptide.getSimpleSequence());
      if (dom != null) {
        adaptHELMCodeForSyncBack(dom, curPeptide, code,
            handledElemetns, helmToSequence);
        AbstractGraphService.addSequence(abEditor.getAbstractGraph(),
            dom);
      }
    }

  }

  /**
   * Validates in order to decide, if we can sync our model back or not!
   * 
   * @param code
   * @return JOptionPane.YES_NO_CANCEL_OPTION
   */
  private int validate(HELMCode code) {
    int blockersInCode = 0;
    boolean activeDomainDetected = false;
    boolean activeDomainChanged = false;

    String domainChangesMsg = "";

    for (HELMElement elem : code.getAllElements()) {
      if (blocksToRemove.contains(elem.getSequenceRepresentation())) {
        blockersInCode++;
      }
      if (elem instanceof HELMPeptide) {
        HELMPeptide pep = (HELMPeptide) elem;
        if (pep.getSimpleSequence().startsWith(
            activeDomain.getSequence())
            || pep.getSimpleSequence().endsWith(
                activeDomain.getSequence())) {
          activeDomainDetected = true;
        } else {
          domainChangesMsg = checkForSequenceChanges(
              activeDomain.getSequence(), pep.getSimpleSequence());
          activeDomainChanged = true;
        }
      }
    }
    if (blockersInCode != blockerCount) {
      JOptionPane.showMessageDialog(me.getFrame(),
          "Blockers were mainpulated! Model cannot be sync back!",
          "Sync Error", JOptionPane.ERROR_MESSAGE);
      return JOptionPane.CANCEL_OPTION;

    } else if (!activeDomainDetected && activeDomainChanged) {
      FontUIResource defaultFont = (FontUIResource) UIManager
          .get("OptionPane.messageFont");
      UIManager.put("OptionPane.messageFont", new FontUIResource(
          "Courier New", FontUIResource.PLAIN, 13));
      int isConfirmed = JOptionPane.showConfirmDialog(me.getFrame(),
          domainChangesMsg, "Sequence has changed",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE);
      UIManager.put("OptionPane.messageFont", defaultFont);

      return isConfirmed;

    }
    return JOptionPane.YES_OPTION;
  }

  /**
   * This method checks for changes between 2 sequences and returns a user friendly html message, describing those
   * changes.
   * 
   * @param oldSequence
   * @param newSequence
   * @return html message
   */
  private String checkForSequenceChanges(String oldSequence,
      String newSequence) {
    if (oldSequence.equals(newSequence))
      return "<html>Both sequences are equal</html>";
    else {

      String domainChanges =
          "<html>Sequence between blockers has changed.<br /> Do you want to apply the changes?<br /><br />";

      domainChanges += "Length old sequence: " + oldSequence.length()
          + "AAs<br />";
      domainChanges += "Length new sequence: " + newSequence.length()
          + "AAs<br /><br />";

      // Blast for alignment
      try {
        BlastSearchService bss = new BlastSearchService();

        bss.uploadAndIndex("Protein", 1,
            ">oldSequence" + System.getProperty("line.separator")
                + oldSequence);

        // Set parameters for BLAST
        BlastSearchParams params = new BlastSearchParams();
        params.setQuery(newSequence);
        params.setSequenceType("Protein");
        params.seteValue(1000);
        params.setWordSize(2);
        params.setCompBasedStats("F");

        bss.searchSequence(params); // <- EXECUTES BLAST
        domainChanges += bss.getBestAlignment();
      } catch (Exception ex) {
        // no Alignment on Blasterror
      }

      return domainChanges;
    }
  }

  private void removeBlocker(HELMCode code) {
    List<HELMElement> elementsToRemove = new ArrayList<HELMElement>();
    for (HELMElement elem : code.getAllElements()) {
      if (blocksToRemove.contains(elem.getSequenceRepresentation())) {
        elementsToRemove.add(elem);
      }
    }
    for (HELMElement helmElement : elementsToRemove) {
      code.removeHELMElement(helmElement);
    }
  }

  private void handleNewConnections(HELMCode code,
      Map<HELMElement, Sequence> sequenceMap) {
    for (HELMConnection helmConnection : code.getAllConnections()) {
      Connection conn = null;
      if (HelmNotationService.getInstance().isCysteinConnection(
          helmConnection)) {
        conn = new CysteinConnectionBuilder()
            .build(canonicalizeHelmConnection(helmConnection),
                sequenceMap);
        if (conn != null) {
          createdConnectionCount++;
        }
      } else {
        conn = new GeneralConnectionBuilder().build(helmConnection,
            sequenceMap);
      }
      if (conn != null && !ab.getConnections().contains(conn)) {
        ab.addConnection(conn);
        AbstractGraphService.addConnection(abEditor.getAbstractGraph(),
            conn);
      }
    }
  }

  /**
   * Sorts source and target by position.
   * 
   * @param connection HelmConnection to canonicalize
   * @return connection with source and target in correct order
   */
  private HELMConnection canonicalizeHelmConnection(HELMConnection connection) {
    if (connection.getSourcePosition() > connection.getTargetPosition()) {
      HELMElement sourceElement = connection.getSource();
      int sourcePosition = connection.getSourcePosition();
      String sourceRest = connection.getSourceRest();

      connection.setSource(connection.getTarget());
      connection.setSourcePosition(connection.getTargetPosition());
      connection.setSourceRest(connection.getTargetRest());
      connection.setTarget(sourceElement);
      connection.setTargetPosition(sourcePosition);
      connection.setTargetRest(sourceRest);

    }

    return connection;
  }

  private List<Sequence> handleNewSequences(HELMCode code,
      Map<HELMElement, Sequence> helmToSequence,
      List<HELMElement> handledElements) {

    List<Sequence> newSequences = new LinkedList<Sequence>();

    for (HELMElement element : code.getAllElements()) {
      if (!handledElements.contains(element)) {
        Sequence newSequence = null;
        // handle CHEM
        if (element instanceof HELMChem) {
          // ME 2014-10-10: Added SMILES notation to ChemElement.
          String smiles = "";
          String molfile = "";
          try {
            MonomerStore store = MonomerStoreCache.getInstance()
                .getCombinedMonomerStore();
            smiles = store.getMonomers("CHEM")
                .get(element.getSequenceRepresentation())
                .getCanSMILES();
            molfile = store.getMonomers("CHEM")
                .get(element.getSequenceRepresentation())
                .getMolfile();
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
          newSequence = new ChemElement(
              element.getSequenceRepresentation(), smiles,
              molfile);
          AbstractGraphService.addSequence(
              abEditor.getAbstractGraph(), newSequence);
          helmToSequence.put(element, newSequence);
        }

        if (element instanceof HELMRna) {
          newSequence = new RNA(element.getSequenceRepresentation());
          AbstractGraphService.addSequence(
              abEditor.getAbstractGraph(), newSequence);
          helmToSequence.put(element, newSequence);
        }

        if (element instanceof HELMPeptide) {
          // we handle only new peptides not peptide1 because this is
          // the active domain
          if (!element.getName().equalsIgnoreCase("PEPTIDE1")) {
            HELMPeptide hPep = (HELMPeptide) element;
            Peptide pep = new Peptide();
            pep.setName(NEW_PEPTIDE_DEFAULT_NAME);
            pep.setSequence(hPep.getSimpleSequence());
            pep.setOriginalSequence(hPep.getSimpleSequence());
            String domainName = DomainService.getInstance()
                .getDefaultName(pep.getSequence());
            Domain dom = new Domain(domainName, pep,
                pep.getStartPosition(), pep.getEndPosition(),
                pep.getStartPosition(), pep.getEndPosition());
            dom.setAntibody(AntibodyEditorAccess.getInstance()
                .getAntibodyEditorPane().getAntibody());
            pep.getDomains().add(dom);
            newSequence = pep;
            AbstractGraphService.addSequence(
                abEditor.getAbstractGraph(), dom);
            helmToSequence.put(element, dom);
          }
        }
        if (newSequence != null) {
          AntibodyService.getInstance().addSequence(newSequence, ab);
          handledElements.add(element);
          newSequences.add(newSequence);
        }
      }
    }

    return newSequences;
  }

  /**
   * We add blocker in the following logic:
   * 
   * CASE 0. if only one domain exists no terminal blocks required
   * 
   * CASE 1. if the domain is at the beginning of a chain, we add a C-Block at the end.
   * 
   * CASE 2. if the domain is at the end of the chain, we add a N-Block at the beginning.
   * 
   * CASE 3. if the domain is in the middle of the chain, we add both blocks
   * 
   * CASE 4. we add CYS-Blocker to all occupied cysteins which are not intra-connected
   * 
   * @param code
   * @param dom
   */
  private void addBlocker(HELMCode code, Domain dom) {
    blockerCount = 0;
    // handle CASE 2+3
    if (dom.getStartPosition() > 1) {
      addCBlock(code);
    }
    // handle CASE 1+3
    if (dom.getEndPosition() - dom.getPeptide().getSequence().length() < 0) {
      addNBlock(code);
    }
    // handle CASE 4
    List<Integer> cysPositionsToBlock = new ArrayList<Integer>();
    for (Connection con : dom.getConnections()) {
      if (con instanceof CysteinConnection) {
        if (!connectionService.isIntraDomainConnection(con)) {
          Sequence realSource = con.getSource();
          Sequence realTarget = con.getTarget();
          int sPos = con.getSourcePosition();
          int tPos = con.getTargetPosition();

          if (realSource instanceof Peptide) {
            Peptide pep = ((Peptide) realSource);
            realSource = pep.getDomain(con.getSourcePosition());
            sPos = domainService
                .transformPeptidePositionToDomainPosition(pep,
                    sPos);
          }
          if (realTarget instanceof Peptide) {
            Peptide pep = ((Peptide) realTarget);
            realTarget = pep.getDomain(con.getTargetPosition());
            tPos = domainService
                .transformPeptidePositionToDomainPosition(pep,
                    tPos);
          }

          if (realSource == dom) {
            cysPositionsToBlock.add(sPos);
          }
          if (realTarget == dom) {
            cysPositionsToBlock.add(tPos);
          }
        }
      }
    }
    addCysBlocks(cysPositionsToBlock, code);
    // CASE 0 -> nothing to do.
  }

  private void addCysBlocks(List<Integer> cysPositionsToBlock, HELMCode code) {
    HELMPeptide peptide = (HELMPeptide) HelmNotationService.getInstance()
        .getHELMElementByName("PEPTIDE1", code);
    for (Integer integer : cysPositionsToBlock) {
      HELMElement cysBlock = new HELMChem(
          appPrefs.getString(PreferencesService.CYS_BLOCKER));
      HELMConnection con = new HELMConnection(peptide, integer, HELM.R3,
          cysBlock, 1, HELM.R1);
      code.addHELMElement(cysBlock);
      code.addHELMConnection(con);
      blockerCount++;
    }
  }

  private void addNBlock(HELMCode code) {
    HELMPeptide peptide = (HELMPeptide) HelmNotationService.getInstance()
        .getHELMElementByName("PEPTIDE1", code);
    HELMElement cBlock = new HELMChem(
        appPrefs.getString(PreferencesService.C_BLOCKER));
    code.addHELMElement(cBlock);
    HELMConnection con = new HELMConnection(peptide, peptide
        .getSimpleSequence().length(), HELM.R2, cBlock, 1, HELM.R1);
    code.addHELMConnection(con);
    blockerCount++;
  }

  private void addCBlock(HELMCode code) {
    HELMPeptide peptide = (HELMPeptide) HelmNotationService.getInstance()
        .getHELMElementByName("PEPTIDE1", code);
    HELMElement nBlock = new HELMChem(
        appPrefs.getString(PreferencesService.N_BLOCKER));
    code.addHELMElement(nBlock);

    HELMConnection con = new HELMConnection(peptide, 1, HELM.R1, nBlock, 1,
        HELM.R1);
    code.addHELMConnection(con);
    blockerCount++;
  }

  private void adaptHELMCodeForSyncBack(Domain newDomain, HELMPeptide pep,
      HELMCode code, List<HELMElement> handledElemets,
      Map<HELMElement, Sequence> helmToSequence) {
    int offset = 0;
    HelmNotationService hs = HelmNotationService.getInstance();
    List<HELMConnection> connections = hs.getConnectionsByHELMElement(code,
        pep);
    if (pep.getSimpleSequence().startsWith(newDomain.getSequence())) {
      offset = newDomain.getSequence().length();
      String newSequence = pep.getSimpleSequence().substring(offset);
      pep.setSequenceRepresentation(hs
          .simpleSequenceToSequenceRepresentation(newSequence));
      HELMPeptide newPeptide = new HELMPeptide(
          hs.simpleSequenceToSequenceRepresentation(newDomain
              .getSequence()));
      code.addHELMElement(newPeptide);
      handledElemets.add(newPeptide);
      helmToSequence.put(newPeptide, newDomain);
      for (HELMConnection con : connections) {
        if (con.getSource() == pep) {
          if (con.getSourcePosition() > offset) {
            con.setSourcePosition(con.getSourcePosition() - offset);
          } else {
            con.setSource(newPeptide);
          }
        }

        if (con.getTarget() == pep) {
          if (con.getTargetPosition() > offset) {
            con.setTargetPosition(con.getTargetPosition() - offset);
          } else {
            con.setTarget(newPeptide);
          }
        }
      }
    }
  }

}
