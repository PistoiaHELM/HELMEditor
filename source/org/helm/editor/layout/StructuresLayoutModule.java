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
package org.helm.editor.layout;

import java.util.HashSet;
import java.util.Set;

import y.algo.GraphChecker;
import y.algo.GraphConnectivity;
import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;
import y.base.NodeList;
import y.base.NodeMap;
import y.layout.BufferedLayouter;
import y.layout.CanonicMultiStageLayouter;
import y.layout.ComponentLayouter;
import y.layout.LayoutGraph;
import y.layout.LayoutMultiplexer;
import y.layout.Layouter;
import y.layout.organic.SmartOrganicLayouter;
import y.module.LayoutModule;
import y.util.GraphCopier;
import y.util.GraphHider;

import org.helm.editor.data.EdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.layout.primitives.AbstractLayoutPrimitives;
import org.helm.editor.layout.procedures.AbstratStructureLayout;
import org.helm.editor.layout.procedures.ChemModifiersLayout;
import org.helm.editor.layout.procedures.CircularStructureLayout;
import org.helm.editor.layout.procedures.ComplementaryStructuresLayout;
import org.helm.editor.layout.procedures.DumbbellStructureLayout;
import org.helm.editor.layout.procedures.HairPinStructuresLayout;
import org.helm.editor.layout.procedures.LinearStructureLayout;
import org.helm.editor.utility.MonomerInfoUtils;

/**
 *
 */
public class StructuresLayoutModule extends LayoutModule {
	AbstractLayoutPrimitives layoutPrimitives = null;

	private AbstratStructureLayout linearStructureLayout = null;
	private AbstratStructureLayout complementaryStructuresLayout = null;
	private AbstratStructureLayout circularStructureLayout = null;
	private AbstratStructureLayout dumbbellStructureLayout = null;
	private AbstratStructureLayout hairPinStructuresLayout = null;
	private AbstratStructureLayout chemModifiersLayout = null;

	private CanonicMultiStageLayouter unsupportedStructuresLayout = null;

	public StructuresLayoutModule() {
		super(StructuresLayoutModule.class.getName(), "", "Layout Module");

		linearStructureLayout = new LinearStructureLayout();
		complementaryStructuresLayout = new ComplementaryStructuresLayout();
		circularStructureLayout = new CircularStructureLayout();
		dumbbellStructureLayout = new DumbbellStructureLayout();
		hairPinStructuresLayout = new HairPinStructuresLayout();
		chemModifiersLayout = new ChemModifiersLayout();

		unsupportedStructuresLayout = new SmartOrganicLayouter();
	}

	@Override
	protected void mainrun() {
		// set module properties
		setMorphingEnabled(false);

		// component arrangement
		ComponentLayouter cl = new ComponentLayouter();
		cl.setComponentSpacing(10);
		cl.setGridSpacing(10);
		cl.setLabelAwarenessEnabled(true);
		cl.setStyle(ComponentLayouter.STYLE_SINGLE_COLUMN);

		cl.setCoreLayouter(new Layouter() {
			public boolean canLayout(LayoutGraph arg0) {
				return true;
			}

			public void doLayout(LayoutGraph graph) {
				BufferedLayouter bufferedLayouter = new BufferedLayouter(
						new Layouter() {
							public void doLayout(LayoutGraph graph) {
								GraphHider graphHider = new GraphHider(graph);
								for (Node n : graph.getNodeArray()) {
									if (MonomerInfoUtils
											.isChemicalModifierPolymer(n)) {
										graphHider.hide(n);
									}
								}

								createLayoutTypeDataProvider(graph);

								// ComponentLayouter uses LayoutMultiplexer as
								// its core layouter, which, for
								// each component, invokes the layouter
								// retrieved from the data provider
								// registered with the graph.
								// Afterwards, the ComponentLayouter nicely
								// arranges the components.
								ComponentLayouter cl = new ComponentLayouter();
								cl.setCoreLayouter(new LayoutMultiplexer());
								cl.setComponentSpacing(10);
								cl.setGridSpacing(10);
								cl.setLabelAwarenessEnabled(true);
								cl.setStyle(ComponentLayouter.STYLE_SINGLE_COLUMN);
								cl.doLayout(graph);
								graphHider.unhideAll();

								// Remove the data providers from the graph.
								deleteDataProviders();
							}

							public boolean canLayout(LayoutGraph arg0) {
								return true;
							}
						});
				bufferedLayouter.doLayout(graph);

				// layout chem nodes
				chemModifiersLayout.doLayout(graph);
			}
		});

		launchLayouter(cl);
	}

