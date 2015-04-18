package com.quattroresearch.antibody;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.model.antibody.Antibody;
import org.roche.antibody.services.IAntibodyLoader;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.services.UIService;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog for fetching an antibody from backend.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class LoadAntibodyFromBackendDialog extends JDialog {

  /** Generated UID */
  private static final long serialVersionUID = 1088796296715648206L;

  private static final int DIALOG_FIXED_HEIGHT = 170;

  private static final int DIALOG_START_WIDTH = 300;

  private Logger LOG = LoggerFactory.getLogger(LoadAntibodyFromBackendDialog.class);

  MacromoleculeEditor editor;

  private JPanel contentPanel;

  private JPanel buttonPanel;

  private JButton btnLoad;

  private JButton btnCancel;

  private JTextField tfID;

  public LoadAntibodyFromBackendDialog(MacromoleculeEditor editor, boolean modal) {
    super(editor.getFrame(), modal);

    this.editor = editor;

    try {
      editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      initComponents();
    } finally {
      editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

  }

  private void initComponents() {
    this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    this.setMinimumSize(new Dimension(DIALOG_START_WIDTH, DIALOG_FIXED_HEIGHT));
    this.setTitle("Load Antibody");

    NumberFormat longFormat = NumberFormat.getNumberInstance();
    longFormat.setGroupingUsed(false);
    NumberFormatter numberFormatter = new NumberFormatter(longFormat);
    numberFormatter.setValueClass(Long.class);
    numberFormatter.setAllowsInvalid(false);
    tfID = new SingleLineFormattedField(numberFormatter);

    btnLoad = new JButton("Load Antibody");
    btnLoad.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnLoadActionPerformed(evt);
      }
    });
    btnCancel = new JButton("Cancel");
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCancelActionPerformed(evt);
      }
    });

    createContentPanel();

    // Reset height because resizing destroys layout
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        e.getComponent().setSize(e.getComponent().getWidth(), DIALOG_FIXED_HEIGHT);
      }
    });

    pack();
  }

  private void createContentPanel() {
    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    contentPanel.add(new Label("Load antibody:"));
    contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    contentPanel.add(new Label("Concept ID"));
    contentPanel.add(tfID);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    buttonPanel.add(Box.createVerticalGlue());
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(btnCancel);
    buttonPanel.add(btnLoad);

    contentPanel.add(buttonPanel);

    this.add(contentPanel);

  }

  private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
    this.dispose();
  }

  private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {
    try {
      try {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Antibody antibody = loadAntibody();
        if (antibody != null) {
          setAntibody(antibody);
        } else {
          throw new RuntimeException("Failed to load antibody.");
        }

      } finally {
        this.setCursor(Cursor.getDefaultCursor());
      }

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Loading failed: " + e.getClass().getName() + " (" + e.getMessage()
          + ")");
    }
    this.dispose();
  }

  private void setAntibody(Antibody antibody) {
    AntibodyEditorPane abEditor = AntibodyEditorAccess.getInstance().getAntibodyEditorPane();
    if (abEditor == null) {
      abEditor = UIService.getInstance().addAntibodyViewEditor(
          editor, null);
    }

    editor.getTabbedSequenceViewPanel().getTabbedPane()
        .setSelectedComponent(abEditor);
    // set the antibody and disable go-back
    abEditor.setModel(antibody);
    abEditor.setIsBackToDomainEditorEnabled(false);
  }

  /**
   * Starts antibody loading by using backend connector and returns the respective registration task id.
   * 
   * @throws Exception
   */
  private Antibody loadAntibody() throws Exception {
    String backendconnector =
        PreferencesService.getInstance().getApplicationPrefs().getString("backend.connector-class.antibodyLoader");

    if (backendconnector == null) {
      LOG.error("backend connector class for antibody loading is null");
      throw new ClassNotFoundException(
          "Configured backend connector was not found. Please check application settings.");

    }

    Class<?> clazz =
        Class.forName(backendconnector);

    if (clazz != null) {
      Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
      String webTarget =
          PreferencesService.getInstance().getApplicationPrefs().getString("uri.backend-antibodyLoader");
      webTarget = webTarget.replaceAll("\\{entity\\}", tfID.getText());

      LOG.debug("Loading antibody from target " + webTarget);

      Object loader =
          constructor.newInstance(webTarget);

      if (loader != null) {
        LOG.debug("class loaded");

        IAntibodyLoader abLoaderInstance = (IAntibodyLoader) loader;
        Antibody antibody = abLoaderInstance.getAntibody();

        LOG.debug("antibody loaded");

        return antibody;
      } else {
        throw new ClassNotFoundException("Loader class not initialized.");
      }

      // TODO throw specific exception on error --> For example "Backend unavailable"
    }

    return null;
  }

  private class SingleLineFormattedField extends JFormattedTextField {
    /** Generated UID */
    private static final long serialVersionUID = 6503665559403044273L;

    public SingleLineFormattedField(AbstractFormatter formatter) {
      super(formatter);
    }

    @Override
    public Dimension getMaximumSize() {
      Dimension size = super.getMaximumSize();
      size.height = getPreferredSize().height * 10;
      return size;
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension size = super.getMinimumSize();
      size.height *= 1.5;
      return size;
    }
  }
}
