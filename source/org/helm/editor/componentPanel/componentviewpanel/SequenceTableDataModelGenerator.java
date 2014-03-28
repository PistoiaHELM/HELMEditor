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
package org.helm.editor.componentPanel.componentviewpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.jdesktop.swingworker.SwingWorker;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerStore;
import org.helm.notation.tools.ComplexNotationParser;

@Deprecated
public class SequenceTableDataModelGenerator extends SwingWorker<Void, Void> {

    private static final String NOTATION_BEGIN = "$";
    private static final String SPILT_REGEX = "[|]";
    private SequenceTableModel _tableModel = null;
    private String _notation;
    private final ArrayList<SequenceTableDataModel> list = new ArrayList<SequenceTableDataModel>();

    public SequenceTableDataModelGenerator(SequenceTableModel tableModel,
            String notation) {

        _tableModel = tableModel;
        _notation = notation;
    }

    @Override
    protected Void doInBackground() throws Exception {
    	
    	MonomerStore monomerStore=MonomerStoreCache.getInstance().getCombinedMonomerStore();

        // render the notation, then get the new one before decompose
//		Graph2DView viewer = new Graph2DView();
//		GraphPair pair = NotationParser.getGraphPair(_notation);
//		Graph2D graph = pair.getGraph();
//		GraphManager manager = pair.getGraphManager();
//		Graph2NotationTranslator.updateHyperGraph(graph, manager);
//		viewer.setGraph2D(graph);

//		SequenceLayout layout = new SequenceLayout(viewer, manager);
//		layout.doLayout();

//		viewer.updateView();
//		Graph2NotationTranslator.updateHyperGraph(graph,manager);
        String sortedNotation = _notation;

        String cutted = sortedNotation.substring(0, sortedNotation.indexOf(NOTATION_BEGIN));

        // In this collection we have right list of components
        ArrayList<String> components = new ArrayList<String>();

        Collections.addAll(components, ComplexNotationParser.decompose(sortedNotation,monomerStore));

        // In this collection we have right order
        Queue<String> orderedComponents = new LinkedList<String>();
        Collections.addAll(orderedComponents, cutted.split(SPILT_REGEX));

        int rowNumber = 1;

        // by all components
        while (!orderedComponents.isEmpty()) {
            String currentSplit = orderedComponents.poll();
            // keep order
            for (int j = 0; j < components.size(); j++) {
                if (components.get(j).contains(currentSplit)) {
                    String component = components.get(j);

                    SequenceTableDataModel tableModel = SequenceTableDataModel.createSequenceTableDataModel(component,monomerStore);
                    tableModel.setAnnotation(String.valueOf(rowNumber));

                    rowNumber++;
                    list.add(tableModel);
                    components.remove(j);
                }
            }
        }

        return null;
    }

    @Override
    protected void done() {
        _tableModel.setData(list);
    }
}
