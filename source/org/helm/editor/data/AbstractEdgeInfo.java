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
package org.helm.editor.data;

import org.helm.editor.componentPanel.sequenceviewpanel.EdgeType;
import org.helm.notation.model.Attachment;

public abstract class AbstractEdgeInfo implements EdgeInfo {
	protected Attachment sourceNodeAttachment;
	protected Attachment targetNodeAttachment;
	protected EdgeType type;

	protected AbstractEdgeInfo(Attachment sourceNodeAtt,
			Attachment targetNodeAtt) {
		this.sourceNodeAttachment = sourceNodeAtt;
		this.targetNodeAttachment = targetNodeAtt;

		this.type = EdgeType.getType(sourceNodeAtt, targetNodeAtt);
	}

	protected AbstractEdgeInfo() {

	}

	public Attachment getSourceNodeAttachment() {
		return sourceNodeAttachment;
	}

	public void setSourceNodeAttachment(Attachment sourceNodeAttachment) {
		this.sourceNodeAttachment = sourceNodeAttachment;
	}

	public Attachment getTargetNodeAttachment() {
		return targetNodeAttachment;
	}

	public void setTargetNodeAttachment(Attachment targetNodeAttachment) {
		this.targetNodeAttachment = targetNodeAttachment;
	}

	public EdgeType getType() {
		return type;
	}

	public void setType(EdgeType type) {
		this.type = type;
	}

	public boolean isPBranchBackbone() {
		return EdgeType.BRANCH_BACKBONE.equals(type);
	}

	public boolean isPBranchBranch() {
		return EdgeType.BRANCH_BRANCH.equals(type);
	}

	public boolean isPair() {
		return EdgeType.PAIR.equals(type);
	}

	public boolean isRegular() {
		return !isPair() && !isPBranchBackbone() && !isPBranchBranch();
	}
}
