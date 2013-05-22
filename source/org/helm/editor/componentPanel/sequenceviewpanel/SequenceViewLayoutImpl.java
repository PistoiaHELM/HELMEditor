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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.data.Annotator;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.EditorView;
import org.helm.editor.editor.EditorViewModel;
import org.helm.editor.layout.LabelInfo;
import org.helm.editor.layout.StructuresLayoutModule;
import org.helm.editor.layout.primitives.AbstractLayoutPrimitives;
import org.helm.editor.layout.primitives.ComponentViewLayoutPrimitives;
import org.helm.editor.layout.primitives.SequenceViewLayoutPrimitives;
import org.helm.editor.utility.ClipBoardProcessor;
import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.SaveAsPNG;

/**
 * This class represent a view part of Sequence view panel
 * 
 * @author Makarov Alexander
 * @version 1.0
 */
public class SequenceViewLayoutImpl extends JPanel implements
		SequenceViewLayout {
	private Graph2DView _view;
	private SequenceViewModel viewModel;
	private EditorViewModel editorModel;
	private EditorView editorView;

	private ViewMetrics _layoutMetrics;

	private GraphElementConstructor _elementsFactory;
	private LabelConstructor _labelConstructor;

	// TODO: introduce class & interface SequenceView for such view stuff
	private Map<Node, Boolean> startingNodelayoutNodeMap = new HashMap<Node, Boolean>();;
	private JPopupMenu popup = null;

	private boolean isLayoutForComponentView;

	private boolean isCompositeChain = false;

	private String errorMessage = "Invalid Notation";
	
	public SequenceViewLayoutImpl() {

		_view = new Graph2DView();
		_view.setAntialiasedPainting(true);
		isLayoutForComponentView = false;

		_layoutMetrics = new ViewMetrics(_view);
		_elementsFactory = new ViewElementsConstructor(_view.getGraph2D(),
				viewModel, _layoutMetrics);
		_labelConstructor = new LabelConstructor(_layoutMetrics, editorView,
				editorModel, viewModel, _elementsFactory, _view.getGraph2D());

		setSize(20, 20);

		popup = new JPopupMenu("copy");
		JMenuItem copyToClipbordItem = new JMenuItem("Copy current image to clipboard");
                copyToClipbordItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClipBoardProcessor.copy(SaveAsPNG
						.getBufferedImageFromImage(_view.getImage()));
			}
		});
		popup.add(copyToClipbordItem);

		startingNodelayoutNodeMap = new HashMap<Node, Boolean>();

		setLayout(new BorderLayout());
		add(_view, BorderLayout.CENTER);
	}

	public void reset() {
		_view.getGraph2D().clear();
		_view.updateView();
	}

	public Graph2DView getGraph2DView() {
		return _view;
	}

	public Image toImage() throws NotationException, MonomerException,
			IOException, JDOMException {
		return _view.getImage();
	}

	public Image toImage(int width, int hight) throws NotationException,
			MonomerException, IOException, JDOMException, StructureException {

		_layoutMetrics.setViewSize(width, hight);
		return _view.getImage();
	}

	public Graph2DView copyGraph2DView() {

		Graph2DView copyView = new Graph2DView();

		Graph2D copyGraph2D = new Graph2D(_view.getGraph2D());

		copyGraph2D.addDataProvider(SequenceViewModel.MODIFICATION_COUNT,
				copyGraph2D.createNodeMap());
		copyGraph2D.addDataProvider(NodeMapKeys.MONOMER_REF, copyGraph2D
				.createNodeMap());
		copyGraph2D.addDataProvider(EdgeMapKeys.EDGE_INFO, copyGraph2D
				.createEdgeMap());

		copyView.setGraph2D(copyGraph2D);

		return copyView;
	}

	public void initView() throws NotationException, JDOMException,
			MonomerException, IOException {
		startingNodelayoutNodeMap.clear();

		_view.getGraph2D().clear();

		Graph2D viewGraph = _view.getGraph2D();
		viewGraph.addDataProvider(SequenceViewModel.MODIFICATION_COUNT,
				viewGraph.createNodeMap());
		viewGraph.addDataProvider(NodeMapKeys.MONOMER_REF, viewGraph
				.createNodeMap());
		viewGraph.addDataProvider(EdgeMapKeys.EDGE_INFO, viewGraph
				.createEdgeMap());

		viewGraph.addDataProvider(SequenceViewModel.COMPLEMENTARY_VIEW_NODE, viewGraph
				.createNodeMap());
		viewGraph.addDataProvider(SequenceViewModel.IS_FLIPPED, viewGraph
				.createNodeMap());
		viewGraph.addDataProvider(NodeMapKeys.NODE2STARTING_NODE, viewGraph.createNodeMap());
		viewGraph.addDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE, viewGraph.createNodeMap());
		viewGraph.addDataProvider(NodeMapKeys.HYPERNODE2STARTING_NODE, viewGraph.createNodeMap());
		
		
		
		viewModel = new SequenceViewModelImpl(viewGraph);
		_labelConstructor.setUpView(viewModel);

		if (!editorModel.isEmpty()) {
			transform();
			runLayout();
		}		
		
		_layoutMetrics.updateView();
	}

	public void appendErrorNode() {
		Graph2D graph = _view.getGraph2D();
		graph.clear();
		_elementsFactory.createNode(errorMessage,
				ViewElementsConstructor.ERROR_NODE);

		_layoutMetrics.updateView();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	public void replaceView(Graph2DView view) {
		removeAll();
		
		_view = view;
//		_view = copyGraph2DView();
		_layoutMetrics.setGraph2DView(_view);
		
//		updateAlignment();
		
		add(_view, BorderLayout.CENTER);
		
		_view.updateView();
//		refreshLayout();
		
		revalidate();
		super.repaint();
	}

	public void setupEditorModel(EditorViewModel newEditorModel)
			throws NotationException, JDOMException, MonomerException,
			IOException {

		editorModel = newEditorModel;
		editorView = editorModel.renderView();
		
		_labelConstructor.setUpModels(editorModel, editorView);

		initView();
		
		revalidate();
	}

	public void refreshLayout() {
		reset();
		repaint();
		revalidate();
	}

	public void minimizeGaps() {
		_layoutMetrics.minimizeGaps();
	}

	public void setAlignment(int viewType) {
		_layoutMetrics.setAlignment(viewType, calculateSequenceSize());
	}

	public void updateAlignment() {
		_layoutMetrics.updateAlignment(isCompositeChain);
		_view.repaint();
	}

	public void setComposteFlag(boolean compositeFlag) {
		isCompositeChain = compositeFlag;
	}

	private double calculateSequenceSize() {
		int nodeCount = viewModel == null ? 0 : viewModel.getGraph()
				.nodeCount();

		return nodeCount
				* (_layoutMetrics.getNodeSize() + _layoutMetrics
						.getHDistanceInt());
	}

	public void setLayoutMode(boolean mode) {
		isLayoutForComponentView = mode;
	}

	private void runLayout() {
		// hack to use layout module
		AbstractLayoutPrimitives layoutPrimitives = isLayoutForComponentView 
			? new ComponentViewLayoutPrimitives(
					_labelConstructor, 
					_layoutMetrics)
			: new SequenceViewLayoutPrimitives(
					_labelConstructor,
					_layoutMetrics);

		StructuresLayoutModule layoutModule = new StructuresLayoutModule();
		layoutModule.setLayoutPrimitives(layoutPrimitives);
		layoutModule.start(_view.getGraph2D());

		layoutPrimitives.arrangeNodesVisualisationSettings(_view.getGraph2D());
		layoutPrimitives.arrangeEdgesVisualisationSettings(_view.getGraph2D());

		_layoutMetrics.updateView();		
		_view.getGraph2D().updateViews();
	}

	/**
	 * remove the backbones and chemical s tructures
	 */
	private void transform() throws NotationException, JDOMException,
			IOException, MonomerException {

		transformSequences();

		Graph2D editorGraph = editorModel.getEditorGraph();

		// set up all pair edges
		EdgeMap pairEdgeMap = (EdgeMap) editorGraph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		EdgeCursor edges = editorGraph.edges();

		_elementsFactory.setUpViewModel(viewModel);
		for (; edges.ok(); edges.next()) {
			Edge currentEdge = edges.edge();
			EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) pairEdgeMap.get(currentEdge);
			if (edgeInfo != null && edgeInfo.isPair()) {
				_elementsFactory.createEdge(currentEdge,
						edgeInfo.getSourceNodeAttachment(),
						edgeInfo.getTargetNodeAttachment(),
						ViewElementsConstructor.PAIR_EDGE);
			} else if (edgeInfo != null && edgeInfo.isPBranchBackbone() && MonomerInfoUtils.isPBranchEdge(currentEdge)) {
				_elementsFactory.createEdge(currentEdge,
						edgeInfo.getSourceNodeAttachment(),
						edgeInfo.getTargetNodeAttachment(),
						ViewElementsConstructor.BACKBONE_BRANCH_EDGE);
			} else if (edgeInfo != null && edgeInfo.isPBranchBranch() && MonomerInfoUtils.isPBranchEdge(currentEdge)) {
				_elementsFactory.createEdge(currentEdge,
						edgeInfo.getSourceNodeAttachment(),
						edgeInfo.getTargetNodeAttachment(),
						ViewElementsConstructor.BRANCH_BRANCH_EDGE);
			} else if (GraphUtils.isChemEdge(currentEdge)) {
				_elementsFactory.createEdge(currentEdge,
						edgeInfo.getSourceNodeAttachment(),
						edgeInfo.getTargetNodeAttachment(),
						ViewElementsConstructor.CHEM_EDGE);
			} else if (MonomerInfoUtils.isPInterSequencrEdge(currentEdge)) {
				// another realizer?
				_elementsFactory.createEdge(currentEdge,
						edgeInfo.getSourceNodeAttachment(),
						edgeInfo.getTargetNodeAttachment(),
						ViewElementsConstructor.SIMPLE_EDGE);
			}
		}
	}

	private void transformSequences() throws NotationException,
			MonomerException, JDOMException, IOException {
		
		
		for (Node node : editorModel.getEditorStartingNodes()) {

			List<Node> viewSequence = viewModel.transform(node);

			if (MonomerInfoUtils.isChemicalModifierPolymer(node)) {
				continue;
			}
			
			if ((viewSequence != null) && (!viewSequence.isEmpty())){

				// TODO: move all labeling to the label layouter //
				///////////////////////////////////////////////////
				///////////////////////////////////////////////////
				NodeMap positionMap = (NodeMap)_view.getGraph2D().
							getDataProvider(NodeMapKeys.LABEL_INFO_MAP);
				for (Node n : viewSequence) {
					LabelInfo info = (LabelInfo)positionMap.get(n);
					assignNumber(_view.getGraph2D(), info.getPositionNumber(), n, false);
				}
				
				////////////////////////////////////////////////////
				///////////////////////////////////////////////////
				
			}
		}
	}

	private NodeRealizer assignNumber(Graph2D graph, int positionCount,
			Node node, boolean isPeptide) {
		NodeRealizer nr = graph.getRealizer(node);
		NodeLabel positionLabel;

		if (nr.labelCount() > 1) {
			positionLabel = nr.getLabel(1);
			nr.removeLabel(positionLabel);
		}

		positionLabel = nr.createNodeLabel();
		Annotator.configAnnotationLabel(positionLabel, isPeptide);

		String posititonString = String.valueOf(positionCount);
		
		int numSymb = posititonString.length();
		double xOffset = _layoutMetrics.getNumberLabelXOffset();
		if (numSymb > 1){
			 xOffset -= 3 * numSymb; 
		}
		
		positionLabel.setModel(NodeLabel.FREE);
		positionLabel.setOffset(xOffset, _layoutMetrics.getNumberLabelYOffset());
		
		positionLabel.setText(posititonString);
		positionLabel.setFontSize(_layoutMetrics.getNumberLabelFontSize());
		nr.addLabel(positionLabel);
		
		return nr;
	}

	public double getBoundOffset() {
		return _layoutMetrics.getXStep();
	}

}
