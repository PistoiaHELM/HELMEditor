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
package org.helm.editor.utility;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.LayoutTool;
import y.util.D;
import y.util.GraphCopier;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import y.view.hierarchy.HierarchyManager;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.monomerui.SimpleElemetFactory;
import org.helm.editor.monomerui.treeui.XmlLeafNode;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;

public class DragAndDropSupport {

	private JList templatesList;
	private JTree templatesTree;
	private final Graph2DView view;
	private final MacromoleculeEditor editor;

	public DragAndDropSupport(final Graph2DView view,
			final MacromoleculeEditor editor) {
		this.view = view;
		this.editor = editor;
	}

	public void updateTemplatesTree(final JTree templatesTree) {

		this.templatesTree = templatesTree;

		// define the realizer list to be the drag source
		// use the string-valued name of the realizer as transferable
		final DragSource dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(templatesTree,
				DnDConstants.ACTION_MOVE, new DragGestureListener() {

					public void dragGestureRecognized(DragGestureEvent event) {
						Object selected = templatesTree
								.getLastSelectedPathComponent();

						if (!(selected instanceof XmlLeafNode)) {
							return;
						}

						XmlLeafNode xmlNode = (XmlLeafNode) selected;
						Graph2D graph = (Graph2D) xmlNode.getUserObject();
						final String textValue = getTextValue(getNodeRealizer(graph));
						if (textValue != null) {
							StringSelection text = new StringSelection(
									textValue);
							// as the name suggests, starts the dragging
							try {
								dragSource.startDrag(event,
										DragSource.DefaultMoveDrop, text, null);
							} catch (java.awt.dnd.InvalidDnDOperationException ex) {
								// ugly workaround
								// ex.printStackTrace();
							}
						}
					}
				});

		constructDropTarget();
	}

	/**
	 * switch among different lists
	 * 
	 * @param templatesList1
	 */
	public void updateTemplatesList(JList templatesList1) {
		this.templatesList = templatesList1;

		// define the realizer list to be the drag source
		// use the string-valued name of the realizer as transferable
		final DragSource dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(templatesList,
				DnDConstants.ACTION_MOVE, new DragGestureListener() {

					public void dragGestureRecognized(DragGestureEvent event) {
						Object selected = templatesList.getSelectedValue();
						final String textValue = getTextValue((NodeRealizer) selected);
						if (textValue != null) {
							StringSelection text = new StringSelection(
									textValue);
							// as the name suggests, starts the dragging
							try {
								dragSource.startDrag(event,
										DragSource.DefaultMoveDrop, text, null);
							} catch (java.awt.dnd.InvalidDnDOperationException ex) {
								// ugly workaround
								// ex.printStackTrace();
							}
						}
					}
				});

		constructDropTarget();
	}

	private void constructDropTarget() {
		// define the graph view to be the drop target. Create a newNode with
		// the
		// dropped shape to the graph.

		DropTarget dropTarget = new DropTarget(view.getCanvasComponent(),
				new DropTargetListener() {
					// called by the dnd framework once a drag enters the view

					public void dragEnter(DropTargetDragEvent event) {
						// view.addViewMode(rollOverMode);//todo
						if (checkStringFlavor(event)) {
							event.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
						} else {
							event.rejectDrag();
						}
					}

					// inspects the event and tries to create a NodeRealizer
					// from it
					private boolean checkStringFlavor(DropTargetDragEvent event) {
						// we accept only Strings
						DataFlavor[] flavors = event.getCurrentDataFlavors();
						for (int i = 0; i < flavors.length; i++) {
							if (flavors[i] == DataFlavor.stringFlavor) {
								return true;
							}
						}
						return false;
					}

					// called by the dnd framework once a drag ends with a drop
					// operation
					public void drop(DropTargetDropEvent event) {
						try {
							// we accept only Strings
							boolean foundStringFlavor = false;
							DataFlavor[] flavors = event
									.getCurrentDataFlavors();
							for (int i = 0; i < flavors.length; i++) {
								if (flavors[i] == DataFlavor.stringFlavor) {
									foundStringFlavor = true;
									break;
								}
							}
							if (foundStringFlavor) {
								event.acceptDrop(event.getDropAction());
							} else {
								event.rejectDrop();
								return;
							}
						} catch (RuntimeException rex) {
							event.rejectDrop();
							D.show(rex);
						}
						try {
							Transferable transferable = event.getTransferable();
							String s = (String) transferable
									.getTransferData(DataFlavor.stringFlavor);
							Point p = event.getLocation();
							Node n = getTemplateFromTextValue(s);
							if (n != null) {
								final double worldCoordX = view
										.toWorldCoordX(p.x);
								final double worldCoordY = view
										.toWorldCoordY(p.y);
								Node overlapNode = getOverlappingNode(n,
										worldCoordX, worldCoordY);
								NodeCursor nodes = copyTemplate(editor,
										view.getGraph2D(), n, overlapNode);
								boolean graphChanged = dropNodes(view, nodes,
										worldCoordX, worldCoordY);
								if (graphChanged) {
									editor.onDropCompleteEvent(nodes);
								}
								event.dropComplete(true);

							} else {
								// no suitable realizer
								event.dropComplete(false);
							}
						} catch (MonomerException ex) {
							JOptionPane.showMessageDialog(null,
									ex.getMessage(), "Invalid Monomer!",
									JOptionPane.WARNING_MESSAGE);
							event.dropComplete(false);
						} catch (JDOMException ex) {
							Logger.getLogger(DragAndDropSupport.class.getName())
									.log(Level.SEVERE, null, ex);
							event.dropComplete(false);
						} catch (IOException ioe) {
							// should not happen
							event.dropComplete(false);
							D.show(ioe);
						} catch (UnsupportedFlavorException ufe) {
							// should never happen
							event.dropComplete(false);
							D.show(ufe);
						} catch (RuntimeException x) {
							event.dropComplete(false);
							throw x;
						}
					}

					// called by the dnd framework when a drag leaves the view
					public void dragExit(DropTargetEvent dte) {
					}

					// called by the dnd framework when the drag action changes
					public void dropActionChanged(DropTargetDragEvent dtde) {
					}

					// called by the dnd framework when the drag hovers over the
					// view
					public void dragOver(DropTargetDragEvent event) {
					}
				});
	}

