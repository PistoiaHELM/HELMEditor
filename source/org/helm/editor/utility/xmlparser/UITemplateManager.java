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
package org.helm.editor.utility.xmlparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.Template;
import org.helm.editor.utility.xmlparser.parser.DomParser;
import org.helm.editor.utility.xmlparser.parser.TemplateParser;
import org.helm.editor.utility.xmlparser.parser.TemplateParsingException;
import org.helm.editor.utility.xmlparser.validator.TemplateValidator;
import org.helm.editor.utility.xmlparser.validator.UITemplateValidator;
import org.helm.editor.utility.xmlparser.validator.ValidationTemplateExcaption;

/**
 * @author Alexander Makarov
 */
public class UITemplateManager {

	private String schemaPath;
	private String uiTempaltePath;

	private TemplateValidator validator;
	private TemplateParser parser;

	private Template uiTemplate;

	public UITemplateManager(String schemaPath, String uiTemplatePath) {
		validator = new UITemplateValidator();
		parser = new DomParser();

		this.schemaPath = schemaPath;
		this.uiTempaltePath = uiTemplatePath;
	}

	public void loadTemplates() throws ValidationTemplateExcaption,
			TemplateParsingException {
		validator.setSchema(schemaPath);

		validator.validate(uiTempaltePath);

		uiTemplate = parser.parse(uiTempaltePath);
	}

	public void setUITemplatePath(String templatePath)
			throws ValidationTemplateExcaption, TemplateParsingException {
		uiTempaltePath = templatePath;
		loadTemplates();
	}

	public Iterator<Polymer> getPolymerIntertor() {
		return uiTemplate.getPolymersInterator();
	}

	public Template.UIType getUIType() {
		return uiTemplate.getUiType();
	}

	public boolean isTree() {
		return uiTemplate.getUiType() == Template.UIType.TREE;
	}

	public boolean isTabbedPane() {
		return uiTemplate.getUiType() == Template.UIType.TAB;
	}

	public boolean isSearchPane() {
		return uiTemplate.getUiType() == Template.UIType.SEARCH;
	}

	public List<Polymer> getPolymerList() {
		List<Polymer> resultList = new ArrayList<Polymer>();
		Iterator<Polymer> iter = uiTemplate.getPolymersInterator();
		while (iter.hasNext()) {
			resultList.add(iter.next());
		}

		return resultList;
	}

	public Polymer getPolymerByName(String name) {
		Iterator<Polymer> iter = uiTemplate.getPolymersInterator();

		while (iter.hasNext()) {
			Polymer currPolymer = iter.next();

			if (currPolymer.getName().equalsIgnoreCase(name)) {
				return currPolymer;
			}
		}

		return null;
	}

}
