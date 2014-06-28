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
package org.helm.editor.controller;

import java.util.List;
import y.base.Node;

import org.helm.editor.data.DataException;
import org.helm.editor.data.DataRegistry;
import org.helm.editor.utility.TemplatesModel;

public class TemplateLocator {
	private static TemplateLocator instance = new TemplateLocator();
	private DataRegistry registry = DataRegistry.getInstance();

	private TemplateLocator() {
	}

	public static TemplateLocator getInstance() {
		return instance;
	}

	public Node getTemplate(String polymerType, String monomerId) {
		List<TemplatesModel> models = registry.getTemplateModels(polymerType);
		Node result = null;
		for (TemplatesModel model : models) {
			if (model == null) {
				continue;
			}
			result = model.getNodeByMonomerId(monomerId);
			if (result != null) {
				return result;
			}
		}

		throw new DataException("No template for monomer " + monomerId
				+ " of type " + polymerType);
	}

}
