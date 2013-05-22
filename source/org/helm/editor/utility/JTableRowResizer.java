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

/**
 *
 * @author lih25
 */
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
 
public class JTableRowResizer extends MouseInputAdapter
{ 
    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR); 
 
    private int mouseYOffset, resizingRow; 
    private Cursor otherCursor = resizeCursor; 
    private JTable table; 
 
    public JTableRowResizer(JTable table){ 
        this.table = table; 
        table.addMouseListener(this); 
        table.addMouseMotionListener(this); 
    } 
 
    private int getResizingRow(Point p){ 
        return getResizingRow(p, table.rowAtPoint(p)); 
    } 
 
    private int getResizingRow(Point p, int row){ 
        if(row == -1){ 
            return -1; 
        } 
        int col = table.columnAtPoint(p); 
        if(col==-1) 
            return -1; 
        Rectangle r = table.getCellRect(row, col, true); 
        r.grow(0, -3); 
        if(r.contains(p)) 
            return -1; 
 
        int midPoint = r.y + r.height / 2; 
        int rowIndex = (p.y < midPoint) ? row - 1 : row; 
 
        return rowIndex; 
    } 
 
    public void mousePressed(MouseEvent e){ 
        Point p = e.getPoint(); 
 
        resizingRow = getResizingRow(p); 
        mouseYOffset = p.y - table.getRowHeight(resizingRow); 
    } 
 
    private void swapCursor(){ 
        Cursor tmp = table.getCursor(); 
        table.setCursor(otherCursor); 
        otherCursor = tmp; 
    }
 
    public void mouseMoved(MouseEvent e){
        if((getResizingRow(e.getPoint())>=0) != (table.getCursor() == resizeCursor)){
            swapCursor();
        }
    }
 
    public void mouseDragged(MouseEvent e){
    	int mouseY = e.getY();
 
    	if(resizingRow >= 0){
            int newHeight = mouseY - mouseYOffset;
            if(newHeight > 0)
                table.setRowHeight(resizingRow, newHeight);
        }
    }
}

