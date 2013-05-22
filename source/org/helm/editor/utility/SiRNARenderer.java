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

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.utility.ColorMap;
import org.helm.notation.tools.ComplexNotationParser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import org.jdom.JDOMException;

/**
 *
 * @author lih25
 */
public class SiRNARenderer extends JComponent {

    private String notation;
    private BufferedImage buffereredImage = null;
    /**
     * previous width and height of the display screen
     */
    private int preWidth = 0;
    private int preHeight = 0;
    private Font font = null;
    /**
     * the space between characters
     */
    private int space = 2;

    /**
     * create a component with a giving notation
     * @param notation
     */
    public SiRNARenderer(String notation) {
        super();
        this.notation = notation;
    }

    /**
     * create a component with no notation
     */
    public SiRNARenderer() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        if (preHeight != height || preWidth != width) {
            setImageDirty();
            preHeight = height;
            preWidth = width;
        }

        if (buffereredImage == null) {
            try {
                buffereredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics bufferedGraphics = buffereredImage.createGraphics();
                String[] formatedString = ComplexNotationParser.getFormatedSirnaSequences(notation, " ", "|");
                char[] sequenceChars = null;
                bufferedGraphics.setFont(getFont().deriveFont(Font.BOLD));
                if (font != null) {
                    bufferedGraphics.setFont(font);
                }

                FontMetrics metrics = this.getFontMetrics(bufferedGraphics.getFont());

                int charWidth = metrics.charWidth('W');
                int charHeight = metrics.getMaxAscent();
                int startX = charWidth;
                int startY = charHeight;

                startY = startY < 0 ? 0 : startY;

                int sequenceIndex = 0;
                int charIndex = 0;
                int w = 0;

                for (String sequence : formatedString) {
                    sequenceChars = sequence.toCharArray();
                    charIndex = 0;
                    if (sequenceIndex == 1) {
                        bufferedGraphics.setColor(Color.blue);
                    }
                    for (char c : sequenceChars) {
                        if (sequenceIndex != 1) {
                            bufferedGraphics.setColor(ColorMap.getNucleotidesColor(String.valueOf(c)));
                        }
                        w = metrics.charWidth(c);
                        bufferedGraphics.drawChars(new char[]{c},
                                0, 1, startX + (charWidth + space) * charIndex + (charWidth - w) / 2,
                                startY + charHeight * sequenceIndex);
                        charIndex++;
                    }

                    sequenceIndex++;
                }


            } catch (NotationException ex) {
                Logger.getLogger(SiRNARenderer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MonomerException ex) {
                Logger.getLogger(SiRNARenderer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SiRNARenderer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JDOMException ex) {
                Logger.getLogger(SiRNARenderer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (StructureException ex) {
                Logger.getLogger(SiRNARenderer.class.getName()).log(Level.SEVERE, null, ex);
            }



        }

        g.drawImage(buffereredImage, 0, 0, this);
    }

    /**
     * Set the notation of the display
     * @param notation : complex notation
     */
    public void setNotation(String notation) {
        if (this.notation == null || !this.notation.equalsIgnoreCase(notation)) {
            this.notation = notation;
            setImageDirty();
        }

    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        this.font = font;
        setImageDirty();
    }

    private void setImageDirty() {
        buffereredImage = null;
        repaint();
    }

}
