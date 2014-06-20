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
package org.helm.editor.utility.xmlparser.data;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.JDOMException;
import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.model.Monomer;

/**
 * Representation of Polymer tag in OOP
 * 
 * @author Alexander Makarov
 */
public class Polymer {

	private static final Logger log = Logger
			.getLogger(Polymer.class.toString());
	/**
	 * list of possible monomers
	 */
	private List<Group<XmlMonomer>> monomerGroups;
	/**
	 * list of possible fragments
	 */
	private List<Group<XmlFragment>> fragmentGroups;
	private List<Group<? extends XmlElement>> allGroups;
	/**
	 * Title font color
	 */
	private Color titleColor;
	/**
	 * Tab title
	 */
	private String title;
	/**
	 * Default shape of nodes
	 */
	private String shape;
	/**
	 * ui group name
	 */
	private String name;
	public static final String EMPTY_STRING = "";
	public static final String DEFAULT_OTHERS_GROUP_NAME = "Other";

	public Polymer() {
		monomerGroups = new ArrayList<Group<XmlMonomer>>();
		fragmentGroups = new ArrayList<Group<XmlFragment>>();
		allGroups = new ArrayList<Group<? extends XmlElement>>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Color getTitleColor() {
		return titleColor;
	}

	public void setTitleColor(Color titleColor) {
		this.titleColor = titleColor;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public List<Group<XmlMonomer>> getMonomerGroups() {
		return monomerGroups;
	}

	public List<Group<XmlFragment>> getFragmentGroups() {
		return fragmentGroups;
	}

	public List<Group<? extends XmlElement>> getAllGroups() {
		return allGroups;
	}

	public Iterator<Group<XmlMonomer>> getMonomerGroupsIterator() {
		return monomerGroups.iterator();
	}

	public Iterator<Group<XmlFragment>> getFragmentGroupsIterator() {
		return fragmentGroups.iterator();
	}

	public <T extends XmlElement> List<Group<T>> getChildGroups(
			String parentGroupName, Collection<Group<T>> collection) {
		List<Group<T>> childGroups = new ArrayList<Group<T>>();

		for (Group<T> currGroup : collection) {
			if (currGroup.getParent().equals(parentGroupName)) {
				childGroups.add(currGroup);
			}
		}

		return childGroups;
	}

	public <T extends XmlElement> boolean checkParentGroup(Group<T> element,
			Collection<Group<T>> collection) {
		for (Group<T> currGroup : collection) {
			String parent = currGroup.getParent();
			if (!parent.equals(EMPTY_STRING)
					&& parent.equals(element.getName())) {
				return true;
			}
		}

		return false;
	}

	public <T extends XmlElement> LinkedHashSet<String> getParentSet(
			List<Group<? extends T>> groupList) {
		LinkedHashSet<String> parentSet = new LinkedHashSet<String>();
		for (Group<? extends T> currGroup : groupList) {
			String parentGroup = currGroup.getParent();
			if (!parentGroup.equals(EMPTY_STRING)) {
				parentSet.add(parentGroup);
			}
		}

		return parentSet;
	}

	public boolean isElementExists(String elementName) {

		for (Group<XmlMonomer> monGroup : monomerGroups) {
			if (monGroup.isElementExists(elementName)) {
				return true;
			}
		}

		for (Group<XmlFragment> monGroup : fragmentGroups) {
			if (monGroup.isElementExists(elementName)) {
				return true;
			}
		}

		return false;
	}

	public ShapedXmlFragment getXmlFragmentByName(String elementName) {
		for (Group<XmlMonomer> monGroup : monomerGroups) {
			XmlElement element = monGroup.getElementNyName(elementName);
			if (element != null) {
				ShapedXmlFragment result = new ShapedXmlFragment(elementName,
						this.name);
				result.setBackgroundColor(element.getBackgroundColor());
				result.setFontColor(element.getFontColor());
				result.setShape(monGroup.getShape());
				return result;
			}
		}
		return null;
	}

	public void addMonomerGroup(Group<XmlMonomer> monomerGroup) {
		monomerGroups.add(monomerGroup);
		allGroups.add(monomerGroup);
	}

	public void addFragmentGroup(Group<XmlFragment> fragmentGroup) {
		fragmentGroups.add(fragmentGroup);
		allGroups.add(fragmentGroup);
	}

	// public Group<XmlFragment> getDataComplement() throws MonomerException,
	// IOException, JDOMException {
	// MonomerFactory monomerFactory = MonomerFactory.getInstance();
	// Map<String, Map<String, Monomer>> monomerDB =
	// monomerFactory.getMonomerDB();
	// Map<String, Monomer> currDataSet = monomerDB.get(name);
	//
	// Group<XmlFragment> complementGroup = new Group<XmlFragment>();
	// complementGroup.setShape(shape);
	// complementGroup.setName(DEFAULT_OTHERS_GROUP_NAME);
	//
	// for(String currElement : currDataSet.keySet()){
	// if (!isElementExists(currElement)){
	// Monomer monomer = currDataSet.get(currElement);
	// XmlFragment currFragment = null;
	// currFragment = convertMonomerToXmlFragment(monomer);
	//
	// if (currFragment != null) {
	// complementGroup.addElement(currFragment);
	// }
	// }
	// }
	// return complementGroup;
	// }
	public Map<String, Group<XmlFragment>> getDataComplement()
			throws MonomerException, IOException, JDOMException {
		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB =
		 * monomerFactory.getMonomerDB(); Map<String, Monomer> currDataSet =
		 * monomerDB.get(name);
		 */
		Map<String, Monomer> currDataSet = MonomerStoreCache.getInstance()
				.getCombinedMonomerStore().getMonomerDB().get(name);

		Map<String, Group<XmlFragment>> result = new HashMap<String, Group<XmlFragment>>();
		for (String currElement : currDataSet.keySet()) {
			if (!isElementExists(currElement)) {
				Monomer monomer = currDataSet.get(currElement);
				String monomerType = monomer.getMonomerType();
				XmlFragment currFragment = null;
				currFragment = convertMonomerToXmlFragment(monomer);

				if (currFragment != null) {
					Group<XmlFragment> complementGroup = result
							.get(monomerType);
					if (complementGroup == null) {
						complementGroup = new Group<XmlFragment>();
						complementGroup.setShape(shape);
						complementGroup.setName(DEFAULT_OTHERS_GROUP_NAME);
					}
					complementGroup.addElement(currFragment);

					result.put(monomerType, complementGroup);
				}
			}
		}
		return result;
	}

	public ShapedXmlFragment getNaturalAnalogFragment(String monomerName)
			throws Exception {

		if (!this.name.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
			return null;
		}

		/*
		 * MonomerFactory monomerFactory = MonomerFactory.getInstance();
		 * Map<String, Map<String, Monomer>> monomerDB = monomerFactory
		 * .getMonomerDB(); Map<String, Monomer> currDataSet =
		 * monomerDB.get(this.name);
		 */
		Map<String, Monomer> currDataSet = MonomerStoreCache.getInstance()
				.getCombinedMonomerStore().getMonomerDB().get(this.name);

		Monomer m = currDataSet.get(monomerName);

		if (m == null) {
			log.log(Level.WARNING, "Can't find monomer " + monomerName);
			return null;
		}

		String analog = m.getNaturalAnalog();
		ShapedXmlFragment analogUI = getXmlFragmentByName(analog);

		if (analogUI == null) {
			log.log(Level.WARNING, "Can't find UI data for natural analog "
					+ analog + " of " + monomerName);
		}

		ShapedXmlFragment result = new ShapedXmlFragment(monomerName, null);
		result.setBackgroundColor(analogUI.getBackgroundColor());
		result.setFontColor(analogUI.getFontColor());
		result.setShape(analogUI.getShape());

		return result;
	}

	public List<String> getSubTypes() {
		List<String> subTypes = new ArrayList<String>();

		for (Group<XmlMonomer> group : monomerGroups) {
			subTypes.add(group.getName());
		}

		for (Group<XmlFragment> group : fragmentGroups) {
			subTypes.add(group.getName());
		}

		return subTypes;
	}

	private XmlFragment convertMonomerToXmlFragment(Monomer monomer) {
		XmlFragment fragment = new XmlFragment(monomer.getAlternateId(), null);
		fragment.setFontColor(Color.BLACK);
		if (monomer.getPolymerType().equalsIgnoreCase("CHEM")) {
			fragment.setBackgroundColor(new Color(200, 0, 255));
		} else {
			fragment.setBackgroundColor(new Color(0, 195, 255));
		}
		return fragment;
	}
}
