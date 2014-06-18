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
package org.helm.editor.protein.edit;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NotationException;
import org.helm.notation.model.ComplexPolymer;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.PolymerEdge;
import org.helm.notation.model.PolymerNode;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.SimpleNotationParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;

/**
 * Data from ProteinInputPanel
 * 
 * @author ZHANGTIANHONG
 */
public class ComplexProtein {

	public static final String SEQUENCE_SEPARATOR_SYMBOL = "$";
	public static final String START_MODIFICATION_DECORATOR = "[";
	public static final String END_MODIFICATION_DECORATOR = "]";
	public static final String DISULFIDE_MONOMERS = "C";
	public static final String R1_R3_AMIDE_MONOMERS = "DE";
	public static final String R2_R3_AMIDE_MONOMERS = "K";
	private List<String> sequences;
	private List<SequenceAnnotation> annotations;
	private List<PeptideConnection> connections;

	public List<String> getSequences() {
		return sequences;
	}

	public void setSequences(List<String> sequences) {
		this.sequences = sequences;
	}

	public List<SequenceAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<SequenceAnnotation> annotations) {
		this.annotations = annotations;
	}

	public List<PeptideConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<PeptideConnection> connections) {
		this.connections = connections;
	}

	public String getNotation() throws NotationException, MonomerException,
			IOException, JDOMException {
		if (null == sequences) {
			sequences = new ArrayList<String>();
		}

		if (null == annotations) {
			annotations = new ArrayList<SequenceAnnotation>();
		}

		if (null == connections) {
			connections = new ArrayList<PeptideConnection>();
		}

		if (sequences.isEmpty()) {
			if (!annotations.isEmpty() || !connections.isEmpty()) {
				throw new NotationException(
						"Sequence is empty, but annotation or connection is not");
			} else {
				return "";
			}
		}

		List<List<String>> aaListOfList = parseSequences(sequences);
		List<String> simpleNotations = getSimpleNotations(aaListOfList);

		List<String> aaConIDs = new ArrayList<String>();
		List<String> connectionNotations = getConnectionNotations(aaListOfList,
				connections, aaConIDs);

		List<String> annNotations = getAnnotationNotations(sequences,
				annotations);

		return combine(simpleNotations, connectionNotations, annNotations);
	}

	public static ComplexProtein convert(String notation)
			throws NotationException, MonomerException, JDOMException,
			IOException {
		ComplexProtein protein = new ComplexProtein();
		ComplexPolymer cp = ComplexNotationParser.parse(notation);
		List<PolymerNode> nodeList = cp.getPolymerNodeList();
		List<String> sequences = new ArrayList<String>();
		Map<String, Integer> idMap = new HashMap<String, Integer>();
		Map<String, String> notationMap = new HashMap<String, String>();
		int i = 1;

		if (null != nodeList && !nodeList.isEmpty()) {
			for (PolymerNode node : nodeList) {
				if (!Monomer.PEPTIDE_POLYMER_TYPE.equals(node.getType())) {
					throw new NotationException("Non-Peptide polymer");
				}
				String seq = node.getLabel().replaceAll(
						SimpleNotationParser.GROUP_LEVEL_DELIMITER_REGEX, "");
				sequences.add(seq);
				idMap.put(node.getId(), new Integer(i));
				notationMap.put(node.getId(), node.getLabel());
				i++;
			}
			protein.setSequences(sequences);
		}

		List<PolymerEdge> edgeList = cp.getPolymerEdgeList();
		if (null != edgeList && !edgeList.isEmpty()) {
			List<PeptideConnection> connections = new ArrayList<PeptideConnection>();
			for (PolymerEdge edge : edgeList) {
				String sourceNode = edge.getSourceNode();
				int sourceSeqID = idMap.get(sourceNode).intValue();
				int sourceMonomerNum = edge.getSourceMonomerNumber();
				String sourceNotation = notationMap.get(sourceNode);
				List<String> sourceMonomerIDList = SimpleNotationParser
						.getMonomerIDList(sourceNotation,
								Monomer.PEPTIDE_POLYMER_TYPE);
				String sourceMonomerID = sourceMonomerIDList
						.get(sourceMonomerNum - 1);
				String sourceR = edge.getSourceR();

				String targetNode = edge.getTargetNode();
				int targetSeqID = idMap.get(targetNode).intValue();
				int targetMonomerNum = edge.getTargetMonomerNumber();
				String targetNotation = notationMap.get(targetNode);
				List<String> targetMonomerIDList = SimpleNotationParser
						.getMonomerIDList(targetNotation,
								Monomer.PEPTIDE_POLYMER_TYPE);
				String targetMonomerID = targetMonomerIDList
						.get(targetMonomerNum - 1);
				String targetR = edge.getTargetR();

				String bondType = getBondType(sourceMonomerID, sourceR,
						targetMonomerID, targetR);

				PeptideConnection pc = new PeptideConnection();
				pc.setSourceSequenceIndex(sourceSeqID);
				pc.setSourceAminoAcidIndex(sourceMonomerNum);
				pc.setTargetSequenceIndex(targetSeqID);
				pc.setTargetAminoAcidIndex(targetMonomerNum);
				pc.setBondType(bondType);
				connections.add(pc);
			}
			protein.setConnections(connections);
		}

		Map<String, String> annotations = cp.getPolymerNodeAnnotationMap();
		if (null != annotations && !annotations.isEmpty()) {
			Set<String> nodeSet = annotations.keySet();
			List<SequenceAnnotation> annList = new ArrayList<SequenceAnnotation>();
			for (Iterator<String> it = nodeSet.iterator(); it.hasNext();) {
				String node = it.next();
				int id = idMap.get(node).intValue();
				String ann = annotations.get(node);
				SequenceAnnotation sa = new SequenceAnnotation();
				sa.setId(id);
				sa.setAnnotation(ann);
				annList.add(sa);
			}
			protein.setAnnotations(annList);
		}
		return protein;
	}

	private static String getBondType(String sourceMonomerID, String sourceR,
			String targetMonomerID, String targetR) {
		if ((DISULFIDE_MONOMERS.contains(sourceMonomerID) && "R3"
				.equals(sourceR))
				|| (DISULFIDE_MONOMERS.contains(targetMonomerID) && "R3"
						.equals(targetR))) {
			return PeptideConnection.DISULFIDE_BOND;
		} else {
			return PeptideConnection.AMIDE_BOND;
		}
	}

	/**
	 * This method throws exception if input sequence is invalid, or unable to
	 * determine its validity
	 * 
	 * @param sequence
	 *            - clean, non-null amino acid sequence
	 * @return invalid list of amino acid IDs
	 * @throws NotationException
	 * @throws MonomerException
	 * @throws IOException
	 * @throws JDOMException
	 */
	private List<String> getAminoAcidIDs(String sequence)
			throws NotationException, MonomerException, IOException,
			JDOMException {
		char[] chars = sequence.toCharArray();
		int leftBracketCount = 0;
		int rightBracketCount = 0;
		for (int i = 0; i < chars.length; i++) {
			if (START_MODIFICATION_DECORATOR.equals(String.valueOf(chars[i]))) {
				leftBracketCount++;
			}
			if (END_MODIFICATION_DECORATOR.equals(String.valueOf(chars[i]))) {
				rightBracketCount++;
			}
		}
		if (leftBracketCount != rightBracketCount) {
			throw new NotationException("Number of starting bracket "
					+ leftBracketCount + " and number of ending bracket "
					+ rightBracketCount + " does not match\n");
		}

		Map<String, Monomer> aaMap = MonomerStoreCache.getInstance()
				.getCombinedMonomerStore().getMonomerDB()
				.get(Monomer.PEPTIDE_POLYMER_TYPE);
		// Map<String, Monomer> aaMap =
		// MonomerFactory.getInstance().getMonomerDB().get(Monomer.PEPTIDE_POLYMER_TYPE);
		List<String> aaList = new ArrayList<String>();
		boolean started = false;
		boolean ended = false;
		String tmp = "";

		for (int i = 0; i < chars.length; i++) {
			if (START_MODIFICATION_DECORATOR.equals(String.valueOf(chars[i]))) {
				started = true;
			} else if (END_MODIFICATION_DECORATOR.equals(String
					.valueOf(chars[i]))) {
				ended = true;
			} else {
				if (started) {
					tmp = tmp + String.valueOf(chars[i]);
				} else {
					tmp = String.valueOf(chars[i]);
					started = true;
					ended = true;
				}
			}

			if (started && ended) {
				if (aaMap.containsKey(tmp)) {
					aaList.add(tmp);

				} else {
					throw new NotationException("Unknown amino acid symbol "
							+ tmp + "\n");
				}

				started = false;
				ended = false;
				tmp = "";
			}
		}

		return aaList;
	}

	private List<String> getSimpleNotations(List<List<String>> aaListOfList) {
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < aaListOfList.size(); i++) {
			String notation = getSimpleNotation(i + 1, aaListOfList.get(i));
			l.add(notation);
		}
		return l;
	}

	private String getSimpleNotation(int seqNo, List<String> aaList) {
		String polymerLabel = "PEPTIDE" + seqNo;
		StringBuilder sb = new StringBuilder();
		sb.append(polymerLabel);
		sb.append("{");
		for (int i = 0; i < aaList.size(); i++) {
			String aa = aaList.get(i);
			if (aa.length() == 1) {
				sb.append(aa);
			} else {
				sb.append(START_MODIFICATION_DECORATOR);
				sb.append(aa);
				sb.append(END_MODIFICATION_DECORATOR);
			}
			if (i != aaList.size() - 1) {
				sb.append(".");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	private List<List<String>> parseSequences(List<String> sequences)
			throws NotationException, MonomerException, IOException,
			JDOMException {
		List<List<String>> lol = new ArrayList<List<String>>();
		for (String sequence : sequences) {
			List<String> ids = getAminoAcidIDs(sequence);
			lol.add(ids);
		}
		return lol;
	}

	private String getConnectionNotation(List<List<String>> aaListOfList,
			PeptideConnection con, List<String> aaConIDs)
			throws NotationException {
		String result = null;
		String bondType = con.getBondType();
		if (!con.isValidBondType(bondType)) {
			throw new NotationException("Invalid bond type: " + bondType);
		}

		int sourceSeqIndex = con.getSourceSequenceIndex();
		int sourceAAIndex = con.getSourceAminoAcidIndex();
		int targetSeqIndex = con.getTargetSequenceIndex();
		int targetAAIndex = con.getTargetAminoAcidIndex();

		if (sourceSeqIndex < 1 || sourceSeqIndex > aaListOfList.size()) {
			throw new NotationException(
					"Source sequence index out of range for " + con.toString());
		}

		if (targetSeqIndex < 1 || targetSeqIndex > aaListOfList.size()) {
			throw new NotationException(
					"Target sequence index out of range for " + con.toString());
		}

		if (sourceAAIndex < 1
				|| sourceAAIndex > aaListOfList.get(sourceSeqIndex - 1).size()) {
			throw new NotationException(
					"Source amino acid position out of range for "
							+ con.toString());
		}

		if (targetAAIndex < 1
				|| targetAAIndex > aaListOfList.get(targetSeqIndex - 1).size()) {
			throw new NotationException(
					"Target amino acid position out of range for "
							+ con.toString());
		}

		if (sourceSeqIndex == targetSeqIndex) {
			if (sourceAAIndex == targetAAIndex) {
				throw new NotationException(
						"Source and target amino acid is the same: "
								+ con.toString());
			}
		} else {
			if (sourceAAIndex == 1
					&& targetAAIndex == aaListOfList.get(targetSeqIndex - 1)
							.size()) {
				throw new NotationException(
						"Connection causes chain enlongation: "
								+ con.toString());
			}

			if (targetAAIndex == 1
					&& sourceAAIndex == aaListOfList.get(sourceSeqIndex - 1)
							.size()) {
				throw new NotationException(
						"Connection causes chain enlongation: "
								+ con.toString());
			}
		}

		String sourceAAId = aaListOfList.get(sourceSeqIndex - 1).get(
				sourceAAIndex - 1);
		String targetAAId = aaListOfList.get(targetSeqIndex - 1).get(
				targetAAIndex - 1);

		String sourceConId = "" + sourceSeqIndex + "-" + sourceAAIndex;
		String targetConId = "" + targetSeqIndex + "-" + targetAAIndex;

		String connectionLabel = "PEPTIDE" + sourceSeqIndex + ",PEPTIDE"
				+ targetSeqIndex + ",";
		if (aaConIDs.contains(sourceConId)) {
			throw new NotationException("The same amino acid " + sourceAAId
					+ "[" + sourceConId + "]"
					+ " is used in multiple  connections");
		} else {
			aaConIDs.add(sourceConId);
		}

		if (aaConIDs.contains(targetConId)) {
			throw new NotationException("The same amino acid " + targetAAId
					+ "[" + targetConId + "]"
					+ " is used in multiple  connections");
		} else {
			aaConIDs.add(sourceConId);
		}

		if (bondType.equals(PeptideConnection.DISULFIDE_BOND)) {
			if (!DISULFIDE_MONOMERS.contains(sourceAAId)
					|| !DISULFIDE_MONOMERS.contains(targetAAId)) {
				throw new NotationException("Invalid disulfide bond between "
						+ sourceAAId + "[" + sourceConId + "]" + " and "
						+ targetAAId + "[" + targetConId + "]");
			} else {
				result = connectionLabel + sourceAAIndex + ":R3-"
						+ targetAAIndex + ":R3";
			}
		} else {
			if (sourceSeqIndex == targetSeqIndex) {
				if (sourceAAIndex == 1) {
					if (targetAAIndex == aaListOfList.get(targetSeqIndex - 1)
							.size()) {
						result = connectionLabel + sourceAAIndex + ":R1-"
								+ targetAAIndex + ":R2";
					} else {
						if (R1_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R1-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				} else if (sourceAAIndex == aaListOfList
						.get(sourceSeqIndex - 1).size()) {
					if (targetAAIndex == 1) {
						result = connectionLabel + sourceAAIndex + ":R2-"
								+ targetAAIndex + ":R1";

					} else {
						if (R2_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R2-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				} else {
					if (targetAAIndex == 1) {
						if (R1_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R1";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					} else if (targetAAIndex == aaListOfList.get(
							targetSeqIndex - 1).size()) {
						if (R2_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R2";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					} else {
						if (R1_R3_AMIDE_MONOMERS.contains(sourceAAId)
								&& R2_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R3";
						} else if (R1_R3_AMIDE_MONOMERS.contains(targetAAId)
								&& R2_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				}
			} else {
				// interchain connection
				if (sourceAAIndex == 1) {
					if (targetAAIndex == 1
							|| targetAAIndex == aaListOfList.get(
									targetSeqIndex - 1).size()) {
						throw new NotationException(
								"End to end inter-chain connection not allowed: "
										+ con.toString());
					} else {
						if (R1_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R1-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				} else if (sourceAAIndex == aaListOfList
						.get(sourceSeqIndex - 1).size()) {
					if (targetAAIndex == 1
							|| targetAAIndex == aaListOfList.get(
									targetSeqIndex - 1).size()) {
						throw new NotationException(
								"End to end inter-chain connection not allowed: "
										+ con.toString());
					} else {
						if (R2_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R2-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				} else {
					if (targetAAIndex == 1) {
						if (R1_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R1";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					} else if (targetAAIndex == aaListOfList.get(
							targetSeqIndex - 1).size()) {
						if (R2_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R2";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					} else {
						if (R1_R3_AMIDE_MONOMERS.contains(sourceAAId)
								&& R2_R3_AMIDE_MONOMERS.contains(targetAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R3";
						} else if (R1_R3_AMIDE_MONOMERS.contains(targetAAId)
								&& R2_R3_AMIDE_MONOMERS.contains(sourceAAId)) {
							result = connectionLabel + sourceAAIndex + ":R3-"
									+ targetAAIndex + ":R3";
						} else {
							throw new NotationException(
									"Invalid amide bond between " + sourceAAId
											+ "[" + sourceConId + "]" + " and "
											+ targetAAId + "[" + targetConId
											+ "]");
						}
					}
				}
			}
		}
		return result;
	}

	private List<String> getConnectionNotations(
			List<List<String>> aaListOfList, List<PeptideConnection> conList,
			List<String> aaConIDs) throws NotationException {
		List<String> l = new ArrayList<String>();
		for (PeptideConnection con : conList) {
			String notation = getConnectionNotation(aaListOfList, con, aaConIDs);
			l.add(notation);
		}
		return l;
	}

	private List<String> getAnnotationNotations(List<String> sequences,
			List<SequenceAnnotation> annotations) throws NotationException {
		List<String> l = new ArrayList<String>();
		for (SequenceAnnotation ann : annotations) {
			int seqNo = ann.getId();
			if (seqNo < 1 || seqNo > sequences.size()) {
				throw new NotationException("Sequence Number [" + seqNo
						+ "] does not exist");
			}

			String label = ann.getAnnotation();
			if (!ann.isValidAnnotation()) {
				throw new NotationException("Invalid Sequence Annotation: "
						+ label);
			}

			String text = "PEPTIDE" + seqNo + "{" + label + "}";
			l.add(text);
		}
		return l;
	}

	private String combine(List<String> simpleNotations,
			List<String> conNotations, List<String> annNotations) {
		StringBuilder sb = new StringBuilder();

		boolean isFirst = true;
		for (String simpleNotation : simpleNotations) {
			if (!isFirst) {
				sb.append("|");
			}
			sb.append(simpleNotation);
			isFirst = false;
		}
		sb.append("$");

		isFirst = true;
		for (String con : conNotations) {
			if (!isFirst) {
				sb.append("|");
			}
			sb.append(con);
			isFirst = false;
		}
		sb.append("$$");

		isFirst = true;
		for (String ann : annNotations) {
			if (!isFirst) {
				sb.append("|");
			}
			sb.append(ann);
			isFirst = false;
		}
		sb.append("$");

		return sb.toString();
	}
}
