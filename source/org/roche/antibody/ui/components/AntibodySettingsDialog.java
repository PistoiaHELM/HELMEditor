/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.roche.antibody.ui.components;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import org.roche.antibody.services.ConfigLoaderHelper;
import org.roche.antibody.services.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 */
public class AntibodySettingsDialog extends JDialog implements ActionListener {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory
      .getLogger(AntibodySettingsDialog.class);

  /** */
  private static final long serialVersionUID = 1L;

  private Preferences preferences;

  private JComboBox cboMaxOrMinDomain;

  private JCheckBox chkLibraryEditing;

  private JCheckBox chkUseMasterLibrary;

  private JCheckBox ckUseMasterMutation;

  private JCheckBox ckUseMasterAutoconnector;

  private JComboBox cbDomainLibTable;

  private JComboBox cbMutationLibTable;

  private JComboBox cbAutoconnectorTable;

  private JFormattedTextField tfMaxDomainDistTextField;

  private JCheckBox cbAutoextendDomains;

  private JCheckBox cbDomainConflictSolver;

  private JFormattedTextField tfLowerSortingThreshold;

  private JFormattedTextField tfUpperSortingThreshold;

  private JFormattedTextField tfSkipHitPercentCovThreshold;

  private JFormattedTextField tfSkipHitPercentIdThreshold;

  private JButton cmdSave;

  private JButton cmdCancel;

  private JCheckBox chkUseLowerThreshold;

  private JCheckBox chkUseUpperThreshold;

  public AntibodySettingsDialog(JFrame frame, Dialog.ModalityType modal) {
    super(frame, modal);
    preferences = PreferencesService.getInstance().getUserPrefs();
    initComponents();
  }

