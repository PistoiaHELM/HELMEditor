/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.roche.antibody.ui.components.toolsmenu;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.helm.editor.editor.MacromoleculeEditor;
import org.roche.antibody.model.antibody.AntibodyContainer;
import org.roche.antibody.services.UIService;
import org.roche.antibody.services.xml.XmlAntibodyService;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.roche.antibody.ui.filechooser.XmlFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code AntibodyLoadXmlAction}
 * 
 * Action for loading an XML Antibody-File from the Mainmenu
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: AntibodyLoadXmlAction.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class AntibodyLoadXmlAction extends AbstractAction {

	/** */
	private static final long serialVersionUID = 8270310476746408309L;

	/** The Logger for this class */
	private static final Logger LOG = LoggerFactory
			.getLogger(AntibodyLoadXmlAction.class);

	private MacromoleculeEditor editor;

	private XmlAntibodyService xmlService = XmlAntibodyService.getInstance();

	public AntibodyLoadXmlAction(MacromoleculeEditor editor) {
		super("Load Antibody from XML ...");
		this.editor = editor;
	}

	public void actionPerformed(ActionEvent e) {

		XmlFileChooser dialog = new XmlFileChooser();
		if (dialog.showOpenDialog(editor.getFrame()) == JFileChooser.APPROVE_OPTION) {
			File abFile = dialog.getSelectedFile();

			AntibodyContainer newAbContainer = null;
			try {
				newAbContainer = xmlService.unmarshal(abFile);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(editor.getFrame(),
						"Could not create antibody from given file!", "Error",
						JOptionPane.ERROR_MESSAGE);
				LOG.error("Could not load Antibody-XML from disk! {}", e1);
				return;
			}
			AntibodyEditorPane pane = UIService.getInstance()
					.addAntibodyViewEditor(editor, null);
			pane.setModel(newAbContainer.getAntibody());

		}
	}

}