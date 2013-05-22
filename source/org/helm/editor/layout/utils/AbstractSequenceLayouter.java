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
package org.helm.editor.layout.utils;

import java.awt.Point;

import y.base.Node;
import y.layout.LayoutGraph;

import org.helm.editor.data.NodeSequence;

/**
 * Layouts linear sequence of node. Abstract method <code>getNextLocation(Point p)</code>
 * determines the place for the next node (assuming that current 
 * node is already placed to the node p) 
 * 
 * @author dzhelezov 
 *
 */
public abstract class AbstractSequenceLayouter {
	protected LayoutMetrics metrics;

	protected abstract Point getNextPoint(Point point);
	protected abstract Point getStartingPoint();
	
	public void layout(LayoutGraph graph, NodeSequence sequence) {
		Point currentPostion = getStartingPoint();
		for (Node node : sequence) {
			graph.setCenter(node, currentPostion.x, currentPostion.y);
			currentPostion = getNextPoint(currentPostion);
		}
	}
	
	public void setMetrics(LayoutMetrics metrics) {
		this.metrics = metrics;
	}
}
