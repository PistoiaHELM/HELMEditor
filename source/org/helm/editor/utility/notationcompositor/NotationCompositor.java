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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import y.base.Graph;
import y.base.Node;

import org.helm.editor.data.GraphManager;

public class NotationCompositor {

	// order is important
	private static ArrayList<NotationCreator> _creators = new ArrayList<NotationCreator>(){{
		add(new NotationCoreCreator()); add(new RegularEdgesCreator());
		add(new PairEdgesCreator()); add(new HyperNodeCreator());
	}};
	
	public static final String NOTATION_DELIMETER = "|";
    public static final String NOTATION_BEGINING = "{";
    public static final String NOTATION_ENDING = "}";    
    public static final String NOTATION_PART_ENDING = "$";
    public static final String NOTATION_COMMA = ",";
    public static final String NOTATION_PAIRABLE_EDGES = "pair";
    public static final String NOTATION_EMPTY = "$$$$";
    public static final String EMPTY_STRING = "";
	
	public String getExtendedNotation(GraphManager graphManager){
		
		Graph hyperGraph = graphManager.getHyperGraph();
		if (hyperGraph.isEmpty()) {
            return EMPTY_STRING;
        }
		
		StringBuilder notation = new StringBuilder();
		
		Map<Node, String> nameMap = new HashMap<Node, String>();
		Object[] args = new Object[]{hyperGraph, nameMap};
								
		for(NotationCreator currCreator : _creators){
			notation.append(currCreator.createNotationPart(args));
		}
		notation.append(NOTATION_PART_ENDING);
		
		return notation.toString();
	}
	
	public static boolean checkNotation(String notation){
		if (notation != null && !notation.equals(EMPTY_STRING) && !notation.equals(NOTATION_EMPTY)){
			return true;
		}
		
		return false;
	}
	
}