	/**
	 * put the newly created newNode(s) in the right position
	 * 
	 * @param view
	 * @param nodes
	 *            : newly created nodes
	 * @param worldCoordX
	 * @param worldCoordY
	 * @return
	 * @throws java.io.IOException
	 * @throws org.jdom.JDOMException
	 * @throws org.helm.notation.MonomerException
	 */
	private boolean dropNodes(Graph2DView view, final NodeCursor nodes,
			double worldCoordX, double worldCoordY) throws IOException,
			JDOMException, MonomerException {

		final double x;
		final double y;
		// boolean overlapped = false;

		if (view.getGridMode()) {
			double gridSize = view.getGridResolution();
			x = Math.floor(worldCoordX / gridSize + 0.5) * gridSize;
			y = Math.floor(worldCoordY / gridSize + 0.5) * gridSize;
		} else {
			x = worldCoordX;
			y = worldCoordY;
		}

		final Graph2D graph = view.getGraph2D();
		final Rectangle2D bbx = LayoutTool.getBoundingBox(graph, nodes);

		// final NodeMap monomerInfoNodeMap = (NodeMap)
		// graph.getDataProvider(NodeMapKeys.MONOMER_REF);

		nodes.toFirst();

		boolean replaceable = false;
		Node newNode = null;
		Node oldNode = null;
		if (nodes.size() == 1 && nodes.ok()) {
			newNode = nodes.node();

			NodeCursor graphNodes = graph.nodes();
			NodeRealizer nr = null;

			for (; graphNodes.ok(); graphNodes.next()) {
				if (!graphNodes.node().equals(newNode)) { // since the new
															// newNode is
															// already belonging
															// to the current
															// graph, we need to
															// eliminate self
															// overlapping
					oldNode = graphNodes.node();
					nr = graph.getRealizer(oldNode);
					if (nr.getBoundingBox().contains(worldCoordX, worldCoordY)) {
						replaceable = isReplaceable(graph, newNode, oldNode);
						break;
					}
				}
			}
		}

		if (replaceable) {
			editor.synchronizeZoom();
			return replaceMonomer(graph, editor.getGraphManager(), newNode,
					oldNode);
		}

		handleNewNode(editor, graph, newNode);

		// update the layout
		nodes.toFirst();
		LayoutTool.moveSubgraph(graph, nodes, x - bbx.getCenterX(),
				y - bbx.getCenterY());

		editor.synchronizeZoom();

		view.updateView();
		return true;

		// drop single newNode
		// if (nodes.size() == 1 && nodes.ok()) {
		// Node newNode = nodes.node();
		// MonomerInfo newNodeMonomerInfo = (MonomerInfo)
		// monomerInfoNodeMap.get(newNode);
		// Monomer newNodeMonomer =
		// GraphUtils.getMonomerDB().get(newNodeMonomerInfo.getPolymerType()).get(newNodeMonomerInfo.getMonomerID());
		//
		// // test if this is a replacement action, if it is,
		// // then we performace the replacement actions, otherwise, we just
		// drop the node
		// String nodeType = newNodeMonomerInfo.getPolymerType();
		// if (nodeType.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE) ||
		// nodeType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
		// NodeCursor graphNodes = graph.nodes();
		// NodeRealizer nr = null;
		//
		// double centerX = 0;
		// double centerY = 0;
		//
		// boolean dropSucc = false;//whether to allow drop
		// //looping through all nodes in the graph, get the one that the new
		// newNode is currentlly overlapping with
		//
		// for (; graphNodes.ok(); graphNodes.next()) {
		// if (!graphNodes.node().equals(newNode)) { //since the new newNode is
		// already belonging to the current graph, we need to eliminate self
		// overlapping
		//
		// nr = graph.getRealizer(graphNodes.node());
		// if (nr.getBoundingBox().contains(worldCoordX, worldCoordY)) {
		//
		//
		//
		// //allow drop if the new newNode and the old newNode have the same
		// monomer type
		// MonomerInfo oldNodeMonomerInfo = (MonomerInfo)
		// monomerInfoNodeMap.get(graphNodes.node());
		// Monomer oldNodeMonomer =
		// GraphUtils.getMonomerDB().get(oldNodeMonomerInfo.getPolymerType()).get(oldNodeMonomerInfo.getMonomerID());
		//
		// if
		// (oldNodeMonomer.getMonomerType().equalsIgnoreCase(newNodeMonomer.getMonomerType())
		// &&
		// (newNodeMonomer.getMonomerType().equalsIgnoreCase(Monomer.BRANCH_MOMONER_TYPE)
		// ||
		// (newNodeMonomer.getMonomerType().equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE)
		// &&
		// oldNodeMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)
		// && nodeType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE))
		// ||
		// (oldNodeMonomer.getNaturalAnalog().equalsIgnoreCase(newNodeMonomer.getNaturalAnalog()))))
		// {
		//
		// editor.synchronizeZoom();
		//
		// return replaceMonomer(graph, editor.getGraphManager(), newNode,
		// graphNodes.node());
		//
		// }
		// }
		// }
		// }
		//
		// if (!overlapped &&
		// nodeType.equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE)) {
		// Node newNodeInGraph = null;
		// NodeMap tgtNodeMap = (NodeMap)
		// graph.getDataProvider(NodeMapKeys.MONOMER_REF);
		// MonomerInfo targetMonomerInfo = null;
		// boolean hasEdge = false;
		// NodeCursor nodesInGraph = graph.nodes();
		// Edge[] edgesInGraph = graph.getEdgeArray();
		// for (; nodesInGraph.ok(); nodesInGraph.next()) {
		// newNodeInGraph = nodesInGraph.node();
		// targetMonomerInfo = (MonomerInfo) tgtNodeMap.get(newNodeInGraph);
		// if (newNodeInGraph.equals(newNode)) {
		// break;
		// }
		// }
		//
		// nodesInGraph.toFirst();
		// for (; nodesInGraph.ok(); nodesInGraph.next()) {
		// Node n = nodesInGraph.node();
		// if (graph.containsEdge(newNodeInGraph, n) || graph.containsEdge(n,
		// newNodeInGraph)) {
		// hasEdge = true;
		// break;
		// }
		// }
		// if (!hasEdge) {
		// editor.getGraphManager().addStartingNode(newNode);
		// }
		//
		// dropSucc = true;
		// }
		// //if drop is unsuccessful, then remove the new node that was just
		// added
		// if (!dropSucc) {
		// graph.removeNode(nodes.node());
		//
		// editor.synchronizeZoom();
		//
		// return false;
		// }
		//
		// } else { //for the other types, just add new node
		// editor.getGraphManager().addStartingNode(newNode);
		// }
		// }

		// // update the layout
		// nodes.toFirst();
		// LayoutTool.moveSubgraph(
		// graph,
		// nodes,
		// x - bbx.getCenterX(),
		// y - bbx.getCenterY());
		//
		// editor.synchronizeZoom();
		//
		// view.updateView();
		// return true;
	}

