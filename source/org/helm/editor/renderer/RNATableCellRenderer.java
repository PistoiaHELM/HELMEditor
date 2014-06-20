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

import org.helm.editor.data.RNAPolymer;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.BitSet;
import java.util.HashMap;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Extracted from PFRED by Simon Xi; Useful for display of RNA polymers only,
 * can only render RNAPolymer
 * 
 * @author zhangtianhong
 */
public class RNATableCellRenderer extends DefaultTableCellRenderer {

	public static String NAME = "SIRNA_Cell_Renderer";
	public static int ORIG_CLIP_HEIGHT = -1;
	public final static double MIN_SEQ_POSITION_XAXIS_SCALING = 0.50357;
	private RNAPolymer oligo = null;
	private Rectangle cell_rect = null;
	private String error_message = "Non-RNA structure";
	static Color light_blue = new Color(2, 148, 250);
	static Color dark_green = new Color(41, 135, 55);
	static Color brown = new Color(238, 149, 18);
	static Font lucidiaFont = new Font("Lucida Console", Font.PLAIN, 15);
	static Font arialFont = new Font("Arial Unicode MS", Font.PLAIN, 8);
	static Font lucidiaSmallFont = new Font("Lucida Console", Font.PLAIN, 12);
	static int PADDING = 2;
	private int displayMode = 2;
	public final static int SIMPLE_BLOCK_DISPLAY_MODE = 1;
	public final static int LETTER_DISPLAY_MODE = 2;
	public final static int ENHANCED_BLOCK_DISPLAY_MODE = 3;
	public static HashMap<String, Color> defaultColorMap = new HashMap();
	private static HashMap<String, Color> currColorMap = new HashMap();// potential
																		// not
																		// thread-safe
	public static final int DEFAULT_VIEW_TYPE = 10;
	public static final int MINIMAL_GAP_VIEW_TYPE = 11;
	// private int viewType;
	// private int alignment;

	static {
		defaultColorMap.put("R", Color.blue);
		defaultColorMap.put("dR", new Color(204, 204, 255)); // new
																// Color(255,204,204)
		defaultColorMap.put("RGNA", Color.red);//
		defaultColorMap.put("LR", Color.black);
		defaultColorMap.put("cR", new Color(255, 102, 102));
		defaultColorMap.put("FR", new Color(255, 255, 204));
		defaultColorMap.put("LR", new Color(255, 204, 102));
		defaultColorMap.put("MOE", new Color(51, 153, 255));

		defaultColorMap.put("Mph", new Color(153, 153, 0));
		defaultColorMap.put("mR", new Color(153, 0, 153));
		defaultColorMap.put("SGNA", new Color(204, 255, 204));
		defaultColorMap.put("tnR", new Color(255, 204, 204));
		defaultColorMap.put("tR", Color.green);
		defaultColorMap.put("PONA", new Color(255, 0, 255));
	}

	public RNATableCellRenderer() {
		setBackground(Color.white);
	}

	public void setDisplayMode(int mode) {
		this.displayMode = mode;
	}

	public int getDisplayMode() {
		return displayMode;
	}

	public HashMap<String, Color> getColorMap() {
		return defaultColorMap;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		oligo = null;
		cell_rect = null;

		if (value instanceof RNAPolymer) {
			oligo = (RNAPolymer) value;
		} else {
			if (value instanceof String) {
				oligo = new RNAPolymer((String) value);
			} else {
				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			}
		}

		cell_rect = table.getCellRect(row, column, true);

		return this;

	}

	@Override
	public void paint(Graphics gObject) {
		super.paint(gObject);
		if (null == cell_rect || null == oligo) {
			return;
		}

		if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE
				|| displayMode == LETTER_DISPLAY_MODE
				|| displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {

			gObject.clearRect(cell_rect.x, cell_rect.y, cell_rect.width,
					cell_rect.height);
			cell_rect.x = 0;
			cell_rect.y = 0;
			gObject.setFont(lucidiaFont);

			long start = System.currentTimeMillis();
			switch (oligo.getDataMode()) {
			case RNAPolymer.EMPTY_DATA_MODE:
				break;

			case RNAPolymer.VALID_DATA_MODE:

				if (oligo.getNumberOfStrands() == 2) {
					if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE) {
						paintDoubleStrandBlocks(gObject, oligo, cell_rect);
					} else if (displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {
						paintEnhancedDoubleStrandBlocks(gObject, oligo,
								cell_rect);
					} else {
						paintDoubleStrand(gObject, oligo, cell_rect);
					}
				} else {
					if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE) {
						paintSingleStrandBlocks(gObject, oligo, cell_rect);
					} else if (displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {
						paintEnhancedSingleStrandBlocks(gObject, oligo,
								cell_rect);
					} else {
						paintSingleStrand(gObject, oligo, cell_rect);
					}
				}

				break;

			case RNAPolymer.INVALID_DATA_MODE:
				paintErrorMessage(gObject, error_message, cell_rect);
				break;

			default:
				break;
			}

			long end = System.currentTimeMillis();
		}
		// System.err.println("Render Pfred RNA Structure took " + (end - start)
		// + " msecs");
	}

