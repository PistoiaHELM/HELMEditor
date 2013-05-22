/**
 * 
 */
package org.helm.editor.componentPanel.sequenceviewpanel;

import org.helm.notation.model.Attachment;

public enum EdgeType {
	REGULAR, MODIFIED_P, CHEM, PAIR, BRANCH_BACKBONE, BRANCH_BRANCH; 

	public static EdgeType getType(Attachment source, Attachment target) {
		if ((source == null) || (target == null)) {
			throw new IllegalArgumentException("Source and Target attachments must not be null");
		}
		String sL = source.getLabel();
		String tL = target.getLabel();
		
			
		if (sL.equals(Attachment.PAIR_ATTACHMENT) && tL.equals(Attachment.PAIR_ATTACHMENT)) {
			return PAIR;
		}
		
		if (sL.equals(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
			if (tL.equals(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
				return BRANCH_BRANCH;
			}
			if (tL.equals(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT)
					|| tL.equals(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT)) {
				return BRANCH_BACKBONE;
			}	
		}
		if (sL.equals(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT)
				|| sL.equals(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT)) {
			if (tL.equals(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)) {
				return BRANCH_BACKBONE;
			}
		}
		return REGULAR;
	}
}