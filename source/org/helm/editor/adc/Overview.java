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
/*
 * Viewer.java
 *
 * Created on Oct 21, 2011, 2:26:48 PM
 */
package org.helm.editor.adc;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Dimension;
import chemaxon.formats.MolImporter;
import java.awt.Image;
import chemaxon.struc.*;

/**
 *
 * Draw ADC high level overview graph Antibody-Connection-Drug
 * @author YUANT05
 */
public class Overview extends javax.swing.JPanel {

    /** Creates new form Viewer */
    public Overview() {
        initComponents();
    }
    
    public void setNotation(String notation) {
        String[] ss = ADCEditor.parseNotation(notation);
        if (ss == null || ss.length != 3) {
            error = notation;
            structure.reset();;
            if (this.ab != null) {
                this.ab = null;
                this.repaint();
            }
        }
        else {
            // ss[0] is antibody, ss[1] is drug structure, ss[2] is the connection
            error = null;
            AB a = new AB();
            a.aa = ConnectionEditor.getAminoAcid(ss[2]);
            if (a.aa != null && a.aa.length() > 0) {
                char c = a.aa.charAt(0);
                if (c == 'C')
                    a.r = 16;
                else if (c == 'K')
                    a.r = 7;
            }
            a.sequence = ss[0] != null && ss[0].length() > 0;            
            if (!ss[1].equals(a.smiles)) {
                a.smiles = ss[1];
                structure.reset();;
            }
            if (this.ab == null || !this.ab.equals(a)) {
                this.ab = a;
                this.repaint();
            }
        }
    }
    
    @Override 
    public void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        
        Dimension s = this.getSize();        
        Graphics2D g = (Graphics2D)gr;
        if (error != null && error.length() != 0) {
            // draw error message
            g.setColor(Color.red);
            g.drawString(error, 10, s.height / 2);
            return;
        }
       
        int h = s.height * 2 / 3;
        int w = h * 280 / 80;
        if (w>= s.width * 3 / 4) {
            w = s.width * 3 / 4;
            h = w * 80 / 280;
        }
        Rectangle r = new Rectangle((s.width - w) / 2, (s.height - h) / 2, w, h);
        
        // draw antibody symbol
        int fontsize = r.height / 8;
        drawAntibodySymbol(g, r.x, r.y, r.height * 70 / 80, r.height, fontsize > 4 ? fontsize / 4 : 1); 
        
        // configure graphics to draw connection
        if (ab == null || ab.aa == null || ab.aa.indexOf('?') >= 0)
            g.setStroke(new BasicStroke(fontsize > 10 ? fontsize / 10 : 1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{fontsize <8 ? 2 : fontsize / 4}, 0));
        else
            g.setStroke(new BasicStroke(fontsize > 10 ? fontsize / 10 : 1));
        
        if (font == null || font.getSize() != fontsize)
            font = new Font("Arial", Font.PLAIN,  fontsize);
        g.setColor(Color.black);
        
        g.setFont(font);
        int x = r.x + r.height;
        int y = r.y + r.height / 2;
        
        // draw structure box
        r = new Rectangle(r.x + r.height * 2, r.y, r.height * 120 / 80, r.height);
        // if structure smiles or structure size is changed, regenerate structure images from smiles
        Image img = structure.getImg(ab, r);
        
        if (img != null) {
            // draw structure
            g.drawImage(img, r.x, r.y, null);
        }
        else {
            // no structure
            int m = r.height / 8;
            g.setColor(Color.gray);
            g.drawRoundRect(r.x + m, r.y + m, r.height - m, r.height - m * 2, m, m);
            g.drawString("Linker Payload", r.x + m + m / 4, r.y +r.height / 2 + fontsize / 2);
        }        
       
