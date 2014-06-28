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
package org.helm.editor.componentPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author lih25
 */
public class LegendPanel extends JPanel {

	public static final Color NO_MODIFICATION_COLOR = Color.BLACK;
	public static final Color ONE_MODIFICATION_COLOR = Color.CYAN.darker()
			.darker();
	public static final Color TWO_MODIFICATION_COLOR = Color.MAGENTA;
	public static final Color MODIFIED_P_COLOR = new Color(127, 0, 255);

	public LegendPanel() {
		super();
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(200, 30));
	}

	@Override
	public void paintComponent(Graphics gfx) {

		int w = 10;
		int h = 10;

		FontMetrics fontMetrics = this.getFontMetrics(this.getFont());

		int x = (int) 10;
		int y = (int) 10;

		gfx.drawString("Number of Modifications", x, y + 10);

		x = x + fontMetrics.stringWidth("Number of Modifications:") + 20;

		gfx.setColor(NO_MODIFICATION_COLOR);
		gfx.fillRect(x, y, w, h);
		gfx.setColor(Color.BLACK);
		gfx.drawRect(x, y, w, h);

		gfx.drawString("0", x + w + 10, y + 10);

		// y = y +20;
		x = x + w + 50;
		gfx.setColor(ONE_MODIFICATION_COLOR);
		gfx.fillRect(x, y, w, h);
		gfx.setColor(Color.BLACK);
		gfx.drawRect(x, y, w, h);

		gfx.drawString("1", x + w + 10, y + 10);
		// y = y + 20;
		x = x + w + 50;
		gfx.setColor(TWO_MODIFICATION_COLOR);
		gfx.fillRect(x, y, w, h);
		gfx.setColor(Color.BLACK);
		gfx.drawRect(x, y, w, h);
		gfx.drawString("2", x + w + 10, y + 10);

		// y = y + 20;
		x = x + w + 150;
		w = w - 5;
		h = h - 5;
		gfx.setColor(MODIFIED_P_COLOR);
		gfx.fillRect(x, y + 2, w, h);
		gfx.setColor(Color.BLACK);
		gfx.drawRect(x, y + 2, w, h);
		gfx.drawString("Modified Phosphate", x + w + 10, y + 10);

	}

}
