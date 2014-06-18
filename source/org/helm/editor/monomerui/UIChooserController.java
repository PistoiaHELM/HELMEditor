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
package org.helm.editor.monomerui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFrame;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.services.PreferensesService;
import org.helm.editor.utility.xmlparser.data.Template.UIType;
import org.helm.editor.utility.xmlparser.parser.TemplateParsingException;
import org.helm.editor.utility.xmlparser.validator.ValidationTemplateExcaption;

/**
 * @author Alexander Makarov
 * 
 */
public class UIChooserController {

	private UIChooser chooserFrame;
	private PreferensesService userProperty;

	private MacromoleculeEditor editor;

	public UIChooserController(MacromoleculeEditor editor) throws IOException {
		this.editor = editor;

		chooserFrame = new UIChooser(editor);
		userProperty = PropertyManager.getInstance();

		configDialog();
	}

	public JFrame getFrame() {
		return chooserFrame;
	}

	private void configDialog() {

		String[] uiXmlFiles = getUIXmls();
		chooserFrame.setFileList(uiXmlFiles);
		String uiXML = userProperty.loadUserPreference(PropertyManager.UI_XML);
		chooserFrame.setDefaultForUIXml(uiXML);

		String uiType = userProperty
				.loadUserPreference(PropertyManager.UI_PROPERTY);
		chooserFrame.setDefaultForUIType(uiType);

		chooserFrame.getSetupButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedUIType = chooserFrame.getSelectedUIType();
				String selectedUIXml = chooserFrame.getSelectedUIXml();

				try {
					userProperty.saveUserPreference(
							PropertyManager.UI_PROPERTY, selectedUIType);

					if (selectedUIXml != null) {
						editor.setupUIType(UIType.stringValue(selectedUIType),
								selectedUIXml);
						userProperty.saveUserPreference(PropertyManager.UI_XML,
								selectedUIXml);
					} else {
						editor.setupUIType(UIType.stringValue(selectedUIType));
					}

				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}

				chooserFrame.dispose();
			}
		});
	}

	// TODO now it for test
	private String[] getUIXmls() {
		File propertyDirectory = new File(PropertyManager.PROPERTY_FOLDER);

		return propertyDirectory.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.contains(".xsd") && !name.contains(".property");
			}

		});
	}

}
