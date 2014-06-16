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

import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerStore;
import org.helm.notation.model.MoleculeInfo;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.ExtinctionCoefficientCalculator;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceTableDataModel {

	private static final String SEQUENCE_TYPE_DELIMETER = ", ";
	private static final String UNKNOW_VALUE = "N/A";
	private static final String NOTATION_BEGIN = "$";
	private static Pattern SEQUENCE_TYPE = Pattern.compile("RNA|PEPTIDE|CHEM");
	private String annotation;
	private String sequenceType;
	private String notation;
	private String molWt;
	private String extCoeff;
	private String molFormula;

	private static DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	public String getNotation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public String getMolWt() {
		return molWt;
	}

	public void setMolWt(String molWt) {
		this.molWt = molWt;
	}

	public String getExtCoeff() {
		return extCoeff;
	}

	public void setExtCoeff(String extCoeff) {
		this.extCoeff = extCoeff;
	}

	public String getMolFormula() {
		return molFormula;
	}

	public void setMolFormula(String molFormula) {
		this.molFormula = molFormula;
	}

	public static SequenceTableDataModel createSequenceTableDataModel(
			String notation, MonomerStore monomerStore) {

		SequenceTableDataModel dataModel = new SequenceTableDataModel();
		// ---- sequence ----
		dataModel.notation = notation;
		// sequence types
		dataModel.sequenceType = getSequenceTypes(notation);

		// ---- mol wt and mol formula----
		try {
			// String smiles =
			// ComplexNotationParser.getComplexPolymerSMILES(notation);
			// MoleculeInfo mi = StructureParser.getMoleculeInfo(smiles);

			MoleculeInfo mi = ComplexNotationParser.getMoleculeInfo(notation,
					monomerStore);
			dataModel.molWt = decimalFormat.format(mi.getMolecularWeight());
			dataModel.molFormula = mi.getMolecularFormula();
		} catch (Exception e) {
			dataModel.molWt = UNKNOW_VALUE;
			dataModel.molFormula = UNKNOW_VALUE;
		}

		// -- extinction coefficient
		try {
			float result = ExtinctionCoefficientCalculator.getInstance()
					.calculateFromComplexNotation(notation,
							ExtinctionCoefficientCalculator.RNA_UNIT_TYPE,
							monomerStore);
			dataModel.extCoeff = decimalFormat.format(result);

		} catch (Exception e) {
			dataModel.extCoeff = UNKNOW_VALUE;
		}

		return dataModel;
	}

	public static String getSequenceTypes(String notation) {
		Set<String> sequenceSet = new TreeSet<String>();
		Matcher matcher = SEQUENCE_TYPE.matcher(notation.substring(0,
				notation.indexOf(NOTATION_BEGIN)));
		while (matcher.find()) {
			sequenceSet.add(matcher.group());
		}

		StringBuffer sequenceTypes = new StringBuffer();
		for (String currSeq : sequenceSet) {
			sequenceTypes.append(currSeq);
			sequenceTypes.append(SEQUENCE_TYPE_DELIMETER);
		}

		String resultSequenceType = sequenceTypes.toString();
		resultSequenceType = resultSequenceType.substring(0,
				resultSequenceType.length() - 2);

		return resultSequenceType;
	}

	public Object getFiled(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getAnnotation();
		case 1:
			return getSequenceType();
		case 2:
			return getNotation();
		case 3:
			return getMolWt();
		case 4:
			return getMolFormula();
		case 5:
			return getExtCoeff();

		default:
			return "N/A";
		}
	}
}
