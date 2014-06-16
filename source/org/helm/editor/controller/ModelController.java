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
package org.helm.editor.controller;

import java.util.ArrayList;
import java.util.List;

import org.helm.editor.data.NotationUpdateEvent;
import org.helm.editor.data.DataListener;

/**
 * This class controls the structure rendering in MacromoleculeEditor,
 * MacroMoleculeViewer and SequenceViewerPanes via NotationUpdateEvent, which
 * contains the new notation and owner code who fires the event. It is up to
 * each listener to determine if the event should be handled.
 * 
 * @author zhangtianhong
 */
public class ModelController {

	private static ModelController instance;
	private List<DataListener> listeners = new ArrayList<DataListener>();

	private ModelController() {
	}

	public static ModelController getInstance() {
		if (instance == null) {
			instance = new ModelController();
		}
		return instance;
	}

	public void onEvent(NotationUpdateEvent e) {
		for (DataListener listener : listeners) {
			listener.onUpdate(e);
		}
	}

	public static void notationUpdated(String notation, String owner) {
		NotationUpdateEvent e = new NotationUpdateEvent(notation, owner);
		getInstance().onEvent(e);
	}

	public void registerListener(DataListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(DataListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
}
