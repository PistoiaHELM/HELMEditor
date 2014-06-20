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
package org.helm.editor.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.helm.editor.utility.IconGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextViewer extends JFrame {

	private JTextArea textArea;
	private JFrame parent;
	private JButton okButton;
	private static TextViewer instance;
	private static Dimension MIN_SIZE = new Dimension(600, 400);

	public static TextViewer getInstance(JFrame parent) {
		if (instance == null) {
			instance = new TextViewer(parent);
		}
		return instance;
	}

	private TextViewer(JFrame parent) {
		this.parent = parent;
		initComponents();
	}

	private void initComponents() {
		JPanel mainPanel = new JPanel();

		textArea = new JTextArea();
		textArea.setColumns(60);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setRows(8);

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(textArea);

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(okButton);
		buttonBox.add(Box.createHorizontalGlue());

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scroll, BorderLayout.CENTER);
		mainPanel.add(buttonBox, BorderLayout.SOUTH);

		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setIconImage(IconGenerator
				.getImage(IconGenerator.HELM_APP_ICON_RESOURCE_URL));
		setContentPane(mainPanel);
		setPreferredSize(MIN_SIZE);
		pack();
		setLocationRelativeTo(parent);
	}

	public void setText(String text) {
		textArea.setText(text);
		setVisible(true);
	}

}
