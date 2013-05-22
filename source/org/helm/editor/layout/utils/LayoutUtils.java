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
package org.helm.editor.layout.utils;

import java.awt.Point;

import y.base.Edge;
import y.base.Node;
import y.geom.YPoint;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import y.view.Port;

public class LayoutUtils {

	public static double getCenterX(Graph2D graph, Node n) {
		return graph.getRealizer(n).getCenterX();
	}
	
	public static double getCenterY(Graph2D graph, Node n) {
		
		if (graph == null || n == null || graph.getRealizer(n) == null){
			return 0.0;
		}
		
		return graph.getRealizer(n).getCenterY();
	}
	
	public static boolean isEdgeFromLeft2Right(Node source, Edge edge, Graph2D graph) {
    	
    	NodeRealizer sr = graph.getRealizer(source);
    	NodeRealizer nr = graph.getRealizer(edge.opposite(source));
    	
    	return (sr.getCenterX() < nr.getCenterX());
    }
    
    public static Node getHorizontalSuccessor(boolean left2right, Node node, Graph2D graph) {
    	double nodeX = getCenterX(graph, node);
    	double nodeY = getCenterY(graph, node);
    	int compare = left2right ? 1 : -1;
    	Node result = null;
    	for (Node n : graph.getNodeArray()) {
    		if (Double.compare(nodeY, getCenterY(graph, n)) != 0) {
    			continue;
    		}
    		double currX = getCenterX(graph, n); 
    		if (Double.compare(nodeX, currX) == compare) {
    			if (result == null) {
    				result = n;
    				continue;
    			}
    			if (Double.compare(getCenterX(graph, result), currX) == -compare) {
    				result = n;
    			}
    		}
    	}
    	return result;
    }

    public static void offsetNodes(Graph2D graph2D, Node start, boolean left2right, double offset) {
    	Node next = getHorizontalSuccessor(left2right, start, graph2D);
    	while (next != null) {
    		NodeRealizer r = graph2D.getRealizer(next);
    		r.setCenterX(r.getCenterX() + offset);
            r.repaint();
            next = getHorizontalSuccessor(left2right, next, graph2D);
    	}
    }
    
	public static double calculateAngle(double dx, double dy) {
		double alpha;
		if (Math.abs(dx) > Math.abs(dy)) {
			double tg = dy / dx;
			alpha = Math.atan(tg);

			if (dx < 0) {
				alpha += Math.PI;
			}
		} else {
			double ctg = dx / dy;
			alpha = Math.PI / 2. - Math.atan(ctg);

			if (dy < 0) {
				alpha += Math.PI;
			}
		}
		
		return alpha;
	}
    
