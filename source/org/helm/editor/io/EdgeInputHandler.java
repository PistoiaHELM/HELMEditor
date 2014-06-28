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
package org.helm.editor.io;

import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.notation.model.Attachment;
import org.graphdrawing.graphml.GraphMLConstants;
import org.graphdrawing.graphml.reader.dom.DOMGraphMLParseContext;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import y.base.EdgeMap;
import y.base.Graph;
import yext.graphml.reader.AbstractDOMInputHandler;

/**
 * 
 * @author lih25
 */
public class EdgeInputHandler extends AbstractDOMInputHandler {

	// Assume (parsing) responsibility only for those <data> elements that refer
	// to GraphML attribute declarations with specific additional XML attribute.
	public boolean acceptKey(NamedNodeMap map, int scopeType) {
		if (scopeType != GraphMLConstants.SCOPE_EDGE) {
			return false;
		}
		// 'map' holds the XML attributes of a <key> element.
		Node sourceAttNode = map.getNamedItem(IOConstants.SOURCE_ATTACHMENT);
		Node targetAttNode = map.getNamedItem(IOConstants.TARGET_ATTACHMENT);

		if (sourceAttNode != null) {
			return "true".equals(sourceAttNode.getNodeValue());
		} else if (targetAttNode != null) {
			return "true".equals(targetAttNode.getNodeValue());
		} else {
			return false;
		}

		// return ((node == null) ? false : "true".equals(node.getNodeValue()));
	}

	// Parse the <data> element.
	protected void parseData(DOMGraphMLParseContext context, Graph graph,
			Object edge, boolean defaultMode, org.w3c.dom.Node domNode) {
		// Default mode is not supported.
		if (defaultMode) {
			return;
		}

		String source = "";
		String target = "";
		int index = -1;
		Attachment sourceAttachment = null;
		Attachment targetAttachment = null;
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);

		// 'domNode' holds the <data> element, its XML attributes, and all XML
		// elements nested within.
		org.w3c.dom.NodeList children = domNode.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node n = children.item(i);
				if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {

					if (IOConstants.SOURCE_ATTACHMENT.equals(n.getLocalName())) {
						source = parseMyDataElement(n);
					}

					if (IOConstants.TARGET_ATTACHMENT.equals(n.getLocalName())) {
						target = parseMyDataElement(n);
					}
				}

			}
			index = source.indexOf(":");
			sourceAttachment = new Attachment(source.substring(0, index),
					source.substring(index + 1));
			index = target.indexOf(":");
			targetAttachment = new Attachment(target.substring(0, index),
					source.substring(index + 1));
			edgeMap.set(edge, new EditorEdgeInfoData(sourceAttachment,
					targetAttachment));
		}
	}

	// ((Graph2D) graph).getRealizer((y.base.Node)
	// nodeedge).setLabelText(label);
	// Parse the attribute of <myData> element.
	String parseMyDataElement(org.w3c.dom.Node domNode) {
		NamedNodeMap nm = domNode.getAttributes();
		org.w3c.dom.Node a = nm.getNamedItem("value");
		// String txt = "Node's area is ";
		// txt += ((a == null) ? "n/a." : "" + a.getNodeValue() +
		// " square pixels.");
		return a.getNodeValue();
	}

	// Not supported yet.
	protected void applyDefault(DOMGraphMLParseContext c, Graph g, Object edge) {
	}
}
