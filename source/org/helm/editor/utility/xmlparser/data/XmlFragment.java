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

import java.awt.Color;

/**
 * Representative part of DNA
 * 
 * @author Alexander Makarov
 * 
 */
public class XmlFragment implements XmlElement {

	/**
	 * Fragment name in HELM Editor Example: A, C nucliotides
	 */
	String name;

	/**
	 * Fragment helm notation Example: R(A)P, R(T)P
	 */
	private String notation;

	private Color fontColor;
	private Color backgroundColor;

	public XmlFragment(String name, String notation) {
		this.name = name;
		this.notation = notation;
	}

	public String getName() {
		return name;
	}

	public String getNotation() {
		return notation;
	}

	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}
}
