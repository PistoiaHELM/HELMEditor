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
package org.helm.editor.conversion;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.NucleotideConverter;

public class ConvertNotationController {

	private static final String NEXT_STRING = "\n";

	private static final int NUCLEOTID_NOTATION = 0;
	private static final int HELM_NOTATION = 1;

	public String getAnotherNotation(int fromNotation, int toNotation,
			String reNotatedText) {

		ArrayList<ConvertionResult> allNotations = gettingNotationsArray(reNotatedText);

		// getting helm notation
		switch (fromNotation) {
		case NUCLEOTID_NOTATION:
			for (int i = 0; i < allNotations.size(); i++) {
				String currNotation = allNotations.get(i).getNotation();
				allNotations.set(i, nucleotideToHelm(currNotation));
			}
			break;
		case HELM_NOTATION:
			for (int i = 0; i < allNotations.size(); i++) {
				String currNotation = allNotations.get(i).getNotation();
				allNotations.set(i, standardizeNotation(currNotation));
			}
			break;
		}

		// converting to selected notation
		if (toNotation == HELM_NOTATION) {
			return prepareConvertionReport(allNotations);
		}

		// helm to Nucleotide
		if (toNotation == NUCLEOTID_NOTATION) {
			for (int i = 0; i < allNotations.size(); i++) {
				ConvertionResult currResult = allNotations.get(i);
				if (currResult.haveNotException()) {
					allNotations.set(i,
							helmToNucleotid(currResult.getNotation()));
				}
			}
		}

		return prepareConvertionReport(allNotations);
	}

	public static ConvertionResult standardizeNotation(String notation) {

		ConvertionResult result = new ConvertionResult();
		try {
			result.setNotation(ComplexNotationParser.standardize(notation));
		} catch (Exception ex) {
			result.setException(ex);
		}

		return result;
	}

	public static ConvertionResult nucleotideToHelm(String notation) {

		ConvertionResult result = new ConvertionResult();
		try {
			NucleotideConverter nucleotideConverter = NucleotideConverter.getInstance();			
			result.setNotation(nucleotideConverter.getComplexNotation(notation));
		} catch (Exception ex) {
			result.setException(ex);
		}

		return result;
	}

	public static ConvertionResult helmToNucleotid(String currNotation) {

		ConvertionResult result = new ConvertionResult();

		try {
			NucleotideConverter nucleotideConverter = NucleotideConverter
					.getInstance();
			String nucleotidNotation = nucleotideConverter
					.getNucleotideSequencesFromComplexNotation(currNotation);
			result.setNotation(nucleotidNotation);
		} catch (Exception e) {
			result.setException(e);
		}

		return result;
	}

	public static String prepareConvertionReport(
			ArrayList<ConvertionResult> convertedList) {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < convertedList.size(); i++) {
			result.append(convertedList.get(i).getResult());
			result.append(NEXT_STRING);
		}

		return result.toString();
	}

	private ArrayList<ConvertionResult> gettingNotationsArray(
			String reNotatedText) {
		ArrayList<ConvertionResult> result = new ArrayList<ConvertionResult>();

		StringTokenizer parseTextAreaContent = new StringTokenizer(
				reNotatedText, NEXT_STRING);

		ConvertionResult currResult;
		while (parseTextAreaContent.hasMoreTokens()) {
			currResult = new ConvertionResult(parseTextAreaContent.nextToken());
			result.add(currResult);
		}

		return result;
	}
}
