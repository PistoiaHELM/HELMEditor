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
package org.helm.editor;

import org.helm.notation.MonomerFactory;
import org.helm.notation.NucleotideFactory;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.monomerui.PropertyManager;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 
 * @author zhangtianhong
 */
public class HELMApplet extends JApplet {

	private static final long serialVersionUID = 1L;

	MacromoleculeEditor editor;

	public void init() {
		initializeDataFactories();
	}

	public void start() {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					editor = new MacromoleculeEditor();
					initLnF();
					initGUI();
				} catch (Exception ex) {
					Logger.getLogger(MacromoleculeEditor.class.getName()).log(
							Level.SEVERE, "Unable to launch application GUI",
							ex);
				}
			}
		});

	}

	public void initGUI() {
		editor.addContentTo(this.getRootPane());
	}

	public void stop() {

	}

	public void destroy() {

	}

	/**
	 * initialize monomer DB and nucleteotide templates
	 */
	private static void initializeDataFactories() {
		try {
			MonomerFactory.getInstance();
			Logger.getLogger(MacromoleculeEditor.class.getName()).log(
					Level.INFO, "Initializing monomer DB...Done");

			NucleotideFactory.getInstance();
			Logger.getLogger(MacromoleculeEditor.class.getName()).log(
					Level.INFO, "Initializing nuleotide templates...Done");

			PropertyManager.getInstance();
			Logger.getLogger(MacromoleculeEditor.class.getName()).log(
					Level.INFO, "Initializing monomer UI templates...Done");

		} catch (Exception ex) {
			Logger.getLogger(MacromoleculeEditor.class.getName()).log(
					Level.WARNING, "Error initializing data factories", ex);
		}
	}

	/**
	 * Initializes to a "nice" look and feel.
	 */
	public static void initLnF() {
		ResourceBundle resourceBundle = ResourceBundle
				.getBundle("org.helm.editor.editor.resource.GUIBase");
		String lnf = resourceBundle.getString("Application.lookAndFeel");
		if (lnf != null && lnf.equalsIgnoreCase("System")) {
			try {
				if (!UIManager.getSystemLookAndFeelClassName().equals(
						"com.sun.java.swing.plaf.motif.MotifLookAndFeel")
						&& !UIManager.getSystemLookAndFeelClassName().equals(
								"com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
						&& !UIManager.getSystemLookAndFeelClassName()
								.equals(UIManager.getLookAndFeel().getClass()
										.getName())) {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getNotation() {
		return editor.getNotation();
	}

	public void setNotation(String notation) {
		editor.setNotation(notation);
	}
}
