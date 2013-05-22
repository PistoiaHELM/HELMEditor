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

import java.awt.Color;

import y.base.DataProvider;
import y.base.Node;
import y.base.NodeMap;
import y.util.GraphHider;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.EditorView;
import org.helm.editor.editor.EditorViewModel;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.utility.MonomerInfoUtils;

public class LabelConstructor {

	private static final int LEFT_LABEL = 0;
	private static final int RIGHT_LABEL = 1;
	private static final int LEFT_RIGHT_LABEL = 2;

	private ViewMetrics _layoutMetrics;
	private EditorView _editorView;
	private EditorViewModel _editorModel;
	private SequenceViewModel _viewModel;
	private GraphElementConstructor _elementFactory;
	private Graph2D _graph;

	public static final boolean LEFT_PHOSPHATE_LABEL_POSTION = true;
	public static final boolean RIGHT_PHOSPHATE_LABEL_POSTION = false;

	public LabelConstructor(ViewMetrics layoutMetrics, EditorView editorView,
			EditorViewModel editorModel, SequenceViewModel viewModel,
			GraphElementConstructor elementFactory, Graph2D graph) {

		_layoutMetrics = layoutMetrics;
		_editorView = editorView;
		_editorModel = editorModel;
		_viewModel = viewModel;
		_elementFactory = elementFactory;

		_graph = graph;
	}

	public void setUpModels(EditorViewModel editorModel, EditorView editorView) {
		_editorView = editorView;
		_editorModel = editorModel;
	}

	public void setUpView(SequenceViewModel viewModel) {
		_viewModel = viewModel;
	}

	public void add5Labels(Graph2D graph) {

		GraphHider graphHider = new GraphHider(graph);
		for (Node n : graph.getNodeArray()) {
			if (MonomerInfoUtils.is5Node(n, graph)) {
				graphHider.hide(n);
			}
		}

		for (Node n : _editorModel.getEditorGraphNodes()) {
			NodeLabel ann = _editorView.getEditorLabel(n);
			if (ann == null) {
				continue;
			}

			String text = ann.getText();
			if (MonomerInfoUtils.isAnnotation(text)) {
				Node seqNode = _viewModel.getViewNode(n);
				if (seqNode == null) {
					seqNode = (Node) _elementFactory.realize(graph, n,
							GraphElementConstructor.REALIZE_NEIBOURS);
				}

				Node compNode = _viewModel.getComplementaryViewNode(seqNode);
				boolean isCompl = false;

				if (compNode != null) {
					NodeRealizer seqReqlizer = graph.getRealizer(seqNode);
					NodeRealizer complRealizer = graph.getRealizer(compNode);

					if (seqReqlizer.getCenterY() > complRealizer.getCenterY()) {
						isCompl = true;
					}
				}

				clearAnnotationLabel(seqNode);
				copyAnnotationLabel(ann, seqNode, isCompl);
			}
		}
	}

	private void copyAnnotationLabel(NodeLabel sourcelabel, Node node,
			boolean isComplimentary) {
		NodeRealizer nr = _graph.getRealizer(node);
		NodeLabel label = nr.createNodeLabel();

		nr.setSize(_layoutMetrics.getNodeSize(), _layoutMetrics.getNodeSize());

		label.setModel(NodeLabel.FREE);
		double complMulX = 1;
                double complMulY=1;
		if (isComplimentary || sourcelabel.getPosition() == NodeLabel.SE) {
			complMulX = -1.5;
                        complMulY=-1;
		}

		String labelText = sourcelabel.getText();
		label.setText(labelText);
		
		int modStartLabelStep = 0;
		if (isSenceOrAntiSence(labelText) && isComplimentary==false){
			modStartLabelStep = _layoutMetrics.getModifiedStartLabelStep();			
		}
		
                label.setOffset(complMulX * ( _layoutMetrics.getXStartLabelOffset() + modStartLabelStep),
				 complMulY* _layoutMetrics.getYStartLabelOffset());

		label.setFontSize(_layoutMetrics.getLabelFontSize());
		label.setBackgroundColor(sourcelabel.getBackgroundColor());

		nr.addLabel(label);
	}
	
	private boolean isSenceOrAntiSence(String text){
		return text.equals("5' ss") || text.equals("5' as");
	}

	private void clearAnnotationLabel(Node node) {
		NodeRealizer nr = _graph.getRealizer(node);
		for (int i = 1; i < nr.labelCount(); i++) {
			NodeLabel l = nr.getLabel(i);
			if ((l != null) && (l.getText() != null)) {
				String ann = l.getText();
				if (MonomerInfoUtils.isAnnotation(ann)) {
					nr.removeLabel(l);
					return;
				}
			}
		}
	}

