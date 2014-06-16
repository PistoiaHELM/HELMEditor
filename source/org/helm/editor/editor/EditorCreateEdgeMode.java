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
package org.helm.editor.editor;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jdom.JDOMException;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import org.helm.notation.MonomerException;
import org.helm.editor.componentPanel.sequenceviewpanel.EdgeType;
import org.helm.editor.controller.ModelController;
import org.helm.editor.data.AbstractEdgeInfo;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;

class EditorCreateEdgeMode extends CreateEdgeMode {
	// private static final String ARROW_TYPE_T_SHORT = "T_SHORT";
	private IMacromoleculeEditor editor;
	private GraphManager graphManager;

	public EditorCreateEdgeMode(IMacromoleculeEditor editor) {
		this.editor = editor;

		// Arrow.addCustomArrow(ARROW_TYPE_T_SHORT, new Drawable() {
		// public Rectangle getBounds() {
		// return new Rectangle(2, 6);
		// }
		// public void paint(Graphics2D g) {
		//
		// Stroke s = g.getStroke();
		// g.setStroke(new BasicStroke(2));
		// g.drawLine(0, -3, 0, 3);
		// g.setStroke(s);
		// }
		// });
	}

	@Override
	public void mouseReleasedLeft(double x, double y) {
		super.mouseReleasedLeft(x, y);
	}

	/**
	 * Override the createEdge function to disable some edge creation
	 * functionalities
	 * 
	 * @param graph
	 * @param pNode
	 * @param rNode
	 * @param realizer
	 * @return
	 */
	@Override
	protected Edge createEdge(Graph2D graph, Node sourceNode, Node targetNode,
			EdgeRealizer realizer) {
		graphManager = editor.getGraphManager();

		Edge newEdge = null;

		final NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		final NodeMap hyperNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		final EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		// final NodeMap newMonomerMap = (NodeMap)
		// graph.getDataProvider(NodeMapKeys.NEW_MONOMER);

		MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
		MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);

