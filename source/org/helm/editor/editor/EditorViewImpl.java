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

import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;

import org.helm.editor.data.Annotator;
import org.helm.editor.data.ChemSequenceHolder;
import org.helm.editor.data.GraphManager;
import org.helm.editor.layout.utils.DirectionFinder;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.SequenceLayout;

public class EditorViewImpl implements EditorView {

	private Annotator annotator;
	private SequenceLayout _sequenceLayout;
	private Graph2D _source;
	private GraphManager _graphManager;
	
	public EditorViewImpl(Graph2D source, GraphManager manager) {
		_source = source;
		_graphManager = manager;
	}
	
	public void renderView() {
		Graph2DView parentView = new Graph2DView();
        parentView.setGraph2D(_source);

        try {
        	_sequenceLayout = new SequenceLayout(parentView, _graphManager);
        	
//        	_sequenceLayout.doLayout();

            annotator = _graphManager.getAnnotator();
            annotator.setGraph2D(_source);
            annotator.annotateAllBasePosition();
        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }

	}
	
	public NodeLabel getEditorLabel(Node editorNode) {
		return annotator.getLabel(editorNode);
	}
 
	public ChemSequenceHolder getChemSequenceHolder() {
		return _graphManager.getChemSequenceHolder();
	}
	
	public DirectionFinder getDirectionFinder() {
		return _sequenceLayout.getDirectionFinder();
	}

	public String getEditorAnnotation(Node editorNode) {
		return _graphManager.getAnnotation(editorNode);
	}

	public Graph2D getGraph() {
		return _source;
	}
	
	public boolean isFlipped(Node editorStartingNode) {
		return _graphManager.isFlipped(editorStartingNode);
	}
}

