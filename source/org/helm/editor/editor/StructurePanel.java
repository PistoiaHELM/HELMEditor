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
package org.helm.editor.editor;

import chemaxon.marvin.beans.MSketchPane;
import chemaxon.marvin.beans.MViewPane;
import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;

import java.awt.BorderLayout;

import javax.swing.JPanel;



import java.util.*;

/**
 *
 * @author yuant05
 */
public class StructurePanel extends JPanel {
    
    private MSketchPane sketch = null;
    private MViewPane view = null;

    public StructurePanel() {
        sketch = new MSketchPane();
        view = new MViewPane();
/*
        sketch.setParams(
            "ttmpls0=*Generic*chemaxon/marvin/templates/generic.t\n"+
            "ttmpls1=*Rings*chemaxon/marvin/templates/rings.t\n"+
            "xtmpls=chemaxon/marvin/templates/wedgebonds.t\n"+
            "tmpls11=:Conformers:chemaxon/marvin/templates/conformers.t\n");
*/
        // Adding the bean to the container panel
        setLayout(new BorderLayout());
        //add(sketch, BorderLayout.CENTER); 
        add(view, BorderLayout.CENTER); 
    }
    
    public void setMol(String s) {
        view.setM(0, s);
        sketch.setMol(s);
    }
    
    public String getSmilesEx() {
        return sketch.getMol("cxsmiles");
    }
    
    public void setEditMode(boolean f) {
        if (f) {
            remove(view);
            add(sketch, BorderLayout.CENTER); 
        }
        else {
            remove(sketch);
            add(view, BorderLayout.CENTER);         
        }
    }
}
