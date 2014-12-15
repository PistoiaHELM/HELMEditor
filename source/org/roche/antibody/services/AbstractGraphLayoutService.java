/*--
 *
 * @(#) AbstractGraphLayoutService.java
 *
 * Copyright 2014 by Roche Diagnostics GmbH,
 * Nonnenwald 2, DE-82377 Penzberg, Germany
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Roche Diagnostics GmbH ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Roche Diagnostics GmbH.
 *
 */
package org.roche.antibody.services;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.ui.abstractgraph.CysBridgeEdgeRealizer;
import org.roche.antibody.ui.abstractgraph.DomainEdgeRealizer;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeMap;
import y.layout.CanonicMultiStageLayouter;
import y.layout.hierarchic.GivenLayersLayerer;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;

/**
 * {@code AbstractGraphLayoutService} contains all methods, related to the graphical layout of an Abstract Antibody
 * Graph (derived from {@link AbstractGraphService#getGraph(org.roche.antibody.model.antibody.Antibody)}).
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:clemens.wrzodek@roche.com">Clemens Wrzodek</a>
 * @version $Id: AbstractGraphLayoutService.java 13993 2014-12-12 12:30:53Z schirmb $
 */
public class AbstractGraphLayoutService {

  /** The Logger for this class */
  private static final Logger LOG = Logger.getLogger(AbstractGraphLayoutService.class);

