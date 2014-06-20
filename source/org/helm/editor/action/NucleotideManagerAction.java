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
import org.helm.editor.manager.NucleotideManager;
import org.helm.editor.worker.NucleotideManagerLauncher;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * 
 * @author zhangtianhong
 */
public class NucleotideManagerAction extends AbstractAction {

	private MacromoleculeEditor editor;

	public NucleotideManagerAction(MacromoleculeEditor editor) {
		super("Launch Nucleotide Manager ...");
		this.editor = editor;
	}

	public void actionPerformed(ActionEvent e) {
		editor.getFrame().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		NucleotideManager nm = new NucleotideManager(editor, false);
		nm.setLocationRelativeTo(editor.getFrame());
		NucleotideManagerLauncher launcher = new NucleotideManagerLauncher(nm);
		launcher.execute();
	}
}
