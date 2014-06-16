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
package org.helm.editor.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import y.base.Graph;
import y.base.Node;

import org.helm.editor.utility.GraphUtils;

/**
 * Iterates over maximal connected component of nodes that satisfy
 * <code>isOk(Node node)</code> condition. Also there is extension point for
 * processing terminal nodes (non-ok nodes adjacent to this component) and
 * boundary node (ok nodes in this component with non-ok neighbours)
 * 
 * @author Dmitry Zhelezov
 * 
 */
public abstract class AbstractConnectedComponentIterator implements
		Iterator<Node> {

	private Queue<Node> nodesToProcess = new LinkedList<Node>();
	private Set<Node> visited = new HashSet<Node>();
	protected Graph graph;

	public AbstractConnectedComponentIterator(Graph graph, Node startNode) {
		visited.add(startNode);
		nodesToProcess.add(startNode);
		this.graph = graph;
	}

	public boolean hasNext() {
		return !nodesToProcess.isEmpty();
	}

	public Node next() {
		Node next = nodesToProcess.poll();
		visited.add(next);
		processNode(next);
		return next;
	}

	private void processNode(Node node) {
		Set<Node> neigbours = GraphUtils.getNeighbours(node);
		for (Node neighbour : neigbours) {
			if (isOk(neighbour)) {
				if (!visited.contains(neighbour)) {
					nodesToProcess.add(neighbour);
					processOkNode(neighbour);
				}
			} else {
				processDockNode(neighbour);
			}
		}
	}

	/**
	 * Additional processing of the ok node in this component
	 * 
	 * @param node
	 *            Node to process
	 */
	protected void processOkNode(Node node) {

	}

	/**
	 * Process non-ok node adjacent to ok node
	 * 
	 * @param node
	 *            Non-ok node to process
	 */
	protected void processDockNode(Node node) {

	}

	/**
	 * Determines whether this node is ok, that is belongs to the specific class
	 * of nodes. For example if this method always returns true, this iterator
	 * will iterate over all maximal connected components of the graph.
	 * 
	 * @param node
	 *            node to check
	 * @return true if this node is ok;
	 */
	protected abstract boolean isOk(Node node);

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
