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
package org.helm.editor.componentPanel.sequenceviewpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import y.base.Node;
import y.view.Graph2D;

import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.notation.model.Monomer;

@SuppressWarnings("serial")
public class SequenceViewModelImpl implements SequenceViewModel {

	private List<Node> viewStartingNodes = new ArrayList<Node>();
	private Map<Node, Node> editor2view = new HashMap<Node, Node>();
	private Map<Node, Set<Node>> view2editor = new HashMap<Node, Set<Node>>();
	private Map<Node, Node> viewStarting2paired = new HashMap<Node, Node>();
	private Graph2D viewGraph;

	// TODO : put transformers for all polymer types
	private static Map<String, Class<? extends SequenceTransformer>> TRANSFORMERS = new HashMap<String, Class<? extends SequenceTransformer>>() {
		{
			put(Monomer.NUCLIEC_ACID_POLYMER_TYPE,
					NucleotideSequenceTransformer.class);
			put(Monomer.PEPTIDE_POLYMER_TYPE, PeptideSequenceTransformer.class);
			put(Monomer.CHEMICAL_POLYMER_TYPE, ChemSequenceTransformer.class);

		}
	};

	public SequenceViewModelImpl(Graph2D viewGraph) {
		this.viewGraph = viewGraph;
	}

	public List<Node> getStartingViewNodeList() {
		return Collections.unmodifiableList(viewStartingNodes);
	}

	public void addStrartingViewNode(Node viewNode) {
		viewStartingNodes.add(viewNode);
	}

	public Node getViewNode(Node src) {
		return editor2view.get(src);
	}

	public void putViewNode(Node srcNode, Node viewNode) {
		editor2view.put(srcNode, viewNode);
		Set<Node> reverse = view2editor.get(viewNode);
		if (reverse == null) {
			view2editor.put(viewNode, reverse = new HashSet<Node>());
		}
		reverse.add(srcNode);
	}

	public Map<Node, Node> getEditorViewMap() {
		return Collections.unmodifiableMap(editor2view);
	}

	public Node getComplementaryViewNode(Node viewNode) {
		return viewStarting2paired.get(viewNode);
	}

	public void addComplentaryViewNodes(Node srcViewNode, Node tgtViewNode) {
		viewStarting2paired.put(srcViewNode, tgtViewNode);
		viewStarting2paired.put(tgtViewNode, srcViewNode);
	}

	public Set<Node> getSourceNodes(Node targetNode) {
		return view2editor.get(targetNode);
	}

	public Node getTargetNode(Node sourceNode) {
		return editor2view.get(sourceNode);
	}

	public Graph2D getGraph() {
		return viewGraph;
	}

	public List<Node> transform(Node srcNode) {
		String type = MonomerInfoUtils.getPolymerType(srcNode);
		try {
			return TRANSFORMERS.containsKey(type) ? TRANSFORMERS.get(type)
					.newInstance().transform(srcNode, this) : null;
		} catch (Exception e) {
			Logger.getLogger(SequenceViewModelImpl.class.getName()).log(
					Level.SEVERE, null, e);
			return null;
		}
	}

}
