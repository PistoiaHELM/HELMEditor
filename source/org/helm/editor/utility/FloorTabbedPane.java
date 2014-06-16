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
package org.helm.editor.utility;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.helm.editor.monomerui.tabui.BaseXMLPanel;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.XmlFragment;

/**
 * 
 * @author lih25
 */
public class FloorTabbedPane extends JPanel implements ActionListener {

	private GridBagConstraints gbc = new GridBagConstraints(0, 1, 1, 1, 1.0,
			1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0);

	private GridBagLayout gbl = new GridBagLayout();
	protected CardLayout cl = new CardLayout();
	protected List<JButton> buttons = new ArrayList<JButton>();
	protected Map<JButton, String> buttonTitles = new HashMap<JButton, String>();
	protected Map<JButton, String> boldButtonTitles = new HashMap<JButton, String>();

	protected JPanel panels = new JPanel(cl);

	public FloorTabbedPane() {
		setLayout(gbl);
		add(panels, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.0;
	}

	public void addTab(String name, JComponent component) {
		gbc.gridy = getComponentCount();
		if (gbc.gridy == 1) {
			gbc.gridy = 0;
		}

		if (name != null && !name.equals("")) {
			String boldName = "<html><b>" + name + "</b></html>";

			JButton jb;
			if (buttons.size() == 0) {
				// first button
				jb = new JButton(boldName);
			} else {
				// not first button
				jb = new JButton(name);
			}
			add(jb, gbc);
			buttons.add(jb);
			buttonTitles.put(jb, name);
			boldButtonTitles.put(jb, boldName);
			jb.addActionListener(this);
		}

		panels.add(component, name);
	}

	public void actionPerformed(ActionEvent ae) {

		int y = 0;
		GridBagConstraints tmp;
		JButton srcButton = (JButton) ae.getSource();
		for (JButton button : buttons) {
			String buttonText = button.getText();
			if (button == srcButton) {
				button.setText(boldButtonTitles.get(button));
			} else {
				button.setText(buttonTitles.get(button));
			}
		}
		for (int i = 0; i < buttons.size(); i++) {
			JButton jb = (JButton) buttons.get(i);
			clear();
			tmp = gbl.getConstraints(jb);
			tmp.gridy = y++;
			gbl.setConstraints(jb, tmp);
			if (srcButton == jb) {
				tmp = gbl.getConstraints(panels);
				tmp.gridy = y++;
				gbl.setConstraints(panels, tmp);
			}
		}
		cl.show(panels, buttonTitles.get(srcButton));
	}

	/**
	 * for override
	 */
	public void clear() {

	}

	protected void updateOthersTab() {
	}
}