	public static void cutEdge(Graph2D graph, Edge e, double cutSourceSize, double cutTargetSize) {
		EdgeRealizer er = graph.getRealizer(e); 
		NodeRealizer sourceRealizer = graph.getRealizer(e.source());
		NodeRealizer targetRealizer = graph.getRealizer(e.target());

		YPoint nextAfterSourcePoint = null;
		YPoint prevBeforeTargetPoint = null;
		
		int pointCount = er.pointCount();
		if (pointCount == 0) {
			nextAfterSourcePoint = new YPoint(targetRealizer.getCenterX(), targetRealizer.getCenterY());
			prevBeforeTargetPoint = new YPoint(sourceRealizer.getCenterX(), sourceRealizer.getCenterY());
		} else {
			nextAfterSourcePoint = er.getPoint(0);
			prevBeforeTargetPoint = er.getPoint(pointCount - 1);
		}
		
		double dx = nextAfterSourcePoint.x - sourceRealizer.getCenterX();
		double dy = nextAfterSourcePoint.y - sourceRealizer.getCenterY();
		double sourceAlpha = LayoutUtils.calculateAngle(dx, dy);

		dx = prevBeforeTargetPoint.x - targetRealizer.getCenterX();
		dy = prevBeforeTargetPoint.y - targetRealizer.getCenterY();
		double targetAlpha = LayoutUtils.calculateAngle(dx, dy);
		
		double xSourceShift = (sourceRealizer.getWidth() / 2 + cutSourceSize) * Math.cos(sourceAlpha);
		double ySourceShift = (sourceRealizer.getHeight() / 2 + cutSourceSize) * Math.sin(sourceAlpha);
		double xTargetShift = (targetRealizer.getWidth() / 2 + cutTargetSize) * Math.cos(targetAlpha);
		double yTargetShift = (targetRealizer.getHeight() / 2 + cutTargetSize) * Math.sin(targetAlpha);
		
		er.setSourcePort(new Port(xSourceShift, ySourceShift));
		er.setTargetPort(new Port(xTargetShift, yTargetShift));
	}
//
//	// TODO replace
//	public static void cutSequentEdge(Edge edge, Graph2D graph) {
//		EdgeRealizer er = graph.getRealizer(edge);
//		Node source = edge.source();
//		Node target = edge.target();
//		NodeRealizer sourceRealizer = graph.getRealizer(source);
//		NodeRealizer targetRealizer = graph.getRealizer(target);
//		// if source node is on the top
//		if (sourceRealizer.getCenterY() < targetRealizer.getCenterY()) {
//			er.setSourcePort(new Port(0, sourceRealizer.getHeight() / 2));
//			er.setTargetPort(new Port(0, -targetRealizer.getHeight() / 2));
//
//		} else { // target node is at the top
//			er.setTargetPort(new Port(0, targetRealizer.getHeight() / 2));
//			er.setSourcePort(new Port(0, -sourceRealizer.getHeight() / 2));
//		}
//	}
//	
	public static Point toWorldCoordinates(Graph2DView view, Point point) {
	
	    if (view == null || point == null) {
	        return null;
	    }
	
	    int xInWorldCoord = (int) view.toWorldCoordX(point.x);
	    int yInWorldCoord = (int) view.toWorldCoordY(point.y);
	
	    Point pointInWorldCoords = new Point(xInWorldCoord, yInWorldCoord);
	
	    return pointInWorldCoords;
	}

	public static boolean scalar(NodeRealizer startNode, NodeRealizer finalNode, NodeRealizer chemNode) {
	    double firstVecX = chemNode.getCenterX() - startNode.getCenterX();
	    double firstVecY = chemNode.getCenterY() - startNode.getCenterY();
	
	    double secondVecX = finalNode.getCenterX() - startNode.getCenterX();
	    double secondVecY = finalNode.getCenterY() - startNode.getCenterY();
	
	    return firstVecX * secondVecX + firstVecY * secondVecY > 0;
	}
	
	public static DoublePoint getPoint(Graph2D graph, Node n1, Node n2) {
		double x1 =  LayoutUtils.getCenterX(graph, n1);
		double y1 =  LayoutUtils.getCenterY(graph, n1);
			
		double x2 = LayoutUtils.getCenterX(graph, n2);
		double y2 = LayoutUtils.getCenterY(graph, n2);
		
		return new DoublePoint(x2 - x1, y2 - y1); 
	}
	
	public static double dist(Graph2D graph, Node n1, Node n2) {
		DoublePoint p = getPoint(graph, n1, n2);
		return Math.sqrt(p.x * p.x + p.y * p.y);
	}
	
	public static double cos(Graph2D graph, Node n1, Node n2) {
		DoublePoint p = getPoint(graph, n1, n2); 
		return cos(p);
	}

	private static double cos(DoublePoint p) {
		return p.x / Math.sqrt (p.x * p.x + p.y * p.y);
	}
	
	public static double sin(Graph2D graph, Node n1, Node n2) {
		DoublePoint p = getPoint(graph, n1, n2); 
		return sin(p);
	}

	private static double sin(DoublePoint p) {
		return p.y / Math.sqrt (p.x * p.x + p.y * p.y);
	}
	
