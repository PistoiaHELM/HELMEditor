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

import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewController;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewControllerImpl;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewLayout;

import org.helm.editor.data.RNAPolymer;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A custom TableCellRenderer based on Graph2DView and complex HELM notation string
 * Could be big and slow, but can deal with hybrid structures
 * @author zhangtianhong
 */
public class HELMGraphTableCellRenderer extends DefaultTableCellRenderer {

    public static final int DEFAULT_VIEW_TYPE = 10;
    public static final int MINIMAL_GAP_VIEW_TYPE = 11;
    private SequenceViewController _sequenceViewController;
    private int viewType;
    private int alignment;

    public HELMGraphTableCellRenderer() {
        viewType = MINIMAL_GAP_VIEW_TYPE;
        alignment = SequenceViewLayout.LEFT_ALIGNMENT;

        configureView();
    }

    public HELMGraphTableCellRenderer(int viewType, int alignment) {
        super();

        this.viewType = viewType;
        this.alignment = alignment;

        configureView();
    }

    public void refreshData() {
        _sequenceViewController.refreshSequenceView();
    }

    public Component getTableCellRendererComponent(JTable table, Object notation,
            boolean isSelected, boolean hasFocus, int row, int column) {

        int cellWidth = (int) table.getCellRect(row, column, true).getWidth();
        int cellHeight = (int) table.getCellRect(row, column, true).getHeight();
        _sequenceViewController.getView().setSize(cellWidth, cellHeight);

        if (notation instanceof RNAPolymer) {
            RNAPolymer oligo = (RNAPolymer) notation;
            notation = oligo.getNotation();
        }
        try {
            _sequenceViewController.setNotation((String) notation);
//            _sequenceViewController.updateAlignment();
        } catch (Exception ex) {
            _sequenceViewController.appendErrorNode();
        }
        _sequenceViewController.updateAlignment();

        return _sequenceViewController.getSequenceView();
    }

    private void configureView() {
        _sequenceViewController = new SequenceViewControllerImpl();
        _sequenceViewController.setLayoutMode(true);

        if (viewType == MINIMAL_GAP_VIEW_TYPE) {
            _sequenceViewController.minimizeGaps();
        }

        _sequenceViewController.setAligment(alignment);
    }
}
