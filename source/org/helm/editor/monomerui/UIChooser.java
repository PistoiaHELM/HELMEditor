/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.helm.editor.monomerui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.IconGenerator;
import java.awt.event.ActionListener;
import javax.swing.Box;

/**
 * @author Alexander Makarov
 */
public class UIChooser extends JFrame {

	private MacromoleculeEditor editor;
	private JButton setupUI;
	private JRadioButton[] radioButtons;
	private JList filesList;
	private String[] names = { "Tab", "Tree" };
	private String[] defaultFilesList = { "File_1.xml", "File_2.xml",
			"File_3.xml" };

	// private static final String HELM_ICON_PATH =
	// "/org/helm/editor/editor/resource/Icon-HELM.png";

	public UIChooser(MacromoleculeEditor editor) {
		this.editor = editor;

		constructUI();

		configComponents();
	}

	private void constructUI() {

		// panel for choose ui type
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel,
				BoxLayout.X_AXIS));
		radioButtonPanel.add(Box.createHorizontalStrut(5));
		JLabel chooseUILabel = new JLabel("Monomer Layout Type: ");
		radioButtonPanel.add(chooseUILabel);
		radioButtons = new JRadioButton[names.length];
		ButtonGroup buttonGroup = new ButtonGroup();
		for (int i = 0; i < names.length; i++) {
			radioButtons[i] = new JRadioButton(names[i]);
			buttonGroup.add(radioButtons[i]);
			radioButtonPanel.add(radioButtons[i]);
		}
		radioButtonPanel.add(Box.createHorizontalGlue());

		// panel for choose ui hierarchy
		JPanel uiFilesLabelPanel = new JPanel();
		uiFilesLabelPanel.setLayout(new BoxLayout(uiFilesLabelPanel,
				BoxLayout.X_AXIS));
		uiFilesLabelPanel.add(Box.createHorizontalStrut(5));
		JLabel chooseUIFile = new JLabel("Monomer Categorization Template: ");
		uiFilesLabelPanel.add(chooseUIFile);
		uiFilesLabelPanel.add(Box.createHorizontalGlue());

		filesList = new JList(defaultFilesList);
		filesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		filesList.setLayoutOrientation(JList.VERTICAL_WRAP);
		// filesList.setPreferredSize(new Dimension(200, 50));
		JScrollPane listScrollPane = new JScrollPane(filesList);
		JPanel uiFilesPanel = new JPanel();
		uiFilesPanel.setLayout(new BoxLayout(uiFilesPanel, BoxLayout.X_AXIS));
		uiFilesPanel.add(Box.createHorizontalStrut(5));
		uiFilesPanel.add(listScrollPane);
		uiFilesPanel.add(Box.createHorizontalStrut(5));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		setupUI = new JButton("Save");
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(setupUI);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(closeButton);
		buttonPanel.add(Box.createHorizontalGlue());

		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		add(Box.createVerticalStrut(5));
		add(radioButtonPanel);
		add(Box.createVerticalStrut(5));
		add(uiFilesLabelPanel);
		add(uiFilesPanel);
		add(Box.createVerticalStrut(5));
		add(buttonPanel);
		add(Box.createVerticalStrut(5));

	}

	public void setDefaultForUIType(String type) {
		for (JRadioButton currRButton : radioButtons) {
			if (currRButton.getText().equalsIgnoreCase(type)) {
				currRButton.setSelected(true);
			}
		}
	}

	public String getSelectedUIXml() {
		return (String) filesList.getSelectedValue();
	}

	public void setFileList(String[] data) {
		filesList.setListData(data);
	}

	public void setDefaultForUIXml(String uiXML) {
		int size = filesList.getModel().getSize();
		for (int i = 0; i < size; i++) {
			String tmp = (String) filesList.getModel().getElementAt(i);
			if (tmp.equals(uiXML)) {
				filesList.setSelectedIndex(i);
				break;
			}
		}
	}

	public String getSelectedUIType() {
		for (JRadioButton currRadioButton : radioButtons) {
			if (currRadioButton.isSelected()) {
				return currRadioButton.getText();
			}
		}

		return null;
	}

	public String getSelectedXml() {
		return null;
	}

	public JButton getSetupButton() {
		return setupUI;
	}

	private void configComponents() {
		setTitle("HELM Editor: User Preference");
		setIconImage(IconGenerator
				.getImage(IconGenerator.HELM_APP_ICON_RESOURCE_URL));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		setSize(300, 200);

		setLocationRelativeTo(editor.getFrame());
		setResizable(false);
	}
}
