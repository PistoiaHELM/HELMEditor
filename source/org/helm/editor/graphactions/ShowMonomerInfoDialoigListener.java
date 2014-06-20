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
package org.helm.editor.graphactions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;

import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.NodeRealizer;

import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.editor.MonomerInfoDialog;
import org.helm.notation.model.Monomer;

public class ShowMonomerInfoDialoigListener extends MouseAdapter {

	private static final int DOUBLE_CLICK_COUNT = 2;

	private static MonomerInfoDialog _monomerInfoDialog;

	private static MacromoleculeEditor _editor;

	private static Graph2D _currentGraph;

	public ShowMonomerInfoDialoigListener(MacromoleculeEditor editor,
			Graph2D currentGraph) {
		_editor = editor;
		_currentGraph = currentGraph;
		_monomerInfoDialog = MonomerInfoDialog.getDialog(_editor.getFrame());// new
																				// MonomerInfoDialog(_editor.getFrame());
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (event.getClickCount() == DOUBLE_CLICK_COUNT) {
			try {
				JList list = (JList) event.getSource();
				int index = list.getSelectedIndex();
				Node currNode = getTemplateFromTextValue(index, list);

				NodeMap nodeMap = (NodeMap) _currentGraph
						.getDataProvider(NodeMapKeys.MONOMER_REF);
				MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(currNode);

				/*
				 * MonomerFactory monomerFactory; monomerFactory =
				 * MonomerFactory.getInstance(); Map<String, Map<String,
				 * Monomer>> monomerDB = monomerFactory.getMonomerDB();
				 */
				Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
						.getInstance().getCombinedMonomerStore().getMonomerDB();

				Monomer monomer = monomerDB.get(monomerInfo.getPolymerType())
						.get(monomerInfo.getMonomerID());
				if (monomer != null) {
					// if this is monomer
					_monomerInfoDialog.setMonomer(monomer);

					_monomerInfoDialog.setVisible(true);
				}
			} catch (Exception ex) {
				Logger.getLogger(ShowMonomerInfoDialoigListener.class.getName())
						.log(Level.INFO, "Unable to display monomer info");
			}
		}
	}

	private Node getTemplateFromTextValue(int index, JList templatesList) {

		try {
			return ((NodeRealizer) templatesList.getModel().getElementAt(index))
					.getNode();
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

}