	private void deleteDataProviders() {
		getGraph2D().removeDataProvider(
				getGraph2D().getDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY));
	}

	private void createLayoutTypeDataProvider(LayoutGraph graph) {
		NodeList[] components = GraphConnectivity.connectedComponents(graph);

		// Create a node map that will be used as a data provider to hold a
		// Layouter
		// implementation for each of the components. This layouter will be used
		// for
		// layout calculation.
		NodeMap nm = graph.createNodeMap();
		// Register the node map with the graph using the special look-up key
		// defined
		// by class LayoutMultiplexer.
		graph.addDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY, nm);

		GraphCopier graphCopier = new GraphCopier();
		// hide all pair edges so cycle check procedure won't pay attention to
		// cycles with pair edges in it
		GraphHider graphHider = new GraphHider(graph);
		for (EdgeCursor edges = graph.edges(); edges.ok(); edges.next()) {
			DataProvider edgeTypeMap = graph
					.getDataProvider(EdgeMapKeys.EDGE_INFO);
			EdgeInfo edgeInfo = (EdgeInfo) edgeTypeMap.get(edges.edge());
			if (edgeInfo.isPair()
					|| (layoutPrimitives
							.isPeptidePolymer(edges.edge().source()) && !edgeInfo
							.isRegular())) {
				graphHider.hide(edges.edge());
			}
		}

		// For each component one of the layouters is set. (Actually, a layouter
		// is set
		// for the component's first node only. Nevertheless, this layouter is
		// used for
		// the entire component, since LayoutMultiplexer takes the first
		// non-null
		// Layouter it can retrieve from the data provider.)
		for (NodeList component : components) {
			// check if the component is cyclic
			Graph temp = graphCopier.copy(graph, component.nodes());
			boolean isCyclic = GraphChecker.isCyclic(temp);

			Node startingHyperNode = getHyperNode(component.firstNode());
			DataProvider hyperEdgeDescription = startingHyperNode.getGraph()
					.getDataProvider(EdgeMapKeys.DESCRIPTION);
			Set<Node> pairHyperNodesSet = new HashSet<Node>();
			for (EdgeCursor edges = startingHyperNode.edges(); edges.ok(); edges
					.next()) {
				String description = (String) hyperEdgeDescription.get(edges
						.edge());
				// @@2@@
				if (description.contains("pair")) {
					Node pairHyperNode = startingHyperNode.equals(edges.edge()
							.source()) ? edges.edge().target() : edges.edge()
							.source();
					pairHyperNodesSet.add(pairHyperNode);
				}
				// detect backbone-branch and branch-branch connections
				if (description.contains("R3")) {
					Node pairHyperNode = startingHyperNode.equals(edges.edge()
							.source()) ? edges.edge().target() : edges.edge()
							.source();
					pairHyperNodesSet.add(pairHyperNode);
				}
			}
			if (pairHyperNodesSet.size() == 0) {
				// if this sequence has no paired sequence
				if (isCyclic) {
					nm.set(component.firstNode(), circularStructureLayout);
				} else {
					nm.set(component.firstNode(), linearStructureLayout);
				}
			} else if (pairHyperNodesSet.contains(startingHyperNode)) {
				// the pair node is the same node
				// if it is "stand alone" structure
				if (pairHyperNodesSet.size() == 1) {
					if (isCyclic) {
						nm.set(component.firstNode(), dumbbellStructureLayout);
					} else {
						nm.set(component.firstNode(), hairPinStructuresLayout);
					}
				} else {
					nm.set(component.firstNode(), unsupportedStructuresLayout);
				}
			} else { // doesn't contain
				// if there is at least one cycle them unsupported structures
				// layout should be used
				if (isCyclic) {
					nm.set(component.firstNode(), unsupportedStructuresLayout);
				} else {
					nm.set(component.firstNode(), complementaryStructuresLayout);
				}
			}
		}
		graphHider.unhideAll();
	}

	//
	// private boolean getPairHyperNode(String description) {
	// Node pairHyperNode
	// if (description.contains("pair")) {
	// pairHyperNode = startingHyperNode.equals(edges.edge().source())
	// ? edges.edge().target()
	// : edges.edge().source();
	// }
	// }

	private Node getHyperNode(Node node) {
		DataProvider node2Hipernode = node.getGraph().getDataProvider(
				NodeMapKeys.NODE2PARENT_HYPERNODE);
		return (Node) node2Hipernode.get(node);
	}

	public void setLayoutPrimitives(AbstractLayoutPrimitives layoutPrimitives) {
		this.layoutPrimitives = layoutPrimitives;

		linearStructureLayout.setLayoutPrimitives(layoutPrimitives);
		complementaryStructuresLayout.setLayoutPrimitives(layoutPrimitives);
		circularStructureLayout.setLayoutPrimitives(layoutPrimitives);
		dumbbellStructureLayout.setLayoutPrimitives(layoutPrimitives);
		hairPinStructuresLayout.setLayoutPrimitives(layoutPrimitives);
		chemModifiersLayout.setLayoutPrimitives(layoutPrimitives);
	}
}
