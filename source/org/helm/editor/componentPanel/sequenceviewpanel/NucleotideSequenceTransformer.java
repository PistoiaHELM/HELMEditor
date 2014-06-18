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

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import y.base.Edge;
import y.base.Node;
import y.util.GraphCopier;

import org.helm.editor.data.SequenceViewIterator;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.NodeFactory;
import org.helm.editor.utility.SequenceGraphTools;

public class NucleotideSequenceTransformer extends AbstractSequenceTransformer {
	private Queue<Node> bfsResult = new LinkedList<Node>();
	private LinkedList<Node> PNodes = new LinkedList<Node>();

	public static final String P_LEFT_LABEL = "l";
	public static final String P_RIGHT_LABEL = "r";
	public static final String P_LEFT_RIGHT_LABEL = "b";
	public static final String P_MODIFIED_LABEL = " ";
	public static final String LABEL_DELIMETER = "_";

	@Override
	protected void buildViewSequence(Node editorStartingNode) {
		SequenceViewIterator it = new SequenceViewIterator(editor,
				editorStartingNode);
		while (it.hasNext()) {
			Node n = it.next();
			if (!MonomerInfoUtils.isPMonomer(n)) {
				bfsResult.add(n);
				continue;
			}

			Node P = n;
			PNodes.add(P);

			if (bfsResult.isEmpty()) {
				continue;
			}
			addViewNode(bfsResult.poll());

		}

		if (!bfsResult.isEmpty()) {
			Node n = bfsResult.poll();
			if (MonomerInfoUtils.isBranchMonomer(n)) {
				// should never happen
				viewSequence.add(createBranch(n));
			} else {
				addViewNode(n);
			}
		}

		mapPNodes();
		addPEdges();
	}

	@Override
	protected void fillLabelMaps() {
		for (Node viewNode : viewSequence) {
			LabelInfo viewInfo = getLabelInfo(viewNode);
			for (Node editorNode : viewModel.getSourceNodes(viewNode)) {
				LabelInfo editorInfo = getEditorLabelInfo(editorNode);
				if (editorInfo == null) {
					continue;
				}
				if (MonomerInfoUtils.isBranchMonomer(editorNode)) {
					viewInfo.setPositionNumber(editorInfo.getPositionNumber());
				} else if (MonomerInfoUtils.isRMonomer(editorNode)) {
					viewInfo.setTerminalLabel(editorInfo.getTerminalLabel());
				}
			}
		}

		mapPLinkerLabels();
	}

	private void addPEdges() {
		for (Node p : PNodes) {
			Node source = getSinglePredecessor(p);
			Node target = getSingleSuccessor(p);

			if ((source == null) || (target == null)) {
				continue;
			}

			Node viewSource = viewModel.getViewNode(source);
			Node viewTarget = viewModel.getViewNode(target);

			addEdge(viewSource, viewTarget, p);
		}
	}

	private void mapPNodes() {
		for (Node p : PNodes) {
			Node succ = getSingleSuccessor(p);
			if (succ != null) {
				Node viewNode = viewModel.getViewNode(succ);
				viewModel.putViewNode(p, viewNode);
			} else {
				Node prev = getSinglePredecessor(p);
				if (prev != null) {
					Node viewNode = viewModel.getViewNode(prev);
					viewModel.putViewNode(p, viewNode);
				}
			}
		}
	}

