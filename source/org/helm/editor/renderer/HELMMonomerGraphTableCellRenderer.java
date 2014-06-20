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
package org.helm.editor.renderer;

import org.helm.editor.editor.MacroMoleculeViewer;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A custom TableCellRenderer based on Graph2DView and complex HELM notation
 * string Could be big and slow, but can deal with hybrid structures
 * 
 * @author zhangtianhong
 */
public class HELMMonomerGraphTableCellRenderer extends DefaultTableCellRenderer {

	private MacroMoleculeViewer viewer;

	public HELMMonomerGraphTableCellRenderer() {
		viewer = new MacroMoleculeViewer(false);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object notation, boolean isSelected, boolean hasFocus, int row,
			int column) {
		viewer.setNotation((String) notation);
		// int cellWidth = (int) table.getCellRect(row, column,
		// true).getWidth();
		// int cellHeight = (int) table.getCellRect(row, column,
		// true).getHeight();
		// viewer.setSize(cellWidth, cellHeight);
		// viewer.getGraph2d().fitGraph2DView();

		return viewer;
	}
}
