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
package org.helm.editor.monomerui.tabui;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.JDOMException;

import y.view.Graph2DView;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.editor.data.DataRegistry;
import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.editor.utility.FloorTabbedPane;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.ShapedXmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlElement;
import org.helm.editor.utility.xmlparser.data.XmlFragment;

/**
 * This class represent tab in "accordion" panel
 * 
 * @author Alexander Makarov
 */
public class PolymerUI extends FloorTabbedPane {

	private Polymer polymerData;

	private Graph2DView view;
	private MacromoleculeEditor editor;

	private static final String EMPTY_STRING = "";

	private ArrayList<String> groupList = new ArrayList<String>();
	private HashMap<String, JComponent> tabs = new HashMap<String, JComponent>();

	// private JPanel otherPanel;
	//
	// private Map<String, Group<XmlFragment>> otherFragments;

	/**
	 * Construct tab that based on xml parsed data
	 * 
	 * @param polymer
	 * @param view
	 * @param editor
	 * @throws MonomerException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public PolymerUI(Polymer polymer, Graph2DView view,
			MacromoleculeEditor editor) throws MonomerException, IOException,
			JDOMException {
		polymerData = polymer;
		this.view = view;
		this.editor = editor;

		DataRegistry.getInstance().registerType(polymerData.getName(),
				polymerData.getSubTypes());

		constructPanel();

		// constructOthersTab();
	}

	/**
	 * @return Title of tab in "accordion"
	 */
	public String getTitle() {
		return polymerData.getTitle();
	}

	/**
	 * @return Color of tab title
	 */
	public Color getTitleColor() {
		return polymerData.getTitleColor();
	}

	/**
	 * @throws JDOMException
	 * @throws IOException
	 * @throws MonomerException
	 * @throws NotationException
	 */
	// private void constructOthersTab() throws MonomerException, IOException,
	// JDOMException {
	// Collection<Group<XmlFragment>> otherGroupsCollection =
	// otherFragments.values();
	// if (otherGroupsCollection.isEmpty()) {
	// return;
	// }
	// Group<XmlFragment> otherGroup = new Group<XmlFragment>();
	//
	// // add all fragments to one group
	// for (Group<XmlFragment> group : otherGroupsCollection) {
	// Iterator<XmlFragment> groupIterator = group.getGroupIterator();
	// while (groupIterator.hasNext()) {
	// XmlFragment fragment = groupIterator.next();
	// String name = fragment.getName();
	// ShapedXmlFragment analog = null;
	// try {
	// analog = polymerData.getNaturalAnalogFragment(name);
	// } catch (Exception e) {
	// analog = null;
	// }
	// if (analog == null) {
	// ShapedXmlFragment shaped = new ShapedXmlFragment(fragment.getName(),
	// null);
	// shaped.setBackgroundColor(fragment.getBackgroundColor());
	// shaped.setFontColor(fragment.getFontColor());
	// shaped.setShape(group.getShape());
	// otherGroup.addElement(shaped);
	// } else {
	// otherGroup.addElement(analog);
	// }
	// }
	// }
	//
	// otherPanel = new BaseXMLPanel<XmlFragment>(otherGroup,
	// polymerData.getName(), view, editor);
	//
	// addTab("Other", otherPanel);
	//
	// clear other
	// otherFragments.clear();
	// }