	public void paint(Graphics gObject, RNAPolymer o, Rectangle r, int mode) {

		super.paint(gObject);
		if (null == r || null == o) {
			return;
		}

		if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE
				|| displayMode == LETTER_DISPLAY_MODE
				|| displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {

			gObject.clearRect(r.x, r.y, r.width, r.height);
			gObject.setFont(lucidiaFont);

			switch (o.getDataMode()) {
			case RNAPolymer.EMPTY_DATA_MODE:
				break;

			case RNAPolymer.VALID_DATA_MODE:

				if (o.getNumberOfStrands() == 2) {
					if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE) {
						paintDoubleStrandBlocks(gObject, o, r);
					} else if (displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {
						paintEnhancedDoubleStrandBlocks(gObject, o, r);
					} else {
						paintDoubleStrand(gObject, o, r);
					}
				} else {
					if (displayMode == SIMPLE_BLOCK_DISPLAY_MODE) {
						paintSingleStrandBlocks(gObject, o, r);
					} else if (displayMode == ENHANCED_BLOCK_DISPLAY_MODE) {
						paintEnhancedSingleStrandBlocks(gObject, o, r);
					} else {
						paintSingleStrand(gObject, o, r);
					}
				}

				break;

			case RNAPolymer.INVALID_DATA_MODE:
				paintErrorMessage(gObject, error_message, r);
				break;

			default:
				break;
			}
		}
	}

	private static void paintErrorMessage(Graphics gObject, String message,
			Rectangle clip) {
		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2DObject.setColor(Color.cyan);
		g2DObject.setFont(lucidiaFont);

		FontMetrics metrics = g2DObject.getFontMetrics(g2DObject.getFont());
		int width = metrics.stringWidth("A") + PADDING;
		int height = metrics.getMaxAscent();

		int start_x = clip.x + 5;
		int start_y = clip.y + clip.height / 2 + height / 2;

		g2DObject.drawString(message, start_x, start_y);

	}

