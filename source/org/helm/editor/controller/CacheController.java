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
package org.helm.editor.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import y.view.Graph2DView;

/**
 * Cache for faster loading Sequence and Component View
 * 
 * @author Makarov Alexander
 * @version 1.0
 */
public class CacheController {

	private static CacheController _instance;

	private Map<String, Graph2DView> _cache;
	private Map<String, Integer> _effectiveness;

	private static final int DEFAULT_CACHE_SIZE = 10;
	private static final int MAXIMUM_CACHE_SIZE = 50;

	// regex part
	private static final int NOTATION_TYPE = 1;
	private static final int NOTATION_VALUE = 2;
	private static final Pattern PURY_NOTATION_PATTERN = Pattern
			.compile("(RNA|PEPTIDE|CHEM)[1-9]+\\{([^}]+)");

	private CacheController() {
		_cache = Collections.synchronizedMap(new HashMap<String, Graph2DView>(
				DEFAULT_CACHE_SIZE));
		_effectiveness = Collections
				.synchronizedMap(new HashMap<String, Integer>(
						DEFAULT_CACHE_SIZE));
	}

	public synchronized static CacheController getInstance() {
		if (_instance == null) {
			_instance = new CacheController();
		}

		return _instance;
	}

	public boolean isValueInCache(String notation) {
		notation = getClearNotation(notation);

		if (notation == null) {
			return false;
		}

		return _cache.containsKey(notation);
	}

	public synchronized void addToCache(String notation, Graph2DView view) {

		notation = getClearNotation(notation);

		if (notation == null || _cache.containsKey(notation)) {
			return;
		}

		if (_cache.size() == MAXIMUM_CACHE_SIZE) {
			String keyWithSmallestPriority = getKeyWithSmallestPriority();
			deleteValue(keyWithSmallestPriority);
		}

		_effectiveness.put(notation, new Integer(0));
		_cache.put(notation, view);
	}

	public synchronized Graph2DView getCachedValue(String notation) {

		notation = getClearNotation(notation);

		if (notation == null) {
			return null;
		}

		Integer effect = _effectiveness.get(notation);
		effect++;
		_effectiveness.put(notation, effect);

		return _cache.get(notation);
	}

	public void cacheClear() {
		_cache.clear();
		_effectiveness.clear();
	}

	private void deleteValue(String key) {
		_cache.remove(key);
		_effectiveness.remove(key);
	}

	/**
	 * Weight = notation lenght + intensity of value using
	 * 
	 * @return notation with minimum weight
	 */
	@SuppressWarnings("unchecked")
	private String getKeyWithSmallestPriority() {

		List<Integer> sortedValues = new ArrayList<Integer>(
				_effectiveness.values());
		Collections.sort(sortedValues);
		Integer minValue = Collections.min(sortedValues);

		List<String> valuesWithMinimumPriority = new ArrayList<String>();
		for (String currNotation : _effectiveness.keySet()) {
			Integer val = _effectiveness.get(currNotation);

			if (val.equals(minValue)) {
				valuesWithMinimumPriority.add(currNotation);
			}
		}

		Collections.sort(valuesWithMinimumPriority, new Comparator<String>() {

			public int compare(String o1, String o2) {
				Integer l1 = o1.length();
				Integer l2 = o2.length();

				return l1.compareTo(l2);
			}

		});

		return valuesWithMinimumPriority.get(0);
	}

	private String getClearNotation(String notation) {

		// append main notation part
		Matcher matcher = PURY_NOTATION_PATTERN.matcher(notation);
		StringBuffer clearNotation = new StringBuffer();
		while (matcher.find()) {
			clearNotation.append(matcher.group(NOTATION_TYPE)
					+ matcher.group(NOTATION_VALUE));
		}

		// append edges
		int startEdgesPart = notation.indexOf("$");
		if (startEdgesPart < notation.length()) {
			String edgePart = notation.substring(startEdgesPart,
					notation.length());

			// remove '$' symbol
			edgePart = edgePart.replaceAll("\\$", "");

			clearNotation.append(edgePart);
		}

		return clearNotation.toString();
	}

}
