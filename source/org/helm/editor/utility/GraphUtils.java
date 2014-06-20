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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.YCursor;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.model.Monomer;

public class GraphUtils {

	public static Set<Node> getNeighbours(Node node) {
		Set<Node> result = new HashSet<Node>();
		if (null != node) {
			NodeCursor neighbors = node.neighbors();
			if (null != neighbors) {
				for (; neighbors.ok(); neighbors.next()) {
					result.add(neighbors.node());
				}
			}
		}
		return result;
	}

	public static Set<Node> getSuccessors(Node node) {
		Set<Node> result = new HashSet<Node>();
		if (null != node) {
			NodeCursor successors = node.successors();
			if (null != successors) {
				for (; successors.ok(); successors.next()) {
					result.add(successors.node());
				}
			}
		}
		return result;
	}

	public static int getChemicalNeighborsCount(Node node) {
		int chemNeighbours = 0;
		NodeCursor neighbors = node.neighbors();
		if (neighbors == null) {
			return 0;
		}
		for (; neighbors.ok(); neighbors.next()) {
			if (MonomerInfoUtils.isChemicalModifierPolymer(neighbors.node())) {
				chemNeighbours++;
			}
		}
		return chemNeighbours;
	}

	public static Node getBranchNeighbor(Node node) {

		Set<Node> neighbors = getNeighbours(node);

		for (Node n : neighbors) {
			if (MonomerInfoUtils.isBranchMonomer(n)) {
				return n;
			}
		}
		return null;
	}

	public static int getNonChemicalNeiboursCount(Node node) {
		return node.neighbors().size() - getChemicalNeighborsCount(node);
	}

	public static boolean isChemEdge(Edge edge) {
		Node target = edge.target();
		Node source = edge.source();
		return MonomerInfoUtils.isChemicalModifierPolymer(target)
				|| MonomerInfoUtils.isChemicalModifierPolymer(source);
	}

	public static Map<String, Map<String, Monomer>> getMonomerDB()
			throws MonomerException, IOException, JDOMException {
		return MonomerStoreCache.getInstance().getCombinedMonomerStore()
				.getMonomerDB();
		// return MonomerFactory.getInstance().getMonomerDB();
	}

	public static NodeCursor oneTimeVisitNodeCursor(NodeCursor cursor) {
		return new OneTimeVisitNodeCursor(cursor);
	}

	public static EdgeCursor oneTimeVisitNodeCursor(EdgeCursor cursor) {
		return new OneTimeVisitEdgeCursor(cursor);
	}

	private static class OneTimeVisitCursor implements YCursor {
		protected Set<Object> visited = new HashSet<Object>();
		protected YCursor cursor;

		public OneTimeVisitCursor(YCursor cursor) {
			this.cursor = cursor;
		}

		public Object current() {
			return cursor.current();
		}

		public void next() {
			cursor.next();
			visited.add(cursor.current());
		}

		public boolean ok() {
			return cursor.ok() && visited.contains(cursor.current());
		}

		public void prev() {
			cursor.prev();
			visited.add(cursor.current());
		}

		public int size() {
			return cursor.size();
		}

		public void toFirst() {
			cursor.toFirst();
		}

		public void toLast() {
			cursor.toLast();
		}

	}

	private static class OneTimeVisitNodeCursor extends OneTimeVisitCursor
			implements NodeCursor {

		public OneTimeVisitNodeCursor(YCursor cursor) {
			super(cursor);
		}

		public void cyclicNext() {
			((NodeCursor) cursor).cyclicNext();
		}

		public void cyclicPrev() {
			((NodeCursor) cursor).cyclicPrev();
		}

		public Node node() {
			return ((NodeCursor) cursor).node();
		}

	}

	private static class OneTimeVisitEdgeCursor extends OneTimeVisitCursor
			implements EdgeCursor {

		public OneTimeVisitEdgeCursor(YCursor cursor) {
			super(cursor);
		}

		public void cyclicNext() {
			((EdgeCursor) cursor).cyclicNext();
			visited.add(cursor.current());
		}

		public void cyclicPrev() {
			((EdgeCursor) cursor).cyclicPrev();
			visited.add(cursor.current());

		}

		public Edge edge() {
			return ((EdgeCursor) cursor).edge();
		}

	}
}
