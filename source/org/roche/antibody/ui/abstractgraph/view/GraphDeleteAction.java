package org.roche.antibody.ui.abstractgraph.view;

import org.roche.antibody.services.AbConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DViewActions;

/**
 * {@code GraphDeleteAction}
 * 
 * This is the specific DeleteAction applied for the Antibody graph. We can only delete CysteinBridges. All other
 * elements are not allowed to delete. This action is required for Keyboard Usage
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:stefan_dieter.zilch@contractors.roche.com">Stefan Zilch</a>
 * 
 * @version $Id: GraphDeleteAction.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class GraphDeleteAction extends Graph2DViewActions.DeleteSelectionAction {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(GraphDeleteAction.class);

  /** */
  private static final long serialVersionUID = 1366134263005029056L;

  public GraphDeleteAction() {
    super();
    setDeletionMask(TYPE_EDGE | TYPE_SIMPLE_NODE);
  }

  @Override
  protected boolean acceptEdge(Graph2D graph, Edge edge) {
    // only delete edges, which have a model attached (cysteinbridges).
    boolean readyToDelete = super.acceptEdge(graph, edge);
    DataProvider map = graph.getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
    if (map.get(edge) != null) {
      return readyToDelete & true;
    }
    return false;
  }

  @Override
  protected boolean acceptNode(Graph2D graph, Node node) {
    boolean readyToDelete = super.acceptNode(graph, node);
    LOG.trace("Node selected: {} -> {} readyToDelete: {}", node, graph.isSelected(node), readyToDelete);
    DataProvider deletable = graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
    if (deletable.getBool(node)) {
      return readyToDelete & true;
    }
    return false;
  }

}
