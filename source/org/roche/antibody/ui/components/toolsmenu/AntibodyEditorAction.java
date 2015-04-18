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
package org.roche.antibody.ui.components.toolsmenu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.helm.editor.editor.MacromoleculeEditor;

import com.quattroresearch.antibody.AntibodyEditorDialog;


/**
 * {@code AntibodyEditorAction}
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id$
 */
public class AntibodyEditorAction extends AbstractAction {

  /** */
  private static final long serialVersionUID = 8270310476746408309L;
    private MacromoleculeEditor editor;
    private AntibodyEditorDialog editorDialog;

    public AntibodyEditorAction(MacromoleculeEditor editor) {
        super("Launch Antibody Editor ...");
        this.editor = editor;
    }

    public void actionPerformed(ActionEvent e) {
        if (null == editorDialog) {
            editorDialog = new AntibodyEditorDialog(editor, false);
            editorDialog.setLocationRelativeTo(editor.getFrame());
        }

        if (!editorDialog.isVisible()) {
            editorDialog.setVisible(true);
        }
    }	
	
}