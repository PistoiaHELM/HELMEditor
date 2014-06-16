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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import y.base.Node;

public class EmptyChemSequenceHolder implements ChemSequenceHolder {
	private static ChemSequenceHolder instance = new EmptyChemSequenceHolder();

	public static ChemSequenceHolder getInstance() {
		return instance;
	}

	public List<NodeSequence> getConnectedSequences(Node dockNode) {
		return Collections.emptyList();
	}

	public List<Node> getDockedNodes() {
		return Collections.emptyList();
	}

	public Set<NodeSequence> getDockedSequences() {
		return Collections.emptySet();
	}

	public List<Node> getPendantNodes() {
		return Collections.emptyList();
	}

	public Set<NodeSequence> getSequences() {
		return Collections.emptySet();
	}

}