  public static IncrementalHierarchicLayouter createLayouter() {
    // create and configure the layout algorithm
    IncrementalHierarchicLayouter hierarchicLayouter = new IncrementalHierarchicLayouter();
    hierarchicLayouter.setFixedElementsLayerer(new GivenLayersLayerer());
    hierarchicLayouter.setComponentLayouterEnabled(false);
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    hierarchicLayouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));

    return hierarchicLayouter;
  }

  /**
   * Idea: we find the hinge in the chain and calculate an offset to layer 0; afterwards we find the layer indexes with
   * the constraint that each hinge is in layer 0. so domains before the hinge are < 0 and domains after the hinge are >
   * 0
   * 
   * @param domainMap
   * @param sequenceNodes
   * @param layerIdMap
   */
  public static void bringHingeToHorizontalLine(NodeMap domainMap, List<Node> sequenceNodes, NodeMap layerIdMap) {
    int offset = 0;
    for (int i = 0; i < sequenceNodes.size(); i++) {
      if (((Domain) domainMap.get(sequenceNodes.get(i))).isHinge()) {
        offset = i;
        break;
      }
    }
    for (int i = 0; i < sequenceNodes.size(); i++) {
      int layerIndex = i - offset;
      layerIdMap.setInt(sequenceNodes.get(i), layerIndex);
    }
  }

  /**
   * Layouts the ANTIBODY graph.
   * 
   * @param abstractGraph
   * @param abstractGraphLayouter
   */
  public static void layout(Graph2D abstractGraph) {
    layout(abstractGraph, null);
  }

  /**
   * Layouts the ANTIBODY graph.
   * 
   * @param abstractGraph
   * @param abstractGraphLayouter instance of {@link AbstractGraphService#createLayouter()} or {@code null} to create a
   *          new instance.
   */
  public static void layout(Graph2D abstractGraph, CanonicMultiStageLayouter abstractGraphLayouter) {
    if (abstractGraphLayouter == null) {
      abstractGraphLayouter = createLayouter();
    }
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.doLayout(abstractGraph, abstractGraphLayouter);
  }

  public static void updateLayoutHints(Graph2D graph) {
    if (graph == null) {
      return;
    }

    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap layers = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
    NodeMap nodeToSequence = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap swimlaneMap = (NodeMap) graph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);

    // Inspect all nodes with missing layer information
    for (Node n : graph.getNodeArray()) {
      // graph.setLabelText(n, String.valueOf(layers.get(n))); // Can be used for debugging the layers

      Integer layerId = layers.get(n) == null ? null : layers.getInt(n);

      if (!(nodeToSequence instanceof Domain))
        continue;
      // Domain d = (Domain) (nodeToSequence.get(n) == null ? null : nodeToSequence.get(n));
      Domain d = (Domain) nodeToSequence.get(n);

      if (layerId == null || (layerId == 0 && !d.isHinge())) {
        if (d.isHinge()) {
          layers.setInt(n, 0);
          continue;
        }

        // This node needs to get a layer info => Inspect adjacent nodes
        Collection<Integer> adjacentLayers = getLevelOfAdjacentNodes(n, graph);
        // Now we need to stick our node either on top of the adjavent (=adjacentLayer-1) or below (=adjacentLayer+1).
        // we actually need to inspect the adjacent nodes of the adjecent node to decide which direction to go
        // however, we aproximate since the top layer always has layer id "1". This fails only if top layer gets
        // deleted.
        if (adjacentLayers == null || adjacentLayers.size() < 1) {
          continue; // Can't do antything
        }
        int adjacentLayer = (int) max(adjacentLayers);

        // Based on value of adjacent nodes, assign value to this node
        layers.setInt(n, adjacentLayer > 1 ? adjacentLayer + 1 : adjacentLayer - 1);
        swimlaneMap.set(n, getAdjacentSwimlane(n, graph)); // Place in same column as adjacent nodes

        LOG.debug(String.format("Fixed missing adjacent layer for node '%s' ('%s'). Assigned: %s. Swimlane: %s.",
            graph.getLabelText(n).replace("\n", " | "), d.getName(), layers.getInt(n), swimlaneMap.get(n)));
      }
    }

  }

  /**
   * @param adjacentLayers
   * @return 0 if given collection is empty (or null), else, the minimum element.
   */
  private static int max(Collection<Integer> adjacentLayers) {
    Integer max = Integer.MIN_VALUE;

    if (adjacentLayers == null || adjacentLayers.size() < 1) {
      return 0;
    }

    for (Integer i : adjacentLayers) {
      if (i != null && i > max) {
        max = i;
      }
    }

    return max;
  }

  /**
   * @param n
   * @return
   */
  private static Collection<Integer> getLevelOfAdjacentNodes(Node n, Graph2D abstractGraph) {
    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap layers = (NodeMap) abstractGraph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);

    // Inspect mapValues in layers of adjacent nodes
    EdgeCursor ec = n.edges();
    Set<Integer> adjacentLayer = new HashSet<Integer>();
    Set<Integer> adjacentLayerCys2ndPrio = new HashSet<Integer>();
    while (ec.ok()) {
      Edge e = (Edge) ec.current();
      Node connectedNode = e.opposite(n);
      EdgeRealizer r = abstractGraph.getRealizer(e);
      if (r instanceof DomainEdgeRealizer) {
        if (layers.get(connectedNode) != null) {
          adjacentLayer.add(layers.getInt(connectedNode));
        }

      } else if (!(r instanceof CysBridgeEdgeRealizer)) { // Don't consider Cystein Brdidges!
        // Most likely GeneralConnectionEdgeRealizer
        if (layers.get(connectedNode) != null) {
          adjacentLayerCys2ndPrio.add(layers.getInt(connectedNode));
        }
      }
      ec.next();
    }

    // Consider only backbone edges if available. Else, consider non-cys.
    return (adjacentLayer.size() > 0) ? adjacentLayer : adjacentLayerCys2ndPrio;
  }

  /**
   * @param n
   * @return
   */
  private static Object getAdjacentSwimlane(Node n, Graph2D abstractGraph) {
    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap swimlaneMap =
        (NodeMap) abstractGraph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);

    // This node needs to get a layer info => Inspect adjacent nodes
    EdgeCursor ec = n.edges();
    Object adjacentSwimlandID = null;
    Object adjacentSwimlandID2ndPrio = null;
    while (ec.ok()) {
      Edge e = (Edge) ec.current();
      Node connectedNode = e.opposite(n);
      EdgeRealizer r = abstractGraph.getRealizer(e);
      if (r instanceof DomainEdgeRealizer) {
        adjacentSwimlandID = swimlaneMap.get(connectedNode);

      } else if (!(r instanceof CysBridgeEdgeRealizer)) { // Don't consider Cystein Brdidges!
        // Most likely GeneralConnectionEdgeRealizer
        adjacentSwimlandID2ndPrio = swimlaneMap.get(connectedNode);
      }
      ec.next();
    }

    return adjacentSwimlandID != null ? adjacentSwimlandID : adjacentSwimlandID2ndPrio;
  }

}
