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
import org.graphdrawing.graphml.writer.GraphMLWriteContext;
import org.graphdrawing.graphml.writer.XmlWriter;
import y.base.EdgeMap;
import y.base.Graph;
import yext.graphml.writer.AbstractOutputHandler;

/**
 * 
 * @author lih25
 */
public class EdgeOutputHandler extends AbstractOutputHandler {

	@Override
	public void printDataOutput(GraphMLWriteContext arg0, Graph graph,
			Object edge, XmlWriter writer) {
		final EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);

		EditorEdgeInfoData edgeInfoData = (EditorEdgeInfoData) edgeMap
				.get(edge);
		writer.writeStartElement(IOConstants.SOURCE_ATTACHMENT, null);
		writer.writeAttribute("value", edgeInfoData.getSourceNodeAttachment()
				.toString());
		writer.writeEndElement();

		writer.writeStartElement(IOConstants.TARGET_ATTACHMENT, null);
		writer.writeAttribute("value", edgeInfoData.getTargetNodeAttachment()
				.toString());
		writer.writeEndElement();

	}

	public void printKeyAttributes(GraphMLWriteContext arg0, XmlWriter writer) {
		writer.writeAttribute(IOConstants.TARGET_ATTACHMENT, true);
		writer.writeAttribute(IOConstants.SOURCE_ATTACHMENT, true);
	}

	public void printKeyOutput(GraphMLWriteContext arg0, XmlWriter writer) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}