        // draw connection line
        g.drawLine(x, y - structure.deltaY, x + r.height, y - structure.deltaY);
        if (ab != null && ab.aa != null && ab.aa.length() > 0)
            g.drawString(ab.aa, x + r.height / 3, y - structure.deltaY + (structure.deltaY > 0 ?  fontsize : -fontsize / 2));        
    }
    
    private void drawAntibodySymbol(Graphics2D g, int x, int y, int w, int h, int linewidth) {
        //if (numberChains == 0)
        //    return;
        int cx = x + w / 2;
        
        int w2 =  w / 2;
        int gap = w / 12;
        int yh = h / 3;
        int dx = w / 12;
        int dy = h / 8;

        // draw the Y shape
        g.setColor(ab != null && ab.sequence ? Color.blue : Color.gray);
        g.setStroke(new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawLine(cx - w2, y, cx - gap, y + yh);
        g.drawLine(cx - gap, y + yh, cx - gap, y + h);         
           
        g.drawLine(cx + w2 + dx, y + dy, cx + gap + dx, y + yh + dy);
        g.drawLine(cx + w2, y, cx + gap, y + yh);
        g.drawLine(cx - w2 - dx, y + dy, cx - gap - dx, y + yh + dy);
        g.drawLine(cx + gap, y + yh, cx +gap, y + h);  

//        // draw indicator that drug is connected to hc or lc
//        int m = linewidth * 3;
//        if (ab != null) {
//            if ("hc".equals(ab.chain)) {
//                // heavy chain
//                g.drawOval(cx + (w2  + gap) / 2 - m / 2, y + yh / 2 - m / 2, m, m);
//            }
//            else if ("lc".equals(ab.chain)) {
//                // light chain
//                g.drawOval(cx + (w2  + gap) / 2 - m / 2 + dx, y + yh / 2 - m / 2 + dy, m, m);    
//            }
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    String error = null;
    AB ab = null;
    Img structure = new Img();
    Font font = null;
    
    class Img
    {
        public Image img = null;
        public java.awt.Dimension structureSize = null;       
        public int deltaY = 0;
        
        public void reset() {
            img = null;
            deltaY = 0;
            structureSize = null;
        }
        
        public Image getImg(AB ab, Rectangle r) {
            if (structure == null || structureSize == null || r.width != structureSize.width || r.height != structureSize.height) {
                smilesToImage(ab, new java.awt.Dimension(r.width, r.height));
            }
            return img;
        }        

        private void smilesToImage(AB ab, Dimension size) {
            reset();
            if (ab == null || ab.smiles == null || ab.smiles.length() == 0)
                return;

            Molecule mol = null;
            try {
                mol = MolImporter.importMol(ab.smiles);

                for (int i = 0; i < mol.getAtomCount(); ++i) {
                    MolAtom a = mol.getAtom(i);
                    if ("R#".equals(a.getSymbol()) && a.getBondCount() == 1) {
                        MolBond b = a.getBond(0);
                        MolAtom oa = b.getAtom1() == a ? b.getAtom2() : b.getAtom1();

                        // generating 2d coordinates
                        mol.clean(2, "");

                        // rotate mol to make R group bond horizontally
                        rotateMol(mol, a, oa);
                        DPoint3 p = a.getLocation();
                        p.x -= oa.getLocation().x - p.x;
                        a.setLocation(p);
                        if (ab.r != 0) {
                            a.setAtno(ab.r);
                            a.setImplicitHcount(ab.r == 7 ? 1 : 0);
                        }

                        chemaxon.marvin.util.MolImageSize s = mol.getImageSize("image:h" + size.height);
                        deltaY = (int)((p.y - mol.calcCenter().y) * s.scale * 0.8); // times 0.8 to match the image margin           
                        break;
                    }
                }
            }
            catch (Exception e) {
                mol = null;
            }

            img = mol == null ? null :  (Image) mol.toObject("image:h" + size.height);
            if (img != null)
                structureSize = size;
        }

        private void rotateMol(Molecule m, MolAtom a, MolAtom oa) {
            DPoint3 o = a.getLocation();
            double deg = angleTo(oa.getLocation(), o);
            if (deg == 0)
                return;
            deg = -deg;
            for (int i = 0; i < m.getAtomCount(); ++i) {
                MolAtom t = m.getAtom(i);
                if (t != a) {
                    DPoint3 p = t.getLocation();
                    p = rotate(p, o, deg);
                    t.setLocation(p);
                }
            }
        }

        private double angleTo(DPoint3 p, DPoint3 o) {
            double a = Math.atan2(p.y - o.y, p.x - o.x) * 180 /Math.PI;
            return a < 0 ? (a + 360) : a;
        }

        private  DPoint3 rotate(DPoint3 p, DPoint3 o, double deg) {
            double dx = p.x - o.x;
            double dy = p.y - o.y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d == 0)
                return p;
            double a = angleTo(p, o);
            double x = o.x + d * Math.cos((a + deg) * Math.PI / 180);
            double y = o.y + d * Math.sin((a + deg) * Math.PI / 180);
            return new DPoint3(x, y, 0);
        }        
    }
    
    class AB
    {
        public String smiles;
        public String aa;
        public int r;
        public boolean sequence;
        
        public boolean equals(AB ab) {
            return ab != null && eq(smiles, ab.smiles) && eq(aa, ab.aa)  && r == ab.r && ab.sequence == sequence;
        }
        
        private boolean eq(String s1, String s2) {
                if (s1 == null && s2 == null)
                    return true;
                if (s1 == null || s2 == null)
                    return false;
                return s1.equals(s2);
        }
    }
    
    static chemaxon.marvin.beans.MSketchPane sketcher;
}
