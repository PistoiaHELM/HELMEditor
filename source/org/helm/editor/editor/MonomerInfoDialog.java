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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.helm.editor.manager.MonomerViewer;
import org.helm.editor.utility.IconGenerator;
import org.helm.notation.model.Monomer;

public class MonomerInfoDialog extends JFrame {

	private JPanel _mainPanel;
	private MonomerViewer _monomerView;
	private JFrame _parent;
	private JButton _okButton;
	private static MonomerInfoDialog dialog;

	public static MonomerInfoDialog getDialog(JFrame parent) {
		if (dialog == null) {
			dialog = new MonomerInfoDialog(parent);
		}
		return dialog;
	}

	private MonomerInfoDialog(JFrame parent) {

		_parent = parent;

		initComponents();
		configComponents();
	}

	private void initComponents() {
		_mainPanel = new JPanel();

		_monomerView = MonomerViewer.getNamedInstance("MonomerInfoDialog");
		_monomerView.setModifiableStatus(false);
		_mainPanel.add(_monomerView);

		_okButton = new JButton("Ok");
		_mainPanel.add(_okButton);
	}

	private void configComponents() {
		// config components
		_mainPanel.setLayout(new BoxLayout(_mainPanel, BoxLayout.Y_AXIS));

		_okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				MonomerInfoDialog.this.setVisible(false);
			}
		});

		// config frame
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setTitle("Monomer Info");
		setIconImage(IconGenerator
				.getImage(IconGenerator.HELM_APP_ICON_RESOURCE_URL));
		setContentPane(_mainPanel);
		pack();
		setLocationRelativeTo(_parent);
	}

	public void setMonomer(Monomer monomer) {
		_monomerView.setMonomer(monomer);
	}
}