	private void constructPanel() throws MonomerException, IOException,
			JDOMException {

		// otherFragments = polymerData.getDataComplement();

		constructComplexPanels(polymerData.getFragmentGroups());
		constructComplexPanels(polymerData.getMonomerGroups());

		constructSimplePanels(polymerData.getFragmentGroups());
		constructSimplePanels(polymerData.getMonomerGroups());

		addTabs();
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				for (JComponent panel : tabs.values()) {
					if (panel instanceof JTabbedPane) {
						JTabbedPane tabbed = (JTabbedPane) panel;
						for (int i = 0; i < tabbed.getTabCount(); i++) {
							BaseXMLPanel t = (BaseXMLPanel) tabbed
									.getComponent(i);
							if (t != null)
								t.clearSelection();
						}
					} else {
						if (panel instanceof BaseXMLPanel) {
							((BaseXMLPanel) panel).clearSelection();
						}
					}
				}
			}

		});
	}

	private <T extends XmlElement> void constructComplexPanels(
			Collection<Group<T>> collection) throws MonomerException,
			IOException, JDOMException {
		Set<String> setOfParentGroups = new LinkedHashSet<String>();
		for (Group<T> currGroup : collection) {
			// remember index
			registerGroup(currGroup);

			String parentGroup = currGroup.getParent();

			if (parentGroup != null && !parentGroup.equals(EMPTY_STRING)
					&& !setOfParentGroups.contains(parentGroup)) {

				setOfParentGroups.add(parentGroup);

				List<Group<T>> childGroups = polymerData.getChildGroups(
						parentGroup, collection);
				final JTabbedPane parentTab = new JTabbedPane(JTabbedPane.TOP,
						JTabbedPane.WRAP_TAB_LAYOUT);
				int tabCount = childGroups.size();
				final String[] tabTitles = new String[tabCount];
				final String[] boldTabTitles = new String[tabCount];
				final BaseXMLPanel[] panels = new BaseXMLPanel[tabCount];
				for (int i = 0; i < tabCount; i++) {
					Group<T> currChildGroup = childGroups.get(i);
					tabTitles[i] = currChildGroup.getName();
					boldTabTitles[i] = "<html><b>" + tabTitles[i]
							+ "</b></html>";

					BaseXMLPanel panel = new BaseXMLPanel<T>(currChildGroup,
							polymerData.getName(), view, editor);
					// if (tabTitles[i].equals("Other")) {
					// String currName = currGroup.getName();
					// Group<XmlFragment> otherGroup =
					// otherFragments.get(currName);
					// if (otherGroup == null) {
					// continue;
					// }
					// otherFragments.remove(currName);
					// panel = new BaseXMLPanel<XmlFragment>(
					// otherGroup, polymerData.getName(), view, editor);
					// } else {
					// panel = new BaseXMLPanel<T>(
					// currChildGroup, polymerData.getName(), view, editor);
					// }
					parentTab.addTab(tabTitles[i], panel);
					panels[i] = panel;
				}
				prepareTab(parentGroup, parentTab);

				parentTab.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						int selectedIndex = parentTab.getSelectedIndex();

						for (int i = 0; i < parentTab.getTabCount(); i++) {
							if (i == selectedIndex) {
								parentTab.setTitleAt(i, boldTabTitles[i]);
							} else {
								parentTab.setTitleAt(i, tabTitles[i]);
							}
						}
						panels[selectedIndex].clearSelection();
					}

				});

				int selected = parentTab.getSelectedIndex();
				parentTab.setTitleAt(selected, boldTabTitles[selected]);
			}
		}
	}

	private <T extends XmlElement> void constructSimplePanels(
			Collection<Group<T>> collection) throws MonomerException,
			IOException, JDOMException {
		for (Group<T> currGroup : collection) {
			registerGroup(currGroup);
			if (!currGroup.getParent().equals(EMPTY_STRING)
					|| polymerData.checkParentGroup(currGroup, collection)) {
				continue;
			}

			BaseXMLPanel<T> panel = new BaseXMLPanel<T>(currGroup,
					polymerData.getName(), view, editor);
			prepareTab(currGroup.getName(), panel);
		}
	}

	private void registerGroup(Group group) {
		String groupName = group.getName();
		if (!groupList.contains(groupName)) {
			groupList.add(groupName);
		}
	}

	private void prepareTab(String tabName, JComponent component) {
		tabs.put(tabName, component);
	}

	private void addTabs() {
		for (String name : groupList) {
			if (tabs.containsKey(name)) {
				addTab(name, tabs.get(name));
			}
		}
	}
}
