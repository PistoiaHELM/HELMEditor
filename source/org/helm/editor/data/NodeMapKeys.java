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

/**
 * 
 * @author lih25
 */
public class NodeMapKeys {

	public static final Object MONOMER_REF = "Monomer Reference";

	public static final Object MONOMER_POSITION = "Monomer position";

	public static final Object NODE2PAIR_NODE = "node to pair node";
	public static final Object NODE2STARTING_NODE = "node to starting node";

	/**
	 * if the node is the starting node
	 */
	// public static final Object IS_STARTING = "Is Starting Node";

	public static final Object HYPERNODE_POLYMER_TYPE = "NODE TYPE";
	public static final Object HYPERNODE_POLYMER_NOTATION = "HyperNode Polymer Notation";
	public static final Object HYPERNODE = "Hyper Node";
	public static final Object EXSMIELS = "EXSMIELS";
	// RNA1, PEPTIDE1
	public static final Object HYPERNODE_NAME = "Hyper Node name";

	public static final Object NODE2PARENT_HYPERNODE = "Parent hyperNode";
	public static final Object HYPERNODE2STARTING_NODE = "map from hypernode to starting node";
	public static final Object HYPERNODE2INDEX = "hypernode index";

	public static final Object POSITION = "Monomer Notation";
	/**
	 * indicate anotations. for a RNA sequence, this could be sense or antisense
	 */
	public static final Object HYPERNODE_ANOTATION = "ANOTATION";

	public static final Object FOLDER_NODE_NOTATION = "Folder node notation";

	public static final String LABEL_INFO_MAP = "map for node labels";

}
