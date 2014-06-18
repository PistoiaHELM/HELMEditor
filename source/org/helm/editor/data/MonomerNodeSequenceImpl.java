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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import y.base.Graph;
import y.base.Node;
import org.helm.editor.utility.GraphUtils;

public abstract class MonomerNodeSequenceImpl implements NodeSequence {
	private Graph graph;
	private Node startNode;
	private List<Node> myNodes;
	private boolean initialized = false;
	private boolean isFloating = true;

	public MonomerNodeSequenceImpl(Graph graph, Node startNode) {
		this.graph = graph;
		this.startNode = startNode;
		this.myNodes = new LinkedList<Node>();

	}

	/**
	 * Lazy initialization is done for lazy walking as it is expensive operation
	 * 
	 */
	private void init() {
		myNodes.clear();
		// this will walk through sequence and populate all properties
		for (Node n : this) {
			myNodes.add(n);
			if (GraphUtils.getNonChemicalNeiboursCount(n) > 0) {
				isFloating = false;
			}
		}
		initialized = true;
	}

	public Node getStartNode() {
		return startNode;
	}

	public boolean isFloating() {
		if (!initialized) {
			init();
		}

		return isFloating;
	}

	public Iterator<Node> iterator() {
		return new MonomerTypeIterator(graph, startNode) {

			@Override
			protected boolean isOk(Node node) {
				return isNodeRightMonomerType(
						MonomerNodeSequenceImpl.this.graph, node);
			}

		};
	}

	public List<Node> getNodes() {
		if (!initialized) {
			init();
		}

		return myNodes;
	}

	protected abstract boolean isNodeRightMonomerType(Graph graph, Node node);

}
