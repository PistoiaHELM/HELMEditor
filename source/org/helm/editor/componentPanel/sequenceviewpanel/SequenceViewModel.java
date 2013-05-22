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

import java.util.List;
import java.util.Map;

import y.base.Node;
import y.view.Graph2D;

import org.helm.editor.mapping.GraphMapper;

public interface SequenceViewModel extends GraphMapper {

	//public static final String EDGETYPE_REGULAR = "Regular";
	//public static final String EDGETYPE_PNODE = "modified phosphate";
	//public static final String EDGETYPE_PAIR = "Pair";

	public static final Object MODIFICATION_COUNT = "ModificationCount";

	public static final Object COMPLEMENTARY_VIEW_NODE = "ComplementaryViewNode";
	public static final Object IS_FLIPPED = "IsFlipped";
	
	public static final String LABELS_MAP = "edeg-label map";
	
	public List<Node> getStartingViewNodeList();

	public void addStrartingViewNode(Node viewNode);

	public Node getViewNode(Node src);

	public void putViewNode(Node srcNode, Node viewNode);

	public Map<Node, Node> getEditorViewMap();

	public Node getComplementaryViewNode(Node viewNode);

	public void addComplentaryViewNodes(Node srcViewNode, Node tgtViewNode);

	public Graph2D getGraph();

	public List<Node> transform(Node srcNode);
}