	private void mapPLinkerLabels() {
		// special case with 2 p labels
		if ((viewSequence.size() == 1) && (PNodes.size() == 2)) {
			Node seqNode = viewSequence.get(0);
			Node p1 = PNodes.get(0);
			Node p2 = PNodes.get(1);

			viewNodesLabelsMap.set(seqNode, P_LEFT_RIGHT_LABEL
					+ getLabelText(p1) + LABEL_DELIMETER + getLabelText(p2));

			LabelInfo labelInfo = getLabelInfo(seqNode);
			labelInfo
					.setLeftLinker(MonomerInfoUtils.getMonomerID(p1) == null ? "p"
							: MonomerInfoUtils.getMonomerID(p1));
			labelInfo
					.setRightLinker(MonomerInfoUtils.getMonomerID(p2) == null ? "p"
							: MonomerInfoUtils.getMonomerID(p2));

			return;
		}

		for (Node p : PNodes) {
			// skip the P node if it has 2 regular neighbors
			if (p.degree() > 2) {
				continue;
			}
			Node succ = getSingleSuccessor(p);
			Node prev = getSinglePredecessor(p);
			if (succ != null
					&& MonomerInfoUtils.isChemicalModifierPolymer(succ)) {
				succ = null;
			}
			if (prev != null
					&& MonomerInfoUtils.isChemicalModifierPolymer(prev)) {
				prev = null;
			}
			if (succ != null && prev != null) {
				continue;
			}
			// set labels
			String name = MonomerInfoUtils.getMonomerID(p) == null ? "p"
					: MonomerInfoUtils.getMonomerID(p);
			if (succ != null) {
				viewNodesLabelsMap.set(viewModel.getViewNode(succ),
						P_LEFT_LABEL + getLabelText(p));
				getLabelInfo(viewModel.getViewNode(succ)).setLeftLinker(name);
			} else if (prev != null) {
				viewNodesLabelsMap.set(viewModel.getViewNode(prev),
						P_RIGHT_LABEL + getLabelText(p));
				getLabelInfo(viewModel.getViewNode(prev)).setRightLinker(name);
			}
		}
	}

	private String getLabelText(Node node) {
		if (isUnmodifierP(node)) {
			return node.toString();
		}

		return P_MODIFIED_LABEL;
	}

	private boolean isUnmodifierP(Node node) {
		if (node == null) {
			return false;
		}

		return node.toString().equalsIgnoreCase("p");
	}

	private Node createBranch(Node editorBranch) {
		Node viewNode = createNode(
				MonomerInfoUtils.getNaturalAnalog(editorBranch), view);
		viewModel.putViewNode(editorBranch, viewNode);
		return viewNode;
	}

	private int getModifications(Node viewNode) {
		int result = 0;
		boolean hasBranch = false;
		Set<Node> mapped = viewModel.getSourceNodes(viewNode);
		for (Node n : mapped) {
			if (MonomerInfoUtils.getMonomer(n).isModified()
					&& !MonomerInfoUtils.isPMonomer(n)) {
				result++;
			}

			if (MonomerInfoUtils.isBranchMonomer(n)) {
				hasBranch = true;
			}
		}

		if (!hasBranch) {
			result++;
		}

		return result;
	}

	private Node addViewNode(Node R) {

		Node viewNode;
		Node editorBranch = null;

		if (bfsResult.isEmpty()) {
			// Branch was deleted
			GraphCopier copier = SequenceGraphTools.getGraphCopier(editor);
			viewNode = copier.copy(
					NodeFactory.createDummyNucleicAcidBaseNode("X"), view)
					.firstNode();
		} else {
			editorBranch = bfsResult.poll();
			viewNode = createBranch(editorBranch);
		}

		if (editorBranch != null) {
			viewModel.putViewNode(editorBranch, viewNode);
		}

		viewModel.putViewNode(R, viewNode);
		viewSequence.add(viewNode);
		viewModificationsCount.set(viewNode, getModifications(viewNode));

		return viewNode;
	}

	private void addEdge(Node viewSource, Node viewTarget, Node PLinker) {
		if ((viewSource == null) || (viewTarget == null)) {
			return;
		}
		Edge e = view.createEdge(viewSource, viewTarget);
		viewEdgeTypes
				.set(e,
						MonomerInfoUtils.getMonomer(PLinker).isModified() ? new SViewEdgeInfo(
								EdgeType.MODIFIED_P) : new SViewEdgeInfo(
								EdgeType.REGULAR));
	}

}
