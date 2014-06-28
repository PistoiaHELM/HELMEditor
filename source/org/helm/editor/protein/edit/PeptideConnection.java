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
package org.helm.editor.protein.edit;

/**
 * 
 * @author ZHANGTIANHONG
 */
public class PeptideConnection {

	public static final String DISULFIDE_BOND = "Disulfide";
	public static final String AMIDE_BOND = "Amide";
	public static final String[] VALID_BOND_TYPES = { DISULFIDE_BOND,
			AMIDE_BOND };
	private String bondType;
	private int sourceSequenceIndex;
	private int sourceAminoAcidIndex;
	private int targetSequenceIndex;
	private int targetAminoAcidIndex;

	public String getBondType() {
		return bondType;
	}

	public void setBondType(String bondType) {
		this.bondType = bondType;
	}

	public int getSourceSequenceIndex() {
		return sourceSequenceIndex;
	}

	public void setSourceSequenceIndex(int sourceSequenceIndex) {
		this.sourceSequenceIndex = sourceSequenceIndex;
	}

	public int getSourceAminoAcidIndex() {
		return sourceAminoAcidIndex;
	}

	public void setSourceAminoAcidIndex(int sourceAminoAcidIndex) {
		this.sourceAminoAcidIndex = sourceAminoAcidIndex;
	}

	public int getTargetAminoAcidIndex() {
		return targetAminoAcidIndex;
	}

	public void setTargetAminoAcidIndex(int targetAminoAcidIndex) {
		this.targetAminoAcidIndex = targetAminoAcidIndex;
	}

	public int getTargetSequenceIndex() {
		return targetSequenceIndex;
	}

	public void setTargetSequenceIndex(int targetSequenceIndex) {
		this.targetSequenceIndex = targetSequenceIndex;
	}

	public boolean isValidBondType(String bondType) {
		for (String type : VALID_BOND_TYPES) {
			if (type.equalsIgnoreCase(bondType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return bondType + "=" + sourceSequenceIndex + ":"
				+ sourceAminoAcidIndex + "-" + targetSequenceIndex + ":"
				+ targetAminoAcidIndex;
	}
}