	public void addLabelsToSequence(Node startNode, Node lastNode,
			Graph2D graph, boolean reverse) {
		
		DataProvider nodeMap = graph.getDataProvider(SequenceViewModel.LABELS_MAP);
		
		if (nodeMap == null){
			return;
		}
		
		NodeMap labelsMap = (NodeMap) nodeMap;
		
		// part code for left P label
		Object commonLabelText = labelsMap.get(startNode);
		NodeRealizer nodeRealizer = graph.getRealizer(startNode);

		int checkResult = checkLabel(true, commonLabelText);

		if (checkResult == LEFT_RIGHT_LABEL) {

			String strCommonLabelText = (String) commonLabelText;
			int labelDelimenterPosition = strCommonLabelText
					.indexOf(NucleotideSequenceTransformer.LABEL_DELIMETER);

			String leftLabelText = ((String) commonLabelText).substring(1,
					labelDelimenterPosition);
			String rightLabelText = ((String) commonLabelText)
					.substring(labelDelimenterPosition + 1);

			addLabelPToNode(nodeRealizer, leftLabelText,
					LEFT_PHOSPHATE_LABEL_POSTION);
			addLabelPToNode(nodeRealizer, rightLabelText,
					RIGHT_PHOSPHATE_LABEL_POSTION);
		}

		if (checkResult == LEFT_LABEL) {
			String labelText = ((String) commonLabelText).substring(1);
			addLabelPToNode(nodeRealizer, labelText,
					reverse ? RIGHT_PHOSPHATE_LABEL_POSTION
							: LEFT_PHOSPHATE_LABEL_POSTION);
		}

		// part code for right P label
		commonLabelText = labelsMap.get(lastNode);
		nodeRealizer = graph.getRealizer(lastNode);

		checkResult = checkLabel(false, commonLabelText);
		if (checkResult == RIGHT_LABEL) {
			String labelText = ((String) commonLabelText).substring(1);
			addLabelPToNode(nodeRealizer, labelText,
					reverse ? LEFT_PHOSPHATE_LABEL_POSTION
							: RIGHT_PHOSPHATE_LABEL_POSTION);
		}

	}
	
	public void addLabelsToSequenceImproved(Graph2D graph) {
		
		DataProvider nodeMap = graph.getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
		
		if (nodeMap == null){
			return;
		}
		
		NodeMap labelsMap = (NodeMap) nodeMap;
		
		for (Node n : graph.getNodeArray()) {
			LabelInfo lInfo = (LabelInfo)labelsMap.get(n);
			boolean reverse = lInfo.isFlipped();
			if (lInfo != null) {
				NodeRealizer nodeRealizer = graph.getRealizer(n);
				if (lInfo.getLeftLinker() != null) {
					addLabelPToNode(nodeRealizer, lInfo.getLeftLinker(),
							reverse ? RIGHT_PHOSPHATE_LABEL_POSTION
									: LEFT_PHOSPHATE_LABEL_POSTION);
				}
				
				if (lInfo.getRightLinker() != null) {
					addLabelPToNode(nodeRealizer, lInfo.getRightLinker(),
							reverse ? LEFT_PHOSPHATE_LABEL_POSTION
									: RIGHT_PHOSPHATE_LABEL_POSTION);
				}
			}
		}

	}

	private int checkLabel(boolean isLeft, Object labelText) {
		boolean standartCondition = labelText != null
				&& labelText instanceof String;

		if (!standartCondition) {
			return -1;
		}

		String castedString = (String) labelText;
		
		if (castedString
				.startsWith(NucleotideSequenceTransformer.P_LEFT_RIGHT_LABEL)) {
			return LEFT_RIGHT_LABEL;
		}

		if (isLeft
				&& castedString
						.startsWith(NucleotideSequenceTransformer.P_LEFT_LABEL)) {
			return LEFT_LABEL;
		}

		if (!isLeft
				&& castedString
						.startsWith(NucleotideSequenceTransformer.P_RIGHT_LABEL)) {
			return RIGHT_LABEL;
		}

		return -1;
	}

	private void addLabelPToNode(NodeRealizer nodeRealizer, String labelText,
			boolean isLeft) {
		NodeLabel label = nodeRealizer.createNodeLabel();
				
		double xOffSet = _layoutMetrics.calculateLabelOffset(isLeft);
		
		nodeRealizer.setFillColor(Color.RED);
		
		label.setModel(NodeLabel.FREE);
		label.setOffset(xOffSet, 0);
		label.setText(labelText);
		label.setFontSize(_layoutMetrics.getPhosphateLabelFont());
		
		if (labelText.equals(NucleotideSequenceTransformer.P_MODIFIED_LABEL)){
			label.setAutoSizePolicy(NodeLabel.AUTOSIZE_NONE);
			
			label.setContentHeight(4);
			label.setContentWidth(6);
			
			if (isLeft){
				label.setContentWidth(_layoutMetrics.getLeftModifiedLabelWidth());
				label.setOffset(_layoutMetrics.getLeftModifiedLabelXOffset(), 8);
			} else {
				label.setContentWidth(_layoutMetrics.getRightModifiedLabelWidth());
				label.setOffset(_layoutMetrics.getRightModifiedLabelXOffset(), 8);
			}
			
			label.setBackgroundColor(new Color(128, 0, 255));
		}
		
		nodeRealizer.addLabel(label);
	}

}
