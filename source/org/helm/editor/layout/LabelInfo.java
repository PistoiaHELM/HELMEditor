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
package org.helm.editor.layout;

import java.util.ArrayList;
import java.util.List;

import y.base.Node;

public class LabelInfo {
	private int positionNumber;
	private String terminalLabel;
	private List<Node> linkers = new ArrayList<Node>();
	private boolean isFlipped;
	private String leftLinker;
	private String rightLinker;

	public int getPositionNumber() {
		return positionNumber;
	}

	public void setPositionNumber(int number) {
		this.positionNumber = number;
	}

	public String getTerminalLabel() {
		return terminalLabel;
	}

	public void setTerminalLabel(String terminalLabel) {
		this.terminalLabel = terminalLabel;
	}

	public List<Node> getLinkers() {
		return linkers;
	}

	public String getLeftLinker() {
		return leftLinker;
	}

	public void setLeftLinker(String leftLinker) {
		this.leftLinker = leftLinker;
	}

	public String getRightLinker() {
		return rightLinker;
	}

	public void setFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}

	public boolean isFlipped() {
		return isFlipped;
	}

	public void setRightLinker(String rightLinker) {
		this.rightLinker = rightLinker;
	}

}