	private static void paintDoubleStrand(Graphics gObject, RNAPolymer oligo,
			Rectangle clip) {
		// Paint the oligo graphics using the cell rectangle size
		// instead of the clip area

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		FontMetrics metrics = g2DObject.getFontMetrics(g2DObject.getFont());
		int width = metrics.stringWidth("A") + PADDING;
		int height = metrics.getMaxAscent();

		int[][] pairing = oligo.getBasePairing();
		if (pairing == null) {
			// it could be that it is not specified in the RNA notation, just
			// quit on it for now
			return;
		}

		int top_length = oligo.getLength(0);
		int[] top_overhangs = getOverHangLength(pairing, top_length, 0);

		int bottom_length = oligo.getLength(1);
		int[] bottom_overhangs = getOverHangLength(pairing, bottom_length, 1);

		// ******************* draw the forward strand
		// ***************************/
		int left_margin = 5;
		String seq = oligo.getSingleLetterSeq(0);
		if (seq == null) {
			return;
		}
		int top_start_x = (top_overhangs[0] >= bottom_overhangs[1]) ? 0
				: (bottom_overhangs[1] - top_overhangs[0]);
		top_start_x = clip.x + left_margin + top_start_x * width;
		int start_y = clip.y + clip.height / 2 - 10;

		drawSequence(g2DObject, seq, top_start_x, start_y, width, height, true);
		// draw the forward position labels
		drawPosition(g2DObject, seq.length(), top_start_x, (int) (start_y
				- height - 4), width, true);
		// highlight modified base
		BitSet modifiedBaseOrSugarPos = oligo.getModifiedBaseOrSugarPos(0);
		drawModifiedBaseOrSugar(g2DObject, seq.length(),
				modifiedBaseOrSugarPos, top_start_x,
				(int) (start_y - height - 3), width, true);
		// draw modified phosphate
		BitSet modifiedPhosphatePos = oligo.getModifiedPhophatePos(0);
		drawModifiedPhosphate(g2DObject, seq.length(), modifiedPhosphatePos,
				top_start_x, (int) (start_y - height / 2), width, true);

		// ******************* draw the reverse strand
		// ***************************/
		seq = oligo.getSingleLetterSeq(1);
		if (seq == null) {
			return;
		}
		int bottom_start_x = (top_overhangs[0] <= bottom_overhangs[1]) ? 0
				: (top_overhangs[0] - bottom_overhangs[1]);
		bottom_start_x = clip.x + left_margin + bottom_start_x * width;
		start_y = clip.y + clip.height / 2 + height;

		drawSequence(g2DObject, seq, bottom_start_x, start_y, width, height,
				false);
		drawPosition(g2DObject, seq.length(), bottom_start_x, (int) (start_y
				+ height + 2), width, false);

		modifiedBaseOrSugarPos = oligo.getModifiedBaseOrSugarPos(1);
		drawModifiedBaseOrSugar(g2DObject, seq.length(),
				modifiedBaseOrSugarPos, bottom_start_x, (int) (start_y) + 3,
				width, false);

		// draw modified phosphate
		modifiedPhosphatePos = oligo.getModifiedPhophatePos(1);
		drawModifiedPhosphate(g2DObject, seq.length(), modifiedPhosphatePos,
				bottom_start_x, (int) (start_y - height / 2), width, false);

		// draw the base pairing
		int top_y = clip.y + clip.height / 2 - 8;
		int bottom_y = clip.y + clip.height / 2 - 2;
		g2DObject.setColor(Color.black);

		for (int i = 0; i < pairing.length; i++) {
			int top_x = clip.x + top_start_x + pairing[i][0] * width + width
					/ 2;
			int bottom_x = clip.x + bottom_start_x
					+ (bottom_length - pairing[i][1] - 1) * width + width / 2;
			g2DObject.drawLine(top_x, top_y, bottom_x, bottom_y);
		}

	}

	private static int[] getOverHangLength(int[][] basepairing, int length,
			int strand) {
		// find the min and max base paired position
		int min = 0;
		int max = 0;
		for (int i = 0; i < basepairing.length; i++) {
			if (min > basepairing[i][strand]) {
				min = basepairing[i][strand];
			}
			if (max < basepairing[i][strand]) {
				max = basepairing[i][strand];
			}
		}
		int[] overhangs = new int[2];
		overhangs[0] = min;
		overhangs[1] = length - max - 1;
		return overhangs;
	}

	private static void drawSequence(Graphics2D g2DObject, String seq,
			int start_x, int start_y, int width, int height,
			boolean drawLeft2Right) {
		g2DObject.setFont(lucidiaFont);

		int size = seq.length();

		if (!drawLeft2Right) {
			StringBuffer buffer = new StringBuffer(seq);
			seq = buffer.reverse().toString();
		}

		for (int i = 0; i < size; i++) {
			char c = seq.charAt(i);
			Color color = Color.black;
			switch (c) {
			case 'A':
			case 'a':
				color = dark_green;
				break;
			case 'T':
			case 't':
			case 'U':
			case 'u':
				color = Color.blue;
				break;
			case 'C':
			case 'c':
				color = Color.red;
				break;
			case 'G':
			case 'g':
				color = brown;
				break;
			}

			// Set oligo text color and placing
			g2DObject.setColor(color);

			// Position of oligo text on y-axis is dependent on the height
			// of the clipping rectangle, height of the text and the scaling
			// factor
			// This position places the text in the middle of the cell on the
			// y-axis
			int yTextPosition = start_y;
			int xTextPosition = start_x + width * i;

			g2DObject.drawChars(new char[] { c }, 0, 1, xTextPosition,
					(int) (yTextPosition));

			// Set sequence position color and placing
			g2DObject.setColor(Color.black);
		}
	}

