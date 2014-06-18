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
package org.helm.editor.utility.xmlparser.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class included all ui type in HELM Editor instance.
 * 
 * @author Alexander Makarov
 */
public class Template {

	/**
	 * ui tabs in application Example: Bases, Sugars
	 */
	private List<Polymer> polymers;

	/**
	 * tab caption Example: Peptide
	 */
	private String name;

	/**
	 * @see UIType
	 */
	private UIType uiType;

	/**
	 * Possible ui elements in HELM Editor application
	 * 
	 * @author Alexander Makarov
	 */
	public static enum UIType {
		TAB, TREE, SEARCH;

		private static final String TAB_STRING = "tab";
		private static final String TREE_STRING = "tree";
		private static final String SEARCH_STRING = "search";

		public static UIType stringValue(String value) {

			if (value.equalsIgnoreCase(TAB_STRING)) {
				return TAB;
			} else if (value.equalsIgnoreCase(TREE_STRING)) {
				return TREE;
			} else if (value.equalsIgnoreCase(SEARCH_STRING)) {
				return SEARCH;
			}

			return null;
		}
	}

	public Template() {
		polymers = new ArrayList<Polymer>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUiType(UIType uiType) {
		this.uiType = uiType;
	}

	public UIType getUiType() {
		return uiType;
	}

	public void addPolymer(Polymer polymer) {
		polymers.add(polymer);
	}

	public Iterator<Polymer> getPolymersInterator() {
		return polymers.iterator();
	}

}
