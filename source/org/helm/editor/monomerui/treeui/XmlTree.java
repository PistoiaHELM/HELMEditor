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
package org.helm.editor.monomerui.treeui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdom.JDOMException;

import y.view.Graph2D;
import y.view.Graph2DView;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.editor.MonomerInfoDialog;
import org.helm.editor.monomerui.SimpleElemetFactory;
import org.helm.editor.utility.DragAndDropSupport;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.ShapedXmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlElement;
import org.helm.editor.utility.xmlparser.data.XmlFragment;
import org.helm.notation.model.Monomer;

/**
 * @author Alexander Makarov
 * 
 */
public class XmlTree extends JTree {

	private List<Polymer> data;

	private SimpleElemetFactory elementFactory;

	private MacromoleculeEditor editor;

	private Graph2DView view;

	private static final String EMPTY_STRIGN = "";

	private MonomerInfoDialog monomerInfoDialog;

	public XmlTree(List<Polymer> data, MacromoleculeEditor editor,
			Graph2DView view) {
		this.data = data;
		this.editor = editor;
		this.view = view;

		monomerInfoDialog = MonomerInfoDialog.getDialog(editor.getFrame());// new
																			// MonomerInfoDialog(editor.getFrame());
	}

	public void constructTree() throws MonomerException, IOException,
			JDOMException {

		elementFactory = SimpleElemetFactory.getInstance();

		TreeNode root = convertXmlToTree();
		setModel(new DefaultTreeModel(root));

		// monomerFactory = MonomerFactory.getInstance();

		ToolTipManager.sharedInstance().registerComponent(this);

		setUpActions();

		configTree();
	}

