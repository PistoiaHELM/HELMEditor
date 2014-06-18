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
package org.helm.editor.layout.primitives;

import java.awt.Point;
import java.util.Set;

import y.base.Node;
import y.layout.LayoutGraph;

import org.helm.editor.componentPanel.sequenceviewpanel.LabelConstructor;
import org.helm.editor.componentPanel.sequenceviewpanel.ViewMetrics;
import org.helm.editor.layout.metrics.ComponentViewViewMetrics;

/**
 * @author Nikita Karuze
 */
public class ComponentViewLayoutPrimitives extends SequenceViewLayoutPrimitives {
	private ViewMetrics baseViewMetrics;

	public ComponentViewLayoutPrimitives(LabelConstructor labelConstructor,
			ViewMetrics baseViewMetrics) {

		super(labelConstructor, baseViewMetrics);
		this.baseViewMetrics = baseViewMetrics;
		this.layoutMetrics = new ComponentViewViewMetrics(baseViewMetrics);
	}

	// ////////////////////////////////
	// chem modifiers layout metrics//
	// ////////////////////////////////

	public Point getChemNodesFloatongSequenceLayoutMetrics() {
		return new Point(baseViewMetrics.getHDistanceInt()
				+ baseViewMetrics.getChemNodeSize(),
				baseViewMetrics.getVDsitanceExt());
	}

	public Point getChemNodesDockedSequenceLayoutMetrics() {
		return new Point(baseViewMetrics.getChemNodeSize()
				+ baseViewMetrics.getHDistanceInt(), 0);
	}

	public int getChemNodesYLayoutStart(LayoutGraph graph,
			Set<Node> layoutedNodes) {
		return baseViewMetrics.getYBound();
	}
}
