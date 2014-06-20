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
package org.helm.editor.action;

import org.helm.editor.editor.NotationProducer;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * @author lih25
 */
public class ViewNotationAction extends AbstractAction {

	private NotationProducer producer;

	public ViewNotationAction(NotationProducer producer, String actionName) {
		super(actionName);
		this.producer = producer;
	}

	public void actionPerformed(ActionEvent e) {
		producer.getGraphViewComponent().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JComponent message = createDisplay();
				JOptionPane.showMessageDialog(producer.getGraphViewComponent(),
						message, "Polymer Notation",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		producer.getGraphViewComponent().setCursor(
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	private JComponent createDisplay() {

		String notation = producer.getNotation();
		if (null == notation || notation.trim().length() == 0) {
			return new JLabel("There is no structure to show notation");
		}

		JScrollPane jScrollPane = new JScrollPane();
		JTextArea notationTextArea = new JTextArea();
		notationTextArea.setColumns(60);
		notationTextArea.setEditable(false);
		notationTextArea.setLineWrap(true);
		notationTextArea.setRows(8);
		jScrollPane.setViewportView(notationTextArea);
		notationTextArea.setText(notation);

		return jScrollPane;
	}
}