		Map<String, Map<String, Monomer>> monomerDB = null;
		try {
			monomerDB = GraphUtils.getMonomerDB();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Monomer sourceMonomer = monomerDB.get(
				sourceMonomerInfo.getPolymerType()).get(
				sourceMonomerInfo.getMonomerID());
		Monomer targetMonomer = monomerDB.get(
				targetMonomerInfo.getPolymerType()).get(
				targetMonomerInfo.getMonomerID());

		Attachment sourceAttachment = null;
		Attachment targetAttachment = null;
		// regular connection
		if (editor.isRegularConnection()) {
			try {
				// check if connection is allowed, it doesn't check the if the
				// attachment point is available
				int allowed = isConnectionAllowed(sourceNode, targetNode,
						sourceMonomerInfo, targetMonomerInfo);
				List<Attachment> sourceAvailAttList = null;

				List<Attachment> targetAvailAttList = null;
				Icon icon = null;
				switch (allowed) {
				case -1:
					return null;
				case 0:
					// 5' -> R //no such cases anymore
					break;
				case 1:
					// //R->5' , no such cases anymore

					break;
				case 2:
					sourceAttachment = targetMonomerInfo
							.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
					targetAttachment = sourceMonomerInfo
							.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
					if (!sourceMonomerInfo.isConnected(targetAttachment)
							&& !targetMonomerInfo.isConnected(sourceAttachment)) {
						sourceMonomerInfo.setConnection(sourceAttachment, true);
						targetMonomerInfo.setConnection(targetAttachment, true);
						if (graphManager.isStartingNode(sourceNode)
								&& !SequenceGraphTools
										.getNucleicAcidSequenceNodes(
												sourceNode, graph).contains(
												targetNode)) {
							graphManager.removeStartingNode(sourceNode);
						}

						// if (graphManager.isStartingNode(targetNode) &&
						// !SequenceGraphTools.getNucleicAcidSequenceNodes(targetNode,
						// graph).contains(sourceNode)) {
						// graphManager.removeStartingNode(targetNode);
						// }

						newEdge = graph.createEdge(targetNode, sourceNode);
						edgeMap.set(newEdge, new EditorEdgeInfoData(
								sourceAttachment, targetAttachment));

					} else {
						sourceAttachment = targetMonomerInfo
								.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
						targetAttachment = sourceMonomerInfo
								.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
						if (!sourceMonomerInfo.isConnected(targetAttachment)
								&& !targetMonomerInfo
										.isConnected(sourceAttachment))
							createEdge(graph, targetNode, sourceNode, realizer);
					}

					break;
				case 3:
					sourceAttachment = sourceMonomerInfo
							.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
					targetAttachment = targetMonomerInfo
							.getAttachment(Attachment.BRANCH_MONOMER_ATTACHEMENT);
					if (!sourceMonomerInfo.isConnected(sourceAttachment)
							&& !targetMonomerInfo.isConnected(targetAttachment)) {
						sourceMonomerInfo.setConnection(sourceAttachment, true);
						targetMonomerInfo.setConnection(targetAttachment, true);
						newEdge = graph.createEdge(sourceNode, targetNode);
						edgeMap.set(newEdge, new EditorEdgeInfoData(
								sourceAttachment, targetAttachment));
						if (graphManager.isStartingNode(targetNode)) {
							graphManager.removeStartingNode(targetNode);
						}
					}
					// SequenceGraphTools.annotateAllBasePosition(graph,
					// graphManager);
					break;
				case 4:
					// base -> R, allowed, but need to exchange source and
					// target node
					sourceAttachment = sourceMonomerInfo
							.getAttachment(Attachment.BRANCH_MONOMER_ATTACHEMENT);
					targetAttachment = targetMonomerInfo
							.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT);
					if (!sourceMonomerInfo.isConnected(sourceAttachment)
							&& !targetMonomerInfo.isConnected(targetAttachment)) {
						sourceMonomerInfo.setConnection(sourceAttachment, true);
						targetMonomerInfo.setConnection(targetAttachment, true);
						newEdge = graph.createEdge(targetNode, sourceNode);

						edgeMap.set(newEdge, new EditorEdgeInfoData(
								sourceAttachment, targetAttachment));
						if (graphManager.isStartingNode(sourceNode)) {
							graphManager.removeStartingNode(sourceNode);
						}
					}
					break;
				case 5:
					// peptide -> peptide
					newEdge = chooseConnectionsAndCreate(graph, sourceNode,
							targetNode, true);
					// Try reversed connection
					if (newEdge == null) {
						Node t = sourceNode;
						sourceNode = targetNode;
						targetNode = t;
						newEdge = chooseConnectionsAndCreate(graph, sourceNode,
								targetNode, true);
					}

					if (newEdge == null)
						break;

					AbstractEdgeInfo einfo = (AbstractEdgeInfo) edgeMap
							.get(newEdge);

					boolean sameSequence = sameSequence(graph, sourceNode,
							targetNode);
					// remove starting node if backbone connection between
					// sequences
					if (graphManager.isStartingNode(targetNode)
							&& !sameSequence
							&& (einfo.getType() == EdgeType.REGULAR)) {
						graphManager.removeStartingNode(targetNode);
					}
					// SequenceGraphTools.annotateAllBasePosition(graph,
					// graphManager);
					break;
				case 6:
					// chem -> sequence

					sourceAvailAttList = sourceMonomerInfo
							.getAvailableAttachmentList();
					targetAvailAttList = targetMonomerInfo
							.getAvailableAttachmentList();

					// get the targetAtt point
					if (sourceAvailAttList.size() > 1
							&& targetAvailAttList.size() >= 1) {
						icon = getMonomerIcon(sourceMonomer);
						sourceAttachment = (Attachment) JOptionPane
								.showInputDialog(
										null,
										"There are more than one possible attachments for "
												+ sourceMonomerInfo
														.getMonomerID() + ":\n"
												+ "\"Please choose one...\"",
										sourceMonomerInfo.getMonomerID(),
										JOptionPane.QUESTION_MESSAGE, icon,
										sourceAvailAttList.toArray(),
										sourceAvailAttList.get(0));
					} else if (sourceAvailAttList.size() == 1) {
						sourceAttachment = sourceAvailAttList.get(0);
					}

					if (targetAvailAttList.size() > 1
							&& sourceAvailAttList.size() >= 1) {

						icon = getMonomerIcon(targetMonomer);
						targetAttachment = (Attachment) JOptionPane
								.showInputDialog(
										null,
										"There are more than one possible attachments for "
												+ targetMonomerInfo
														.getMonomerID() + ":\n"
												+ "\"Please choose one...\"",
										targetMonomerInfo.getMonomerID(),
										JOptionPane.QUESTION_MESSAGE, icon,
										targetAvailAttList.toArray(),
										targetAvailAttList.get(0));
					} else if (targetAvailAttList.size() == 1) {
						targetAttachment = targetAvailAttList.get(0);
					}

					if (sourceAttachment != null && targetAttachment != null) {
						sourceMonomerInfo.setConnection(sourceAttachment, true);
						targetMonomerInfo.setConnection(targetAttachment, true);
						newEdge = graph.createEdge(targetNode, sourceNode);
						edgeMap.set(newEdge, new EditorEdgeInfoData(
								targetAttachment, sourceAttachment));

						if (targetMonomerInfo.getPolymerType()
								.equalsIgnoreCase(
										Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
							// EdgeRealizer edgeRealizer =
							// graph.getRealizer(newEdge);
						}
					}

					break;
				case 7:
					// sequence -> chem
					sourceAvailAttList = sourceMonomerInfo
							.getAvailableAttachmentList();
					targetAvailAttList = targetMonomerInfo
							.getAvailableAttachmentList();

					// get the targetAtt point
					if (sourceAvailAttList.size() > 1
							&& targetAvailAttList.size() >= 1) {
						icon = getMonomerIcon(sourceMonomer);
						sourceAttachment = (Attachment) JOptionPane
								.showInputDialog(
										null,
										"There are more than one possible attachments for "
												+ sourceMonomerInfo
														.getMonomerID() + ":\n"
												+ "\"Please choose one...\"",
										sourceMonomerInfo.getMonomerID(),
										JOptionPane.QUESTION_MESSAGE, icon,
										sourceAvailAttList.toArray(),
										sourceAvailAttList.get(0));
						//
					} else if (sourceAvailAttList.size() == 1) {
						sourceAttachment = sourceAvailAttList.get(0);
					}

					if (targetAvailAttList.size() > 1
							&& sourceAvailAttList.size() >= 1) {
						icon = getMonomerIcon(targetMonomer);

						targetAttachment = (Attachment) JOptionPane
								.showInputDialog(
										null,
										"There are more than one possible attachments for "
												+ targetMonomerInfo
														.getMonomerID() + ":\n"
												+ "\"Please choose one...\"",
										targetMonomerInfo.getMonomerID(),
										JOptionPane.QUESTION_MESSAGE, icon,
										targetAvailAttList.toArray(),
										targetAvailAttList.get(0));
					} else if (targetAvailAttList.size() == 1) {
						targetAttachment = targetAvailAttList.get(0);
					}

					if (sourceAttachment != null && targetAttachment != null) {
						sourceMonomerInfo.setConnection(sourceAttachment, true);
						targetMonomerInfo.setConnection(targetAttachment, true);
						newEdge = graph.createEdge(sourceNode, targetNode);
						edgeMap.set(newEdge, new EditorEdgeInfoData(
								sourceAttachment, targetAttachment));

					}

					break;
				case 8:
					// R->R
					Node pNode = null;
					boolean cyclizationEdge = hyperNodeMap.get(sourceNode)
							.equals(hyperNodeMap.get(targetNode));

					if (SequenceGraphTools
							.isLastNucleicacidBackbone(sourceNode)
							&& SequenceGraphTools
									.isFirstNucleicacidBackbone(targetNode)) {
						pNode = SequenceGraphTools.appendPhosphate(sourceNode);
						sourceNode = pNode;
					} else if (SequenceGraphTools
							.isLastNucleicacidBackbone(targetNode)
							&& SequenceGraphTools
									.isFirstNucleicacidBackbone(sourceNode)) {
						pNode = SequenceGraphTools.appendPhosphate(targetNode);
						targetNode = sourceNode;
					}
					if (pNode != null) {
						sourceNode = pNode;
						sourceMonomerInfo = (MonomerInfo) nodeMap
								.get(sourceNode);
						sourceMonomer = monomerDB.get(
								sourceMonomerInfo.getPolymerType()).get(
								sourceMonomerInfo.getMonomerID());

						targetMonomerInfo = (MonomerInfo) nodeMap
								.get(targetNode);
						targetMonomer = monomerDB.get(
								targetMonomerInfo.getPolymerType()).get(
								targetMonomerInfo.getMonomerID());

						sourceAttachment = sourceMonomerInfo
								.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
						targetAttachment = targetMonomerInfo
								.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
						if (!sourceMonomerInfo.isConnected(sourceAttachment)
								&& !targetMonomerInfo
										.isConnected(targetAttachment)) {
							sourceMonomerInfo.setConnection(sourceAttachment,
									true);
							targetMonomerInfo.setConnection(targetAttachment,
									true);
							newEdge = graph.createEdge(sourceNode, targetNode);

							edgeMap.set(newEdge, new EditorEdgeInfoData(
									sourceAttachment, targetAttachment));

							if (graphManager.isStartingNode(targetNode)
									&& !cyclizationEdge) {
								graphManager.removeStartingNode(targetNode);
							}
						}
						// if the connection failed
						if (newEdge == null && pNode != null) {
							// remove pNode
							EdgeCursor inEdges = pNode.inEdges();
							for (; inEdges.ok(); inEdges.next()) {

								EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap
										.get(inEdges.edge());
								sourceMonomerInfo.setConnection(
										edgeInfo.getSourceNodeAttachment(),
										false);
								targetMonomerInfo.setConnection(
										edgeInfo.getTargetNodeAttachment(),
										false);
							}
							graph.removeNode(pNode);
						}
					}
					break;
				case 9: // chemical to chemical
					newEdge = chooseConnectionsAndCreate(graph, sourceNode,
							targetNode, false);
					break;
				default:
					break;
				}
			} catch (Exception ex) {
				ExceptionHandler.handleException(ex);
			}
		} else {
			try {
				if (SequenceGraphTools.pairable(sourceMonomerInfo,
						targetMonomerInfo)) {
					if (SequenceGraphTools.checkDistance(sourceNode,
							targetNode, graph, true)) {
						newEdge = pairEdge(sourceNode, targetNode, graph);
					}
				}
			} catch (Exception ex) {
				ExceptionHandler.handleException(ex);
			}
		}

		if (newEdge != null) {
			// realizer = graph.getRealizer(newEdge);
			// if (editor.isRegularConnection()) {
			// EdgeInfo ei =
			// (EdgeInfo)graph.getDataProvider(EdgeMapKeys.EDGE_INFO).get(newEdge);
			// if (ei.isPBranchBranch()) {
			// realizer.setSourceArrow(Arrow.getCustomArrow(ARROW_TYPE_T_SHORT));
			// realizer.setSourceArrow(Arrow.getCustomArrow(ARROW_TYPE_T_SHORT));
			// }
			// if (ei.isPBranchBackbone()) {
			// realizer.setSourceArrow(Arrow.getCustomArrow(ARROW_TYPE_T_SHORT));
			// }
			// realizer.setLineType(LineType.LINE_1);
			// realizer.setLineColor(Color.BLACK);
			// } else {
			// realizer.setLineType(LineType.DOTTED_3);
			// realizer.setLineColor(Color.BLUE);
			// }

			ModelController.notationUpdated(editor.getNotation(),
					editor.getOwnerCode());
		}

		return newEdge;
	}

