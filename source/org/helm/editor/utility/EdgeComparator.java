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
package org.helm.editor.utility;

import org.helm.editor.data.EdgeMapKeys;
import java.util.Comparator;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;

/**
 * Compare two edges 1. compare source node 2. compare the target node 3.
 * compare description
 * 
 * @author LIH25
 */
public class EdgeComparator implements Comparator<Edge> {

	public int compare(Edge e1, Edge e2) {
		int order = 0;
		final Graph graph = e1.getGraph();
		final EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.DESCRIPTION);
		String s1 = (String) edgeMap.get(e1);
		String s2 = (String) edgeMap.get(e2);

		Node source1 = e1.source();
		Node source2 = e2.source();

		Node target1 = e1.target();
		Node target2 = e2.target();

		if (!source1.equals(source2)) {
			NodeComparator nodeComparator = new NodeComparator(null);
			order = nodeComparator.compare(source1, source2);
			if (order == 0) {
				// compare target
				order = nodeComparator.compare(target1, target2);

				// compare description
				if (order == 0) {
					order = s1.compareToIgnoreCase(s2);
				}
			}

		} else { // same source
			NodeComparator nodeComparator = new NodeComparator(e1.source());
			order = nodeComparator.compare(target1, target2);
			if (order == 0) {
				order = s1.compareToIgnoreCase(s2);
			}
		}

		return order;
	}
}