  /**
   * UI Initialization
   */
  private void initComponents() {
    setResizable(false);
    setTitle("Antibody Editor Settings");
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(
        new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("General", buildGeneralTab());
    tabbedPane.addTab("Domain Detection", buildDomainDetectionTab());

    getContentPane().add(tabbedPane);
    getContentPane().add(buildButtonPanel());

    pack();
  }

  /**
   * Builds the {@link JPanel} that contains general settings.
   * 
   * @return panel
   */
  private JPanel buildGeneralTab() {
    JPanel pnlGeneralTab = new JPanel();
    pnlGeneralTab.setLayout(new BoxLayout(pnlGeneralTab, BoxLayout.Y_AXIS));

    pnlGeneralTab.add(buildLibrarySettingsPanel());
    pnlGeneralTab.add(buildMutationSettingsPanel());
    pnlGeneralTab.add(buildAutoconnectorSettingsPanel());
    pnlGeneralTab.add(Box.createVerticalGlue());

    return pnlGeneralTab;
  }

  /**
   * Builds the {@link JPanel} that contains domain detection settings.
   * 
   * @return panel
   */
  private JPanel buildDomainDetectionTab() {
    JPanel pnlDomainDetection = new JPanel();
    pnlDomainDetection.setLayout(new GridLayout(1, 1));
    pnlDomainDetection.add(buildBlastSettings());

    return pnlDomainDetection;
  }

  /**
   * @return {@link JPanel} of library settings
   */
  private JPanel buildLibrarySettingsPanel() {
    JPanel pnlLibrarySettings = new JPanel();
    pnlLibrarySettings.setLayout(new GridLayout(2, 1));
    pnlLibrarySettings.setBorder(BorderFactory
        .createTitledBorder("Domain Library Settings"));
    boolean canWriteToFile;
    canWriteToFile = PreferencesService.getInstance()
        .canWriteDomainLibrary();

    chkLibraryEditing = new JCheckBox("Enables Sequence Library Editing");
    if (canWriteToFile) {
      chkLibraryEditing.setSelected(preferences.getBoolean(
          PreferencesService.ALLOW_LIBRARY_EDITING, false));
    } else {
      chkLibraryEditing.setSelected(false);
      chkLibraryEditing.setEnabled(false);
    }
    chkLibraryEditing
        .setToolTipText("<html>Allows the User to edit the domain library.<br>"
            + "The local library can differ from a master<br>"
            + "library afterwards.");
    // CURRENTLY NOT USED!
    // pnlLibrarySettings.add(chkLibraryEditing);

    chkUseMasterLibrary = new JCheckBox("Use Master Domain Library");
    chkUseMasterLibrary.setSelected(preferences.getBoolean(
        PreferencesService.USE_MASTER_DOMAIN_LIB, false));
    chkUseMasterLibrary.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        setLibraryOptionsEnabled(!chkUseMasterLibrary.isSelected());
      }
    });
    pnlLibrarySettings.add(chkUseMasterLibrary);

    // set up combobox with config tables
    cbDomainLibTable = new JComboBox();
    try {
      List<String> domainLibTables =
          ConfigLoaderHelper.fetchAllConfigTables(PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_JDBC), PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_URL), ConfigLoaderHelper.DOMAIN_LIB_CONFIG_TYPE);
      for (String tableName : domainLibTables) {
        cbDomainLibTable.addItem(tableName);
      }
      String userPrefTable =
          PreferencesService.getInstance().getUserPrefs().get(PreferencesService.USER_DOMAIN_LIB_TABLE, null);
      if (userPrefTable == null && cbDomainLibTable.getItemCount() > 0) {
        cbDomainLibTable.setSelectedIndex(0);
      } else {
        cbDomainLibTable.setSelectedItem(userPrefTable);
      }

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Could not fetch domain library tables from database.", "Error", JOptionPane.ERROR_MESSAGE);
      LOG.error(e.toString());
      return null;
    }
    pnlLibrarySettings.add(cbDomainLibTable);
    pnlLibrarySettings.setMaximumSize(new Dimension((int) pnlLibrarySettings.getMaximumSize().getWidth(), 0));

    setLibraryOptionsEnabled(!chkUseMasterLibrary.isSelected());

    return pnlLibrarySettings;
  }

  /**
   * @return {@link JPanel} of mutation settings
   */
  private JPanel buildMutationSettingsPanel() {
    JPanel pnlMutationSettings = new JPanel();
    pnlMutationSettings.setLayout(new GridLayout(2, 1));
    pnlMutationSettings.setBorder(BorderFactory
        .createTitledBorder("Mutation Library Settings"));

    ckUseMasterMutation = new JCheckBox("Use Master Mutation Library");
    ckUseMasterMutation.setSelected(preferences.getBoolean(
        PreferencesService.USE_MASTER_MUTATION_LIB, false));
    ckUseMasterMutation.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        setMutationLibraryOptionsEnabled(!ckUseMasterMutation.isSelected());
      }
    });
    pnlMutationSettings.add(ckUseMasterMutation);

    // set up combobox with config tables
    cbMutationLibTable = new JComboBox();
    try {
      List<String> mutationLibTables =
          ConfigLoaderHelper.fetchAllConfigTables(PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_JDBC), PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_URL), ConfigLoaderHelper.MUTATION_LIB_CONFIG_TYPE);
      for (String tableName : mutationLibTables) {
        cbMutationLibTable.addItem(tableName);
      }
      String userPrefTable =
          PreferencesService.getInstance().getUserPrefs().get(PreferencesService.USER_MUTATION_LIB_TABLE, null);
      if (userPrefTable == null && cbMutationLibTable.getItemCount() > 0) {
        cbMutationLibTable.setSelectedIndex(0);
      } else {
        cbMutationLibTable.setSelectedItem(userPrefTable);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Could not fetch mutation library tables from database.", "Error", JOptionPane.ERROR_MESSAGE);
      LOG.error(e.toString());
      return null;
    }
    pnlMutationSettings.add(cbMutationLibTable);
    pnlMutationSettings.setMaximumSize(new Dimension((int) pnlMutationSettings.getMaximumSize().getWidth(), 0));

    setMutationLibraryOptionsEnabled(!ckUseMasterMutation.isSelected());

    return pnlMutationSettings;
  }

  /**
   * @return {@link JPanel} of autoconnector configuration settings
   */
  private JPanel buildAutoconnectorSettingsPanel() {
    JPanel pnlAutoconnectorSettings = new JPanel();
    pnlAutoconnectorSettings.setLayout(new GridLayout(2, 1));
    pnlAutoconnectorSettings.setBorder(BorderFactory
        .createTitledBorder("Autoconnector Settings"));

    ckUseMasterAutoconnector = new JCheckBox("Use Master Autoconnector Configuration");
    ckUseMasterAutoconnector.setSelected(preferences.getBoolean(
        PreferencesService.USE_MASTER_AUTOCONNECTOR_CONFIG, false));
    ckUseMasterAutoconnector.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        setAutoconnectorOptionsEnabled(!ckUseMasterAutoconnector.isSelected());
      }
    });
    pnlAutoconnectorSettings.add(ckUseMasterAutoconnector);

    // set up combobox with config tables
    cbAutoconnectorTable = new JComboBox();
    try {
      List<String> autoconnectorTables =
          ConfigLoaderHelper.fetchAllConfigTables(PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_JDBC), PreferencesService.getInstance().getApplicationPrefs().getString(PreferencesService.CONFIG_LOADER_URL), ConfigLoaderHelper.AUTOCONNECTOR_CONFIG_TYPE);
      for (String tableName : autoconnectorTables) {
        cbAutoconnectorTable.addItem(tableName);
      }
      String userPrefTable =
          PreferencesService.getInstance().getUserPrefs().get(PreferencesService.USER_AUTOCONNECTOR_TABLE, null);
      if (userPrefTable == null && cbAutoconnectorTable.getItemCount() > 0) {
        cbAutoconnectorTable.setSelectedIndex(0);
      } else {
        cbAutoconnectorTable.setSelectedItem(userPrefTable);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Could not fetch autoconnector tables from database.", "Error", JOptionPane.ERROR_MESSAGE);
      LOG.error(e.toString());
      return null;
    }
    pnlAutoconnectorSettings.add(cbAutoconnectorTable);
    pnlAutoconnectorSettings.setMaximumSize(new Dimension((int) pnlAutoconnectorSettings.getMaximumSize().getWidth(), 0));

    setAutoconnectorOptionsEnabled(!ckUseMasterAutoconnector.isSelected());

    return pnlAutoconnectorSettings;
  }

  /**
   * @return @link JPanel} of blast settings
   */
  private JPanel buildBlastSettings() {
    JPanel pnlBlast = new JPanel();
    pnlBlast.setLayout(new GridLayout(8, 1));
    pnlBlast.setBorder(BorderFactory.createTitledBorder("Domain Detection"));

    JPanel pnlMaxOrMinSelection = new JPanel(
        new FlowLayout(FlowLayout.LEFT));
    String[] maxOrMinChoice = new String[] {PreferencesService.MIN,
        PreferencesService.MAX, PreferencesService.MINMAX};
    cboMaxOrMinDomain = new JComboBox(maxOrMinChoice);
    cboMaxOrMinDomain.setPreferredSize(new Dimension(80, 20));
    cboMaxOrMinDomain.setSelectedItem(preferences.get(
        PreferencesService.MAX_OR_MIN_DOMAINS,
        PreferencesService.MINMAX));
    JLabel maxOrMinDomainLabel = new JLabel("Sort Domains Which Way?");
    maxOrMinDomainLabel
        .setToolTipText("<html>With MIN-option, the domains are seperated more,<br>"
            + "possibly leading to overlap.<br>"
            + "With MAX-option there will be less overlap but<br>"
            + "compatible domains might be sorted into one domain area.");
    pnlMaxOrMinSelection.add(cboMaxOrMinDomain);
    pnlMaxOrMinSelection.add(maxOrMinDomainLabel);
    pnlBlast.add(pnlMaxOrMinSelection);

    JPanel pnlMaxDomainDist = new JPanel(new FlowLayout(FlowLayout.LEFT));
    NumberFormatter distanceFormatter = new NumberFormatter();
    distanceFormatter.setValueClass(Integer.class);
    distanceFormatter.setMinimum(0);
    distanceFormatter.setMaximum(Integer.MAX_VALUE);
    distanceFormatter.setCommitsOnValidEdit(true);
    tfMaxDomainDistTextField = new JFormattedTextField(distanceFormatter);
    tfMaxDomainDistTextField.setPreferredSize(new Dimension(30, 20));
    tfMaxDomainDistTextField.setText(String.valueOf(preferences.getInt(
        PreferencesService.MAX_DOMAIN_DISTANCE, 3)));
    JLabel maxDomainDistLabel = new JLabel(
        "Maximal Distance Between Domains");
    maxDomainDistLabel
        .setToolTipText("<html>If the distance between two domains is greater<br>"
            + "than this value, you will be prompted to insert<br>"
            + "a new domain between them.");
    pnlMaxDomainDist.add(tfMaxDomainDistTextField);
    pnlMaxDomainDist.add(maxDomainDistLabel);
    pnlBlast.add(pnlMaxDomainDist);

    JPanel pnlAutoextendDomains = new JPanel(new FlowLayout(FlowLayout.LEFT));
    cbAutoextendDomains = new JCheckBox();
    cbAutoextendDomains.setPreferredSize(new Dimension(30, 20));
    cbAutoextendDomains.setSelected(preferences.getBoolean(PreferencesService.DD_AUTOEXTEND_DOMAINS, true));
    JLabel autoextendDomainsLabel = new JLabel(
        "Autoextend Domains");
    autoextendDomainsLabel
        .setToolTipText("<html>If a library domain was not fully recognized by BLAST,<br /> the algorithm will try to extend the domain to the left and/or right,<br />if applicable.");
    pnlAutoextendDomains.add(cbAutoextendDomains);
    pnlAutoextendDomains.add(autoextendDomainsLabel);
    pnlBlast.add(pnlAutoextendDomains);

    JPanel pnlDomainConflictSolver = new JPanel(new FlowLayout(FlowLayout.LEFT));
    cbDomainConflictSolver = new JCheckBox();
    cbDomainConflictSolver.setPreferredSize(new Dimension(30, 20));
    cbDomainConflictSolver.setSelected(preferences.getBoolean(PreferencesService.DD_DOMAIN_CONFLICT_SOLVER, false));
    cbDomainConflictSolver.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (cbDomainConflictSolver.isSelected()) {
          cbAutoextendDomains.setEnabled(false);
          cbAutoextendDomains.setSelected(true);
        } else {
          cbAutoextendDomains.setEnabled(true);
        }
      }
    });
    JLabel lblDomainConflictSolver = new JLabel(
        "Domain Conflict Solver");
    lblDomainConflictSolver
        .setToolTipText("<html>If a library domain was not fully recognized by BLAST,<br /> the algorithm will try to resolve overlaps or remove gaps by extending or shrinking the respective domains.<br />This setting is an extension to 'Autoextend Domains'.");
    pnlDomainConflictSolver.add(cbDomainConflictSolver);
    pnlDomainConflictSolver.add(lblDomainConflictSolver);
    pnlBlast.add(pnlDomainConflictSolver);

    cbAutoextendDomains.setEnabled(!cbDomainConflictSolver.isSelected());

    JPanel pnlLowerSortingThreshold = new JPanel(new FlowLayout(
        FlowLayout.LEFT));
    distanceFormatter.setValueClass(Integer.class);
    distanceFormatter.setCommitsOnValidEdit(true);

    tfLowerSortingThreshold = new JFormattedTextField();
    tfLowerSortingThreshold.setPreferredSize(new Dimension(50, 20));
    tfLowerSortingThreshold.setText(String.valueOf(preferences.getDouble(
        PreferencesService.DD_LOWER_SORTING_THRESHOLD, 1e-2)));
    JLabel lblLowerSortingThreshold = new JLabel(
        "Threshold for coverage sorting");
    lblLowerSortingThreshold
        .setToolTipText("<html>When the eValue is bigger(worse) than this <br/>"
            + "setting, it is considered as not significant.<br />"
            + "Therefore these hits are sorted by coverage only.<br /><br />"
            + "This value may be entered in decimal or scientific notation.");
    chkUseLowerThreshold = new JCheckBox();
    chkUseLowerThreshold.setSelected(preferences.getBoolean(PreferencesService.USE_LOWER_THRESHOLD, true));
    if (chkUseLowerThreshold.isSelected() == false) {
      tfLowerSortingThreshold.setEnabled(false);
    }
    chkUseLowerThreshold.setToolTipText("Enables threshold for coverage sorting");
    chkUseLowerThreshold.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        setLowerThresholdEnabled(chkUseLowerThreshold.isSelected());

      }
    });
    pnlLowerSortingThreshold.add(chkUseLowerThreshold);
    pnlLowerSortingThreshold.add(tfLowerSortingThreshold);
    pnlLowerSortingThreshold.add(lblLowerSortingThreshold);
    pnlBlast.add(pnlLowerSortingThreshold);

    JPanel pnlUpperSortingThreshold = new JPanel(new FlowLayout(
        FlowLayout.LEFT));
    distanceFormatter.setValueClass(Integer.class);
    distanceFormatter.setCommitsOnValidEdit(true);
    tfUpperSortingThreshold = new JFormattedTextField();
    tfUpperSortingThreshold.setPreferredSize(new Dimension(50, 20));
    tfUpperSortingThreshold.setText(String.valueOf(preferences.getDouble(
        PreferencesService.DD_UPPER_SORTING_THRESHOLD, 1e-8)));
    JLabel lblUpperSortingThreshold = new JLabel(
        "Threshold for coverage*identity sorting");
    lblUpperSortingThreshold
        .setToolTipText("<html>When the eValue is lower(better) than this <br/>"
            + "setting, it is considered as highly significant.<br />"
            + "Therefore these hits are sorted by the product of coverage and identity.<br />"
            + "<br />" + "This value may be entered in decimal or scientific notation.");
    chkUseUpperThreshold = new JCheckBox();
    chkUseUpperThreshold.setSelected(preferences.getBoolean(PreferencesService.USE_UPPER_THRESHOLD, true));
    if (chkUseLowerThreshold.isSelected() == false) {
      tfUpperSortingThreshold.setEnabled(false);
    }
    chkUseUpperThreshold.setToolTipText("Enables threshold for coverage * identity sorting");
    chkUseUpperThreshold.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        setUpperThresholdEnabled(chkUseUpperThreshold.isSelected());

      }
    });
    pnlUpperSortingThreshold.add(chkUseUpperThreshold);
    pnlUpperSortingThreshold.add(tfUpperSortingThreshold);
    pnlUpperSortingThreshold.add(lblUpperSortingThreshold);
    pnlBlast.add(pnlUpperSortingThreshold);

    // // Skip Hit Percents
    NumberFormatter percentFormatter = new NumberFormatter();
    percentFormatter.setValueClass(Integer.class);
    percentFormatter.setMinimum(0);
    percentFormatter.setMaximum(100);
    percentFormatter.setCommitsOnValidEdit(false);

    JPanel pnlSkipHitPercentCovThreshold = new JPanel(new FlowLayout(
        FlowLayout.LEFT));
    tfSkipHitPercentCovThreshold = new JFormattedTextField(percentFormatter);
    tfSkipHitPercentCovThreshold.setPreferredSize(new Dimension(30, 20));
    tfSkipHitPercentCovThreshold.setText(String.valueOf(preferences.getInt(
        PreferencesService.DD_SKIP_HIT_PERCENT_COV_THRESHOLD, 50)));
    JLabel lblSkipHitPercentCovThreshold = new JLabel("% Minimum Coverage.");
    lblSkipHitPercentCovThreshold
        .setToolTipText("<html>Blast Hit won't appear in the list of hits, <br />"
            + "when their coverage is smaller than this threshold.");
    pnlSkipHitPercentCovThreshold.add(tfSkipHitPercentCovThreshold);
    pnlSkipHitPercentCovThreshold.add(lblSkipHitPercentCovThreshold);
    pnlBlast.add(pnlSkipHitPercentCovThreshold);

    JPanel pnlSkipHitPercentIdThreshold = new JPanel(new FlowLayout(
        FlowLayout.LEFT));
    tfSkipHitPercentIdThreshold = new JFormattedTextField(percentFormatter);
    tfSkipHitPercentIdThreshold.setPreferredSize(new Dimension(30, 20));

    tfSkipHitPercentIdThreshold.setText(String.valueOf(preferences.getInt(
        PreferencesService.DD_SKIP_HIT_PERCENT_ID_THRESHOLD, 50)));
    JLabel lblSkipHitPercentIdThreshold = new JLabel("% Minimum Identity.");
    lblSkipHitPercentIdThreshold
        .setToolTipText("<html>Blast Hit won't appear in the list of hits, <br />"
            + "when their identity with parent sequence is smaller than this threshold.");
    pnlSkipHitPercentIdThreshold.add(tfSkipHitPercentIdThreshold);
    pnlSkipHitPercentIdThreshold.add(lblSkipHitPercentIdThreshold);
    pnlBlast.add(pnlSkipHitPercentIdThreshold);

    pnlBlast.setMaximumSize(pnlBlast.getMinimumSize());

    return pnlBlast;
  }

  /**
   * @return {@link JPanel} of command buttons
   */
  private JPanel buildButtonPanel() {
    JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    cmdSave = new JButton("Save and Close");
    cmdSave.addActionListener(this);
    cmdCancel = new JButton("Cancel");
    cmdCancel.addActionListener(this);
    pnlButtons.add(cmdCancel);
    pnlButtons.add(cmdSave);
    return pnlButtons;
  }

  private void setLibraryOptionsEnabled(boolean enabled) {
    cbDomainLibTable.setEnabled(enabled);
  }

  private void setMutationLibraryOptionsEnabled(boolean enabled) {
    cbMutationLibTable.setEnabled(enabled);
  }

  private void setAutoconnectorOptionsEnabled(boolean enabled) {
    cbAutoconnectorTable.setEnabled(enabled);
  }

  private void setLowerThresholdEnabled(boolean enabled) {
    tfLowerSortingThreshold.setEnabled(enabled);
  }

  private void setUpperThresholdEnabled(boolean enabled) {
    tfUpperSortingThreshold.setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cmdSave) {
      try {
        preferences.putBoolean(
            PreferencesService.ALLOW_LIBRARY_EDITING,
            chkLibraryEditing.isSelected());
        preferences.putBoolean(
            PreferencesService.USE_MASTER_DOMAIN_LIB,
            chkUseMasterLibrary.isSelected());
        preferences.put(PreferencesService.MAX_OR_MIN_DOMAINS,
            (String) cboMaxOrMinDomain.getSelectedItem());
        preferences.put(PreferencesService.USER_DOMAIN_LIB_TABLE,
            cbDomainLibTable.getSelectedItem().toString());

        preferences.putBoolean(
            PreferencesService.USE_MASTER_MUTATION_LIB,
            ckUseMasterMutation.isSelected());
        preferences.put(PreferencesService.USER_MUTATION_LIB_TABLE,
            cbMutationLibTable.getSelectedItem().toString());

        preferences.putBoolean(
            PreferencesService.USE_MASTER_AUTOCONNECTOR_CONFIG,
            ckUseMasterAutoconnector.isSelected());
        preferences.put(PreferencesService.USER_AUTOCONNECTOR_TABLE,
            cbAutoconnectorTable.getSelectedItem().toString());

        preferences.putInt(PreferencesService.MAX_DOMAIN_DISTANCE,
            Integer.parseInt(nvl(
                tfMaxDomainDistTextField.getText(), "0")));
        preferences.putBoolean(PreferencesService.USE_LOWER_THRESHOLD, chkUseLowerThreshold.isSelected());
        preferences.putBoolean(PreferencesService.DD_AUTOEXTEND_DOMAINS, cbAutoextendDomains.isSelected());
        preferences.putBoolean(PreferencesService.DD_DOMAIN_CONFLICT_SOLVER, cbDomainConflictSolver.isSelected());
        preferences.putDouble(
            PreferencesService.DD_LOWER_SORTING_THRESHOLD,
            Double.parseDouble((nvl(tfLowerSortingThreshold.getText(),
                "0"))));
        preferences.putBoolean(PreferencesService.USE_UPPER_THRESHOLD, chkUseUpperThreshold.isSelected());
        preferences.putDouble(
            PreferencesService.DD_UPPER_SORTING_THRESHOLD,
            Double.parseDouble(tfUpperSortingThreshold.getText()));
        preferences.putInt(
            PreferencesService.DD_SKIP_HIT_PERCENT_COV_THRESHOLD,
            Integer.parseInt(nvl(
                tfSkipHitPercentCovThreshold.getText(), "0")));
        preferences.putInt(
            PreferencesService.DD_SKIP_HIT_PERCENT_ID_THRESHOLD,
            Integer.parseInt(nvl(
                tfSkipHitPercentIdThreshold.getText(), "0")));

        preferences.flush();
        dispose();
      } catch (BackingStoreException ex) {
        JOptionPane.showMessageDialog(this,
            "Could not save preferences",
            "Error saving preferences", JOptionPane.ERROR_MESSAGE);
        LOG.error("Error saving Preferences: {}", e);
      } catch (NumberFormatException exc) {
        JOptionPane.showMessageDialog(this,
            "Wrong threshold value entered.",
            "Error saving preferences", JOptionPane.ERROR_MESSAGE);
        tfUpperSortingThreshold.setText("");
        tfLowerSortingThreshold.setText("");
        LOG.error("NumberFormatException {}", exc.getMessage());
      }

    } else if (e.getSource() == cmdCancel) {
      dispose();

    }
  }

  /**
   * Returns given string, or default when string is null
   * 
   * @param str string
   * @param def default string
   * @return str or def depending on str == null
   */
  private String nvl(String str, String def) {
    return str == null ? def : str;
  }

}