	private static void paintSingleStrand(Graphics gObject, RNAPolymer oligo,
			Rectangle clip) {

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		FontMetrics metrics = g2DObject.getFontMetrics(g2DObject.getFont());
		int width = metrics.stringWidth("A") + PADDING;
		int height = metrics.getMaxAscent();

		String seq = oligo.getSingleLetterSeq();
		if (seq == null) {
			return;
		}

		int start_x = clip.x + 5;
		int start_y = clip.y + clip.height / 2 + height / 2;

		drawSequence(g2DObject, seq, start_x, start_y, width, height, true);

		// draw the positions
		drawPosition(g2DObject, seq.length(), start_x,
				(int) (start_y + height), width, true);

		// draw modified base
		BitSet modifiedBaseOrSugarPos = oligo.getModifiedBaseOrSugarPos(0);
		drawModifiedBaseOrSugar(g2DObject, seq.length(),
				modifiedBaseOrSugarPos, start_x, (int) (start_y - height - 3),
				width, true);

		// draw modified phosphate
		BitSet modifiedPhosphatePos = oligo.getModifiedPhophatePos(0);
		drawModifiedPhosphate(g2DObject, seq.length(), modifiedPhosphatePos,
				start_x, (int) (start_y - height / 2), width, true);
	}

	private static void drawPosition(Graphics2D g2DObject, int len,
			int start_x, int start_y, int width, boolean drawLeft2Right) {
		g2DObject.setFont(arialFont);

		g2DObject.setColor(Color.DARK_GRAY);
		g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// g2DObject.scale(xscale, yscale);

		for (int i = 0; i < len; i++) {
			// Spaces between single digit numbers and double digit
			// numbers is different
			int pos = i + 1;
			if (!drawLeft2Right) {
				pos = len - i;
			}

			int width_ctr = start_x + width * i + 3;
			if (pos >= 10) {
				width_ctr = start_x + width * i;
			}

			// Draw
			g2DObject.drawString(Integer.toString(pos), (float) (width_ctr),
					(int) (start_y));

		}

		// g2DObject.setTransform(at_temp);

	}

	private static void drawModifiedBaseOrSugar(Graphics2D g2DObject, int len,
			BitSet modifiedPos, int start_x, int start_y, int width,
			boolean drawLeft2Right) {

		g2DObject.setColor(Color.magenta);
		int size = modifiedPos.size();
		for (int i = 0; i < len && i < size; i++) {
			// Spaces between single digit numbers and double digit
			// numbers is different
			int pos = i;
			if (!drawLeft2Right) {
				pos = len - i - 1;
			}

			int width_ctr = start_x + width * pos + width / 2 - 2;

			// Draw
			if (modifiedPos.get(i)) {
				g2DObject.fillOval(width_ctr, start_y, 4, 4);
			}

		}
	}

	private static void drawModifiedPhosphate(Graphics2D g2DObject, int len,
			BitSet modifiedPos, int start_x, int start_y, int width,
			boolean drawLeft2Right) {
		// g2DObject.setFont(arialFont);

		g2DObject.setColor(Color.magenta);
		// g2DObject.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		// g2DObject.scale(xscale, yscale);
		int size = modifiedPos.length();
		for (int i = 0; i < len && i < size; i++) {
			// Spaces between single digit numbers and double digit
			// numbers is different
			int pos = i;
			if (!drawLeft2Right) {
				pos = len - i - 1;
			}

			int width_ctr = start_x + width * pos;
			if (!drawLeft2Right) {
				width_ctr = width_ctr - 3;
			} else {
				width_ctr = width_ctr + width - 3;
			}

			// Draw
			if (modifiedPos.get(i)) {
				g2DObject.fillOval(width_ctr, start_y, 4, 4);
			}

		}
	}

	private void paintDoubleStrandBlocks(Graphics gObject, RNAPolymer oligo,
			Rectangle clip) {

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		String[] codes = oligo.getSugarCodes(0);
		String[] reverse_code = oligo.getSugarCodes(1);
		if (codes == null || reverse_code == null) {
			return;
		}

		// /// block size ////////
		int block_width = 7;
		int block_height = 7;

		// /// calculate the overhang length ////////
		int[][] pairing = oligo.getBasePairing();
		if (pairing == null) {
			// it could be that it is not specified in the RNA notation, just
			// quit on it for now
			return;
		}

		int top_length = oligo.getLength(0);
		int[] top_overhangs = getOverHangLength(pairing, top_length, 0);

		int bottom_length = oligo.getLength(1);
		int[] bottom_overhangs = getOverHangLength(pairing, bottom_length, 1);

		int left_margin = 5;
		String seq = oligo.getSingleLetterSeq(0);
		if (seq == null) {
			return;
		}
		int top_start_x = (top_overhangs[0] >= bottom_overhangs[1]) ? 0
				: (bottom_overhangs[1] - top_overhangs[0]);
		top_start_x = clip.x + left_margin + top_start_x * (block_width + 1);
		int start_y = clip.y + clip.height / 2 - (block_height + 1);

		// draw forward strand
		drawBlock(g2DObject, codes, currColorMap, top_start_x, start_y,
				block_width, block_height, true);

		// draw backward strand
		int bottom_start_x = (top_overhangs[0] <= bottom_overhangs[1]) ? 0
				: (top_overhangs[0] - bottom_overhangs[1]);
		bottom_start_x = clip.x + left_margin + bottom_start_x
				* (block_width + 1);

		start_y = clip.y + clip.height / 2;
		drawBlock(g2DObject, reverse_code, currColorMap, bottom_start_x,
				start_y, block_width, block_height, false);
	}

