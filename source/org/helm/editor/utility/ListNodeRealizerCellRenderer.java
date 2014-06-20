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
package org.helm.editor.utility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import y.base.Node;
import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 *
 * @author lih25
 */
/**
 * ListCellRenderer implementation that handles NodeRealizer instances. Used
 * internally by {@link DragAndDropSupport} and publicly available for others.
 */
public final class ListNodeRealizerCellRenderer implements ListCellRenderer {

	private DefaultListCellRenderer renderer = new DefaultListCellRenderer();
	private NodeRealizerIcon icon = new NodeRealizerIcon();

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) renderer.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		icon.setRealizer((NodeRealizer) value);
		String config = "";
		label.setText("");
		if (value instanceof GenericNodeRealizer) {
			config = ((GenericNodeRealizer) value).getLabelText();
			label.setToolTipText("draw a " + config + " node");
		} else if (value instanceof GroupNodeRealizer) {
			Node node = ((GroupNodeRealizer) value).getNode();
			String nucleotideTooltip = NucleotideNodeHelper.getTooltip(node);
			label.setToolTipText(nucleotideTooltip);
		} else if (value instanceof ShapeNodeRealizer) {
			Node node = ((ShapeNodeRealizer) value).getNode();
			String monomerNodeTooltip = MonomerNodeHelper.getTooltip(node);
			label.setToolTipText(monomerNodeTooltip);
		} else {
			label.setText("");
			label.setToolTipText("");
		}
		label.setIcon(icon);
		return label;
	}

	/**
	 * Icon implementation that renders a NodeRealizer
	 */
	public static final class NodeRealizerIcon implements Icon {

		private static final int inset = 10;
		private NodeRealizer realizer;
		private static final int borderWidth = 2;
		private static int defaultWidth = 30;
		private static int defaultHeight = 30;

		public void setRealizer(NodeRealizer realizer) {
			this.realizer = realizer;
		}

		public int getIconWidth() {
			if (realizer instanceof GroupNodeRealizer) {
				return (defaultWidth + inset);
			} else {
				return (int) (realizer.getWidth() + inset);
			}
		}

		public int getIconHeight() {
			if (realizer instanceof GroupNodeRealizer) {
				return (defaultHeight + inset);
			} else {
				return (int) (realizer.getHeight() + inset);
			}
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			realizer.setLocation(x + inset * 0.5d, y + inset * 0.5d);
			g = g.create();
			Font font = g.getFont();
			try {
				final Graphics2D gfx = (Graphics2D) g;
				if (realizer instanceof GroupNodeRealizer) {

					Font labelFont = font.deriveFont(Font.BOLD, 18);

					gfx.setFont(labelFont);
					FontMetrics fm = gfx.getFontMetrics();

					String text = realizer.getLabelText();

					int stringW = fm.stringWidth(text);
					int stringH = fm.getHeight();

					int xPosition = (int) (getIconWidth() - stringW) / 2;
					int yPosition = (getIconHeight() - 10) < stringH ? stringH
							: (getIconHeight() - 10);

					gfx.drawString(text, xPosition, yPosition);

					gfx.setColor(Color.DARK_GRAY);
					gfx.drawString(text, ShiftWest(xPosition, 1),
							ShiftNorth(yPosition, 1));
					gfx.drawString(text, ShiftWest(xPosition, 1),
							ShiftSouth(yPosition, 1));
					gfx.drawString(text, ShiftEast(xPosition, 1),
							ShiftNorth(yPosition, 1));
					gfx.drawString(text, ShiftEast(xPosition, 1),
							ShiftSouth(yPosition, 1));
					gfx.setColor(realizer.getFillColor());
					gfx.drawString(text, xPosition, yPosition);

				} else {
					realizer.paint(gfx);
				}
			} finally {
				g.dispose();
			}
		}

		// methods assist drawing the outline of a font
		private int ShiftNorth(int p, int distance) {
			return (p - distance);
		}

		int ShiftSouth(int p, int distance) {
			return (p + distance);
		}

		int ShiftEast(int p, int distance) {
			return (p + distance);
		}

		int ShiftWest(int p, int distance) {
			return (p - distance);
		}
	}
}
