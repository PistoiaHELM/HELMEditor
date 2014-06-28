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
package org.helm.editor.layout.procedures;

import y.base.Node;
import y.layout.LayoutGraph;

public class HairPinStructuresLayout extends AbstratStructureLayout {

	@Override
	protected boolean canLayoutCore(LayoutGraph arg0) {
		return false;
	}

	@Override
	protected void doLayoutCore(LayoutGraph graph) {
		Node startingNode = layoutPrimitives.getStartingNode(graph.firstNode());

		Node[] loopStartAndLoopEnd = layoutPrimitives.getLoopBounds(
				startingNode, graph);

		// get the R startingNode that pBaseNode is connecting to
		Node loopStart = layoutPrimitives.getRNode(loopStartAndLoopEnd[0]);
		Node loopEnd = layoutPrimitives.getRNode(loopStartAndLoopEnd[1]);

		layoutPrimitives.layoutSequence(graph, startingNode, loopStart, true);
		layoutPrimitives.setFlipState(
				layoutPrimitives.getSequenceNodes(startingNode, loopStart,
						graph, true).nodes(), false);

		layoutPrimitives.layoutSequence(graph, loopEnd, null, true);
		layoutPrimitives.rotate180(loopEnd, null, graph);
		layoutPrimitives.shiftSubgraph(graph, loopEnd, null, null, true);
		layoutPrimitives.setFlipState(
				layoutPrimitives.getSequenceNodes(loopEnd, null, graph, true)
						.nodes(), true);

		layoutPrimitives.layoutLoop(graph, loopStart, loopEnd, false);
	}

}
