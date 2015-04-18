package org.roche.antibody.ui.components;

/**
 * 
 * {@code AntibodyEditorAccess} encapsulates all-time-access to the antibody editor pane.
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:lanig@quattro-research.com">Marco Lanig</a>, quattro research GmbH
 * 
 * @version $Id$
 */
public class AntibodyEditorAccess {
	private static AntibodyEditorAccess _instance;
	private AntibodyEditorPane abEditor;

	private AntibodyEditorAccess() {
	}

	public static AntibodyEditorAccess getInstance() {
		if (_instance == null)
			_instance = new AntibodyEditorAccess();
		return _instance;
	}
	
	public void setAntibodyEditorPane(AntibodyEditorPane abEditor) {
		this.abEditor = abEditor;
	}
	
	public AntibodyEditorPane getAntibodyEditorPane() {
		return abEditor;
	}
	
}
