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
package org.helm.editor.worker;

import org.helm.editor.manager.NucleotideManager;
import java.awt.Cursor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.SwingWorker;

/**
 * 
 * @author zhangtianhong
 */
public class NucleotideManagerLauncher extends SwingWorker<Void, Void> {

	private NucleotideManager manager;

	public NucleotideManagerLauncher(NucleotideManager manager) {
		this.manager = manager;
	}

	@Override
	protected Void doInBackground() throws Exception {
		return null;
	}

	@Override
	protected void done() {
		manager.getEditor().getFrame()
				.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		try {
			get();
			manager.customInit();
			manager.getEditor().updatePolymerPanels();
			manager.setVisible(true);
		} catch (Exception ex) {
			Logger.getLogger(NucleotideManagerLauncher.class.getName()).log(
					Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(
					manager.getEditor().getFrame(),
					"Unable to authenticate your NT credentials\n"
							+ ex.getMessage(), "Authentication Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
