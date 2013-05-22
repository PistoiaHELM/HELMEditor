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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;

import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.MonomerInfoUtils;

public class SequenceViewIterator extends SenseIterator {
	private Node queuedP = null;
	Set<Node> visitedP = new HashSet<Node>();
	
	public SequenceViewIterator(Graph graph, Node startNode) {
		super(graph, startNode);
	}

	@Override
	protected boolean isOk(Node node) {
		if (MonomerInfoUtils.isChemicalModifierPolymer(node)) 
			 return false;
		//in case of cyclic structures, prevent 2 linkers being queued 
		//simultaneosly at the beginning 
		if (MonomerInfoUtils.isPMonomer(node)) {
			if (queuedP == null) {
				if (visitedP.contains(node)) {
					return false;
				}
				queuedP = node;
				return true;
			}
			// we already have P node queued
			return false;
		}
		return true;
	}
	
	@Override
	public void preProcess(Node node) {
		if (queuedP == node) {
			queuedP = null;
		}
		visitedP.add(node);
	}
	
	@Override
	protected Set<Node> getAdjacentNodes(Node node) {
		Set<Node> neighbours = GraphUtils.getSuccessors(node);
		
		EdgeCursor edges = node.outEdges();
		for (; edges.ok(); edges.next()) {
			Edge e = edges.edge();
			Node n = e.opposite(node);
			if (MonomerInfoUtils.isPair(e)) {
				neighbours.remove(n);
			}	
		}
		
		Node[] sorted = new Node[neighbours.size()];
		
		Arrays.sort(neighbours.toArray(sorted),
    		new Comparator<Node>() {
				public int compare(Node o1, Node o2) {
					if (MonomerInfoUtils.isChemicalModifierPolymer(o2)) {
						return -1;
					}
					if (MonomerInfoUtils.isChemicalModifierPolymer(o1)) {
						return 1;
					}
					if (MonomerInfoUtils.isPMonomer(o2)) {
						return -1;
					}
					if (MonomerInfoUtils.isPMonomer(o1)) {
						return 1;
					}
					if (MonomerInfoUtils.isBranchMonomer(o2)) {
						return -1;
					}
					if (MonomerInfoUtils.isBranchMonomer(o1)) {
						return 1;
					}
					return 0;
				}
        });
		
        
		LinkedHashSet<Node> result = new LinkedHashSet<Node>();
        Collections.addAll(result, sorted);
        return result;
	}

}
