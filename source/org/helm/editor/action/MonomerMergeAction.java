package org.helm.editor.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.manager.MonomerManager;
import org.helm.editor.manager.MonomerMergeManager;
import org.helm.editor.worker.MonomerManagerLauncher;

public class MonomerMergeAction extends AbstractAction {

	
	 private MacromoleculeEditor editor;

	    public MonomerMergeAction(MacromoleculeEditor editor) {
	        super("Launch Temporary Monomer Registration ...");
	        this.editor = editor;
	    }

	    public void actionPerformed(ActionEvent e) {
	        MonomerMergeManager mm = new MonomerMergeManager(editor, false);
	        mm.setLocationRelativeTo(editor.getFrame());
	        mm.setVisible(true);
	    }
	    
	

}