	private void setUpActions() {
		final DragAndDropSupport dndSupport = new DragAndDropSupport(view,
				editor);
		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				dndSupport.updateTemplatesTree(XmlTree.this);
			}
		});

		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {

				XmlTree tree = (XmlTree) e.getSource();
				TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());

				if (treePath == null) {
					return;
				}

				XmlParentNode parentNode = ((XmlParentNode) treePath
						.getPathComponent(1));

				Object lastPathComponent = treePath.getLastPathComponent();
				if (lastPathComponent instanceof XmlLeafNode
						&& e.getClickCount() == 2) {
					XmlLeafNode clickedNode = (XmlLeafNode) lastPathComponent;

					// Map<String, Map<String, Monomer>> monomerDB =
					// monomerFactory.getMonomerDB();
					Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
							.getInstance().getCombinedMonomerStore()
							.getMonomerDB();
					Monomer monomer = monomerDB.get(parentNode.getInnerName())
							.get(clickedNode.toString());

					if (monomer != null) {
						monomerInfoDialog.setMonomer(monomer);
						monomerInfoDialog.setVisible(true);
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

		});

	}

	private void configTree() {
		setRootVisible(false);
		setDragEnabled(true);

		setCellRenderer(new NodeDecorator());

		final JTree tree = this;
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TreePath path = getUI().getClosestPathForLocation(tree,
						e.getX(), e.getY());
				if (path != null) {
					if (!getUI().getPathBounds(tree, path).contains(e.getX(),
							e.getY())) {
						return;
					}
					if (isExpanded(path)) {
						collapsePath(path);
					} else {
						expandPath(path);
					}
				}
			}
		});
	}

	private TreeNode convertXmlToTree() throws MonomerException, IOException,
			JDOMException {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tree");
		Iterator<Polymer> polymerIterator = data.iterator();
		while (polymerIterator.hasNext()) {
			final Polymer currPolymer = polymerIterator.next();
			final XmlParentNode polymerNode = new XmlParentNode(
					currPolymer.getTitle(), currPolymer.getName());
			polymerNode.setColor(currPolymer.getTitleColor());

			addGroupToTree(polymerNode, currPolymer, currPolymer.getAllGroups());

			root.add(polymerNode);

			// construct "Others" node
			// Group<XmlFragment> othersGroup = currPolymer.getDataComplement();
			// final DefaultMutableTreeNode othersGroupNode = new
			// DefaultMutableTreeNode(othersGroup.getName());
			// addElementsToTree(othersGroupNode,
			// othersGroup.getGroupIterator(), currPolymer.getName(),
			// othersGroup.getShape());
			// polymerNode.add(othersGroupNode);

			// find "Others" nodes
			// Map<String, Group<XmlFragment>> othersMap =
			// currPolymer.getDataComplement();
			// Enumeration children = polymerNode.children();
			// for (String monomerType : othersMap.keySet()) {
			// boolean otherGroupFound = false;
			// while (children.hasMoreElements()) {
			// DefaultMutableTreeNode node =
			// (DefaultMutableTreeNode)(children.nextElement());
			// if
			// (((String)(node.getUserObject())).equalsIgnoreCase(monomerType))
			// {
			// Enumeration nodes = node.children();
			// while (nodes.hasMoreElements()) {
			// DefaultMutableTreeNode currentNode =
			// (DefaultMutableTreeNode)(nodes.nextElement());
			// if
			// (((String)(currentNode.getUserObject())).equalsIgnoreCase("Other"))
			// {
			// Iterator<XmlFragment> groupIterator =
			// othersMap.get(monomerType).getGroupIterator();
			// addElementsToTree(currentNode, groupIterator, currPolymer,
			// othersMap.get(monomerType).getShape(), true);
			// otherGroupFound = true;
			// break;
			// }
			// }
			// break;
			// }
			// }
			// if (! otherGroupFound) {
			// if (!othersMap.get(monomerType).getGroupIterator().hasNext()) {
			// continue;
			// }
			// DefaultMutableTreeNode othersGroupNode = new
			// DefaultMutableTreeNode("Other");
			// addElementsToTree(othersGroupNode,
			// othersMap.get(monomerType).getGroupIterator(), currPolymer,
			// othersMap.get(monomerType).getShape(), true);
			// polymerNode.add(othersGroupNode);
			// }
			// }
		}

		return root;
	}

	private <T extends XmlElement> void addGroupToTree(
			DefaultMutableTreeNode parentNode, Polymer polymer,
			List<Group<? extends T>> data) {

		String type = polymer.getName();
		// workaround for emulate multitree set
		Map<String, List<DefaultMutableTreeNode>> childGroups = new HashMap<String, List<DefaultMutableTreeNode>>();
		Map<String, DefaultMutableTreeNode> parentGroups = new HashMap<String, DefaultMutableTreeNode>();

		LinkedHashSet<String> parentSet = polymer.getParentSet(data);

		// constructing upper level hierarchy
		Iterator<Group<? extends T>> groupIterator = data.iterator();
		while (groupIterator.hasNext()) {
			Group<? extends T> currGroup = groupIterator.next();
			currGroup.sort();

			String name = currGroup.getName();

			if (name.equals("Other") && !currGroup.getGroupIterator().hasNext()) {
				continue;
			}

			DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
					currGroup.getName());

			if (!currGroup.getName().equals(EMPTY_STRIGN)) {
				addElementsToTree(groupNode, currGroup.getGroupIterator(),
						polymer, currGroup.getShape(), false);
			} else {
				addElementsToTree(parentNode, currGroup.getGroupIterator(),
						polymer, currGroup.getShape(), false);
			}

			String parentName = currGroup.getParent();
			String currGroupName = currGroup.getName();
			if (!parentName.equals(EMPTY_STRIGN)) {
				List<DefaultMutableTreeNode> childs = childGroups
						.get(parentName);
				if (childs == null) {
					childs = new ArrayList<DefaultMutableTreeNode>();
					childGroups.put(parentName, childs);
				}

				childs.add(groupNode);
			} else if (!currGroupName.equals(EMPTY_STRIGN)) {
				parentNode.add(groupNode);
			}

			if (parentSet.contains(currGroupName)) {
				parentGroups.put(currGroupName, groupNode);
			}
		}

		// added son-parent relationship
		for (String parentGroupName : childGroups.keySet()) {
			DefaultMutableTreeNode parent = parentGroups.get(parentGroupName);
			List<DefaultMutableTreeNode> childs = childGroups
					.get(parentGroupName);
			for (DefaultMutableTreeNode childNode : childs) {
				parent.add(childNode);
			}
		}

	}

	private <T extends XmlElement> void addElementsToTree(
			DefaultMutableTreeNode parentNode, Iterator<T> groupIterator,
			Polymer polymer, String shape, boolean isOther) {

		String type = polymer.getName();
		while (groupIterator.hasNext()) {
			XmlElement currElement = groupIterator.next();
			Graph2D nodeContent = null;
			try {
				if (isOther) {
					ShapedXmlFragment analog;
					try {
						analog = polymer.getNaturalAnalogFragment(currElement
								.getName());
					} catch (Exception e) {
						analog = null;
					}
					if (analog != null) {
						nodeContent = elementFactory.createNode(type,
								analog.getShape(), analog);
						currElement = analog;
					}
				}

				if (nodeContent == null) {// usual node or can't find natural
											// analog for others tab
					nodeContent = elementFactory.createNode(type, shape,
							currElement);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (nodeContent != null) {
				XmlLeafNode elementNode = new XmlLeafNode(nodeContent,
						currElement);

				parentNode.add(elementNode);
			}
		}
	}
}
