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


import java.io.IOException;
import java.util.List;
import org.jdom.JDOMException;

import y.base.Node;
import y.view.Graph2D;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphPair;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.NotationParser;

public class EditorViewModelImpl implements EditorViewModel{
	private Graph2D editor;
	private GraphManager editorGraphManager;
	
	public EditorViewModelImpl(String notation) throws NotationException, MonomerException, IOException, JDOMException, StructureException  {
		GraphPair graphPair = NotationParser.getGraphPair(notation);

		editor = graphPair.getGraph();
		editorGraphManager = graphPair.getGraphManager();
		
		Graph2NotationTranslator.updateHyperGraph(editor, editorGraphManager);
	}
	
	public boolean isEmpty() {
		return editor == null || editor.isEmpty() || 
		editorGraphManager == null || editorGraphManager.getStartingNodeList().isEmpty();
	}
	
	public Node[] getEditorGraphNodes() {
		return editor.getNodeArray();
	}
	
	public Graph2D getEditorGraph() {
		return editor;
	}
	
	public void sortEditorStartingNodes() {
		try {
			editorGraphManager.sortStartingNodeList();
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		}
	}
	
	public List<Node> getEditorStartingNodes() {
		return editorGraphManager.getStartingNodeList();
	}
	
	public boolean isFlipped(Node editorStartingNode) {
		return editorGraphManager.isFlipped(editorStartingNode);
	}
	
	public EditorViewImpl renderView() {
		EditorViewImpl view = new EditorViewImpl(editor, editorGraphManager);
		view.renderView();
		return view;
	}
}