	private void paintEnhancedDoubleStrandBlocks(Graphics gObject,
			RNAPolymer oligo, Rectangle clip) {

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		String[] codes = oligo.getSugarCodes(0);
		String[] reverse_code = oligo.getSugarCodes(1);

		BitSet modifiedPhosphatePos = oligo.getModifiedPhophatePos(0);
		BitSet modifiedBasePos = oligo.getModifiedBasePos(0);

		BitSet reverse_modifiedPhosphatePos = oligo.getModifiedPhophatePos(1);
		BitSet reverse_modifiedBasePos = oligo.getModifiedBasePos(1);

		if (codes == null || reverse_code == null
				|| modifiedPhosphatePos == null || modifiedBasePos == null
				|| reverse_modifiedPhosphatePos == null
				|| reverse_modifiedBasePos == null) {
			return;
		}

		// /// block size ////////
		int block_width = 7;
		int block_height = 7;

		// /// calculate the overhang length ////////
		int[][] pairing = oligo.getBasePairing();
		if (pairing == null) {
			// it could be that it is not specified in the RNA notation, just
			// quit on it for now
			return;
		}

		int top_length = oligo.getLength(0);
		int[] top_overhangs = getOverHangLength(pairing, top_length, 0);

		int bottom_length = oligo.getLength(1);
		int[] bottom_overhangs = getOverHangLength(pairing, bottom_length, 1);

		int left_margin = 5;
		String seq = oligo.getSingleLetterSeq(0);
		if (seq == null) {
			return;
		}
		int top_start_x = (top_overhangs[0] >= bottom_overhangs[1]) ? 0
				: (bottom_overhangs[1] - top_overhangs[0]);
		top_start_x = clip.x + left_margin + top_start_x * (block_width + 1);
		// int start_y = clip.y + clip.height / 2 - (block_height + 1);
		int start_y = clip.y + clip.height / 2 - (block_height + 1)
				- (block_height / 2);

		// draw forward strand
		drawEnhancedBlock(g2DObject, codes, modifiedPhosphatePos,
				modifiedBasePos, currColorMap, top_start_x, start_y,
				block_width, block_height, true, false);

		// draw backward strand
		int bottom_start_x = (top_overhangs[0] <= bottom_overhangs[1]) ? 0
				: (top_overhangs[0] - bottom_overhangs[1]);
		bottom_start_x = clip.x + left_margin + bottom_start_x
				* (block_width + 1);

		// start_y = clip.y + clip.height / 2;
		start_y = clip.y + clip.height / 2 + (block_height / 2);
		drawEnhancedBlock(g2DObject, reverse_code,
				reverse_modifiedPhosphatePos, reverse_modifiedBasePos,
				currColorMap, bottom_start_x, start_y, block_width,
				block_height, false, true);

	}

	private void paintEnhancedSingleStrandBlocks(Graphics gObject,
			RNAPolymer oligo, Rectangle clip) {

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		String[] codes = oligo.getSugarCodes(0);
		BitSet modifiedPhosphatePos = oligo.getModifiedPhophatePos(0);
		BitSet modifiedBasePos = oligo.getModifiedBasePos(0);
		if (codes == null || modifiedPhosphatePos == null
				|| modifiedBasePos == null) {
			return;
		}

		int start_x = clip.x + 5;
		int block_width = 7;
		int block_height = 7;
		int start_y = clip.y + clip.height / 2 - block_height / 2;

		drawEnhancedBlock(g2DObject, codes, modifiedPhosphatePos,
				modifiedBasePos, currColorMap, start_x, start_y, block_width,
				block_height, true, true);

	}

