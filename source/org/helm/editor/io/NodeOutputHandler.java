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

import y.base.DataProvider;
import y.io.graphml.KeyScope;
import y.io.graphml.output.AbstractOutputHandler;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.GraphMLXmlAttribute;
import y.io.graphml.output.XmlWriter;

import java.util.Collection;

/**
 * 
 * @author lih25
 */
public class NodeOutputHandler extends AbstractOutputHandler {
	public NodeOutputHandler() {
		setScope(KeyScope.NODE);
		final Collection attrs = getKeyDefinitionAttributes();
		attrs.add(new GraphMLXmlAttribute(IOConstants.MONOMER_ID, null, "true"));
		attrs.add(new GraphMLXmlAttribute(IOConstants.POLYMER_TYPE, null, "true"));
	}

	@Override
	protected void writeValueCore(
					final GraphMLWriteContext context, final Object data
	) throws GraphMLWriteException {
		final XmlWriter writer = context.getWriter();

		MonomerInfo monomerInfo = (MonomerInfo) data;
		writer.writeStartElement(IOConstants.POLYMER_TYPE, null);
		writer.writeAttribute("value", monomerInfo.getPolymerType());
		writer.writeEndElement();

		writer.writeStartElement(IOConstants.MONOMER_ID, IOConstants.MONOMER_ID);
		writer.writeAttribute("value", monomerInfo.getMonomerID());
		writer.writeEndElement();
	}

	@Override
	protected Object getValue(
					final GraphMLWriteContext context, final Object node
	) throws GraphMLWriteException {
		final DataProvider dp = context.getGraph()
						.getDataProvider(NodeMapKeys.MONOMER_REF);
		return dp.get(node);
	}
}
