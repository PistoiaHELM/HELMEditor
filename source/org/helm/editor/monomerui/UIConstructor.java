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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;

import org.jdom.JDOMException;

import y.view.Graph2DView;

import org.helm.notation.MonomerException;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.monomerui.tabui.PolymerUI;
import org.helm.editor.monomerui.treeui.XmlTree;
import org.helm.editor.utility.xmlparser.UITemplateManager;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.parser.TemplateParsingException;
import org.helm.editor.utility.xmlparser.validator.ValidationTemplateExcaption;

/**
 * Use this class for creation common xml ui
 * 
 * @author Alexander Makarov
 * 
 */
public class UIConstructor {

	private UITemplateManager templateManager;
	private static final Logger log = Logger.getLogger(UIConstructor.class
			.toString());
	private static UIConstructor instance;
	// ui elements
	private List<PolymerUI> uiPanels;
	private XmlTree tree;
	private Graph2DView view;
	private MacromoleculeEditor editor;
	private PropertyManager propertyManager;

	/**
	 * Using with default schema and xml ui
	 * 
	 * @throws IOException
	 */
	public static UIConstructor getInstance() throws IOException,
			ValidationTemplateExcaption, TemplateParsingException {
		if (null == instance) {
			instance = new UIConstructor();
		}
		return instance;
	}

	private UIConstructor() throws IOException, ValidationTemplateExcaption,
			TemplateParsingException {
		propertyManager = PropertyManager.getInstance();
		templateManager = new UITemplateManager(PropertyManager.SCHEMA_FILE,
				propertyManager.getUIFilePath());
		templateManager.loadTemplates();
	}

	/**
	 * @return boolean
	 */
	public boolean isTreeUI() {
		return templateManager.isTree();
	}

	/**
	 * @return boolean
	 */
	public boolean isTabbedPane() {
		return templateManager.isTabbedPane();
	}

	public void bind(Graph2DView view, MacromoleculeEditor editor) {
		this.view = view;
		this.editor = editor;
	}

	public void setupUIXml(String path) throws ValidationTemplateExcaption,
			TemplateParsingException {
		String realPath = PropertyManager.PROPERTY_FOLDER + path;
		templateManager.setUITemplatePath(realPath);
	}

	/**
	 * Initializations all ui components. Need for forced initialization.
	 * 
	 * @throws JDOMException
	 * @throws IOException
	 * @throws MonomerException
	 * 
	 */
	public void initUIInstances() throws MonomerException, IOException,
			JDOMException {
		getPanels();
		getTree();
	}

	/**
	 * @return getting all constructed panels
	 * @throws JDOMException
	 * @throws IOException
	 * @throws MonomerException
	 */
	public List<PolymerUI> getPanels() throws MonomerException, IOException,
			JDOMException {
		if (uiPanels == null) {
			uiPanels = new ArrayList<PolymerUI>();
			constructPanels();
		}

		return uiPanels;
	}

	public JTree getTree() throws MonomerException, IOException, JDOMException {
		if (tree == null) {
			tree = new XmlTree(templateManager.getPolymerList(), editor, view);
			tree.constructTree();
		}

		return tree;
	}

	/**
	 * Update all panels in current HELM Editor instance
	 */
	public void updatePanels() {
		// Iterator<PolymerUI> uiIter = uiPanels.iterator();
		// while(uiIter.hasNext()){
		// //uiIter.next().updatePanel();
		//
		// remove(uiIter.next());//.updatePanel();
		//
		// }
		uiPanels = null;
		// .clear();

		try {
			templateManager.loadTemplates();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can't load UI Monomer templates", e);
		}
		try {
			tree = new XmlTree(templateManager.getPolymerList(), editor, view);
			tree.constructTree();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can't create UI Monomer tree", e);
		}
	}

	private void constructPanels() throws MonomerException, IOException,
			JDOMException {

		Iterator<Polymer> polymersIter = templateManager.getPolymerIntertor();
		while (polymersIter.hasNext()) {
			Polymer currPolymer = polymersIter.next();
			PolymerUI currUI = new PolymerUI(currPolymer, view, editor);
			uiPanels.add(currUI);
		}

	}

	public UITemplateManager getUITemplateManager() {
		return templateManager;
	}
}
