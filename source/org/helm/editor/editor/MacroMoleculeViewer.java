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
package org.helm.editor.editor;

import java.awt.BorderLayout;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdom.Element;
import org.jdom.JDOMException;

import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.action.ViewNotationAction;
import org.helm.editor.componentPanel.SequenceViewPanes;
import org.helm.editor.controller.ModelController;
import org.helm.editor.data.Annotator;
import org.helm.editor.data.DataListener;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphPair;
import org.helm.editor.data.NotationUpdateEvent;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.ListNotComparableException;
import org.helm.editor.utility.NotationParser;
import org.helm.editor.utility.NumericalUtils;
import org.helm.editor.utility.SequenceLayout;
import org.helm.notation.tools.ComplexNotationParser;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import y.view.AutoDragViewMode;
import y.view.EditMode;
import y.view.ViewMode;

/**
 * 
 * @author lih25
 */
public class MacroMoleculeViewer extends JPanel implements DataListener,
		NotationProducer {

	private Graph2DView viewer;
	private SequenceViewPanes seqenceViewPanels;
	private GraphManager graphManager;
	private boolean hasSequenceViewPanel;
	private String _ownerCode;
	private String _notation;
	private boolean _numberNodes = false;

	public MacroMoleculeViewer() {
		this(false);
	}

	public MacroMoleculeViewer(boolean hasSequenceViewPanel) {
		this(hasSequenceViewPanel, false);
	}

	/**
	 * creat a macromolecule viewer
	 * 
	 * @param hasSequenceViewPanel
	 *            - if it will have a sequence viewer panel at the bottom
	 */
	public MacroMoleculeViewer(boolean hasSequenceViewPanel, boolean numberNodes) {
		this.hasSequenceViewPanel = hasSequenceViewPanel;
		viewer = new Graph2DView();
		viewer.setAntialiasedPainting(true);
		viewer.setFitContentOnResize(true);
		graphManager = new GraphManager();
		_numberNodes = numberNodes;
		setLayout(new BorderLayout());

		final JPopupMenu popupMenu = new JPopupMenu();
		ViewNotationAction viewNotationAtction = new ViewNotationAction(this,
				"Show Notation");
		popupMenu.add(viewNotationAtction);
		viewer.addViewMode(new ViewMode() {
			@Override
			public void mousePressed(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON3) {
					popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
				}
			}
		});

		registerViewModes();

		// Note that mouse wheel support requires J2SE 1.4 or higher.
		viewer.getCanvasComponent().addMouseWheelListener(
				new Graph2DViewMouseWheelZoomListener());
		ModelController.getInstance().registerListener(this);

		try {
			_ownerCode = NumericalUtils.getUniqueCode();
		} catch (NoSuchAlgorithmException ex) {
			ExceptionHandler.handleException(ex);
		}

		if (hasSequenceViewPanel) {
			seqenceViewPanels = new SequenceViewPanes(_ownerCode);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					viewer, seqenceViewPanels);
			splitPane.setDividerLocation(240);
			splitPane.setOneTouchExpandable(true);

			add(splitPane, BorderLayout.CENTER);

			ModelController.getInstance().registerListener(seqenceViewPanels);
		} else {
			add(viewer, BorderLayout.CENTER);
		}

	}

	public boolean containsSequenceViewPanel() {
		return hasSequenceViewPanel;
	}

	public String getOwnerCode() {
		return _ownerCode;
	}

	/**
	 * Note that -notation can only be modified via setNotation() method. Just
	 * need to track its state in setNotation(), not necessary to regenerate it.
	 * 
	 * @return notation
	 */
	public String getNotation() {
		return _notation;
	}

	/**
	 * generate the canonical string notation for the graph being displaied
	 * 
	 * @return canonical string notation
	 * @throws StructureException
	 */
	public String getCanonicalNotation() throws StructureException,
			NotationException, JDOMException, MonomerException, IOException,
			ClassNotFoundException {
		if (null == _notation) {
			return null;
		} else {
			return ComplexNotationParser.getCanonicalNotation(_notation);
		}
	}

	/**
	 * Get the information of the current display structure
	 * 
	 * @return the structure information in XML format
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 * @throws org.helm.notation.StructureException
	 * @see Graph2NotationTranslator #getStructureInfoXML(Graph2D, GraphManager)
	 * 
	 */
	public String getStructureInfoXML() throws NotationException,
			MonomerException, IOException, JDOMException, StructureException,
			ListNotComparableException, ClassNotFoundException {
		return Graph2NotationTranslator.getStructureInfoXML(
				viewer.getGraph2D(), graphManager);
	}

	/**
	 * get the information of the current display structure
	 * 
	 * @return the structure information as a string
	 * @throws org.helm.notation.NotationException
	 * @throws org.helm.notation.MonomerException
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 * @throws org.helm.notation.StructureException
	 * @throws org.helm.editor.utility.ListNotComparableException
	 * @see Graph2NotationTranslator #getStructureInfoElement(Graph2D,
	 *      GraphManager)
	 */
	public Element getStructureInfoElement() throws NotationException,
			MonomerException, IOException, JDOMException, StructureException,
			ListNotComparableException, ClassNotFoundException {
		return Graph2NotationTranslator.getStructureInfoElement(
				viewer.getGraph2D(), graphManager);
	}

	/**
	 * get the graphxml string representation of the current graph
	 * 
	 * @return graph XML string
	 */
	public String getGraphXML() {
		return MacromoleculeEditor.getGraphXML(viewer.getGraph2D());
	}

	public void onUpdate(NotationUpdateEvent event) {
		if (!event.getOwner().equals(_ownerCode)) {
			return;
		}

		String notation = event.getData();
		if (notation == null || notation.equals("")) {
			reset();
			return;
		}

		try {
			ComplexNotationParser.validateComplexNotation(notation);
			GraphPair pair = NotationParser.getGraphPair(notation);
			Graph2NotationTranslator.updateHyperGraph(pair.getGraph(),
					pair.getGraphManager());
			viewer.setGraph2D(pair.getGraph());
			graphManager = pair.getGraphManager();

			updateAnnotator();

			(new SequenceLayout(viewer, graphManager)).doLayout();
			viewer.updateView();
			graphManager.getAnnotator().annotateAllBasePosition();

		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
		}

	}

	public void setNotation(String notation) {
		if (null == _notation) {
			_notation = "";
		}

		if (null == notation) {
			notation = "";
		}

		if (!notation.equals(_notation)) {
			_notation = notation;
			ModelController.notationUpdated(notation, _ownerCode);
		}
	}

	public Graph2D getGraph2d() {
		return viewer.getGraph2D();
	}

	public JComponent getGraphViewComponent() {
		return viewer;
	}

	private void reset() {
		viewer.getGraph2D().clear();
		graphManager.reset();
		if (null != seqenceViewPanels) {
			seqenceViewPanels.init(viewer.getGraph2D(), graphManager);
		}
		viewer.fitContent();
		viewer.updateView();
	}

	private void updateAnnotator() {
		Annotator currAnnotator = graphManager.getAnnotator();
		currAnnotator.setGraph2D(viewer.getGraph2D());
		currAnnotator.setManager(graphManager);
		currAnnotator.numberNodes(_numberNodes);
	}

	private void registerViewModes() {
		// edit mode will show tool tips over nodes
		EditMode editMode = createEditMode();
		if (editMode != null) {
			viewer.addViewMode(editMode);
		}
		viewer.addViewMode(new AutoDragViewMode());
	}

	private EditMode createEditMode() {
		EditMode editMode = new HierarchyEditMode();
		editMode.showNodeTips(true);
		return editMode;
	}
}
