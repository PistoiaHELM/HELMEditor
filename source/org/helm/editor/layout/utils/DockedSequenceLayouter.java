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

public class DockedSequenceLayouter extends AbstractSequenceLayouter {
	private boolean left2right;
	private int leftvoffset = 0;
	private int rightvoffset = 0;
	private Point dock;
	
	public DockedSequenceLayouter(Point dockPoint, boolean direction) {
		this.left2right = direction;
		this.dock = dockPoint;
	}
	
	@Override
	protected Point getNextPoint(Point point) {
		Point result = new Point(point);
		if (left2right) {
			result.x += metrics.distanceH; 
		} else {
			result.x -= metrics.distanceH;
		}
		return result;
	}

	@Override
	protected Point getStartingPoint() {
		int x, y;
		if (left2right && (rightvoffset > 0)) {
			left2right = !left2right;
		}
		if (left2right) {
			x = dock.x + metrics.distanceH;
			y = dock.y + rightvoffset * metrics.distanceV;
			rightvoffset ++; 
		} else {
			x = dock.x - metrics.distanceH;
			y = dock.y + leftvoffset * metrics.distanceV;
			leftvoffset ++;
		}
		return new Point(x, y);
	}

}
