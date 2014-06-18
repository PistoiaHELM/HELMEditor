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

import org.helm.notation.NotationConstant;
import org.helm.notation.NucleotideFactory;
import org.helm.editor.manager.NucleotideManager;
import org.helm.editor.utility.XMLConverter;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.tools.NucleotideSequenceParser;
import java.awt.Cursor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.SwingWorker;
import org.jdom.Element;

/**
 * 
 * @author zhangtianhong
 */
public class NucleotideRegistar extends SwingWorker<Void, Void> {

	private NucleotideManager manager;

	public NucleotideRegistar(NucleotideManager manager) {
		this.manager = manager;
	}

	@Override
	protected Void doInBackground() throws Exception {
		String xmlString = manager.getNewNucleotideXMLString();
		Element element = XMLConverter.getElementFromString(xmlString);
		Map<String, Map<String, String>> map = NucleotideFactory.getInstance()
				.getNucleotideTemplates();

		Nucleotide nucleotide = NucleotideSequenceParser.getNucleotide(element);
		map.get(NotationConstant.NOTATION_SOURCE).put(nucleotide.getSymbol(),
				nucleotide.getNotation());
		NucleotideFactory.getInstance().setNucleotideTemplates(map);

		// save nucleotide templates to local file after successful update
		try {
			NucleotideFactory.getInstance().saveNucleotideTemplates();
		} catch (Exception ignore) {
		}

		return null;
	}

	@Override
	protected void done() {
		try {
			manager.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			get();
			manager.refreshContent();
			manager.getEditor().updatePolymerPanels();
			JOptionPane.showMessageDialog(manager,
					"Successfully registered nucleotide into local file",
					"Register Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(
					manager,
					"Error registering nucleotide into local file\n"
							+ ex.getMessage(), "Register Failure",
					JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(NucleotideRegistar.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
}
