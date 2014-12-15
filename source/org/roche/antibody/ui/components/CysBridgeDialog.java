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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.services.AbstractGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog class for cystein bridge creation dialog. Event listener for main view panel will detect graph creation event
 * and instantiate this class. After user confirms which would be the start and end, either the dialog disvalidates
 * user's choices (due to pre-defined reason) or confirms the creation. Conformation process could then be divided into
 * three parts : deletion of bridge already created by the user, creation of "structured" bridge by the class and
 * surpression of the event caused by recreation of the bridge.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * 
 * @see InitCysBridgeCreationDialog
 * 
 */
public class CysBridgeDialog extends JDialog {

  private static final int UNDEFINED = -1;

  /** */
  private static final long serialVersionUID = 1L;

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractGraphService.class);

  public static final int CMD_CANCEL = 0;

  public static final int CMD_SUCCESS = 1;

  private final JPanel contentPanel = new JPanel();

  private JLabel mainLabel;

  private JLabel sourceDomain;

  private JLabel targetDomain;

  private JLabel sourceChain;

  private JLabel targetChain;

  private JComboBox cmbSourcePos;

  private JComboBox cmbTargetPos;

  private JButton okButton;

  private JButton cancelButton;

  private AntibodyEditorPane viewDialog;

  private Domain source;

  private Domain target;

  private CysteinConnection createdCysBridge;

  private int dialogState;

  /**
   * Constructor
   * 
   * @param antibody antibody on which the change would take place
   * @param viewDialog the caller of this instance of dialog
   * @param source source Domain of the new edge/cystein bridge
   * @param target target Domain of the new edge/cystein bridge
   */
  public CysBridgeDialog(Domain source, Domain target) {

    this.source = source;
    this.target = target;
    initateComponents();
  }

  /**
   * Initiates Swing components and possible model structure underlying it
   */
  private void initateComponents() {
    this.setModal(true);
    setAlwaysOnTop(true);
    setResizable(false);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        dialogState = CMD_CANCEL;
        super.windowClosing(e);
      }
    });
    setLocationRelativeTo(viewDialog);
    getContentPane().setLayout(new BorderLayout());

    mainLabel = new JLabel("Choose cystein position from source and target domain");
    mainLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    getContentPane().add(mainLabel, BorderLayout.NORTH);

    contentPanel.setLayout(new GridLayout(1, 2));
    contentPanel.add(buildSourcePanel());
    contentPanel.add(buildTargetPanel());
    getContentPane().add(contentPanel, BorderLayout.CENTER);

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        confirmButtonPerformed();
      }

    });
    buttonPane.add(okButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        dialogState = CMD_CANCEL;
        LOG.debug("CANCEL BUTTON pressed!");
        dispose();
      }
    });
    buttonPane.add(cancelButton);
    getRootPane().setDefaultButton(cancelButton);
    pack();
  }

  private JPanel buildSourcePanel() {
    JPanel sourcePanel = new JPanel(new GridLayout(3, 1));
    sourcePanel.setBorder(BorderFactory.createTitledBorder("Source Domain"));

    sourceDomain = new JLabel(source.getPeptide().getName());
    sourcePanel.add(sourceDomain);

    sourceChain = new JLabel(source.getName());
    sourcePanel.add(sourceChain);

    cmbSourcePos = new JComboBox();
    cmbSourcePos.setModel(new DefaultComboBoxModel(source.getFreeCysteinPositions().toArray()));
    sourcePanel.add(cmbSourcePos);

    return sourcePanel;
  }

  private JPanel buildTargetPanel() {
    JPanel targetPanel = new JPanel(new GridLayout(3, 1));
    targetPanel.setBorder(BorderFactory.createTitledBorder("Target Domain"));

    targetDomain = new JLabel(target.getPeptide().getName());
    targetPanel.add(targetDomain);

    targetChain = new JLabel(target.getName());
    targetPanel.add(targetChain);

    cmbTargetPos = new JComboBox();
    cmbTargetPos.setModel(new DefaultComboBoxModel(target.getFreeCysteinPositions().toArray()));
    targetPanel.add(cmbTargetPos);

    return targetPanel;
  }

  public int showDialog() {
    setCreatedCysBridge(null);
    dialogState = UNDEFINED;
    setVisible(true);
    LOG.debug("Returning state: {}", dialogState);
    return dialogState;
  }

  /**
   * Implements action for confirm button's listener. This method implements simple validation method deciding whether
   * the new bridge is allowed. If it allows it would then proceed to updateModel(), while illegal bridge would create a
   * dialog warning.
   */
  private void confirmButtonPerformed() {
    dialogState = CMD_SUCCESS;
    LOG.debug("OK BUTTON pressed!");
    if (source == target && cmbSourcePos.getSelectedIndex() == cmbTargetPos.getSelectedIndex()) {
      JOptionPane.showMessageDialog(this, "Couldn't create bond from identical sources!", "Warning", JOptionPane.WARNING_MESSAGE);
    }
    else {
      int sourcePos = (Integer) cmbSourcePos.getSelectedItem();
      int targetPos = (Integer) cmbTargetPos.getSelectedItem();
      setCreatedCysBridge(new CysteinConnection(sourcePos, targetPos, source.getPeptide(), target.getPeptide()));
      LOG.debug("CysBridge created: {}", getCreatedCysBridge());
      dispose();
    }
  }

  public CysteinConnection getCreatedCysBridge() {
    return this.createdCysBridge;
  }

  public void setCreatedCysBridge(CysteinConnection createdCysBridge) {
    this.createdCysBridge = createdCysBridge;
  }

}