	public static double tan(Graph2D graph, Node n1, Node n2) {
		DoublePoint p = getPoint(graph, n1, n2); 
		if (Double.compare(0.0, p.y)  == 0) {
			return p.x > 0 ? Double.MAX_VALUE : Double.MIN_VALUE;
		}
		return p.x / p.y;
	}
	
	public static double ctan(Graph2D graph, Node n1, Node n2) {
		DoublePoint p = getPoint(graph, n1, n2); 
		if (Double.compare(0.0, p.x)  == 0) {
			return p.y > 0 ? Double.MAX_VALUE : Double.MIN_VALUE;
		}
		return p.y / p.x;
	}
	
	public static class DoublePoint {
		public double x, y;
		
		public DoublePoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public static boolean isXLine(Graph2D graph, Node n1, Node n2) {
		return Double.compare(getCenterY(graph, n1),
				getCenterY(graph, n2)) == 0;
	}

	public static boolean isYLine(Graph2D graph, Node n1, Node n2) {
		return Double.compare(getCenterX(graph, n1),
				getCenterX(graph, n2)) == 0;
	}

	public static boolean isCloser(Graph2D graph, Node src, Node close, Node far) {
		double d1 = dist(graph, src, close);
		double d2 = dist(graph, src, far);
		
		return d1 < d2;
	}

	public static boolean isLeft(Graph2D graph, Node src, Node left) {
		DoublePoint p = getPoint(graph, src, left);
		return  (Double.compare(p.y, 0.0) == 0) && p.x < 0;
	}

	public static boolean isRight(Graph2D graph, Node src, Node right) {
		DoublePoint p = getPoint(graph, src, right);
		return (Double.compare(p.y, 0.0) == 0) && p.x > 0;
	}
	
	public static boolean crosses(Graph2D graph, Node e1b, Node e1e, Node e2b, Node e2e) {
		if (!graph.containsEdge(e1b, e1e) && !graph.containsEdge(e1e, e1b)) {
			throw new IllegalArgumentException("There is no edge between " + e1e + " and " + e1b);
		}	
		if (!graph.containsEdge(e2b, e2e) && !graph.containsEdge(e2e, e2b)) {
			throw new IllegalArgumentException("There is no edge between " + e2e + " and " + e2b);
		}
		return lineCrosses(getCenterX(graph, e1b), getCenterY(graph, e1b),
				getCenterX(graph, e1e), getCenterY(graph, e1e),
				getCenterX(graph, e2e), getCenterY(graph, e2e),
				getCenterX(graph, e2b), getCenterY(graph, e2b));		
	}
	
	public static boolean lineCrosses(double x1,double y1,
			double x2, double y2,
			double x3, double y3,
			double x4, double y4) {
		return oppositeSides(x1, y1, x2, y2, x3, y3, x4, y4) 
			&& oppositeSides(x3, y3, x4, y4, x1, y1, x2, y2);
	}
	
	public static boolean lineCrosses(DoublePoint p1, DoublePoint p2, DoublePoint p3, DoublePoint p4) {
		return lineCrosses(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	}
	
	private static boolean oppositeSides(double x1,double y1,
			double x2, double y2,
			double x3, double y3,
			double x4, double y4) {
		return oppositeVectors(x1 - x3, y1 - y3, x2 - x3, y2 - y3, x4 - x3, y4 - y3);
	}
	
	private static boolean oppositeVectors(double x1, double y1,
			double x2, double y2,
			double x3, double y3) {
		DoublePoint p1 = rotate(x1, y1, x3, y3);
		DoublePoint p2 = rotate(x2, y2, x3, y3);
		return p1.y * p2.y < 0;
	}
	
	private static DoublePoint rotate(double x, double y, double ax, double ay) {
		DoublePoint a = new DoublePoint(ax, ay);
		return new DoublePoint(x * cos (a) + y * sin (a), y * cos(a) - x * sin(a));
	}
}
