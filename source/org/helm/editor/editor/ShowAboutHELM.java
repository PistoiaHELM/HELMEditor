/**
 * 
 */
package org.helm.editor.editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ShowAboutHELM extends AbstractAction {

	public ShowAboutHELM() {
		super("About HELM Editor");
	}

	public void actionPerformed(ActionEvent e) {
		HELMAboutBox aboutBox = new HELMAboutBox(null, true);
		aboutBox.setVisible(true);
	}
}