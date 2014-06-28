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

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JPanel;

import org.jdom.JDOMException;

import y.base.NodeMap;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.controller.CacheController;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.data.NotationUpdateEvent;
import org.helm.editor.editor.EditorViewModel;
import org.helm.editor.editor.EditorViewModelImpl;

/**
 * Controller for sequence panel
 * 
 * @author Alexander Makarov
 */
public class SequenceViewControllerImpl implements SequenceViewController {

	private volatile SequenceViewLayout _sequenceViewLayout;
	private boolean layoutMode;
	// IMPORTANT! TURN ON BEFORE PRODUCTION
	private static final boolean USE_CACHE = false;
	private static final int DEFAULT_NUMBER_SEQUENCES = 2;

	public SequenceViewControllerImpl() {

		_sequenceViewLayout = new SequenceViewLayoutImpl();

		final Graph2DView currnetView = _sequenceViewLayout.getGraph2DView();

		currnetView.getCanvasComponent().addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					_sequenceViewLayout.getPopupMenu().show(e.getComponent(),
							e.getX(), e.getY());
				}
			}
		});

		Graph2D graph = currnetView.getGraph2D();
		NodeMap modificationCountMap = graph.createNodeMap();
		graph.addDataProvider(SequenceViewModel.MODIFICATION_COUNT,
				modificationCountMap);

		graph.addDataProvider(EdgeMapKeys.EDGE_INFO, graph.createEdgeMap());

		graph.addDataProvider(SequenceViewModel.LABELS_MAP,
				graph.createNodeMap());
		graph.addDataProvider(NodeMapKeys.LABEL_INFO_MAP, graph.createNodeMap());

		currnetView.getCanvasComponent().addMouseWheelListener(
				new Graph2DViewMouseWheelZoomListener());
	}

	/**
	 * renders a notation to a sequence image
	 * 
	 * @param notation
	 * @return image
	 */
	public Image toImage(String notation) throws NotationException,
			MonomerException, IOException, JDOMException, StructureException {

		setNotation(notation);

		return _sequenceViewLayout.toImage();
	}

	/**
	 * renders a notation to a sequence image
	 * 
	 * @param notation
	 * @param width
	 * @param hight
	 * @return Image -- an Image of the notatin
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public Image toImage(String notation, int width, int hight)
			throws NotationException, MonomerException, IOException,
			JDOMException, StructureException {
		setNotation(notation);

		return _sequenceViewLayout.toImage(width, hight);
	}

	/**
	 * set notation to this display panel.
	 * 
	 * @param notation
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 */
	public synchronized void setNotation(String notation)
			throws NotationException, MonomerException, IOException,
			JDOMException, StructureException {
		if (notation == null || notation.length() == 0) {
			_sequenceViewLayout.reset();
			return;
		}

		CacheController cacheController = CacheController.getInstance();

		if (checkCache(notation)) {
			_sequenceViewLayout.replaceView(initializeCachedView(notation));
		} else {
			EditorViewModel editorModel = null;
			// try {
			editorModel = new EditorViewModelImpl(notation);
			// } catch (Exception e) {
			// _sequenceViewLayout.appendErrorNode();
			// return;
			// }

			_sequenceViewLayout.setLayoutMode(layoutMode);
			_sequenceViewLayout.setupEditorModel(editorModel);
			_sequenceViewLayout.setComposteFlag(isComposite(notation));

			cacheController.addToCache(notation,
					_sequenceViewLayout.copyGraph2DView());
		}
	}

	private boolean isComposite(String notation) {

		if (notation == null) {
			return false;
		}

		String[] connectiosAndStrands = notation.split("[$]");
		if (connectiosAndStrands == null || connectiosAndStrands.length == 0) {
			return false;
		}

		return connectiosAndStrands[0].split("RNA|PEPTIDE").length > DEFAULT_NUMBER_SEQUENCES;
	}

	public JPanel getSequenceView() {
		return (JPanel) _sequenceViewLayout;
	}

	public void refreshSequenceView() {
		_sequenceViewLayout.refreshLayout();
	}

	public Graph2DView getView() {
		return _sequenceViewLayout.getGraph2DView();
	}

	public void appendErrorNode() {
		_sequenceViewLayout.appendErrorNode();
	}

	public void minimizeGaps() {
		_sequenceViewLayout.minimizeGaps();
	}

	public void setAligment(int viewType) {
		_sequenceViewLayout.setAlignment(viewType);
	}

	public void updateAlignment() {
		_sequenceViewLayout.updateAlignment();
	}

	private Graph2DView initializeCachedView(String notation) {

		CacheController cacheController = CacheController.getInstance();

		Graph2DView cachedView = cacheController.getCachedValue(notation);

		Graph2D copyGraph = new Graph2D(cachedView.getGraph2D());
		copyGraph.addDataProvider(SequenceViewModel.MODIFICATION_COUNT,
				copyGraph.createNodeMap());
		copyGraph.addDataProvider(NodeMapKeys.MONOMER_REF,
				copyGraph.createNodeMap());
		copyGraph.addDataProvider(EdgeMapKeys.EDGE_INFO,
				copyGraph.createEdgeMap());

		Graph2DView view = new Graph2DView(copyGraph);
		return view;
	}

	private boolean checkCache(String notation) {
		if (isFromPanes()) {
			return false;
		}

		CacheController cacheController = CacheController.getInstance();
		if (USE_CACHE && cacheController.isValueInCache(notation)) {
			return true;
		}

		return false;
	}

	private boolean isFromPanes() {
		for (StackTraceElement se : Thread.currentThread().getStackTrace()) {
			if (se.getClassName().contains("SequenceViewPanes")) {
				return true;
			}
		}
		return false;
	}

	public void setLayoutMode(boolean mode) {
		layoutMode = mode;
	}

	public double getBoundOffset() {
		return _sequenceViewLayout.getBoundOffset();
	}
}
