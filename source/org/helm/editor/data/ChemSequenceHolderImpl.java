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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import y.base.Graph;
import y.base.Node;
import y.layout.LayoutGraph;

import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.MonomerInfoUtils;

public class ChemSequenceHolderImpl implements ChemSequenceHolder {
	private Map<Node, List<NodeSequence>> dockNode2ChemSequence;
	private Set<NodeSequence> allSequences;
	private Set<NodeSequence> dockedSequences = new LinkedHashSet<NodeSequence>();
	private List<Node> pendantChemNodes = new ArrayList<Node>();
	private LayoutGraph graph;

	// private Set<Node> visited = new HashSet<Node>();

	public ChemSequenceHolderImpl(LayoutGraph graph) {
		this.dockNode2ChemSequence = new HashMap<Node, List<NodeSequence>>();
		this.graph = graph;
		this.allSequences = new LinkedHashSet<NodeSequence>();
	}

	public void pushNode(Node node) {
		if (!MonomerInfoUtils.isChemicalModifierPolymer(node)) {// ||
																// visited.contains(node))
																// {
			return;
		}
		if (GraphUtils.getChemicalNeighborsCount(node) > 1) {
			return;
		}

		if (node.neighbors().size() <= 1) {
			pendantChemNodes.add(node);
		}

		NodeSequence sequence = new MonomerNodeSequenceImpl(graph, node) {
			@Override
			protected boolean isNodeRightMonomerType(Graph graph, Node node) {
				return MonomerInfoUtils.isChemicalModifierPolymer(node);
			}

		};

		allSequences.add(sequence);
		boolean added = false;
		for (Node neighbour : GraphUtils.getNeighbours(node)) {
			if (!MonomerInfoUtils.isChemicalModifierPolymer(neighbour)) {
				addSequenceToDock(sequence, neighbour);
				if (!added) {
					dockedSequences.add(sequence);
					added = true;
				}
			}
		}

		// visited.addAll(sequence.getNodes());
	}

	public List<Node> getPendantNodes() {
		return Collections.unmodifiableList(pendantChemNodes);
	}

	public Set<NodeSequence> getSequences() {
		Set<NodeSequence> result = new LinkedHashSet<NodeSequence>(
				allSequences.size());
		result.addAll(allSequences);
		return result;
	}

	public List<NodeSequence> getConnectedSequences(Node dockNode) {
		List<NodeSequence> result = dockNode2ChemSequence.get(dockNode);
		return result == null ? new ArrayList<NodeSequence>() : result;
	}

	public List<Node> getDockedNodes() {
		Set<Node> docks = dockNode2ChemSequence.keySet();
		List<Node> result = new ArrayList<Node>(docks.size());
		result.addAll(docks);
		return result;
	}

	public Set<NodeSequence> getDockedSequences() {
		return dockedSequences;
	}

	private void addSequenceToDock(NodeSequence sequence, Node dockNode) {
		List<NodeSequence> sequences;
		if (!dockNode2ChemSequence.containsKey(dockNode)) {
			sequences = new LinkedList<NodeSequence>();
		} else {
			sequences = dockNode2ChemSequence.get(dockNode);
		}
		sequences.add(sequence);
		dockNode2ChemSequence.put(dockNode, sequences);
	}

}
