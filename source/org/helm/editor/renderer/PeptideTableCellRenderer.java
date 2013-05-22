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

import org.helm.editor.protein.view.PeptidePolymer;

import org.helm.editor.protein.view.PeptideSequenceViewer;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A custom TableCellRenderer based on Graph2DView and complex HELM notation string
 * Could be big and slow, but can deal with hybrid structures
 * @author zhangtianhong
 */
public class PeptideTableCellRenderer extends DefaultTableCellRenderer {

    private int tickInterval;
    private int lettersPerLine;
    private int positionLabelMode;
    private PeptideSequenceViewer viewer;

    public PeptideTableCellRenderer() {
        this(PeptideSequenceViewer.DEFAULT_TICK_INTERVAL, PeptideSequenceViewer.DEFAULT_LETTERS_PER_LINE, PeptideSequenceViewer.TOP_TICK_MODE);
    }

    public PeptideTableCellRenderer(int tickInterval,int lettersPerLine, int positionLabelMode) {
        super();
        viewer = new PeptideSequenceViewer(tickInterval, lettersPerLine, positionLabelMode);
    }

    public int getLettersPerLine() {
        return lettersPerLine;
    }

    public void setLettersPerLine(int lettersPerLine) {
        this.lettersPerLine = lettersPerLine;
    }

    public int getPositionLabelMode() {
        return positionLabelMode;
    }

    public void setPositionLabelMode(int positionLabelMode) {
        this.positionLabelMode = positionLabelMode;
    }

    public int getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(int tickInterval) {
        this.tickInterval = tickInterval;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        PeptidePolymer polymer = new PeptidePolymer();
        if (value instanceof PeptidePolymer) {
            polymer = (PeptidePolymer) value;
        } else if (value instanceof String) {
            polymer = new PeptidePolymer((String) value);
        }
        viewer.setPeptidePolymer(polymer);
        return viewer;
    }

}
