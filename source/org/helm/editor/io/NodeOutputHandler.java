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

import org.graphdrawing.graphml.writer.GraphMLWriteContext;
import org.graphdrawing.graphml.writer.XmlWriter;
import y.base.Graph;
import y.base.NodeMap;
import yext.graphml.writer.AbstractOutputHandler;

/**
 * 
 * @author lih25
 */
public class NodeOutputHandler extends AbstractOutputHandler {

	@Override
	public void printDataOutput(GraphMLWriteContext arg0, Graph graph,
			Object node, XmlWriter writer) {
		final NodeMap nodemap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);

		MonomerInfo monomerInfo = (MonomerInfo) nodemap.get(node);
		writer.writeStartElement(IOConstants.POLYMER_TYPE, null);
		writer.writeAttribute("value", monomerInfo.getPolymerType());
		writer.writeEndElement();

		writer.writeStartElement(IOConstants.MONOMER_ID, IOConstants.MONOMER_ID);
		writer.writeAttribute("value", monomerInfo.getMonomerID());
		writer.writeEndElement();

	}

	public void printKeyAttributes(GraphMLWriteContext arg0, XmlWriter writer) {
		writer.writeAttribute(IOConstants.POLYMER_TYPE, true);
		writer.writeAttribute(IOConstants.MONOMER_ID, true);
	}

	public void printKeyOutput(GraphMLWriteContext arg0, XmlWriter arg1) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}
