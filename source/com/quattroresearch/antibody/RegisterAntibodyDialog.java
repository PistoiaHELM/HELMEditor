package com.quattroresearch.antibody;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.management.ReflectionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.model.antibody.Antibody;
import org.roche.antibody.model.antibody.AntibodyContainer;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.services.IAntibodyRegistration;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.services.xml.XmlAntibodyService;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog for registering an antibody that was built in the editor.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class RegisterAntibodyDialog extends JDialog {

  /** Generated UID */
  private static final long serialVersionUID = 1088796296715648206L;

  private static final int DIALOG_FIXED_HEIGHT = 350;

  private static final int DIALOG_START_WIDTH = 300;

  private Logger LOG = LoggerFactory.getLogger(RegisterAntibodyDialog.class);

  private Antibody antibody;

  private JPanel contentPanel;

  private JPanel buttonPanel;

  private JButton btnRegister;

  private JButton btnCancel;

  JComboBox cbProject = new SingleLineComboBox();

  List<JComboBox> cbSpecificityList = new LinkedList<JComboBox>();

  public RegisterAntibodyDialog(MacromoleculeEditor editor, boolean modal) {
    super(editor.getFrame(), modal);

    try {
      editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      antibody = AntibodyEditorAccess.getInstance()
          .getAntibodyEditorPane().getAntibody();
      initComponents();
    } finally {
      editor.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

  }

  private void initComponents() {
    this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    this.setMinimumSize(new Dimension(DIALOG_START_WIDTH, DIALOG_FIXED_HEIGHT));
    this.setTitle("Register Antibody");

    btnRegister = new JButton("Register Antibody");
    btnRegister.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnRegisterActionPerformed(evt);
      }
    });
    btnCancel = new JButton("Cancel Registration");
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

  private static List<Integer> getListOfParatopes(Antibody antibody) {
    List<Integer> paratopes = new LinkedList<Integer>();
    for (Peptide pep : antibody.getPeptides()) {
      for (Domain dom : pep.getDomains()) {
        if (dom.getParatope() != null && !paratopes.contains(dom.getParatope())) {
          paratopes.add(dom.getParatope());
        }
      }
    }
    return paratopes;
  }

  private void createContentPanel() {

    List<Integer> paratopes = getListOfParatopes(antibody);
    for (int i = 0; i < paratopes.size(); i++) {
      cbSpecificityList.add(new SingleLineComboBox());
    }

    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    contentPanel.add(new Label("Please choose project and one specificity for every Fv:"));
// contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    JPanel pnlProject = new JPanel();
    pnlProject.setBorder(BorderFactory
        .createTitledBorder("Project"));
    pnlProject.add(cbProject);
    contentPanel.add(pnlProject);
// contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    JPanel pnlSpecificity = new JPanel();
    pnlSpecificity.setLayout(new GridLayout(paratopes.size(), 1));
    pnlSpecificity.setBorder(BorderFactory
        .createTitledBorder("Specificities"));
    // add a combobox for every paratope
    for (int i = 0; i < paratopes.size(); i++) {
      JPanel pnlCurrentSpec = new JPanel(new FlowLayout(FlowLayout.LEFT));
      pnlCurrentSpec.add(new Label("Fv " + (i + 1)));
      pnlCurrentSpec.add(cbSpecificityList.get(i));
      pnlSpecificity.add(pnlCurrentSpec);
    }
    contentPanel.add(pnlSpecificity);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    buttonPanel.add(Box.createVerticalGlue());
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(btnCancel);
    buttonPanel.add(btnRegister);

    contentPanel.add(buttonPanel);

    this.add(contentPanel);

    try {
      TreeMap<Integer, String> lov = loadLOV("uri.backend-lov-project");
      if (lov.entrySet() == null) {
        throw new Exception("Could not load project data.");
      } else {
        for (Entry<Integer, String> entry : lov.entrySet()) {
          String proj = entry.getValue();
          if (proj.length() > 55) {
            proj = StringUtils.substring(proj, 0, 55) + "(..)";
          }
          cbProject.addItem(new ProjectItem(entry.getKey(), proj));
        }
      }

      lov = loadLOV("uri.backend-lov-specificity");
      if (lov.entrySet() == null) {
        throw new Exception("Could not load specificity data.");
      } else {
        for (Entry<Integer, String> entry : lov.entrySet()) {
          for (JComboBox cbSpec : cbSpecificityList) {
            cbSpec.addItem(new SpecificityItem(entry.getKey(), entry.getValue()));
          }
        }
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
// for (StackTraceElement el : e.getStackTrace()) {
// LOG.error(el.toString());
// }

      // TODO refactor out of editor project
      if (e.getCause() != null) {
        JOptionPane.showMessageDialog(this, String.format("Could not load LOV. Please check Backend availability. ( %s caused by '%s')", e.getClass().getName(), e.getCause().getMessage()), "Initialization failed", JOptionPane.ERROR_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(this, String.format("Could not load LOV. Please check Backend availability. ( %s)", e.getClass().getName()), "Initialization failed", JOptionPane.ERROR_MESSAGE);
      }

      this.dispose();
    }

  }

  private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
    this.dispose();
  }

  private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
    Integer taskID = null;

    try {
      try {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        taskID = registerAntibody();
      } catch (Exception e) {
        LOG.error(e.getMessage());
        throw new RegistrationException("Antibody wasn't registered: " + e.getClass().getSimpleName());
      } finally {
        this.setCursor(Cursor.getDefaultCursor());
      }

      String buttons[] = new String[] {"Open in browser"};
      int option =
          JOptionPane.showOptionDialog(this, String.format("Registration started with task ID '%d'. %s You may see your registration progress on the web page.",
              taskID, System.getProperty("line.separator")), "Registration ongoing", JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, buttons, buttons[0]);

      if (option == 0) {
        String taskUrl =
            PreferencesService.getInstance().getApplicationPrefs().getString("uri.registration-task");
        // CW 2014-12-08: functionality was broken because url was like [http://xxx.com/registrationP1AA001] - avoiding by double-checking for trailing slash.
        if (!taskUrl.endsWith("/")) {
          taskUrl += "/";
        }
        Desktop.getDesktop().browse(new URI(taskUrl + taskID));
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Registration failed: " + e.getClass().getSimpleName() + " ("
          + e.getMessage()
          + ")");
    }
    this.dispose();
  }

  /**
   * Starts antibody registration by using Rest-Connector and returns the respective registration task id.
   * 
   * @throws JAXBException
   * @throws ClassNotFoundException
   * @throws ReflectionException
   */
  private Integer registerAntibody() throws JAXBException, ClassNotFoundException, ReflectionException {
    // get backend loader
    String backendconnector =
        PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.BACKEND_CONNECTOR_REGISTRATION);
    if (backendconnector == null) {
      throw new ClassNotFoundException("Backend connector class for registration was not found.");
    }
    Class<?> clazz =
        Class.forName(backendconnector);

    if (clazz != null) {
      Constructor<?> constructor;
      IAntibodyRegistration registrar = null;
      try {
        constructor = clazz.getDeclaredConstructor(String.class);

        registrar =
            (IAntibodyRegistration) constructor.newInstance(((Object[])
                PreferencesService.getInstance().getApplicationPrefs().getStringArray(PreferencesService.URI_REGISTRATION)));
      } catch (Exception e) {
        throw new ReflectionException(e, "Calling the antibody registrar failed.");
      }

      if (registrar == null) {
        LOG.error("Antibody registrar could not get created.");
        throw new ClassNotFoundException("Antibody registrar could not get created.");
      }

      AntibodyContainer abContainer = new AntibodyContainer();
      abContainer.setAntibody(antibody);
      abContainer.setHelmCode(AntibodyEditorAccess.getInstance().getAntibodyEditorPane().getHELMStringFromTextArea());

      List<Integer> specificities = new LinkedList<Integer>();
      for (JComboBox cbSpec : cbSpecificityList) {
        specificities.add(((SpecificityItem) cbSpec.getSelectedItem()).getID());
      }

      int taskID =
          registrar.register(XmlAntibodyService.getInstance().marshal(abContainer), ((ProjectItem)
              cbProject.getSelectedItem()).getID(), specificities);
      return taskID;

    }

    return null;
  }

  private TreeMap<Integer, String> loadLOV(String webTarget) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
      ClassNotFoundException {
// String backendconnector =
// PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.BACKEND_CONNECTOR_AUTOCONNECTOR);
    String backendconnector =
        PreferencesService.getInstance().getApplicationPrefs().getString("backend.connector-class.lov");

    if (backendconnector == null) {
      LOG.error("backend connector class for lov loading is null");
    }

    Class<?> clazz =
        Class.forName(backendconnector);

    if (clazz != null) {
      Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
      Object loader =
          constructor.newInstance(((Object[])
              PreferencesService.getInstance().getApplicationPrefs().getStringArray(webTarget)));

      if (loader != null) {
        LOG.debug("class loaded");
        Method method = clazz.getDeclaredMethod("load");
        Object returnVal = method.invoke(loader);

        @SuppressWarnings("unchecked")
        TreeMap<Integer, String> lov = (TreeMap<Integer, String>) returnVal;

        LOG.debug("lov loaded");
        return lov;
      } else {
        throw new ClassNotFoundException("Loader class not initialized.");
      }

      // TODO throw specific exception on error --> For example "Backend unavailable"
    }

    return null;
  }

  private class ProjectItem {
    private String name;

    private int id;

    public ProjectItem(int id, String name) {
      this.name = name;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public int getID() {
      return id;
    }

    public String toString() {
      return getID() + ": " + getName();
    }
  }

  private class SpecificityItem {
    private String name;

    private int id;

    public SpecificityItem(int id, String name) {
      this.name = name;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public int getID() {
      return id;
    }

    public String toString() {
      return getID() + ": " + getName();
    }
  }

  private class SingleLineComboBox extends JComboBox {
    /** Generated UID */
    private static final long serialVersionUID = 6503665559403044273L;

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

  private class RegistrationException extends Exception {
    /** Generated UID */
    private static final long serialVersionUID = -8281366095328701461L;

    public RegistrationException(String string) {
      super(string);
    }
  }

}
