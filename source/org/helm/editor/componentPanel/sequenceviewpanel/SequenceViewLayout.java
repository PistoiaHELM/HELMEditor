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
import java.io.IOException;

import javax.swing.JPopupMenu;

import org.jdom.JDOMException;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.editor.EditorViewModel;

import y.view.Graph2DView;

public interface SequenceViewLayout {

	// possible values for aligment variable
	public static final int LEFT_ALIGNMENT = 0;
	public static final int CENTRAL_ALIGNMENT = 1;
	public static final int RIGHT_ALIGNMENT = 2;

	Graph2DView getGraph2DView();

	Image toImage() throws NotationException, MonomerException, IOException,
			JDOMException;

	Image toImage(int width, int hight) throws NotationException,
			MonomerException, IOException, JDOMException, StructureException;

	Graph2DView copyGraph2DView();

	void reset();

	void initView() throws NotationException, JDOMException, MonomerException,
			IOException;

	void replaceView(Graph2DView view);

	void refreshLayout();

	void updateAlignment();

	double getBoundOffset();

	void minimizeGaps();

	void appendErrorNode();

	void setAlignment(int viewType);

	void setLayoutMode(boolean mode);

	void setComposteFlag(boolean compositeFlag);

	void setupEditorModel(EditorViewModel editorModel)
			throws NotationException, JDOMException, MonomerException,
			IOException;

	JPopupMenu getPopupMenu();
}