	public static boolean replaceMonomer(Graph2D graph,
			GraphManager graphManager, Node newNode, Node oldNode)
			throws MonomerException, IOException, JDOMException {

		NodeMap monomerInfoNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo newNodeMonomerInfo = (MonomerInfo) monomerInfoNodeMap
				.get(newNode);

		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);

		// for out edges
		outEdgesCycle(graph, newNode, oldNode, monomerInfoNodeMap,
				newNodeMonomerInfo, edgeMap);

		// for in edges
		inEdgesCycle(graph, graphManager, newNode, oldNode, monomerInfoNodeMap,
				newNodeMonomerInfo, edgeMap);

		// update the graph manager if needed
		// GraphManager graphManager = editor.getGraphManager();
		if (graphManager.isStartingNode(oldNode)) {
			int index = graphManager.getIndex(oldNode);
			graphManager.removeStartingNode(oldNode);
			graphManager.addStartingNode(index, newNode);
		}

		// remove the old node, and the connected edges will be removed too.
		graph.removeNode(oldNode);

		return true;
	}

	private static void inEdgesCycle(Graph2D graph, GraphManager graphManager,
			Node newNode, Node oldNode, NodeMap monomerInfoNodeMap,
			MonomerInfo newNodeMonomerInfo, EdgeMap edgeMap)
			throws MonomerException, JDOMException, IOException {
		Edge oldEdge;
		EdgeRealizer oldEdgeRealizer;
		Edge newEdge;
		EdgeRealizer newEdgeRealizer;
		EditorEdgeInfoData oldEdgeInfo;
		Attachment newAttachment;
		NodeCursor predecessors = oldNode.predecessors();
		MonomerInfo sourceMonomerInfo = null;

		for (; predecessors.ok(); predecessors.next()) {
			oldEdge = predecessors.node().getEdgeTo(oldNode);
			oldEdgeInfo = (EditorEdgeInfoData) edgeMap.get(oldEdge);

			oldEdgeRealizer = graph.getRealizer(oldEdge);

			newAttachment = newNodeMonomerInfo.getAttachment(oldEdgeInfo
					.getTargetNodeAttachment().getLabel());
			sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap
					.get(predecessors.node());

			if (newAttachment != null) {
				if (!oldEdgeInfo.isPair()) {

					NodeCursor cursor = newNode.predecessors();
					for (; cursor.ok(); cursor.next()) {
						Node currNode = cursor.node();
						graph.removeNode(currNode);

						// check for starting nodes
						if (graphManager.isStartingNode(currNode)) {
							graphManager.removeStartingNode(currNode);
						}
					}

					newNodeMonomerInfo.setConnection(newAttachment, true);
					newEdge = graph.createEdge(predecessors.node(), newNode);
					edgeMap.set(
							newEdge,
							new EditorEdgeInfoData(oldEdgeInfo
									.getSourceNodeAttachment(), newAttachment));

					newEdgeRealizer = graph.getRealizer(newEdge);
					newEdgeRealizer.setLineType(oldEdgeRealizer.getLineType());
					newEdgeRealizer
							.setLineColor(oldEdgeRealizer.getLineColor());
				} else {

					if (SequenceGraphTools.pairable(sourceMonomerInfo,
							newNodeMonomerInfo)) {
						sourceMonomerInfo.setConnection(
								oldEdgeInfo.getSourceNodeAttachment(), false);
						graph.removeEdge(oldEdge);
						pairEdge(oldEdge.source(), newNode, graph);
					}

				}
			}
		}
	}

	private static void outEdgesCycle(Graph2D graph, Node newNode,
			Node oldNode, NodeMap monomerInfoNodeMap,
			MonomerInfo newNodeMonomerInfo, EdgeMap edgeMap)
			throws MonomerException, IOException, JDOMException {

		Edge oldEdge;
		EdgeRealizer oldEdgeRealizer;
		Edge newEdge;
		EdgeRealizer newEdgeRealizer;
		EditorEdgeInfoData oldEdgeInfo;
		Attachment newAttachment;
		NodeCursor successors = oldNode.successors();

		for (; successors.ok(); successors.next()) {
			oldEdge = successors.node().getEdgeFrom(oldNode);
			oldEdgeRealizer = graph.getRealizer(oldEdge);

			// update the attachment
			oldEdgeInfo = (EditorEdgeInfoData) edgeMap.get(oldEdge);

			// get the new attachment, it could be null
			newAttachment = newNodeMonomerInfo.getAttachment(oldEdgeInfo
					.getSourceNodeAttachment().getLabel());
			if (newAttachment != null) {
				if (!oldEdgeInfo.isPair()) {
					newNodeMonomerInfo.setConnection(newAttachment, true);

					newEdge = graph.createEdge(newNode, successors.node());
					edgeMap.set(newEdge, new EditorEdgeInfoData(newAttachment,
							oldEdgeInfo.getTargetNodeAttachment()));
					newEdgeRealizer = graph.getRealizer(newEdge);
					newEdgeRealizer.setLineType(oldEdgeRealizer.getLineType());
					newEdgeRealizer
							.setLineColor(oldEdgeRealizer.getLineColor());
				} else {
					MonomerInfo targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap
							.get(oldEdge.target());
					if (SequenceGraphTools.pairable(newNodeMonomerInfo,
							targetMonomerInfo)) {
						targetMonomerInfo.setConnection(
								oldEdgeInfo.getTargetNodeAttachment(), false);
						graph.removeEdge(oldEdge);
						pairEdge(newNode, oldEdge.target(), graph);
					} else {
						newNodeMonomerInfo.setConnection(newAttachment, false);
						targetMonomerInfo.setConnection(
								oldEdgeInfo.getTargetNodeAttachment(), false);
					}
				}
			}
		}
	}

	public static Edge pairEdge(Node sourceNode, Node targetNode,
			final Graph2D graph) {

		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		final MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap
				.get(sourceNode);
		final MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap
				.get(targetNode);

		Attachment sourceAttachment = sourceMonomerInfo
				.getAttachment(Attachment.PAIR_ATTACHMENT);
		Attachment targetAttachment = targetMonomerInfo
				.getAttachment(Attachment.PAIR_ATTACHMENT);

		if (!sourceMonomerInfo.isConnected(sourceAttachment)
				&& !targetMonomerInfo.isConnected(targetAttachment)) {
			sourceMonomerInfo.setConnection(sourceAttachment, true);
			targetMonomerInfo.setConnection(targetAttachment, true);
			Edge newEdge = graph.createEdge(sourceNode, targetNode);
			edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment,
					targetAttachment));
			return newEdge;
		}

		return null;
	}

	public static Node copySingleNode(MacromoleculeEditor editor, Graph2D tgt,
			final Node newNode1) throws Exception {
		NodeCursor nc = copyTemplate(editor, tgt, newNode1, tgt.firstNode());
		nc.toFirst();
		return nc.node();
	}

	/**
	 * Copy the newly created node to the graph. if the new node is a Nucleic
	 * acid group node, it will add it to current graph and also connect the
	 * possible edges. For other single node, it just add the new node to the
	 * graph
	 * 
	 * @param newNode1
	 * @return NodeCursor
	 */
	public static NodeCursor copyTemplate(MacromoleculeEditor editor,
			Graph2D tgt, final Node newNode1, Node overlapNode) // , double
																// worldCoordX,
																// double
																// worldCoordY)
			throws MonomerException, JDOMException, IOException {
		// final Graph2D tgt = view.getGraph2D();
		final Graph2D src = (Graph2D) newNode1.getGraph();

		final HierarchyManager nhm = HierarchyManager.getInstance(src);

		GraphCopier copier = SequenceGraphTools.getGraphCopier(src);

		// droping a new folder node. Only nucleotide acids are implemented as
		// folder nodes
		// TODO commented code is workaround
		if (nhm != null) { // && ((nhm.isGroupNode(newNode1) ||
							// nhm.isFolderNode(newNode1)))) {
			return dropFolderNode(editor, tgt, newNode1, src, copier,
					overlapNode);

		} else {// drop single new node, either replace an existing nucleic acid
				// monomerInfo or create a new peptide/chemical structure node

			return dropSingleNode(editor, tgt, newNode1, overlapNode, copier);
		}
	}

	private static NodeCursor dropSingleNode(MacromoleculeEditor editor,
			Graph2D tgt, final Node newNode1, Node overlapNode,
			GraphCopier copier) throws MonomerException, IOException,
			JDOMException {
		boolean replacementAction = (overlapNode != null);
		final NodeMap nodeMap = (NodeMap) newNode1.getGraph().getDataProvider(
				NodeMapKeys.MONOMER_REF);
		MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(newNode1);
		NodeCursor copies = null;
		Monomer monomer = null;
		if (monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			copies = copier.copy(
					SimpleElemetFactory.getInstance().createMonomerNode(
							Monomer.NUCLIEC_ACID_POLYMER_TYPE,
							monomerInfo.getMonomerID()), tgt).nodes();
			// monomer =
			// editor.getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
			// if
			// (monomer.getMonomerType().equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE))
			// {
			// copies =
			// copier.copy(NodeFactory.createNucleicAcidBackboneNode(monomerInfo.getMonomerID(),
			// monomer.getNaturalAnalog()), tgt).nodes();
			// } else {
			// copies =
			// copier.copy(NodeFactory.createNucleicAcidBaseNode(monomerInfo.getMonomerID()),
			// tgt).nodes();
			// }
		} else if (monomerInfo.getPolymerType().equalsIgnoreCase(
				Monomer.PEPTIDE_POLYMER_TYPE)) {
			copies = copier.copy(
					SimpleElemetFactory.getInstance().createMonomerNode(
							Monomer.PEPTIDE_POLYMER_TYPE,
							monomerInfo.getMonomerID()), tgt).nodes();
			// copies.toFirst();
			// //Test if this's replacement action. Do nothing if yes
			// //isReplacementAction(newNode1, copies, graph, graphNodes);
			// if (replacementAction) {
			// return copies;
			// }
			//
			// final GraphManager graphManager = editor.getGraphManager();
			// final List<Node> startingNodeList =
			// graphManager.getStartingNodeList();
			// if (startingNodeList.size() == 1) {
			// Node startingNode = startingNodeList.get(0);
			// NodeList peptides =
			// SequenceGraphTools.getPeptideSequence(startingNode, tgt);
			// if (peptides != null && !peptides.isEmpty()) {
			// Node last = (Node) peptides.last();
			// Node newNodeInGraph = null;
			// NodeMap tgtNodeMap = (NodeMap)
			// tgt.getDataProvider(NodeMapKeys.MONOMER_REF);
			// MonomerInfo targetMonomerInfo = null;
			// for (; copies.ok(); copies.next()) {
			// newNodeInGraph = copies.node();
			// targetMonomerInfo = (MonomerInfo) tgtNodeMap.get(newNodeInGraph);
			// if
			// (targetMonomerInfo.getMonomerID().equalsIgnoreCase(monomerInfo.getMonomerID()))
			// {
			// break;
			// }
			//
			// }
			// Edge edge = createNucleotideEdge(tgt, last, newNodeInGraph);
			// }
			// }
		} else {
			copies = copier.copy(
					SimpleElemetFactory.getInstance().createMonomerNode(
							Monomer.CHEMICAL_POLYMER_TYPE,
							monomerInfo.getMonomerID()), tgt).nodes();
		}

		copies.toFirst();
		return copies;
	}

	private static NodeCursor dropFolderNode(MacromoleculeEditor editor,
			Graph2D tgt, final Node newNode1, final Graph2D src,
			GraphCopier copier, Node overlapNode) throws MonomerException,
			IOException, JDOMException {
		boolean overlaps = (overlapNode != null);
		NodeMap folderNodeNotationMap = (NodeMap) src
				.getDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION);
		String notation = (String) folderNodeNotationMap.get(newNode1);
		final Graph innerGraph = NotationParser.createNucleotideGraph(notation);
		final GraphManager graphManager = editor.getGraphManager();
		final List<Node> startingNodeList = graphManager.getStartingNodeList();
		final NodeMap innerNodeMap = (NodeMap) innerGraph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		NodeMap tgtNodeMap = (NodeMap) tgt
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo monomerInfo = null;
		Monomer monomer = null;
		Graph2D newNCSubgraph = new Graph2D();

		copier.copy(innerGraph, newNCSubgraph);
		NodeCursor copies = copier.copy(newNCSubgraph, tgt).nodes();
		boolean connected = false; // if the new nucleic acid can be connected
									// to the existing sequence

		// if there is only one starting node ...
		if (overlapNode != null) {
			// Node startingNode = graphManager.g;
			Node lastBackbone = SequenceGraphTools
					.getTheLastBackboneNuclicacidNode(overlapNode);

			monomerInfo = (MonomerInfo) tgtNodeMap.get(overlapNode);

			Attachment sourceAtt = null;
			Attachment targetAtt = null;

			MonomerInfo sourceMonomerInfo = null;
			MonomerInfo targetMonomerInfo = null;

			// if the existing starting node is a nucleic_Acid, then we search
			// for the last backbone node
			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				lastBackbone = SequenceGraphTools
						.getTheLastBackboneNuclicacidNode(overlapNode);
				boolean appendPhosphateNode = false;

				if (lastBackbone != null) {
					sourceMonomerInfo = (MonomerInfo) tgtNodeMap
							.get(lastBackbone);
					monomer = editor.getMonomerDB()
							.get(sourceMonomerInfo.getPolymerType())
							.get(sourceMonomerInfo.getMonomerID());

					// if the last backbone node is a R node, append a phosphate
					// node to it
					if (monomer.getNaturalAnalog().equalsIgnoreCase(
							Monomer.ID_R)) {
						Node pNode = SequenceGraphTools
								.appendPhosphate(lastBackbone);
						lastBackbone = pNode;
						sourceMonomerInfo = (MonomerInfo) tgtNodeMap
								.get(lastBackbone);
						appendPhosphateNode = true;
					}

					sourceAtt = sourceMonomerInfo
							.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);

					// the R node of the new nuclicacid node
					Node postRNode = null;

					// get the new new nodeMap with new nodes added
					tgtNodeMap = (NodeMap) tgt
							.getDataProvider(NodeMapKeys.MONOMER_REF);

					// find the connection point
					copies.toFirst();
					for (; copies.ok(); copies.next()) {
						postRNode = copies.node();
						targetMonomerInfo = (MonomerInfo) tgtNodeMap
								.get(postRNode);
						monomer = editor.getMonomerDB()
								.get(targetMonomerInfo.getPolymerType())
								.get(targetMonomerInfo.getMonomerID());

						if (monomer.getNaturalAnalog().equalsIgnoreCase(
								Monomer.ID_R)) {
							if (!targetMonomerInfo
									.isConnected(targetMonomerInfo
											.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT))) {
								targetAtt = targetMonomerInfo
										.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
								break;
							}
						}
					}
					// if we can connect them
					if (sourceAtt != null
							&& !sourceMonomerInfo.isConnected(sourceAtt)
							&& targetAtt != null
							&& !targetMonomerInfo.isConnected(targetAtt)) {
						Edge edge = createNucleotideEdge(tgt, lastBackbone,
								postRNode);
						if (edge != null) {
							EdgeRealizer er = tgt.getRealizer(edge);
							// er.setLineType(MacromoleculeEditor.INSERTION_LINE_TYPE);
							connected = true;
							// remove the last phosphate
							SequenceGraphTools.removeLastPhosphate(overlapNode);

						} else { // if the connecting is failed, remove the
									// added phosphate
							if (appendPhosphateNode) {
								SequenceGraphTools
										.removeLastPhosphate(overlapNode);
							}
						}
					}

				}
			}
		}
		// if the new nucleic acid node cannot be connected to the existing
		// sequence, we will update the starting node list
		if (!connected) {
			copies.toFirst();
			tgtNodeMap = (NodeMap) tgt.getDataProvider(NodeMapKeys.MONOMER_REF);

			for (; copies.ok(); copies.next()) {
				monomerInfo = (MonomerInfo) tgtNodeMap.get(copies.node());
				if (monomerInfo.getPolymerType().equalsIgnoreCase(
						Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
					monomer = editor.getMonomerDB()
							.get(monomerInfo.getPolymerType())
							.get(monomerInfo.getMonomerID());
					if (monomer.getNaturalAnalog().equalsIgnoreCase(
							Monomer.ID_R)) {
						graphManager.addStartingNode(copies.node());
						SequenceGraphTools.removeLastPhosphate(copies.node());
						break;
					}
				}
			}
		}
		copies.toFirst();
		return copies;
	}

	// private static NodeCursor addNodeToGraph(MacromoleculeEditor editor,
	// Graph2D tgt, Node newNode1, boolean isReplacmentMode,
	// Graph2D src, GraphCopier copier) throws MonomerException, IOException,
	// JDOMException {
	//
	// NodeMap folderNodeNotationMap = (NodeMap)
	// src.getDataProvider(NodeMapKeys.FOLDER_NODE_NOTATION);
	// String notation = (String) folderNodeNotationMap.get(newNode1);
	// Graph innerGraph = NodeFactory.createNucleictideNodeGraph(notation);
	// GraphManager graphManager = editor.getGraphManager();
	// List<Node> startingNodeList = graphManager.getStartingNodeList();
	//
	// NodeMap tgtNodeMap = (NodeMap)
	// tgt.getDataProvider(NodeMapKeys.MONOMER_REF);
	// MonomerInfo monomerInfo = null;
	// Monomer monomer = null;
	// Graph2D newNCSubgraph = new Graph2D();
	//
	// copier.copy(innerGraph, newNCSubgraph);
	// NodeCursor copies = copier.copy(newNCSubgraph, tgt).nodes();
	// // if the new nucleic acid can be connected to the existing sequence
	// boolean connected = false;
	//
	// //if there is only one starting node ...
	// if (startingNodeList.size() == 1) {
	// Node startingNode = startingNodeList.get(0);
	// Node lastBackbone =
	// SequenceGraphTools.getTheLastBackboneNuclicacidNode(startingNode);
	//
	// monomerInfo = (MonomerInfo) tgtNodeMap.get(startingNode);
	//
	// Attachment sourceAtt = null;
	// Attachment targetAtt = null;
	//
	// MonomerInfo sourceMonomerInfo = null;
	// MonomerInfo targetMonomerInfo = null;
	//
	// //if the existing starting node is a nucleic_Acid, then we search for the
	// last backbone node
	// if
	// (monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE))
	// {
	// lastBackbone =
	// SequenceGraphTools.getTheLastBackboneNuclicacidNode(startingNode);
	// boolean appendPhosphateNode = false;
	//
	// if (lastBackbone != null) {
	// sourceMonomerInfo = (MonomerInfo) tgtNodeMap.get(lastBackbone);
	// monomer =
	// editor.getMonomerDB().get(sourceMonomerInfo.getPolymerType()).get(sourceMonomerInfo.getMonomerID());
	//
	// // if the last backbone node is a R node, append a phosphate node to it
	// if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R) &&
	// !isReplacmentMode) {
	// Node pNode = SequenceGraphTools.appendPhosphate(lastBackbone);
	// lastBackbone = pNode;
	// sourceMonomerInfo = (MonomerInfo) tgtNodeMap.get(lastBackbone);
	// appendPhosphateNode = true;
	// }
	//
	// sourceAtt =
	// sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
	//
	// // the R node of the new nuclicacid node
	// Node postRNode = null;
	//
	// //get the new new nodeMap with new nodes added
	// tgtNodeMap = (NodeMap) tgt.getDataProvider(NodeMapKeys.MONOMER_REF);
	//
	// //find the connection point
	// copies.toFirst();
	// for (; copies.ok(); copies.next()) {
	// postRNode = copies.node();
	// targetMonomerInfo = (MonomerInfo) tgtNodeMap.get(postRNode);
	// monomer =
	// editor.getMonomerDB().get(targetMonomerInfo.getPolymerType()).get(targetMonomerInfo.getMonomerID());
	//
	// if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
	// if
	// (!targetMonomerInfo.isConnected(targetMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT)))
	// {
	// targetAtt =
	// targetMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
	// break;
	// }
	// }
	// }
	//
	// //if we can connect them
	// if (sourceAtt != null && !sourceMonomerInfo.isConnected(sourceAtt) &&
	// targetAtt != null
	// && !targetMonomerInfo.isConnected(targetAtt)) {
	// Edge edge = createNucleotideEdge(tgt, lastBackbone, postRNode);
	// if (edge != null) {
	// connected = true;
	// //remove the last phosphate
	// SequenceGraphTools.removeLastPhosphate(startingNode);
	//
	// } else { // if the connecting is failed, remove the added phosphate
	// if (appendPhosphateNode) {
	// SequenceGraphTools.removeLastPhosphate(startingNode);
	// }
	// }
	// }
	//
	// }
	// }
	// }
	//
	// //if the new nucleic acid node cannot be connected to the existing
	// sequence, we will update the starting node list
	// if (!connected) {
	// addedNewStartingNode(editor, tgt, graphManager, copies);
	// }
	//
	// copies.toFirst();
	// return copies;
	// }

	private static void addedNewStartingNode(MacromoleculeEditor editor,
			Graph2D tgt, final GraphManager graphManager, NodeCursor copies)
			throws IOException, MonomerException, JDOMException {
		NodeMap tgtNodeMap;
		MonomerInfo monomerInfo;
		Monomer monomer;
		copies.toFirst();
		tgtNodeMap = (NodeMap) tgt.getDataProvider(NodeMapKeys.MONOMER_REF);

		for (; copies.ok(); copies.next()) {
			monomerInfo = (MonomerInfo) tgtNodeMap.get(copies.node());
			if (monomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				monomer = editor.getMonomerDB()
						.get(monomerInfo.getPolymerType())
						.get(monomerInfo.getMonomerID());
				if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
					graphManager.addStartingNode(copies.node());
					SequenceGraphTools.removeLastPhosphate(copies.node());
					break;
				}
			}
		}
	}

	// private static NodeCursor nodeReplacment(MacromoleculeEditor editor,
	// Graph2D tgt, final Node newNode1, boolean replacementAction,
	// final Graph2D src, GraphCopier copier) throws MonomerException,
	// IOException, JDOMException {
	// final NodeMap nodeMap = (NodeMap)
	// src.getDataProvider(NodeMapKeys.MONOMER_REF);
	// MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(newNode1);
	// NodeCursor copies = null;
	//
	// if
	// (monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE))
	// {
	// copies =
	// copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.NUCLIEC_ACID_POLYMER_TYPE,
	// monomerInfo.getMonomerID()), tgt).nodes();
	// // copies = monomerReplacment(editor, tgt, copier, monomerInfo);
	// } else if
	// (monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.PEPTIDE_POLYMER_TYPE))
	// {
	// copies =
	// copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.PEPTIDE_POLYMER_TYPE,
	// monomerInfo.getMonomerID()), tgt).nodes();
	// // copies = peptideReplacment(editor, tgt, replacementAction, copier,
	// monomerInfo);
	// } else {
	// copies =
	// copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.CHEMICAL_POLYMER_TYPE,
	// monomerInfo.getMonomerID()), tgt).nodes();
	// }
	//
	// copies.toFirst();
	// return copies;
	// }
	//
	// private static NodeCursor monomerReplacment(MacromoleculeEditor editor,
	// Graph2D tgt, GraphCopier copier, MonomerInfo monomerInfo)
	// throws MonomerException, IOException, JDOMException {
	// NodeCursor copies;
	// Monomer monomer;
	// monomer =
	// editor.getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
	// if
	// (monomer.getMonomerType().equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE))
	// {
	// copies =
	// copier.copy(NodeFactory.createNucleicAcidBackboneNode(monomerInfo.getMonomerID(),
	// monomer.getNaturalAnalog()), tgt).nodes();
	// } else {
	// copies =
	// copier.copy(NodeFactory.createNucleicAcidBaseNode(monomerInfo.getMonomerID()),
	// tgt).nodes();
	// }
	// return copies;
	// }
	//
	// private static NodeCursor peptideReplacment(MacromoleculeEditor editor,
	// Graph2D tgt, boolean replacementAction, GraphCopier copier,
	// MonomerInfo monomerInfo) throws MonomerException, IOException,
	// JDOMException {
	// NodeCursor copies;
	// copies =
	// copier.copy(SimpleElemetFactory.getInstance().createMonomerNode(Monomer.PEPTIDE_POLYMER_TYPE,
	// monomerInfo.getMonomerID()), tgt).nodes();
	// copies.toFirst();
	//
	// //Test if this's replacement action. Do nothing if yes
	// if (replacementAction) {
	// return copies;
	// }
	//
	// final GraphManager graphManager = editor.getGraphManager();
	// final List<Node> startingNodeList = graphManager.getStartingNodeList();
	// if (startingNodeList.size() == 1) {
	// Node startingNode = startingNodeList.get(0);
	// NodeList peptides = SequenceGraphTools.getPeptideSequence(startingNode,
	// tgt);
	// if (peptides != null && !peptides.isEmpty()) {
	// Node newNodeInGraph = null;
	// NodeMap tgtNodeMap = (NodeMap)
	// tgt.getDataProvider(NodeMapKeys.MONOMER_REF);
	// MonomerInfo targetMonomerInfo = null;
	// for (; copies.ok(); copies.next()) {
	// newNodeInGraph = copies.node();
	// targetMonomerInfo = (MonomerInfo) tgtNodeMap.get(newNodeInGraph);
	// if
	// (targetMonomerInfo.getMonomerID().equalsIgnoreCase(monomerInfo.getMonomerID()))
	// {
	// break;
	// }
	//
	// }
	// }
	// }
	// return copies;
	// }

	private Node getOverlappingNode(final Node newNode1, double worldCoordX,
			double worldCoordY) {
		final Graph2D graph = view.getGraph2D();
		NodeCursor graphNodes = graph.nodes();
		NodeRealizer nr = null;

		for (; graphNodes.ok(); graphNodes.next()) {
			Node current = graphNodes.node();
			if (!current.equals(newNode1)) {
				nr = graph.getRealizer(current);
				if (nr.getBoundingBox().contains(worldCoordX, worldCoordY)) {
					return current;
				}
			}
		}
		return null;
	}

	/**
	 * Create an edge that connects two nucleotide nodes. two node are both
	 * nucleotide
	 * 
	 * @param graph
	 * @param sourceNode
	 * @param targetNode
	 * @return new edge
	 */
	private static Edge createNucleotideEdge(Graph graph, Node sourceNode,
			Node targetNode) {

		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		Edge edge = null;

		MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
		MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);

		Attachment sourceAtt = sourceMonomerInfo
				.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
		Attachment targetAtt = targetMonomerInfo
				.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);

		if (!sourceMonomerInfo.isConnected(sourceAtt)
				&& !targetMonomerInfo.isConnected(targetAtt)) {
			sourceMonomerInfo.setConnection(sourceAtt, true);
			targetMonomerInfo.setConnection(targetAtt, true);
			edge = graph.createEdge(sourceNode, targetNode);
			edgeMap.set(edge, new EditorEdgeInfoData(sourceAtt, targetAtt));
		}

		return edge;
	}

	/**
	 * Returns a <code>String</code> representation of the index of the
	 * specified realizer's associated newNode.
	 */
	private String getTextValue(final NodeRealizer nr) {
		if (nr == null) {
			return null;
		} else {
			return Integer.toString(nr.getNode().index());
		}

	}

	/**
	 * Returns the template newNode whose index equals the specified
	 * <code>String</code>.
	 */
	private Node getTemplateFromTextValue(final String s) {
		if (s == null) {
			return null;
		} else {
			try {

				// TODO ugly workaround for test
				if (templatesList != null) {
					return ((NodeRealizer) templatesList.getModel()
							.getElementAt(Integer.parseInt(s))).getNode();
				}

				if (templatesTree != null) {

					if (!(templatesTree.getLastSelectedPathComponent() instanceof XmlLeafNode)) {
						return null;
					}

					XmlLeafNode node = (XmlLeafNode) templatesTree
							.getLastSelectedPathComponent();
					Graph2D graph = (Graph2D) node.getUserObject();
					Node[] nodeArray = graph.getNodeArray();
					return nodeArray[0];
				}

			} catch (NumberFormatException nfe) {
				return null;
			}

			return null;
		}
	}

	private NodeRealizer getNodeRealizer(Graph2D graph) {
		Node[] nodeArray = graph.getNodeArray();
		return graph.getRealizer(nodeArray[0]);
	}

	/**
	 * Return the JList that has been configured by this support class.
	 */
	public JList getList() {
		return templatesList;
	}

	private boolean isReplaceable(Graph2D graph, Node newNode, Node oldNode)
			throws MonomerException, IOException, JDOMException {
		final NodeMap monomerInfoNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		MonomerInfo newMonomerInfo = (MonomerInfo) monomerInfoNodeMap
				.get(newNode);
		MonomerInfo oldMonomerInfo = (MonomerInfo) monomerInfoNodeMap
				.get(oldNode);

		Monomer newMonomer;
		Monomer oldMonomer;

		// polymer type must match
		if (newMonomerInfo.getPolymerType().equals(
				oldMonomerInfo.getPolymerType())) {
			// only RNA and PEPTIDE polymer allows monomer replacement
			if (newMonomerInfo.getPolymerType().equals(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				newMonomer = MonomerStoreCache.getInstance()
						.getCombinedMonomerStore().getMonomerDB()
						.get(Monomer.NUCLIEC_ACID_POLYMER_TYPE)
						.get(newMonomerInfo.getMonomerID());
				oldMonomer = MonomerStoreCache.getInstance()
						.getCombinedMonomerStore().getMonomerDB()
						.get(Monomer.NUCLIEC_ACID_POLYMER_TYPE)
						.get(oldMonomerInfo.getMonomerID());
				/*
				 * newMonomer =
				 * MonomerFactory.getInstance().getMonomerDB().get(Monomer
				 * .NUCLIEC_ACID_POLYMER_TYPE
				 * ).get(newMonomerInfo.getMonomerID()); oldMonomer =
				 * MonomerFactory.getInstance().getMonomerDB().get(Monomer.
				 * NUCLIEC_ACID_POLYMER_TYPE
				 * ).get(oldMonomerInfo.getMonomerID());
				 */
				// Monomer Type must match
				if (newMonomer.getMonomerType().equals(
						oldMonomer.getMonomerType())) {
					// backbone monomers should have the same natural analog
					if (newMonomer.getMonomerType().equals(
							Monomer.BACKBONE_MOMONER_TYPE)) {
						if (newMonomer.getNaturalAnalog().equalsIgnoreCase(
								oldMonomer.getNaturalAnalog())) {
							return true;
						}
					} else {
						return true;
					}
				}
			} else if (newMonomerInfo.getPolymerType().equals(
					Monomer.PEPTIDE_POLYMER_TYPE)) {
				
				
				List<Attachment> usedAttList = oldMonomerInfo
						.getUsedAttachmentList();
				List<Attachment> newAttList = newMonomerInfo
						.getAttachmentList();

				//2014-05-13 special handling for adhoc monomers:
				//only check attachment count of both monomers, because adhoc monomers always have R-H attachments 
				oldMonomer = MonomerStoreCache.getInstance().getCombinedMonomerStore().getMonomer(Monomer.PEPTIDE_POLYMER_TYPE, oldMonomerInfo.getMonomerID());
				if (oldMonomer.isAdHocMonomer()){
					if (usedAttList.size()==newAttList.size()){
						return true;
					}	
				}
				
				boolean missing = false;
				for (Attachment usedAtt : usedAttList) {
					String usedID = usedAtt.getAlternateId();
					boolean exist = false;
					for (Attachment newAtt : newAttList) {
						String newID = newAtt.getAlternateId();
						if (newID.equals(usedID)) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						missing = true;
						break;
					}
				}

				if (!missing) {
					return true;
				}
			}
		}
		return false;
	}

	private void handleNewNode(MacromoleculeEditor editor, Graph2D graph,
			Node newNode) throws MonomerException, IOException, JDOMException {
		if (null != newNode) {
			final NodeMap monomerInfoNodeMap = (NodeMap) graph
					.getDataProvider(NodeMapKeys.MONOMER_REF);
			MonomerInfo newMonomerInfo = (MonomerInfo) monomerInfoNodeMap
					.get(newNode);

			if (newMonomerInfo.getPolymerType().equals(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				Monomer newMonomer = MonomerStoreCache.getInstance()
						.getCombinedMonomerStore().getMonomerDB()
						.get(Monomer.NUCLIEC_ACID_POLYMER_TYPE)
						.get(newMonomerInfo.getMonomerID());
				// Monomer newMonomer =
				// MonomerFactory.getInstance().getMonomerDB().get(Monomer.NUCLIEC_ACID_POLYMER_TYPE).get(newMonomerInfo.getMonomerID());
				if (newMonomer.getMonomerType().equals(
						Monomer.BACKBONE_MOMONER_TYPE)) {
					if (newMonomer.getNaturalAnalog().equals(Monomer.ID_R)) {
						editor.getGraphManager().addStartingNode(newNode);
					} else if (newMonomer.getNaturalAnalog().equals(
							Monomer.ID_P)) {
						graph.removeNode(newNode);
					}
				} else if (newMonomer.getMonomerType().equals(
						Monomer.BRANCH_MOMONER_TYPE)) {
					graph.removeNode(newNode);
				}
			} else if (newMonomerInfo.getPolymerType().equals(
					Monomer.PEPTIDE_POLYMER_TYPE)) {
				editor.getGraphManager().addStartingNode(newNode);
			} else if (newMonomerInfo.getPolymerType().equals(
					Monomer.CHEMICAL_POLYMER_TYPE)) {
				editor.getGraphManager().addStartingNode(newNode);
			}

		}
	}
}
