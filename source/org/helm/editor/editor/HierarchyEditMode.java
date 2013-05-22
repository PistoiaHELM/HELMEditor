/**
 * 
 */
package org.helm.editor.editor;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.view.EditMode;
import y.view.Graph2D;

import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.utility.MonomerNodeHelper;

public class HierarchyEditMode extends EditMode {

    /**
     * Overwritten to display information about the template used to create
     * the specified node.
     */
    @Override
    protected String getNodeTip(final Node node) {
        return MonomerNodeHelper.getTooltip(node);
    }

    @Override
    protected String getEdgeTip(
            final Edge edge) {
        String tooltip = null;
        if (edge.getGraph() != null) {
            final EdgeMap edgeMap = (EdgeMap) edge.getGraph().getDataProvider(EdgeMapKeys.EDGE_INFO);
            if (edgeMap != null) {
                tooltip = ((EditorEdgeInfoData) edgeMap.get(edge)).toString();
                if (tooltip != null) {
                    tooltip.replaceAll("null", "");
                }
            }

        }

        return tooltip;
    }

    /**
     * override the createNode function to disable the normal create node option
     * @param graph
     * @param x
     * @param y
     * @return node
     */
    @Override
    protected Node createNode(
            Graph2D graph,
            double x,
            double y) {

        return null;
    }
}