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
package org.helm.editor.protein.view;

import org.helm.editor.editor.TextViewer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * @author zhangtianhong
 */
public class PeptideSequenceViewer extends JPanel {
    
    private javax.swing.JPopupMenu sequencePopupMenu;
    private String error_message = "Non-Preotein Structure";
    static Color light_blue = new Color(2, 148, 250);
    static Color dark_green = new Color(41, 135, 55);
    static Color brown = new Color(238, 149, 18);
    static Font lucidiaPlain15Font = new Font("Lucida Console", Font.PLAIN, 15);
    static Font lucidiaPlain8Font = new Font("Lucida Console", Font.PLAIN, 8);
    static Font lucidiaPlain12Font = new Font("Lucida Console", Font.PLAIN, 12);
    private Color modificationColor = Color.red;
    private Stroke lineStroke = new BasicStroke(1.5f);
    private int tickInterval;
    public static final int DEFAULT_TICK_INTERVAL = 20;
    private int lettersPerLine;
    public static final int DEFAULT_LETTERS_PER_LINE = 50;
    public static final int DEFAULT_LETTERS_PER_LINE_INCREMENT = 5;
    private PeptidePolymer peptidePolymer;
    private int positionLabelMode;
    public final static int NO_LABEL_MODE = 0;
    public final static int RIGHT_LINE_MODE = 1;
    public final static int TOP_TICK_MODE = 2;
    public final static int DEFAULT_LABEL_MODE = TOP_TICK_MODE;
    private boolean fitContent;
    public final static boolean DEFAULT_FIT_CONTENT = false;
    public final static String[] SUPPORTED_LABEL_MODES = {"No Label", "Right Line", "Top Tick"};
    private final static int X_OFFSET = 5;
    private final static int X_PADDING = 2;
    private final static int LINES_BETWEEN_SEQUENCES = 1;
    private final static int LINES_PER_SEQUENCE_HEADER = 1;
    private final static int Y_OFFSET = 5;
    private PeptideSequenceConfigurationDialog configDialog;
    private TextViewer notationViewer;
    private int zoom = 0;
    private static final double ZOOM_AMOUNT = 1.08;
    private Dimension preferredSize = new Dimension(200, 200);
    
    public PeptideSequenceViewer() {
        this(DEFAULT_TICK_INTERVAL, DEFAULT_LETTERS_PER_LINE, DEFAULT_LABEL_MODE);
        init();
    }
    
    public PeptideSequenceViewer(int tickInterval, int lettersPerLine, int positionLabelMode) {
        this(tickInterval, lettersPerLine, positionLabelMode, false, new PeptidePolymer());
        init();
    }
    
    public PeptideSequenceViewer(int tickInterval, int lettersPerLine, int positionLabelMode, boolean fitContent, PeptidePolymer peptidePolymer) {
        this.tickInterval = tickInterval;
        this.lettersPerLine = lettersPerLine;
        this.peptidePolymer = peptidePolymer;
        this.positionLabelMode = positionLabelMode;
        this.fitContent = fitContent;
        setBackground(Color.white);
        init();
    }
    
