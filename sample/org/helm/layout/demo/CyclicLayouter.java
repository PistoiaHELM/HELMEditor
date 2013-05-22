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
package org.helm.layout.demo;
import java.util.ArrayList;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.CanonicMultiStageLayouter;
import y.layout.ComponentLayouter;
import y.layout.GraphLayout;
import y.layout.LayoutGraph;
import y.layout.circular.CircularLayouter;
import y.layout.circular.SingleCycleLayouter;

public class CyclicLayouter extends CanonicMultiStageLayouter
{
  double minimalNodeDistance = 40;
  
  /**
   * Creates a new instance of DiagonalLayouter
   */
  public CyclicLayouter()
  {
    //do not use defualt behaviour. we handle parallel edge routing ourselves.  
    setParallelEdgeLayouterEnabled(false);
    ComponentLayouter cl = (ComponentLayouter)getComponentLayouter();
    cl.setGridSpacing(20);
    cl.setStyle(ComponentLayouter.STYLE_SINGLE_COLUMN);
  }
  
  /**
   * Sets the minimal distance between nodes.
   */
  public void setMinimalNodeDistance(double d)
  {
    minimalNodeDistance = d;
  }
  
  /**
   * Returns the minimal distance between nodes.
   */
  public double getMinimalNodeDistance()
  {
    return minimalNodeDistance;
  }
  
  /**
   * Returns always <code>true</code>, because every graph can be
   * layed out.
   */
  public boolean canLayoutCore(LayoutGraph graph)
  {
    return true;
  }
  
  /**
   * Perform the layout.
   */
  protected void doLayoutCore(LayoutGraph graph)
  {
	NodeMap selected = graph.createNodeMap();
	graph.addDataProvider(SELECTED_NODES, selected);
	
	
	Node[][] cycles = analyzeGraphCycles(graph);
    
    int maxCycleSize = 1;
    for (Node[] cycle : cycles) {
    	if (cycle.length > maxCycleSize) {
    		maxCycleSize = cycle.length;
    	}
    }
    
    int verticalSize = maxCycleSize / 2 + 2;
    
    int offset = 0;
    int stepSize = 75;
    double radius = 60.0;
	
    Node lastLayouted = null;
    boolean firstcycle = true;
    for (int i = 0; i < cycles.length; i++) {
    	
    	firstcycle = firstcycle && (i != cycles.length - 1);
    	Node[] cycle = cycles[i];
    	int cycleSize = cycle.length;
		if (cycleSize == 1) {
			graph.setLocation(cycle[0], offset + stepSize, verticalSize * stepSize);
			offset += stepSize;
			lastLayouted = cycle[0];
			continue;
		}
		
		int index = 0;
		for (int ii = 0; ii < cycle.length; ii ++) {
			Node n = cycle[ii];
			
			if (n.neighbors().size() > 2) {
				index = ii;
				break;
			}
		}
		
		
		layoutCycleManual(graph, cycle, radius, index, firstcycle);
		firstcycle = false;
	
		double xOffset = lastLayouted == null ? 0 : 
			graph.getLocation(lastLayouted).getX() - graph.getLocation(cycle[index]).getX() + stepSize;
		double yOffset = lastLayouted == null ? verticalSize * stepSize 
				: graph.getLocation(lastLayouted).getY() - graph.getLocation(cycle[index]).getY();
			
		shiftNodes(graph, cycle, xOffset, yOffset);
		
		offset += stepSize;
    	lastLayouted = cycle[index];

    }
    
   }
  
    // retrurn radius
	private double layoutCycle(final LayoutGraph graph, final Node[] cycle) {
		// the graph.
		
		NodeMap selected = graph.createNodeMap();
		CircularLayouter cl = new CircularLayouter();
		SingleCycleLayouter scl = cl.getSingleCycleLayouter();
		//scl.setFixedRadius(800.0);
		scl.setInitialAngle(Math.PI);
		scl.setSubgraphLayouterEnabled(true);
		cl.setSubgraphLayouterEnabled(true);
		
		for (Node n : graph.getNodeArray()) {
			selected.setBool(n, false);
		}
		for (Node n : cycle) {
			selected.setBool(n, true);
		}
		graph.addDataProvider(SELECTED_NODES, selected);
			
		GraphLayout gl = scl.calcLayout(graph);
		for (Node n : graph.getNodeArray()) {
			double x = gl.getNodeLayout(n).getX();
			double y = gl.getNodeLayout(n).getY();
			graph.setLocation(n, x, y);
		}
		
		return cl.getSingleCycleLayouter().getLastAppliedRadius();
	}
	
	private void shiftNodes(LayoutGraph graph, Node[] nodes, double xOffset, double yOffset) {
		for (Node n : nodes) {
			double x = graph.getLocation(n).x;
			double y = graph.getLocation(n).y;
		
			graph.setLocation(n, xOffset + x, yOffset + y);
				
		}
	}
  
  private Node[][] analyzeGraphCycles(LayoutGraph graph) {
	  ArrayList<Node[]> result = new ArrayList<Node[]>();
	  
	  Node[] nodes = graph.getNodeArray();
	  
	  int afterLastCycle = 0;
	  
	  for (int i = 0; i < nodes.length; i++) {
		  NodeCursor neighbors = nodes[i].neighbors();
		  neighbors.toFirst();
		  while (neighbors.ok()) {
			  Node neighbor = neighbors.node();
			  int index = neighbor.index();
			  if (index < i - 1) {
				  // store cycles from one element
				  for (int j = afterLastCycle; j < index; j++) {
					  Node[] littleCycle = new Node[1];
					  littleCycle[0] = nodes[j];
					  result.add(littleCycle);
				  }
				  
				  Node[] cycle = new Node[i - index + 1];
				  for (int j = index; j <= i; j++) {
					  cycle[j - index] = nodes[j];
				  }
				  result.add(cycle);
				  
				  afterLastCycle = i + 1;
			  }
			  neighbors.next();
		  }
		  
	  }
	  
	  // store cycles from one element
	  for (int i = afterLastCycle; i < nodes.length; i++) {
		  Node[] littleCycle = new Node[1];
		  littleCycle[0] = nodes[i];
		  result.add(littleCycle);
	  }

	  return result.toArray(new Node[0][]);
  
  }
  
  private void layoutCycleManual(LayoutGraph graph, final Node[] cycle, double radius, int startIndex, boolean clockWise) {
	  int size = cycle.length;
	  int orient = clockWise ? 1 : -1;
	  
	  for (int i = 0; i < size; i++) {
		  Node n = cycle[(startIndex + i) % size];
		  graph.setLocation(n, orient * radius * Math.cos(2 * Math.PI * i/ size), 
				  orient * radius * Math.sin(2 * Math.PI * i/ size));
	  }
  }

  
  //private void layout 
  
}
