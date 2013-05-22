/**
 * *****************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *****************************************************************************
 */
package org.helm.editor.layout.demo;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;

public class Main {

    private static Graph2D graph;
    private static Graph2DView graphViewer;

    public static void main(String[] args) {
        System.out.println("Started!");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createTestGraph9();
                    initUI();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void initUI() {
        JFrame mainFrame = new JFrame("Visualization");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graphViewer = new Graph2DView();
        graphViewer.setGraph2D(graph);
        mainFrame.getContentPane().add(graphViewer);

        graphViewer.setZoom(0.5);
        CyclicLayouter layouter = new CyclicLayouter();
        //graphViewer.applyLayout(layouter);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private static void createTestGraph1() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();

        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node1);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node5);
    }

    private static void createTestGraph2() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();

        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node2);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node6);
    }

    private static void createTestGraph3() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();
        Node node11 = graph.createNode();
        Node node12 = graph.createNode();
        Node node13 = graph.createNode();
        Node node14 = graph.createNode();
        Node node15 = graph.createNode();

        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);
        graph.createEdge(node9, node10);
        graph.createEdge(node10, node11);
        graph.createEdge(node11, node4);
        graph.createEdge(node11, node12);
        graph.createEdge(node12, node13);
        graph.createEdge(node14, node15);
        graph.createEdge(node15, node13);
    }

    private static void createTestGraph4() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();

        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node1);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node4);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
    }

    private static void createTestGraph5() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();

        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node1);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);
        graph.createEdge(node9, node10);
        graph.createEdge(node10, node6);
    }

    private static void createTestGraph6() {
        graph = new Graph2D();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();
        Node node11 = graph.createNode();
        Node node12 = graph.createNode();
        Node node13 = graph.createNode();


        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node1);

        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);

        graph.createEdge(node9, node10);
        graph.createEdge(node10, node11);
        graph.createEdge(node11, node12);
        graph.createEdge(node12, node13);
        graph.createEdge(node13, node9);


        //graph.createEdge(node10, node6);		
    }

    private static void createTestGraph8() {
        graph = new Graph2D();
        Node node0 = graph.createNode();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();


        graph.createEdge(node0, node1);
        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node0);

        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);
        graph.createEdge(node9, node10);

        //graph.createEdge(node10, node6);		
    }

    private static void createTestGraph7() {
        graph = new Graph2D();
        Node node0 = graph.createNode();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();
        Node node11 = graph.createNode();
        Node node12 = graph.createNode();


        graph.createEdge(node0, node1);
        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node0);



        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);
        graph.createEdge(node9, node10);
        graph.createEdge(node10, node11);
        graph.createEdge(node11, node12);


        //graph.createEdge(node10, node6);		
    }

    private static void createTestGraph9() {
        graph = new Graph2D();
        Node node0 = graph.createNode();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Node node3 = graph.createNode();
        Node node4 = graph.createNode();
        Node node5 = graph.createNode();
        Node node6 = graph.createNode();
        Node node7 = graph.createNode();
        Node node8 = graph.createNode();
        Node node9 = graph.createNode();
        Node node10 = graph.createNode();


        graph.createEdge(node0, node1);
        graph.createEdge(node1, node2);
        graph.createEdge(node2, node3);
        graph.createEdge(node3, node4);
        graph.createEdge(node4, node5);
        graph.createEdge(node5, node6);
        graph.createEdge(node6, node7);
        graph.createEdge(node7, node0);



        graph.createEdge(node7, node8);
        graph.createEdge(node8, node9);
        graph.createEdge(node9, node10);


        Node node01 = graph.createNode();
        Node node11 = graph.createNode();
        Node node21 = graph.createNode();
        Node node31 = graph.createNode();
        Node node41 = graph.createNode();
        Node node51 = graph.createNode();
        Node node61 = graph.createNode();
        Node node71 = graph.createNode();
        Node node81 = graph.createNode();
        Node node91 = graph.createNode();
        Node node101 = graph.createNode();


        graph.createEdge(node01, node11);
        graph.createEdge(node11, node21);
        graph.createEdge(node21, node31);
        graph.createEdge(node31, node41);
        graph.createEdge(node41, node51);
        graph.createEdge(node51, node01);

        graph.createEdge(node51, node61);
        graph.createEdge(node61, node71);
        graph.createEdge(node71, node81);
        graph.createEdge(node81, node91);
        graph.createEdge(node91, node101);
        //graph.createEdge(node10, node6);		
    }
}
