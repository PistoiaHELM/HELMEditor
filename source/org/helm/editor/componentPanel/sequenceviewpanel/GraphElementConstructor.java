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
package org.helm.editor.componentPanel.sequenceviewpanel;

import org.helm.notation.model.Attachment;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

public interface GraphElementConstructor {

	// nodes that factory can construct
	public static final int SIMPLE_NODE = 0;
	public static final int CHEM_NODE = 1;
	public static final int ERROR_NODE = 2;

	// edges that factory can construct
	public static final int CHEM_EDGE = 0;
	public static final int PAIR_EDGE = 1;
	public static final int BACKBONE_BRANCH_EDGE = 2;
	public static final int BRANCH_BRANCH_EDGE = 3;
	public static final int SIMPLE_EDGE = 4;

	public static final int REALIZE_CHEM_EDGE = 0;
	public static final int REALIZE_NEIBOURS = 1;

	// public static final String EDGETYPE_CHEM = "Chem";

	Node createNode(String label, int nodeType);

	void createEdge(Edge currentEdge, Attachment sourceAttachment,
			Attachment targetAttachment, int edgeType);

	Object realize(Graph2D graph, Object realizedObject, int realizationType);

	void setUpViewModel(SequenceViewModel viewModel);

	void setMetrics(ViewMetrics layoutMetrics);

}
