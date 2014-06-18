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

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.helm.notation.MonomerFactory;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.utility.MonomerNodeHelper;
import org.helm.editor.utility.NucleotideNodeHelper;
import org.helm.editor.utility.xmlparser.data.XmlElement;
import org.helm.editor.utility.xmlparser.data.XmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlMonomer;
import org.helm.notation.model.Monomer;

/**
 * @author Alexander Makarov
 * 
 */
public class NodeDecorator extends DefaultTreeCellRenderer {

	private static ImageIcon closeIcon = new ImageIcon(
			NodeDecorator.class
					.getResource("/org/helm/editor/monomerui/treeui/resource/helm_arrow_right.png"));
	private static ImageIcon openIcon = new ImageIcon(
			NodeDecorator.class
					.getResource("/org/helm/editor/monomerui/treeui/resource/helm_arrow_expanded.png"));
	private static ImageIcon leafIcon = new ImageIcon(
			NodeDecorator.class
					.getResource("/org/helm/editor/monomerui/treeui/resource/helm_leaf.png"));

	public NodeDecorator() {
		setClosedIcon(closeIcon);
		setOpenIcon(openIcon);
		setLeafIcon(leafIcon);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (value instanceof XmlParentNode) {
			XmlParentNode parentNode = (XmlParentNode) value;
			Color color = parentNode.getNodeColor();
			if ((color != null) && (!color.equals(Color.BLACK))) {
				setForeground(color);
			}
		}

		if (value instanceof XmlLeafNode) {
			XmlElement dataElement = ((XmlLeafNode) value).getDataElement();

			String nodeTooltip = "";
			if (dataElement instanceof XmlFragment) {
				nodeTooltip = NucleotideNodeHelper.getTooltip(dataElement);
			}
			if (dataElement instanceof XmlMonomer) {
				try {
					XmlMonomer xmlMonomer = (XmlMonomer) dataElement;
					Monomer monomer = MonomerStoreCache.getInstance()
							.getCombinedMonomerStore()
							.getMonomerDB()
							.
							// Monomer monomer =
							// MonomerFactory.getInstance().getMonomerDB().
							get(xmlMonomer.getPolymerName())
							.get(xmlMonomer.getName());
					nodeTooltip = MonomerNodeHelper.getTooltip(monomer);
				} catch (Exception e) {
					nodeTooltip = "";
				}
			}
			setToolTipText(nodeTooltip);

			Color backgroundColor = dataElement.getBackgroundColor();

			if (backgroundColor == null || backgroundColor.equals(Color.WHITE)) {
				setForeground(dataElement.getFontColor());
			} else {
				setForeground(backgroundColor);
			}
		}

		return this;
	}

}
