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
package org.helm.editor.tests;

import java.io.IOException;
import java.util.ArrayList;

import org.jdom.JDOMException;

import y.base.Graph;
import y.base.NodeList;
import y.view.Graph2D;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.notation.StructureException;
import org.helm.editor.componentPanel.SequenceViewPanes;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphPair;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.ListNotComparableException;
import org.helm.editor.utility.NotationParser;

import junit.framework.TestCase;

/**
 * Test case for GraphToNotationTransator class 
 * @author Makarov Alexander
 * @version 1.0
 */
public class GraphToNotationTest extends TestCase {

	private ArrayList<String> _notationList;
	
	// constants for update hyper graph
	private static final String UHG_BEGIN_NOTATION = "RNA1{R(G)P.R(G)P.R(A)}$$$$";
	private static final String UHG_NOTATION_AFTER_NODE_ADDING = "RNA1{R(G)P.R(G)P.R(A)P.R(T)}$$$$";
	private static final String UHG_NOTATION_NEW_CHAIN = "RNA1{R(G)P.R(G)P.R(A)}|PEPTIDE1{I}$$$$";
		
	private static Graph UHG_AFTER_NODE_ADDING;
	private static Graph UHG_NEW_CHAIN;
	
	static{	
		try {
			UHG_AFTER_NODE_ADDING = (NotationParser.getGraphPair(UHG_NOTATION_AFTER_NODE_ADDING).getGraphManager()).getHyperGraph();
			UHG_NEW_CHAIN 		  = (NotationParser.getGraphPair(UHG_NOTATION_NEW_CHAIN).getGraphManager()).getHyperGraph();
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public GraphToNotationTest() {		
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		_notationList = new ArrayList<String>(){{
			//add("RNA1{R(A)}|RNA2{R(A)P.R(A)}|CHEM1{5Bio}$RNA1,CHEM1,1:R1-1:R1$$$"); //mixture siRNA 
			//add("RNA1{R(A)P.R(A)P.R(A)P.R(A)P.R(A)P.R(A)P.R(A)P.R(A)P.R(A)P.R(U)P.R(A)P.[fR](A)}$$$$"); //simple RNA
			//add("CHEM1{PEG3}$$$$"); //crosslink
			//add("RNA1{R(A)P.RP}$$$$");
			//add("RNA1{R(G)P.R(G)P.[mR](A)}|PEPTIDE1{L.Y.V.G.I}$$$$");	
			
//          //backbone cyclic RNA
            add("RNA1{R(C)P.[dR](U)[sP]}$RNA1,RNA1,6:R2-1:R1$$$");
//
//            //branch cyclic RNA
//            add("RNA1{R(C)P.RP.R(A)P.RP.R(A)P.R(U)P}$RNA1,RNA1,4:R3-9:R3$$$");
//
//            //backbone and branch cyclic RNA
//            add("RNA1{R(C)P.RP.R(A)P.RP.R(A)P.R(U)P}$RNA1,RNA1,1:R1-16:R2|RNA1,RNA1,4:R3-9:R3$$$");

            //backbone cyclic peptide
//            add("PEPTIDE1{K.A.A.G.K}$PEPTIDE1,PEPTIDE1,1:R1-5:R2$$$");
		}};
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

//	public void testGraphCreateGraphPair() throws Exception {
//	   // try {
//			for (String currNotation : _notationList) {        
//				GraphPair graphPair = NotationParser.getGraphPair(currNotation);
//	        }
//	  //  } catch (Exception e) {
//	  //  	fail(e.getMessage());
//	//		e.printStackTrace();
//	//	}		
//	}

	public void testCreateNewNotation() throws Exception {
		for (String currNotation : _notationList) {        
					GraphPair graphPair = NotationParser.getGraphPair(currNotation);
					Graph2NotationTranslator.updateHyperGraph(graphPair.getGraph(), graphPair.getGraphManager());
					
					String newNotation = Graph2NotationTranslator.getNewNotation(graphPair.getGraphManager());
					assertEquals(currNotation, newNotation);					
	    }
	     
	}
	
	public void testUpdateHyperGraph(){
		try {
			GraphPair testGraphPair = NotationParser.getGraphPair(UHG_BEGIN_NOTATION);
			
			Graph2D graph = testGraphPair.getGraph();
			
			GraphManager graphManager = testGraphPair.getGraphManager();
			SequenceViewPanes panel = new SequenceViewPanes("");
			panel.setNotation(UHG_BEGIN_NOTATION);
//			panel.init(graph, graphManager);
			
			panel.setNotation(UHG_NOTATION_AFTER_NODE_ADDING);
			NodeList expectedNodeList = new NodeList(UHG_AFTER_NODE_ADDING.getNodeArray());
			NodeList actualNodeList = new NodeList(graphManager.getHyperGraph().getNodeArray());
			if (Graph2NotationTranslator.compareNodeList(expectedNodeList, 
					actualNodeList) != 0){
				fail();				
			}
			
			panel.setNotation(UHG_NOTATION_NEW_CHAIN);
			expectedNodeList = new NodeList(UHG_NEW_CHAIN.getNodeArray());
			actualNodeList = new NodeList(graphManager.getHyperGraph().getNodeArray());
			if (Graph2NotationTranslator.compareNodeList(expectedNodeList, 
					actualNodeList) != 0){
				fail(); 
			}
						
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		} 
	}
	
}
