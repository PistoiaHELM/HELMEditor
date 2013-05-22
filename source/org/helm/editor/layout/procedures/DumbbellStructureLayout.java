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

public class DumbbellStructureLayout extends AbstratStructureLayout {

	@Override
	protected boolean canLayoutCore(LayoutGraph arg0) {
		return true;
	}

	@Override
	protected void doLayoutCore(LayoutGraph graph) {
		Node startingNode = layoutPrimitives.getStartingNode(graph.firstNode());
		
		Node[] loopStartAndLoopEnd = layoutPrimitives.getLoopBounds(startingNode, graph);
		Node loopStart1 = loopStartAndLoopEnd[0];
		Node loopEnd1 = loopStartAndLoopEnd[1];

		// second loop
		loopEnd1 = layoutPrimitives.getRNode(loopEnd1);
		loopStartAndLoopEnd = layoutPrimitives.getLoopBounds(loopEnd1, graph);
		Node loopStart2 = loopStartAndLoopEnd[0];
		Node loopEnd2 = loopStartAndLoopEnd[1];
		
		loopStart1 = layoutPrimitives.getRNode(loopStart1);
//		loopEnd1 = layoutPrimitives.getRNode(loopEnd1);
		loopStart2 = layoutPrimitives.getRNode(loopStart2);
		loopEnd2 = layoutPrimitives.getRNode(loopEnd2);
		
		layoutPrimitives.layoutSequence(graph, loopEnd2, loopStart1, true);
		layoutPrimitives.setFlipState(layoutPrimitives.getSequenceNodes(loopEnd2, loopStart1, graph, true).nodes(), false);
		
		layoutPrimitives.layoutSequence(graph, loopEnd1, loopStart2, true);
		layoutPrimitives.rotate180(loopEnd1, loopStart2, graph);
		layoutPrimitives.shiftSubgraph(graph, loopEnd1, loopStart2, null, true);
		layoutPrimitives.setFlipState(layoutPrimitives.getSequenceNodes(loopEnd1, loopStart2, graph, true).nodes(), true);
		
		layoutPrimitives.layoutLoop(graph, loopStart1, loopEnd1, false);
		layoutPrimitives.layoutLoop(graph, loopStart2, loopEnd2, false);
	}
	
}