	private Edge chooseConnectionsAndCreate(Graph graph, Node sourceNode,
			Node targetNode, boolean isChain) {

		final NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		final EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);

		MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
		MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);

		List<Attachment> sourceAvailAttList = sourceMonomerInfo
				.getAvailableAttachmentList();
		List<Attachment> targetAvailAttList = targetMonomerInfo
				.getAvailableAttachmentList();

		List<Attachment> newSource = new ArrayList<Attachment>();
		List<Attachment> newTarget = new ArrayList<Attachment>();

		for (Attachment a : sourceAvailAttList) {
			if (!Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT.equals(a
					.getLabel())) {
				newSource.add(a);
			}
		}

		for (Attachment a : targetAvailAttList) {
			if (!Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT.equals(a
					.getLabel())) {
				newTarget.add(a);
			}
		}

		if (isChain) {
			sourceAvailAttList = newSource;
			targetAvailAttList = newTarget;
		}

		Attachment sourceAttachment = null;
		Attachment targetAttachment = null;
		Icon icon = null;

		Map<String, Map<String, Monomer>> monomerDB = null;
		try {
			monomerDB = GraphUtils.getMonomerDB();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Monomer sourceMonomer = monomerDB.get(
				sourceMonomerInfo.getPolymerType()).get(
				sourceMonomerInfo.getMonomerID());
		Monomer targetMonomer = monomerDB.get(
				targetMonomerInfo.getPolymerType()).get(
				targetMonomerInfo.getMonomerID());

		// get the targetAtt point
		if (sourceAvailAttList.size() > 1 && targetAvailAttList.size() >= 1) {
			icon = getMonomerIcon(sourceMonomer);
			sourceAttachment = (Attachment) JOptionPane.showInputDialog(null,
					"There are more than one possible attachments for "
							+ sourceMonomerInfo.getMonomerID() + ":\n"
							+ "\"Please choose one...\"",
					sourceMonomerInfo.getMonomerID(),
					JOptionPane.QUESTION_MESSAGE, icon,
					sourceAvailAttList.toArray(), sourceAvailAttList.get(0));
		} else if (sourceAvailAttList.size() == 1) {
			sourceAttachment = sourceAvailAttList.get(0);
		}

		if (targetAvailAttList.size() > 1 && sourceAvailAttList.size() >= 1) {

			icon = getMonomerIcon(targetMonomer);
			targetAttachment = (Attachment) JOptionPane.showInputDialog(null,
					"There are more than one possible attachments for "
							+ targetMonomerInfo.getMonomerID() + ":\n"
							+ "\"Please choose one...\"",
					targetMonomerInfo.getMonomerID(),
					JOptionPane.QUESTION_MESSAGE, icon,
					targetAvailAttList.toArray(), targetAvailAttList.get(0));
		} else if (targetAvailAttList.size() == 1) {
			targetAttachment = targetAvailAttList.get(0);
		}

		if (sourceAttachment != null && targetAttachment != null) {
			sourceMonomerInfo.setConnection(sourceAttachment, true);
			targetMonomerInfo.setConnection(targetAttachment, true);

			Edge newEdge = null;
			if (changeDirection(graph, sourceNode, targetNode)) {
				newEdge = graph.createEdge(targetNode, sourceNode);
				edgeMap.set(newEdge, new EditorEdgeInfoData(targetAttachment,
						sourceAttachment));
			} else {
				newEdge = graph.createEdge(sourceNode, targetNode);
				edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment,
						targetAttachment));
			}
			return newEdge;
		}
		return null;
	}

	private boolean sameSequence(Graph graph, Node sourceNode, Node targetNode) {
		NodeMap node2hyperNode = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
		Node targetHyperNode = (Node) node2hyperNode.get(targetNode);
		Node sourceHyperNode = (Node) node2hyperNode.get(sourceNode);

		if ((targetHyperNode == null) || (sourceHyperNode == null)) {
			return false;
		}
		return targetHyperNode.equals(sourceHyperNode);
	}

	// if the edge will connect nodes from the same sequence, we should adjust
	// the
	// direction to deal with directed cycles
	private boolean changeDirection(Graph graph, Node sourceNode,
			Node targetNode) {
		if (!sameSequence(graph, sourceNode, targetNode)) {
			return false;
		}

		NodeMap positions = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_POSITION);
		Integer spos = (Integer) positions.get(sourceNode);
		if (spos == null) {
			return false;
		}
		Integer tpos = (Integer) positions.get(targetNode);
		if (tpos == null) {
			return false;
		}
		return (spos < tpos);
	}

	public Edge pairEdge(Node sourceNode, Node targetNode, final Graph graph) {

		NodeMap nodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.MONOMER_REF);
		EdgeMap edgeMap = (EdgeMap) graph
				.getDataProvider(EdgeMapKeys.EDGE_INFO);
		MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
		MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);

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

	/**
	 * check if an connection can be made, interchange source node and target
	 * node if nessesarry
	 * 
	 * @param sourceMonomerInfo
	 * @param targetMonomerInfo
	 * @return -1: not allowed; 0 : allowed; 1: allowed but need to exchange
	 *         source and target
	 * @throws JDOMException
	 * @throws IOException
	 * @throws MonomerException
	 */
	private int isConnectionAllowed(Node sourceNode, Node targetNode,
			MonomerInfo sourceMonomerInfo, MonomerInfo targetMonomerInfo)
			throws MonomerException, IOException, JDOMException {

		// multiple edges between two nodes are not allowed
		if (sourceNode.getEdge(targetNode) != null) {
			return -1;
		}

		// self loop is not allowed
		if (sourceNode == targetNode) {
			return -1;
		}

		Map<String, Map<String, Monomer>> monomerDB = GraphUtils.getMonomerDB();
		Monomer sourceMonomer = monomerDB.get(
				sourceMonomerInfo.getPolymerType()).get(
				sourceMonomerInfo.getMonomerID());
		Monomer targetMonomer = monomerDB.get(
				targetMonomerInfo.getPolymerType()).get(
				targetMonomerInfo.getMonomerID());
		Graph graph = sourceNode.getGraph();
		final NodeMap hyperNodeMap = (NodeMap) graph
				.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);

		// if
		// (hyperNodeMap.get(sourceNode).equals(hyperNodeMap.get(targetNode))) {
		// return -1;
		// }

		// check if the type is connectable
		// 1. allow two node of the same type being connected except chemical
		// structure;
		if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
				targetMonomerInfo.getPolymerType())) {
			// 1.1 if both poly type are the nucleiotide Acid, then we only
			// allow P->R, P->base, R->P, and 5->R, we change the directions if
			// nessesary
			if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
				// 5-> R or R->5 is allowed
				// if
				// (sourceMonomerInfo.getMonomerID().equalsIgnoreCase(MonomerInfo.STARTING)
				// && (targetMonomer != null &&
				// targetMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)))
				// {
				// return 0;
				// } else if
				// (targetMonomerInfo.getMonomerID().equalsIgnoreCase(MonomerInfo.STARTING)
				// && (sourceMonomer != null &&
				// sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)))
				// {
				// return 1;
				// } else

				if (!SequenceGraphTools.checkDistance(sourceNode, targetNode,
						sourceNode.getGraph(), false)) {
					return -1;
				}

				// P->R or R->P is allowed
				if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_P)
						&& (targetMonomer.getNaturalAnalog()
								.equalsIgnoreCase(Monomer.ID_R))) {
					return 2;
				} else if (targetMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_P)
						&& (sourceMonomer.getNaturalAnalog()
								.equalsIgnoreCase(Monomer.ID_R))) {
					return 2;
				} // R-> base or base -> R
				else if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_R)
						&& targetMonomer.getMonomerType().equalsIgnoreCase(
								Monomer.BRANCH_MOMONER_TYPE)) {
					return 3;
				} else if (targetMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_R)
						&& sourceMonomer.getMonomerType().equalsIgnoreCase(
								Monomer.BRANCH_MOMONER_TYPE)) {
					return 4;
				} // R->R
				else if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_R)
						&& targetMonomer.getNaturalAnalog().equalsIgnoreCase(
								Monomer.ID_R)) {
					return 8;
				}
			} else if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.PEPTIDE_POLYMER_TYPE)) {

				return 5; // todo need to check direction?
			} else if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.CHEMICAL_POLYMER_TYPE)) { // two chemical monomer

				// cycles are allowed now
				// Edge tempEdge = graph.createEdge(sourceNode, targetNode);
				//
				// EdgeList edgeList = Cycles.findCycle(graph, false);
				// if (edgeList.contains(tempEdge)) {
				// graph.removeEdge(tempEdge);
				// return -1;
				// }
				//
				// graph.removeEdge(tempEdge);
				return 9;
			}
			// two monomer are of different type, then we only allow sequence ->
			// chemical
		} else if (!sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
				targetMonomerInfo.getPolymerType())) {
			if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.CHEMICAL_POLYMER_TYPE)) {
				return 6;
			} else if (targetMonomerInfo.getPolymerType().equalsIgnoreCase(
					Monomer.CHEMICAL_POLYMER_TYPE)) {
				return 7;
			}
		}
		return -1;
	}

	/**
	 * test if the edge is between different nucleotide units or between a
	 * nucleotide and a chemical structure
	 * 
	 * @param newEdge
	 * @param sourceMonomer
	 * @param targetMonomer
	 * @return
	 */
	private boolean isInsertionEdge(Edge newEdge, Monomer sourceMonomer,
			Monomer targetMonomer) {
		if (newEdge == null) {
			return false;
		}
		if (sourceMonomer.getPolymerType().equalsIgnoreCase(
				Monomer.CHEMICAL_POLYMER_TYPE)
				&& targetMonomer.getPolymerType().equalsIgnoreCase(
						Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return true;
		} else if (sourceMonomer.getPolymerType().equalsIgnoreCase(
				Monomer.NUCLIEC_ACID_POLYMER_TYPE)
				&& targetMonomer.getPolymerType().equalsIgnoreCase(
						Monomer.CHEMICAL_POLYMER_TYPE)) {
			return true;
		} else if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(
				Monomer.ID_P)
				&& targetMonomer.getNaturalAnalog().equalsIgnoreCase(
						Monomer.ID_R)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * generate an icon that displays the monomer structure
	 * 
	 * @param monomer
	 * @return monomer Icon
	 */
	public static Icon getMonomerIcon(Monomer monomer) {
		Icon icon = null;

		if (monomer != null) {
			Molecule mol = null;
			if (monomer.getMolfile() != null) {
				try {
					mol = MolImporter.importMol(monomer.getMolfile());
				} catch (MolFormatException ex) {
					Logger.getLogger(MacromoleculeEditor.class.getName()).log(
							Level.SEVERE, null, ex);
				}
				try {
					mol = MolImporter.importMol(monomer.getCanSMILES());
				} catch (MolFormatException ex) {
					Logger.getLogger(MacromoleculeEditor.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}

			if (mol != null) {
				Image image = (Image) mol.toObject("image");
				icon = new ImageIcon(image);
			}

		}

		return icon;
	}

	private String getNotation() {
		return null;
	}
}
