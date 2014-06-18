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
package org.helm.editor.monomerui.treeui;

import javax.swing.tree.DefaultMutableTreeNode;

import org.helm.editor.utility.xmlparser.data.XmlElement;

import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;

/**
 * @author Alexander Makarov
 * 
 */
public class XmlLeafNode extends DefaultMutableTreeNode {

	private XmlElement dataElement;

	public XmlLeafNode(Object element, XmlElement dataElement) {
		super(element);

		this.dataElement = dataElement;
	}

	public XmlElement getDataElement() {
		return dataElement;
	}

	@Override
	public String toString() {
		if (userObject instanceof String) {
			return userObject.toString();
		}

		if (userObject instanceof Graph2D) {
			Graph2D graphNode = (Graph2D) userObject;

			StringBuffer text = new StringBuffer();
			NodeCursor cursor = graphNode.nodes();
			while (cursor.ok()) {
				Node currNode = (Node) cursor.current();

				text.append(currNode.toString());

				cursor.next();
			}

			return text.toString();
		}

		return null;
	}

}