	private static void drawEnhancedBlock(Graphics2D g2DObject,
			String[] sugarCodes, BitSet modifiedPhosphatePos,
			BitSet modifiedBasePos, HashMap<String, Color> colorMap,
			int start_x, int start_y, int block_width, int block_height,
			boolean drawLeft2Right, boolean phosphateOnTop) {
		int size = sugarCodes.length;

		if (!drawLeft2Right) {
			String[] tmpCodes = new String[sugarCodes.length];
			for (int i = 0; i < tmpCodes.length; i++) {
				tmpCodes[i] = sugarCodes[sugarCodes.length - i - 1];
			}
		}

		for (int i = 0; i < size; i++) {
			// Spaces between single digit numbers and double digit
			// numbers is different
			int pos = i;
			if (!drawLeft2Right) {
				pos = size - i - 1;
			}

			int x = start_x + pos * (block_width + 1);

			if (sugarCodes[i] == null) {
				continue;
			}

			// phosphateOnTop = true;
			// look for modified phosphate
			if (modifiedPhosphatePos.get(i)) {

				g2DObject.setColor(Color.magenta);

				int xAdjust = block_width / 2;
				if (!drawLeft2Right) {
					xAdjust = (-1) * (block_width / 2);
				}
				if (phosphateOnTop) {
					g2DObject.drawArc(x + xAdjust, start_y - block_height / 2,
							block_width, block_height, 0, 180);
				} else {
					g2DObject.drawArc(x + xAdjust, start_y + block_height / 2,
							block_width, block_height, 180, 180);
				}
			}

			// look for modified base
			if (modifiedBasePos.get(i)) {
				g2DObject.setColor(Color.magenta);
				if (phosphateOnTop) {
					g2DObject.fillOval(x, start_y + block_height, block_width,
							block_height);
				} else {
					g2DObject.fillOval(x, start_y - block_height, block_width,
							block_height);
				}
			}

			Color c = colorMap.get(sugarCodes[i]);
			if (c == null) {
				c = defaultColorMap.get(sugarCodes[i]);
			}
			if (c == null) {
				int r = (int) (Math.random() * 255);
				int g = (int) (Math.random() * 255);
				int b = (int) (Math.random() * 255);

				c = new Color(r, g, b);

				colorMap.put(sugarCodes[i], c);
			}
			g2DObject.setColor(c);
			g2DObject.fillRect(x, start_y, block_width, block_height);

		}
	}

	private void paintSingleStrandBlocks(Graphics gObject, RNAPolymer oligo,
			Rectangle clip) {

		Graphics2D g2DObject = (Graphics2D) gObject;
		g2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		String[] codes = oligo.getSugarCodes(0);
		if (codes == null) {
			return;
		}

		int start_x = clip.x + 5;
		int block_width = 7;
		int block_height = 7;
		int start_y = clip.y + clip.height / 2 - block_height / 2;

		drawBlock(g2DObject, codes, currColorMap, start_x, start_y,
				block_width, block_height, true);

	}

	private static void drawBlock(Graphics2D g2DObject, String[] sugarCodes,
			HashMap<String, Color> colorMap, int start_x, int start_y,
			int block_width, int block_height, boolean drawLeft2Right) {
		int size = sugarCodes.length;

		if (!drawLeft2Right) {
			String[] tmpCodes = new String[sugarCodes.length];
			for (int i = 0; i < tmpCodes.length; i++) {
				tmpCodes[i] = sugarCodes[sugarCodes.length - i - 1];
			}
		}

		for (int i = 0; i < size; i++) {
			// Spaces between single digit numbers and double digit
			// numbers is different
			int pos = i;
			if (!drawLeft2Right) {
				pos = size - i - 1;
			}

			int x = start_x + pos * (block_width + 1);

			// Draw
			if (sugarCodes[i] == null) {
				continue;
			}
			Color c = colorMap.get(sugarCodes[i]);
			if (c == null) {
				c = defaultColorMap.get(sugarCodes[i]);
			}
			if (c == null) {
				int r = (int) (Math.random() * 255);
				int g = (int) (Math.random() * 255);
				int b = (int) (Math.random() * 255);

				c = new Color(r, g, b);

				colorMap.put(sugarCodes[i], c);
			}
			g2DObject.setColor(c);
			g2DObject.fillRect(x, start_y, block_width, block_height);
		}
	}
}
