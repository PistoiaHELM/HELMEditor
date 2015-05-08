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

import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import y.base.NodeMap;
import y.io.graphml.input.AbstractInputHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.QueryInputHandlersEvent;

/**
 * 
 * @author lih25
 */
public class NodeInputHandler extends AbstractInputHandler implements InputHandlerProvider {
	public void onQueryInputHandler(
					final QueryInputHandlersEvent event
	) throws GraphMLParseException {
		if (!event.isHandled() && acceptKey(event.getKeyDefinition())) {
			event.addInputHandler(this);
		}
	}

	// Assume (parsing) responsibility only for those <data> elements that refer
	// to GraphML attribute declarations with specific additional XML attribute.
	public boolean acceptKey(Element keyDefinition) {
		final NamedNodeMap map = keyDefinition.getAttributes();

		final Node scope = map.getNamedItem("for");
		if (scope == null || !"node".equals(scope.getNodeValue())) {
			return false;
		}

		// 'map' holds the XML attributes of a <key> element.
		Node monomerID = map.getNamedItem(IOConstants.MONOMER_ID);
		Node polymerType = map.getNamedItem(IOConstants.POLYMER_TYPE);

		if (monomerID != null) {
			return "true".equals(monomerID.getNodeValue());
		} else if (polymerType != null) {
			return "true".equals(polymerType.getNodeValue());
		} else {
			return false;
		}

		// return ((node == null) ? false : "true".equals(node.getNodeValue()));
	}

	// Parse the <data> element.
	@Override
	protected Object parseDataCore(
					final GraphMLParseContext context, final Node domNode
	) throws GraphMLParseException {
		String monomerID = "";
		String polymerType = "";

		// 'domNode' holds the <data> element, its XML attributes, and all XML
		// elements nested within.
		org.w3c.dom.NodeList children = domNode.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node n = children.item(i);
				if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {

					if (IOConstants.MONOMER_ID.equals(n.getLocalName())) {
						monomerID = parseMyDataElement(n);
					}

					if (IOConstants.POLYMER_TYPE.equals(n.getLocalName())) {
						polymerType = parseMyDataElement(n);
					}
				}
			}
		}
		return new MonomerInfo(polymerType, monomerID);
	}

	@Override
	protected void setValue(
					final GraphMLParseContext context, final Object node, final Object data
	) throws GraphMLParseException {
		NodeMap nodeMap = (NodeMap) context.getGraph()
						.getDataProvider(NodeMapKeys.MONOMER_REF);
		nodeMap.set(node, data);
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
}
