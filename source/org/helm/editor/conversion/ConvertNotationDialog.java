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
package org.helm.editor.conversion;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.jdesktop.swingworker.SwingWorker;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.IconGenerator;

public class ConvertNotationDialog extends JFrame {

	private MacromoleculeEditor _editor;
	private ConvertNotationController _controller;

	private final Logger _logger;
	
	private String[] _selections = { "Nucleotide Sequence", "HELM Notation" };

	private JLabel _toNotationLabel;
	private JLabel _fromNotationLabel;

	private JTextArea _toNotationArea;
	private JTextArea _fromNotationArea;
	private JScrollPane _toNotationScrollPane;
	private JScrollPane _fromNotationScrollPane;

	private JComboBox _selectToNotation;
	private JComboBox _selectFromNotation;

	private JButton _convertNotation;
	private JButton _closeButton;

	private JPanel _mainPanel;

	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 300;
	private static final int AREA_SIZE = 5;
	private static final int DEFAULT_SELECTED_INDEX = 0;

	private static final String EMPTY_STRING = "";
	
	private static final boolean CONTROLS_BLOCKED = false;
	private static final boolean CONTROLS_ENABLED = true;
	
	public ConvertNotationDialog(MacromoleculeEditor editor) {
		
		_editor = editor;

		_logger = Logger.getLogger(ConvertNotationDialog.class.getName());

		createComponents();
		setDialogLayout();
		configComponents();
	}

	private void setDialogLayout() {

		_mainPanel.setLayout(new GridBagLayout());
		_controller = new ConvertNotationController();

		topDialogLayout();

		middleDialogLayout();

		bottomDialogLayout();
	}

	private void topDialogLayout() {
		GridBagConstraints constrain = new GridBagConstraints();
		constrain.gridy = 0;

		constrain.fill = GridBagConstraints.NONE;
		constrain.anchor = GridBagConstraints.WEST;
		constrain.gridx = 0;
		constrain.insets = new Insets(0, 10, 0, 10);
		_mainPanel.add(_fromNotationLabel, constrain);

		constrain.fill = GridBagConstraints.NONE;
		constrain.anchor = GridBagConstraints.WEST;
		constrain.gridx = 1;
		constrain.insets = new Insets(10, 0, 10, 10);
		_mainPanel.add(_selectFromNotation, constrain);
		
		constrain.fill = GridBagConstraints.NONE;
		constrain.anchor = GridBagConstraints.WEST;
		constrain.gridx = 3;
		constrain.insets = new Insets(10, 10, 10, 0);
		_mainPanel.add(_toNotationLabel, constrain);

		constrain.fill = GridBagConstraints.NONE;
		constrain.anchor = GridBagConstraints.WEST;
		constrain.gridx = 4;
		constrain.insets = new Insets(10, 10, 10, 10);
		_mainPanel.add(_selectToNotation, constrain);
	}

	private void middleDialogLayout() {
		GridBagConstraints constrain = new GridBagConstraints();
		constrain.gridy = 1;

		constrain.fill = GridBagConstraints.BOTH;
		constrain.gridwidth = 2;
		constrain.weightx = 10;
		constrain.weighty = 10;
		constrain.gridx = 0;
		constrain.insets = new Insets(0, 10, 10, 10);
		_mainPanel.add(_fromNotationScrollPane, constrain);

		constrain.fill = GridBagConstraints.NONE;
		constrain.gridx = GridBagConstraints.RELATIVE;
		constrain.weightx = 0.1;
		constrain.weighty = 0.1;
		constrain.gridwidth = 1;
		_mainPanel.add(_convertNotation, constrain);

		constrain.fill = GridBagConstraints.BOTH;
		constrain.gridwidth = 2;
		constrain.weightx = 10;
		constrain.weighty = 10;
		constrain.gridx = 3;
		constrain.insets = new Insets(0, 10, 10, 10);
		_mainPanel.add(_toNotationScrollPane, constrain);
	}

	private void bottomDialogLayout() {
		GridBagConstraints constrain = new GridBagConstraints();

		constrain.fill = GridBagConstraints.NONE;
		constrain.anchor = GridBagConstraints.LAST_LINE_END;
		constrain.gridx = 4;
		constrain.gridy = 2;
		constrain.insets = new Insets(0, 0, 10, 10);
		_mainPanel.add(_closeButton, constrain);
	}

	private void configComponents() {
		setTitle("HELM Notation Converter");
                setIconImage(IconGenerator.getImage(IconGenerator.HELM_APP_ICON_RESOURCE_URL));
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setLocationRelativeTo(_editor.getFrame());
                _convertNotation.setIcon(IconGenerator.getIcon(IconGenerator.FORWARD_ARROW_ICON_RESOURCE_URL));
		_convertNotation.setVerticalTextPosition(SwingConstants.CENTER);
	    _convertNotation.setHorizontalTextPosition(SwingConstants.LEFT);

		_toNotationArea.setEditable(false);
		_toNotationArea.setLineWrap(true);

		_fromNotationArea.setEditable(true);
		_fromNotationArea.setLineWrap(true);

		_convertNotation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 SwingWorker<Void, String> worker= new SwingWorker<Void, String>(){

					private String _convertingResult;
					 
					@Override
					protected void done() {						
						_toNotationArea.setText( _convertingResult );
						(ConvertNotationDialog.this).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						(ConvertNotationDialog.this).setControlsState(CONTROLS_ENABLED);
					}

					protected Void doInBackground() throws Exception {
						
						(ConvertNotationDialog.this).setControlsState(CONTROLS_BLOCKED);					
						
						(ConvertNotationDialog.this).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						
						_convertingResult = convertinNotationProcess();
						
						return null;
					}
										
				};
				worker.execute();							
			}
		});

		_closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}

		});

		_selectToNotation.setSelectedIndex(DEFAULT_SELECTED_INDEX);
	}
	
	private void setControlsState(boolean state){		
		_selectFromNotation.setEnabled(state);
		_fromNotationArea.setEnabled(state);
		_fromNotationScrollPane.setEnabled(state);
				
		_convertNotation.setEnabled(state);
		
		_selectToNotation.setEnabled(state);
		_toNotationScrollPane.setEnabled(state);
		
		_closeButton.setEnabled(state);
	}
	
	private void createComponents() {
		_mainPanel = new JPanel();

		_fromNotationLabel = new JLabel("From notation");
		_selectFromNotation = new JComboBox(_selections);
		_fromNotationArea = new JTextArea(AREA_SIZE, AREA_SIZE);
		_fromNotationScrollPane = new JScrollPane(_fromNotationArea);

		_convertNotation = new JButton("Convert");
		
		_toNotationLabel = new JLabel("To notation");
		_selectToNotation = new JComboBox(_selections);
		_toNotationArea = new JTextArea(AREA_SIZE, AREA_SIZE);
		_toNotationScrollPane = new JScrollPane(_toNotationArea);

		_closeButton = new JButton("Close");

		setContentPane(_mainPanel);
	}

	private String convertinNotationProcess() {
		String convertedText = _fromNotationArea.getText();
		if (convertedText == null || convertedText.equals(EMPTY_STRING)) {
			JOptionPane.showMessageDialog(ConvertNotationDialog.this,
					"Please, enter notation string.");
		}
		
		_toNotationArea.setText(EMPTY_STRING);
		String newNotation = _controller.getAnotherNotation(_selectFromNotation
				.getSelectedIndex(), _selectToNotation
				.getSelectedIndex(), convertedText);

		return newNotation;
	}


}
