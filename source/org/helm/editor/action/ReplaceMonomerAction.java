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

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.manager.MonomerReplacementManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * 
 * @author zhangtianhong
 */
public class ReplaceMonomerAction extends AbstractAction {

	private MacromoleculeEditor editor;

	private String _ownerCode;

	public ReplaceMonomerAction(MacromoleculeEditor editor, String ownerCode) {
		super("Replace Monomer");
		this.editor = editor;
		_ownerCode = ownerCode;
	}

	public void actionPerformed(ActionEvent e) {
		String notation = editor.getNotation();
		if (null == notation || notation.length() == 0) {
			JOptionPane.showMessageDialog(editor.getFrame(),
					"There is no structure for monomer replacement",
					"Replace Monomer", JOptionPane.WARNING_MESSAGE);
		} else {
			MonomerReplacementManager manager = new MonomerReplacementManager(
					editor, false, _ownerCode);
			manager.setVisible(true);
		}
	}
}