    private void init() {       
        sequencePopupMenu = new javax.swing.JPopupMenu();
        JMenuItem configurationMenuItem = new javax.swing.JMenuItem();
        JMenuItem notationMenuItem = new javax.swing.JMenuItem();
        
        configurationMenuItem.setText("Configure Layout");
        configurationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLayout();
            }
        });
        sequencePopupMenu.add(configurationMenuItem);
        
        notationMenuItem.setText("Show Notation");
        notationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNotation();
            }
        });
        sequencePopupMenu.add(notationMenuItem);
        
        this.setComponentPopupMenu(sequencePopupMenu);
        
        addMouseWheelListener(new MouseWheelListener() {            
            
            public void mouseWheelMoved(MouseWheelEvent e) {
                int steps = e.getWheelRotation();
                zoom -= steps;
                updatePreferredSize(e.getWheelRotation(), e.getPoint());
            }
        });
        
    }
    
    private void updatePreferredSize(int n, Point p) {
        if (fitContent == false) {
            double d = (double) n * ZOOM_AMOUNT;
            d = (n > 0) ? 1 / d : -d;
            
            int w = (int) (getWidth() * d);
            int h = (int) (getHeight() * d);
            preferredSize.setSize(w, h);
            getParent().doLayout();
        }
    }
    
    public int getLettersPerLine() {
        return lettersPerLine;
    }
    
    public void setLettersPerLine(int lettersPerLine) {
        this.lettersPerLine = lettersPerLine;
    }
    
    public PeptidePolymer getPeptidePolymer() {
        return peptidePolymer;
    }
    
    public void setPeptidePolymer(PeptidePolymer peptidePolymer) {
        this.peptidePolymer = peptidePolymer;
        repaint();
    }
    
    public int getTickInterval() {
        return tickInterval;
    }
    
    public void setTickInterval(int tickInterval) {
        this.tickInterval = tickInterval;
    }
    
    public int getPositionLabelMode() {
        return positionLabelMode;
    }
    
    public void setPositionLabelMode(int positionLabelMode) {
        this.positionLabelMode = positionLabelMode;
    }
    
    public static int getPositiontLabelMode(String textLabelMode) {
        for (int i = 0; i < SUPPORTED_LABEL_MODES.length; i++) {
            String mode = SUPPORTED_LABEL_MODES[i];
            if (mode.equals(textLabelMode)) {
                return i;
            }
        }
        return DEFAULT_LABEL_MODE;
    }
    
    public static String getPositionLabelModeText(int labelMode) {
        if (labelMode < 0 || labelMode >= SUPPORTED_LABEL_MODES.length) {
            return SUPPORTED_LABEL_MODES[DEFAULT_LABEL_MODE];
        } else {
            return SUPPORTED_LABEL_MODES[labelMode];
        }
    }
    
    public boolean isFitContent() {
        return fitContent;
    }
    
    public void setFitContent(boolean fitContent) {
        this.fitContent = fitContent;
    }
    
    public void setNotation(String notation) {
        PeptidePolymer p = new PeptidePolymer(notation);
        setPeptidePolymer(p);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }
    
    @Override
    public void paint(Graphics gObject) {
        super.paint(gObject);
        if (null == peptidePolymer) {
            return;
        }
        
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        gObject.setColor(Color.white);
        gObject.fillRect(rect.x, rect.y, rect.width, rect.height);
        
        switch (peptidePolymer.getDataMode()) {
            case PeptidePolymer.EMPTY_DATA_MODE:
                break;
            
            case PeptidePolymer.VALID_DATA_MODE:
                paintPeptide(gObject, peptidePolymer, rect);
                break;
            
            case PeptidePolymer.INVALID_DATA_MODE:
                paintErrorMessage(gObject, error_message, rect);
                break;
            
            default:
                break;
        }
    }
    
    private void paintPeptide(Graphics gObject, PeptidePolymer peptide, Rectangle clip) {
        Graphics2D g2DObject = (Graphics2D) gObject;
        g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getSingleLetterWidth(g2DObject);
        int height = getSingleLineHeight(g2DObject);
        int lineCount = getTotalLineCount(peptide, lettersPerLine);
        
        AffineTransform transformer = null;
        
        int yOffset = (clip.height - height * lineCount) / 2;
        
        if (fitContent) {
            optimizeLettersPerLine(g2DObject, peptide, clip);
            
            lineCount = getTotalLineCount(peptide, lettersPerLine);
            Rectangle contentRect = new Rectangle(width * (lettersPerLine + X_OFFSET), height * lineCount);
            
            double xScale = 1.0;
            if (contentRect.getWidth() > clip.getWidth()) {
                xScale = clip.getWidth() / contentRect.getWidth();
            }
            
            double yScale = 1.0;
            if (contentRect.getHeight() > clip.getHeight()) {
                yScale = clip.getHeight() / contentRect.getHeight();
            }

            //transform needed if scale required
            if (xScale < 1.0 || yScale < 1.0) {
                transformer = new AffineTransform();
                transformer.scale(xScale, yScale);
                yOffset = (int) (clip.height - contentRect.height) / 2;
                transformer.translate(X_OFFSET, -yOffset);
                g2DObject.transform(transformer);
            }
        } else {
            double factor = Math.pow(ZOOM_AMOUNT, zoom);
            
            transformer = new AffineTransform();
            transformer.scale(factor, factor);
            yOffset = (int) (clip.height - height * lineCount) / 2;
            transformer.translate(X_OFFSET, -yOffset);
            g2DObject.transform(transformer);
        }
        
        int start_x = clip.x + X_OFFSET;
        int start_y = clip.y + yOffset;

        //setup map to hold connection position info
        int connCount = peptide.getNumberOfConnections();
        Map<int[], int[]> connMap = new HashMap<int[], int[]>();
        for (int i = 0; i < connCount; i++) {
            int[] seqMonoInfo = peptide.getConnection(i);
            int[] positionInfo = new int[4];
            connMap.put(seqMonoInfo, positionInfo);
        }

        //draw each sequence and build up connection map
        int seqCount = peptide.getNumberOfStrands();
        for (int i = 0; i < seqCount; i++) {
            int nextY = drawSequence(g2DObject, i, peptide, connMap, start_x, start_y, width, height);
            drawAnnotation(g2DObject, i, peptide, start_x, start_y + height);
            start_y = nextY;
        }
        
        drawConnections(g2DObject, connMap);

        //return to original transform of G2DGraphics
        if (null != transformer) {
            g2DObject.translate(-transformer.getTranslateX(), -transformer.getTranslateY());
            g2DObject.scale(1 / transformer.getScaleX(), 1 / transformer.getScaleY());
        }
    }

    /**
     * paint error message
     * Horizontally, with left offset
     * Vertically, center aligned
     */
    private void paintErrorMessage(Graphics gObject, String message, Rectangle clip) {
        Graphics2D g2DObject = (Graphics2D) gObject;
        g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2DObject.setColor(Color.cyan);
        g2DObject.setFont(lucidiaPlain15Font);
        
        int height = getSingleLineHeight(g2DObject);
        
        int start_x = clip.x + X_OFFSET;
        int start_y = clip.y + clip.height / 2 + height / 2;
        
        g2DObject.drawString(message, start_x, start_y);
    }
    
    private void drawAnnotation(Graphics2D g2DObject, int sequenceIndex, PeptidePolymer peptide, int start_x, int start_y) {
        
        g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2DObject.setColor(Color.blue);
        g2DObject.setFont(lucidiaPlain12Font);
        g2DObject.setBackground(Color.yellow);
        
        String annotation = peptide.getAnnotation(sequenceIndex);
        if (null != annotation) {
            g2DObject.drawString(annotation, start_x, start_y);
        }
    }
    
    private int drawSequence(Graphics2D g2DObject, int sequenceIndex, PeptidePolymer peptide, Map<int[], int[]> connMap, int start_x, int start_y, int char_width, int char_height) {
        int nextY = start_y;
        switch (positionLabelMode) {
            case NO_LABEL_MODE:
                nextY = drawSequenceWithoutPositionLabel(g2DObject, sequenceIndex, peptide, connMap, start_x, start_y, char_width, char_height);
                break;
            case RIGHT_LINE_MODE:
                nextY = drawSequenceWithRightLineLabel(g2DObject, sequenceIndex, peptide, connMap, start_x, start_y, char_width, char_height);
                break;
            case TOP_TICK_MODE:
                nextY = drawSequenceWithTopTickLabel(g2DObject, sequenceIndex, peptide, connMap, start_x, start_y, char_width, char_height);
                break;
            
            default:
                break;
        }
        return nextY;
    }
    
    private int drawSequenceWithoutPositionLabel(Graphics2D g2DObject, int sequenceIndex, PeptidePolymer peptide, Map<int[], int[]> connMap, int start_x, int start_y, int char_width, int char_height) {
        String seq = peptide.getSingleLetterSeq(sequenceIndex);
        BitSet modPosition = peptide.getModifiedPos(sequenceIndex);
        g2DObject.setFont(lucidiaPlain15Font);
        int size = seq.length();
        int linePosition = 0;
        int letterPosition = 0;
        
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            Color color = Color.black;
            if (modPosition.get(i)) {
                color = modificationColor;
            }
            // Set letter text color
            g2DObject.setColor(color);
            
            letterPosition = i % lettersPerLine;
            linePosition = i / lettersPerLine;
            
            int xTextPosition = start_x + char_width * letterPosition;
            int yTextPosition = start_y + char_height * 2 + linePosition * char_height * 2;
            
            g2DObject.drawChars(new char[]{c}, 0, 1, xTextPosition, yTextPosition);

            //check connection info and fill position if matches
            checkConnectionInfo(sequenceIndex, i, xTextPosition, yTextPosition, connMap);
        }
        
        return start_y + char_height * 2 + linePosition * char_height * 2 + char_height * LINES_BETWEEN_SEQUENCES;
    }
    
    private int drawSequenceWithRightLineLabel(Graphics2D g2DObject, int sequenceIndex, PeptidePolymer peptide, Map<int[], int[]> connMap, int start_x, int start_y, int char_width, int char_height) {
        String seq = peptide.getSingleLetterSeq(sequenceIndex);
        BitSet modPosition = peptide.getModifiedPos(sequenceIndex);
        int size = seq.length();
        int linePosition = 0;
        int letterPosition = 0;
        
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            Color color = Color.black;
            if (modPosition.get(i)) {
                color = modificationColor;
            }
            // Set letter text color
            g2DObject.setColor(color);
            g2DObject.setFont(lucidiaPlain15Font);
            
            letterPosition = i % lettersPerLine;
            linePosition = i / lettersPerLine;
            
            int xTextPosition = start_x + char_width * letterPosition;
            int yTextPosition = start_y + char_height * 2 + linePosition * char_height * 2;
            
            g2DObject.drawChars(new char[]{c}, 0, 1, xTextPosition, yTextPosition);

            //add position label to the right of each line if necessary
            int marker = 0;
            if (letterPosition == lettersPerLine - 1) {
                marker = (linePosition + 1) * lettersPerLine;
            } else if (size == (linePosition * lettersPerLine + letterPosition + 1)) {
                marker = size;
            }
            
            if (marker > 0) {
                String label = new Integer(marker).toString();
                int labelXStart = start_x + char_width * lettersPerLine + X_OFFSET;
                // Draw
                g2DObject.setFont(lucidiaPlain12Font);
                g2DObject.setColor(Color.black);
                g2DObject.drawString(label, labelXStart, yTextPosition);
            }

            //check connection info and fill position if matches
            checkConnectionInfo(sequenceIndex, i, xTextPosition, yTextPosition, connMap);
        }
        
        return start_y + char_height * 2 + linePosition * char_height * 2 + char_height * LINES_BETWEEN_SEQUENCES;
    }
    
    private int drawSequenceWithTopTickLabel(Graphics2D g2DObject, int sequenceIndex, PeptidePolymer peptide, Map<int[], int[]> connMap, int start_x, int start_y, int char_width, int char_height) {
        String seq = peptide.getSingleLetterSeq(sequenceIndex);
        BitSet modPosition = peptide.getModifiedPos(sequenceIndex);
        int size = seq.length();
        int linePosition = 0;
        int letterPosition = 0;
        
        for (int i = 0; i < size; i++) {
            char c = seq.charAt(i);
            Color color = Color.black;
            if (modPosition.get(i)) {
                color = modificationColor;
            }
            // Set letter text color
            g2DObject.setColor(color);
            g2DObject.setFont(lucidiaPlain15Font);
            
            letterPosition = i % lettersPerLine;
            linePosition = i / lettersPerLine;
            
            int xTextPosition = start_x + char_width * letterPosition;
            int yTextPosition = start_y + char_height * 2 + linePosition * char_height * 2;
            
            g2DObject.drawChars(new char[]{c}, 0, 1, xTextPosition, yTextPosition);

            //add position label to the top of the letter if necessary
            int ticks = i / tickInterval;
            int tickPos = i % tickInterval;
            int marker = 0;
            
            if (tickPos == tickInterval - 1) {
                marker = (ticks + 1) * tickInterval;
            } else if (i == size - 1) {
                marker = size;
            }
            
            if (marker > 0) {
                String label = new Integer(marker).toString();
                Rectangle2D rect = g2DObject.getFontMetrics(lucidiaPlain8Font).getStringBounds(label, g2DObject);
                int labelWidth = (int) rect.getWidth();
                int offset = (char_width - labelWidth) / 2;
                int labelXStart = start_x + char_width * letterPosition + offset;
                int labelYStart = start_y + linePosition * char_height * 2 + char_height;

                // Draw
                g2DObject.setFont(lucidiaPlain8Font);
                g2DObject.setColor(Color.black);
                g2DObject.drawString(label, labelXStart, labelYStart);
            }

            //check connection info and fill position if matches
            checkConnectionInfo(sequenceIndex, i, xTextPosition, yTextPosition, connMap);
        }
        return start_y + char_height * 2 + linePosition * char_height * 2 + char_height * LINES_BETWEEN_SEQUENCES;
    }
    
    private void drawConnections(Graphics2D g2DObject, Map<int[], int[]> connMap) {
        if (connMap.size() > 0) {
            g2DObject.setStroke(lineStroke);
            g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int letterWidth = getSingleLetterWidth(g2DObject);
            int lineHeight = getSingleLineHeight(g2DObject);
            Set<Entry<int[], int[]>> entrySet = connMap.entrySet();
            int index = 0;
            for (Iterator<Entry<int[], int[]>> i = entrySet.iterator(); i.hasNext();) {
                Color lineColor = getConnectionLineColor(index);
                g2DObject.setColor(lineColor);
                Entry<int[], int[]> entry = i.next();
                int[] points = entry.getValue();
                //modification positions
                int x1 = points[0] + letterWidth / 2; //move to right half letter width
                int y1 = points[1] - lineHeight;    //move up one line height
                int x2 = points[2] + letterWidth / 2;
                int y2 = points[3] - lineHeight;
                
                if (y1 == y2) {
                    //on same line, use four points to draw two short vertical lines, one horizontal line
                    //move line to top center of letter
                    int[] xPoints = new int[4];
                    int[] yPoints = new int[4];
                    xPoints[0] = x1;
                    xPoints[1] = x1;
                    xPoints[2] = x2;
                    xPoints[3] = x2;
                    
                    yPoints[0] = y1;
                    yPoints[1] = y1 - lineHeight / 2; //move up half line height
                    yPoints[2] = y2 - lineHeight / 2; //move up half line height
                    yPoints[3] = y2;

                    //drawing from 1 to 2
                    g2DObject.drawPolyline(xPoints, yPoints, 4);
                } else {
                    //on different lines, could from different sequence
                    //use six points, three from each end to three vertical lines, two horizontal line
                    //try to do evenly spilt horizontal line without crossing letters
                    int[] xPoints = new int[6];
                    int[] yPoints = new int[6];
                    
                    int xDelta = x2 - x1;
                    int x3;
                    if (xDelta > 0) {
                        //1 to 2, left to right
                        int letterCount = xDelta / letterWidth;
                        if (letterCount % 2 == 0) {
                            //even letters, x1 takes more
                            x3 = x1 + xDelta / 2 + letterWidth / 2;
                        } else {
                            //odd letters, even
                            x3 = x1 + xDelta / 2;
                        }
                    } else if (xDelta == 0) {
                        //draw line to the right of letter, 1 to 2
                        x3 = x1 + letterWidth / 2;
                    } else {
                        //1 to 2, right to left
                        xDelta = -xDelta;
                        int letterCount = xDelta / letterWidth;
                        if (letterCount % 2 == 0) {
                            //even letters, x1 takes more
                            x3 = x2 + xDelta / 2 + letterWidth / 2;
                        } else {
                            //odd letters, even
                            x3 = x2 + xDelta / 2;
                        }
                    }
                    
                    xPoints[0] = x1;
                    xPoints[1] = x1;
                    xPoints[2] = x3;
                    xPoints[3] = x3;
                    xPoints[4] = x2;
                    xPoints[5] = x2;
                    
                    yPoints[0] = y1;
                    yPoints[1] = y1 - lineHeight / 2;
                    yPoints[2] = y1 - lineHeight / 2;
                    yPoints[3] = y2 - lineHeight / 2;
                    yPoints[4] = y2 - lineHeight / 2;
                    yPoints[5] = y2;
                    
                    g2DObject.drawPolyline(xPoints, yPoints, 6);
                }
                index++;
            }
            
        }
        
    }
    
    private void checkConnectionInfo(int sequenceIndex, int monomerIndex, int xPos, int yPos, Map<int[], int[]> connMap) {
        if (connMap.size() > 0) {
            int sequenceIndexOneBased = sequenceIndex + 1;
            int monomerIndexOneBased = monomerIndex + 1;
            Set<int[]> keySet = connMap.keySet();
            
            for (Iterator<int[]> iterator = keySet.iterator(); iterator.hasNext();) {
                int[] key = iterator.next();
                if (sequenceIndexOneBased == key[0] && monomerIndexOneBased == key[1]) {
                    int[] value = connMap.get(key);
                    value[0] = xPos;
                    value[1] = yPos;
                }
                
                if (sequenceIndexOneBased == key[2] && monomerIndexOneBased == key[3]) {
                    int[] value = connMap.get(key);
                    value[2] = xPos;
                    value[3] = yPos;
                }
            }
        }
    }
    
    private int getSingleLetterWidth(Graphics2D g2DObject) {
        FontMetrics metrics = g2DObject.getFontMetrics(lucidiaPlain15Font);
        return metrics.stringWidth("A") + X_PADDING;
    }
    
    private int getSingleLineHeight(Graphics2D g2DObject) {
        FontMetrics metrics = g2DObject.getFontMetrics(lucidiaPlain15Font);
        return metrics.getMaxAscent();
    }
    
    private Color getConnectionLineColor(int index) {
        Color[] colors = new Color[]{Color.blue, Color.green, Color.cyan, Color.pink};
        int mod = index % 4;
        return colors[mod];
    }
    
    private int getTotalLineCount(PeptidePolymer peptide, int totalLettersPerline) {
        int lineCount = 0;
        int seqCount = peptide.getNumberOfStrands();
        for (int i = 0; i < seqCount; i++) {
            if (lineCount > 0) {
                lineCount = lineCount + LINES_BETWEEN_SEQUENCES + LINES_PER_SEQUENCE_HEADER;
            }
            String seq = peptide.getSingleLetterSeq(i);
            int lines = seq.length() / totalLettersPerline;
            if (seq.length() % totalLettersPerline > 0) {
                lines++;
            }
            lineCount = lineCount + lines * 2; //each seq line takes two real lines
        }
        return lineCount;
    }
    
    private void optimizeLettersPerLine(Graphics2D g2DObject, PeptidePolymer peptide, Rectangle clip) {
        
        int increment = tickInterval;
        if (increment > DEFAULT_LETTERS_PER_LINE_INCREMENT) {
            increment = DEFAULT_LETTERS_PER_LINE_INCREMENT;
        }
        
        int curLettersPerLine = lettersPerLine;
        int width = getSingleLetterWidth(g2DObject);
        int height = getSingleLineHeight(g2DObject);
        
        int lineCount = getTotalLineCount(peptide, curLettersPerLine);
        Rectangle contentRect = new Rectangle(width * (curLettersPerLine + X_OFFSET), height * lineCount);
        
        double xScale = 1.0;
        if (contentRect.getWidth() > clip.getWidth()) {
            xScale = clip.getWidth() / contentRect.getWidth();
        }
        
        double yScale = 1.0;
        if (contentRect.getHeight() > clip.getHeight()) {
            yScale = clip.getHeight() / contentRect.getHeight();
        }
        
        boolean directionReverted = false;
        boolean oldScaleInOrder;
        boolean newScaleInOrder;
        while ((xScale < 1.0 || yScale < 1.0) && (!directionReverted) && (curLettersPerLine > increment)) {
            if (xScale < yScale) {
                oldScaleInOrder = true;
                curLettersPerLine = curLettersPerLine - increment;
            } else {
                oldScaleInOrder = false;
                curLettersPerLine = curLettersPerLine + increment;
            }
            lineCount = getTotalLineCount(peptide, curLettersPerLine);
            contentRect = new Rectangle(width * (curLettersPerLine + X_OFFSET), height * lineCount);
            
            xScale = 1.0;
            if (contentRect.getWidth() > clip.getWidth()) {
                xScale = clip.getWidth() / contentRect.getWidth();
            }
            
            yScale = 1.0;
            if (contentRect.getHeight() > clip.getHeight()) {
                yScale = clip.getHeight() / contentRect.getHeight();
            }
            
            if (xScale < yScale) {
                newScaleInOrder = true;
            } else {
                newScaleInOrder = false;
            }
            
            if ((oldScaleInOrder && !newScaleInOrder) || (!oldScaleInOrder && newScaleInOrder)) {
                directionReverted = true;
            }

            //order reverted, bring it back
            if ((oldScaleInOrder && !newScaleInOrder)) {
                curLettersPerLine = curLettersPerLine + increment;
            }
        }
        lettersPerLine = curLettersPerLine;
    }
    
    public void changeLayout() {
        if (null == configDialog) {
            configDialog = new PeptideSequenceConfigurationDialog(null, this, true);
        }
        
        configDialog.setParameters(this);
        
        Point location = this.getParent().getLocationOnScreen();
        location = new Point(location.x + 5, location.y + 5);
        
        configDialog.setLocation(location);
        
        if (configDialog.isVisible()) {
            configDialog.setVisible(false);
        }
        
        configDialog.setVisible(true);
        
        if (fitContent) {
            preferredSize.setSize(200, 200);
            zoom = 0;
            getParent().doLayout();
        }
    }
    
    public void showNotation() {
        String notation = getPeptidePolymer().getNotation();
        notationViewer = TextViewer.getInstance(null);
        notationViewer.setText(notation);
        notationViewer.setTitle("HELM Notation");
        
        if (notationViewer.isVisible()) {
            notationViewer.setVisible(false);
        }
        notationViewer.setVisible(true);
    }
}
