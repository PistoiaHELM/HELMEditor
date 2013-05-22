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
package org.helm.editor.utility.notationcompositor;

import java.util.Map;

import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;

import org.helm.editor.data.NodeMapKeys;
import org.helm.notation.model.Monomer;

public class NotationCoreCreator implements NotationCreator{

	private static final int HYPER_GRAPH_POSITION = 0;
	private static final int NAME_MAP_POSITION = 1;
	
	@SuppressWarnings("unchecked")
	public String createNotationPart(Object[] args) {

		Graph hyperGraph = (Graph)args[HYPER_GRAPH_POSITION];
		Map<Node, String> nameMap = (Map<Node, String>)args[NAME_MAP_POSITION];
		
		StringBuilder notation = new StringBuilder();
		
		int rnaCount = 0;
        int peptideCount = 0;
        int chemCount = 0;
		
        NodeCursor hyperNodes = hyperGraph.nodes();
        
		NodeMap hyperNodePolymerNotationMap = (NodeMap) hyperGraph.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_NOTATION);
		NodeMap hyperNodePolymerTypeMap = (NodeMap) hyperGraph.getDataProvider(NodeMapKeys.HYPERNODE_POLYMER_TYPE);
                NodeMap smilesMap = (NodeMap) hyperGraph.getDataProvider(NodeMapKeys.EXSMIELS);
		
		String type = null;
        for (; hyperNodes.ok(); hyperNodes.next()) {
        	Node currentNode = hyperNodes.node();
            
        	if (notation.length() > 0){
        		notation.append(NotationCompositor.NOTATION_DELIMETER);
        	}
        	
            type = (String) hyperNodePolymerTypeMap.get(currentNode);
            if (type.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                rnaCount++;
                
                notation.append( createNotationString(currentNode, nameMap, hyperNodePolymerNotationMap, null, type, rnaCount) );
            } else if (type.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
                peptideCount++;
                
                notation.append( createNotationString(currentNode, nameMap, hyperNodePolymerNotationMap, null, type, peptideCount) );
            } else if (type.equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)){
                chemCount++;
                
                // TY
                notation.append( createNotationString(currentNode, nameMap, hyperNodePolymerNotationMap, smilesMap, type, chemCount) );
            }
            
            
        }
        notation.append(NotationCompositor.NOTATION_PART_ENDING);
        
		return notation.toString();
		
	}
	
	private static String createNotationString(Node hyperNode, Map<Node, String> nameMap, 
			NodeMap hyperNodePolymerNatationMap, NodeMap smilesMap, String type, int count){
		StringBuilder notationString = new StringBuilder();
		
        nameMap.put(hyperNode, type + count);
        notationString.append(type);
        notationString.append(count);
        
        notationString.append(NotationCompositor.NOTATION_BEGINING);
        
        
        // TY
        String smiles = smilesMap == null ? null : (String)smilesMap.get(hyperNode);
        notationString.append(smiles != null ? smiles : (String) hyperNodePolymerNatationMap.get(hyperNode) );
        
        
        notationString.append(NotationCompositor.NOTATION_ENDING);
		
		return notationString.toString();
	}

}
