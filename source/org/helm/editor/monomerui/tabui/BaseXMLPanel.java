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
package org.helm.editor.monomerui.tabui;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.JDOMException;

import y.util.GraphCopier;
import y.view.Graph2D;
import y.view.Graph2DView;

import org.helm.notation.MonomerException;
import org.helm.editor.data.DataRegistry;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.graphactions.ShowMonomerInfoDialoigListener;
import org.helm.editor.monomerui.SimpleElemetFactory;
import org.helm.editor.utility.DragAndDropSupport;
import org.helm.editor.utility.ListNodeRealizerCellRenderer;
import org.helm.editor.utility.TemplatesModel;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.ShapedXmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlElement;

/**
 * Representation of simple ui panel
 * 
 * @author Alexander Makarov
 */
public class BaseXMLPanel<T extends XmlElement> extends JPanel {

	private Group<T> data;
	private Graph2D templatesOwner;

	private JList elementsList;
	private TemplatesModel templatesModel;

	private Graph2DView view;
	private MacromoleculeEditor editor;

	private String type;

	/**
	 * Initialization of simple panel in left tabbed panel
	 * 
	 * @param data
	 * @param type
	 * @param view
	 * @param editor
	 * @throws MonomerException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public BaseXMLPanel(Group<T> data, String type, Graph2DView view,
			MacromoleculeEditor editor) throws MonomerException, IOException,
			JDOMException {

		this.data = data;
		this.view = view;
		this.editor = editor;

		this.type = type;

		templatesOwner = new Graph2D();

		createPanelCore();
	}

	/**
	 * Clear selection data
	 */
	public void clearSelection() {
		elementsList.clearSelection();
	}

	/**
	 * Update current panel
	 * 
	 * @throws MonomerException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public void updatePanel() throws MonomerException, IOException,
			JDOMException {
		templatesOwner.clear();

		createPanelCore();

		revalidate();
		repaint();
	}

	private void createPanelCore() throws MonomerException, IOException,
			JDOMException {
		initTemplatesModel();
		initUI();
		setUpActions();
	}

	private void setUpActions() {
		final DragAndDropSupport dndSupport = new DragAndDropSupport(view,
				editor);
		elementsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				dndSupport.updateTemplatesList(elementsList);
			}
		});

		elementsList.addMouseListener(new ShowMonomerInfoDialoigListener(
				editor, view.getGraph2D()));

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				clearSelection();
			}

		});
	}

	private void initUI() {

		templatesModel = new TemplatesModel(templatesOwner);
		elementsList = new JList(templatesModel);

		DataRegistry.getInstance().registerTemplateList(data.getName(),
				templatesModel);

		elementsList.setCellRenderer(new ListNodeRealizerCellRenderer());
		elementsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		elementsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		elementsList.setVisibleRowCount(-1);
		elementsList.setSelectedIndex(-1);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Box.createRigidArea(new Dimension(0, 2)));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(new JScrollPane(elementsList));
	}

	private void initTemplatesModel() throws MonomerException, IOException,
			JDOMException {
		GraphCopier copier = new GraphCopier(
				templatesOwner.getGraphCopyFactory());

		copier.setDataProviderContentCopying(true);
		copier.setEdgeMapCopying(true);
		copier.setNodeMapCopying(true);
		templatesOwner.addDataProvider(NodeMapKeys.MONOMER_REF,
				templatesOwner.createNodeMap());

		SimpleElemetFactory factory = SimpleElemetFactory.getInstance();
		Iterator<T> dataIterator = data.getGroupIterator();

		while (dataIterator.hasNext()) {
			T currElement = dataIterator.next();
			Graph2D node = null;
			try {
				if (currElement instanceof ShapedXmlFragment) {
					node = factory.createNode(type,
							((ShapedXmlFragment) currElement).getShape(),
							currElement);
				} else {
					node = factory.createNode(type, data.getShape(),
							currElement);
				}
			} catch (MonomerException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (JDOMException ex) {
				ex.printStackTrace();
			}

			if (node != null) {
				copier.copy(node, templatesOwner);
			}
		}

	}

}
