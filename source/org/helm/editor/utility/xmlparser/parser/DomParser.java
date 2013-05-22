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
package org.helm.editor.utility.xmlparser.parser;


import java.awt.Color;
import java.io.File;
import java.io.IOException;

import java.lang.Short;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.editor.utility.xmlparser.data.XmlFragment;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.XmlMonomer;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.Template;
import org.helm.notation.model.Monomer;

/**
 * Realizanton of ui data parser
 * @author Alexander Makarov
 */
public class DomParser implements TemplateParser {

    // this constants represent .xml tags hierarchy 
    private static final String TEMPLATE_TAG = "Template";
    private static final String POLYMER_TAG = "Polymer";
    private static final String MONOMER_GROUP_TAG = "MonomerGroup";
    private static final String FRAGMENT_GROUP_TAG = "FragmentGroup";
    // some tags attributes
    private static final String NAME_ATTRIBUTE = "name";
    private static final String PARENT_ATTRIBUTE = "parent";
    private static final String NOTATION_ATTRIBUTE = "notation";
    private static final String UI_TYPE_ATTRIBUTE = "ui";
    private static final String FONT_COLOR_ATTRIBUTE = "fontColor";
    private static final String BACKGROUND_COLOR_ATTRIBUTE = "backgroundColor";
    private static final String SHAPE_ATTRIBUTE = "shape";
    private static final String TITLE_ATTRIBUTE = "title";
    // ui types
    private static final String UI_TYPE_TAB = "Tab";
    private static final String UI_TYPE_TREE = "Tree";
    private static final String UI_TYPE_SEARCH = "Search";
    // service constant
    private static final Short ELEMENT_NODE = Short.valueOf(Node.ELEMENT_NODE);
    // colors map, using double brace because it's static field
    private static final Map<String, Color> COLORS_MAP = new HashMap<String, Color>() {

        {
            put("Green", Color.GREEN);
            put("Black", Color.BLACK);
            put("White", Color.WHITE);
            put("Red", Color.RED);
            put("Yellow", Color.YELLOW);
            put("Blue", Color.BLUE);
            put("Pink", Color.PINK);
            put("Cyan", Color.CYAN);
            put("Light_Cyan", new Color(0, 195, 255));
            put("Purple", new Color(200, 0, 255));
            put("Orange", Color.ORANGE);
            put("Light_Violet", new Color(204, 204, 255));
            put("GAINSBORO" ,new Color(220, 220, 220)); 
            put("SILVER*", new Color(192, 192, 192));
            put("MAROON*" , new Color(128, 0, 0)); 
            put("SGI_BEET" , new Color(142, 56, 142)); 
            put("SGI_SLATEBLUE" , new Color(113, 113, 198)); 
            put("SGI_LIGHTBLUE" , new Color(125, 158, 192));
            put("SGI_TEAL" , new Color(56, 142, 142)); 
            put("SGI_CHARTREUSE" , new Color(113, 198, 113)); 
            put("SGI_OLIVEDRAB" , new Color(142, 142, 56)); 
            put("FLESH" , new Color(255, 125, 64)); 
            put("RAWSIENNA" , new Color(199, 97, 20)); 
            put("LIGHTGOLDENROD" , new Color(255, 236, 139)); 
            put("DARKOLIVEGREEN" , new Color(202, 255, 112)); 
            put("MEDIUMPURPLE", new Color(171, 130, 255)); 
            put("ORCHID", new Color(218, 112, 214)); 
            put("ORCHID1", new Color(255, 131, 250)); 

        }
    };
    // precompiled patterns for extracting color components
    private static final Pattern COLORS_PATTERN = Pattern.compile("#([0-9]+)");

    /* (non-Javadoc)
     * @see org.helm.editor.utility.xmlparser.parser.TemplateParser#parse(java.lang.String)
     */
    public Template parse(String filePath) throws TemplateParsingException {
        return getTemplate(filePath);
    }

    /**
     * Extracting data from <template> tag
     */
    private Template getTemplate(String filePath) throws TemplateParsingException {
        Template resultTemplate = new Template();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        Document doc = null;
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(new File(filePath));
        } catch (ParserConfigurationException e) {
            throw new TemplateParsingException(e);
        } catch (SAXException e) {
            throw new TemplateParsingException(e);
        } catch (IOException e) {
            throw new TemplateParsingException(e);
        }

        // initialize root tag
        NodeList template = doc.getElementsByTagName(TEMPLATE_TAG);
        Element templateTag = (Element) template.item(0);
        resultTemplate.setName(getStringValue(templateTag, NAME_ATTRIBUTE));
        resultTemplate.setUiType(getUiTypeValue(templateTag, UI_TYPE_ATTRIBUTE));

        // fill template
        NodeList polymers = doc.getElementsByTagName(POLYMER_TAG);
        for (int i = 0; i < polymers.getLength(); i++) {
            Node currNode = polymers.item(i);
            Polymer parsedPolymer;
            try {
                parsedPolymer = parsePolymer(currNode);
            } catch (MonomerException ex) {
                throw new TemplateParsingException(ex);
            } catch (IOException ex) {
                throw new TemplateParsingException(ex);
            } catch (JDOMException ex) {
                throw new TemplateParsingException(ex);
            }
            resultTemplate.addPolymer(parsedPolymer);
        }

        return resultTemplate;
    }

    /**
     * Extracting data from <polymer> tag
     */
    private Polymer parsePolymer(Node polymerNode) throws TemplateParsingException, MonomerException, IOException, JDOMException {
        Polymer polymer = new Polymer();

        Element polymerElement = (Element) polymerNode;
        polymer.setName(getStringValue(polymerElement, NAME_ATTRIBUTE));
        polymer.setTitle(getStringValue(polymerElement, TITLE_ATTRIBUTE, true));
        polymer.setShape(getStringValue(polymerElement, SHAPE_ATTRIBUTE));

        String fontColor = getStringValue(polymerElement, FONT_COLOR_ATTRIBUTE);
        polymer.setTitleColor(getColorByString(fontColor));

        NodeList groups = polymerNode.getChildNodes();
        Node currNode = null;
        for (int i = 0; i < groups.getLength(); i++) {

            // for skipping non functionality elements (trash data)
            if (currNode == groups.item(i)) {
                i++;
            }

            currNode = getElementNode(groups.item(i));

            if (currNode == null) {
                break;
//                return polymer;
            }

            String nodeName = currNode.getNodeName();

            if (isMonomerGroup(nodeName)) {
                Group<XmlMonomer> monGroup = parseMonomerGroup(currNode, polymer);
                polymer.addMonomerGroup(monGroup);
            } else {
                Group<XmlFragment> fragGroup = parseFragmentGroup(currNode, polymer);
                polymer.addFragmentGroup(fragGroup);
            }

        }

        Group<XmlMonomer> otherGroup = generateOtherMonomerGroup(polymer);
        polymer.addMonomerGroup(otherGroup);


        return polymer;
    }

    /**
     * Parsing <MonomerGroup> tag
     */
    private Group<XmlMonomer> parseMonomerGroup(Node node, Polymer polymer) throws TemplateParsingException {
        Group<XmlMonomer> monomerGroup = new Group<XmlMonomer>();

        Element monomerElement = (Element) node;
        monomerGroup.setName(getStringValue(monomerElement, NAME_ATTRIBUTE));
        monomerGroup.setParent(getStringValue(monomerElement, PARENT_ATTRIBUTE, false));
        monomerGroup.setShape(getStringValue(monomerElement, SHAPE_ATTRIBUTE));

        NodeList monomers = node.getChildNodes();
        Node currNode = null;
        for (int i = 0; i < monomers.getLength(); i++) {

            // for skipping non functionality elements (trash data)
            if (currNode == monomers.item(i)) {
                i++;
            }

            currNode = getElementNode(monomers.item(i));

            if (currNode == null) {
                return monomerGroup;
            }

            if (isMonomerGroup(currNode.getNodeName())) {
                Group<XmlMonomer> childGroup = parseMonomerGroup(currNode, polymer);
                childGroup.setParent(monomerGroup.getName());

                polymer.addMonomerGroup(childGroup);
                continue;
            }

            Element currMonomer = (Element) currNode;
            String monomerName = currMonomer.getAttribute(NAME_ATTRIBUTE);

            XmlMonomer monomer = new XmlMonomer(monomerName, polymer.getName());
            monomer.setFontColor(getColorByString(currMonomer.getAttribute(FONT_COLOR_ATTRIBUTE)));
            monomer.setBackgroundColor(getColorByString(currMonomer.getAttribute(BACKGROUND_COLOR_ATTRIBUTE)));

            monomerGroup.addElement(monomer);
        }

        return monomerGroup;
    }

    private Group<XmlMonomer> generateOtherMonomerGroup(Polymer polymer) throws TemplateParsingException {
        Group<XmlMonomer> monomerGroup = null;
        try {
            monomerGroup = new Group<XmlMonomer>();
            monomerGroup.setName(Polymer.DEFAULT_OTHERS_GROUP_NAME);
            monomerGroup.setShape(polymer.getShape());
            monomerGroup.setParent(Polymer.EMPTY_STRING);

            MonomerFactory monomerFactory = MonomerFactory.getInstance();
            Map<String, Map<String, Monomer>> monomerDB = monomerFactory.getMonomerDB();
            Map<String, Monomer> currDataSet = monomerDB.get(polymer.getName());

            for (String currElement : currDataSet.keySet()) {
                if (!polymer.isElementExists(currElement)) {
                    XmlMonomer monomer = new XmlMonomer(currElement, polymer.getName());
                    monomerGroup.addElement(monomer);
                }
            }

        } catch (MonomerException ex) {
            throw new TemplateParsingException(ex);
        } catch (IOException ex) {
            throw new TemplateParsingException(ex);
        } catch (JDOMException ex) {
            throw new TemplateParsingException(ex);
        }
        return monomerGroup;
    }

    /**
     * Parsing <FragmentGroup> tag
     */
    private Group<XmlFragment> parseFragmentGroup(Node node, Polymer polymer) throws TemplateParsingException {
        Group<XmlFragment> fragmentGroup = new Group<XmlFragment>();

        Element fragmentElement = (Element) node;
        fragmentGroup.setName(getStringValue(fragmentElement, NAME_ATTRIBUTE));
        fragmentGroup.setParent(getStringValue(fragmentElement, PARENT_ATTRIBUTE, false));
        fragmentGroup.setShape(getStringValue(fragmentElement, SHAPE_ATTRIBUTE));

        NodeList fragments = node.getChildNodes();
        Node currNode = null;
        for (int i = 0; i < fragments.getLength(); i++) {

            // for skipping non functionality elements (trash data)
            if (currNode == fragments.item(i)) {
                i++;
            }

            currNode = getElementNode(fragments.item(i));

            if (currNode == null) {
                return fragmentGroup;
            }

            if (isMonomerGroup(currNode.getNodeName())) {
                Group<XmlMonomer> childGroup = parseMonomerGroup(currNode, polymer);
                childGroup.setParent(fragmentGroup.getName());

                polymer.addMonomerGroup(childGroup);
                continue;
            }

            if (isFragmentGroup(currNode.getNodeName())) {
                Group<XmlFragment> childGroup = parseFragmentGroup(currNode, polymer);
                childGroup.setParent(fragmentGroup.getName());

                polymer.addFragmentGroup(childGroup);
                continue;
            }

            Element currFragment = (Element) currNode;
            String fragmentName = currFragment.getAttribute(NAME_ATTRIBUTE);
            String notation = currFragment.getAttribute(NOTATION_ATTRIBUTE);

            XmlFragment fragment = new XmlFragment(fragmentName, notation);
            fragment.setFontColor(getColorByString(currFragment.getAttribute(FONT_COLOR_ATTRIBUTE)));
            fragment.setBackgroundColor(getColorByString(currFragment.getAttribute(BACKGROUND_COLOR_ATTRIBUTE)));

            fragmentGroup.addElement(fragment);
        }

        return fragmentGroup;
    }

    private Color getColorByString(String color) {

        if (COLORS_MAP.keySet().contains(color)) {
            return COLORS_MAP.get(color);
        }

        Matcher matcher = COLORS_PATTERN.matcher(color);

        if (!matcher.find()) {
            return null;
        }

        return Color.decode(matcher.group(1));
    }

    private String getStringValue(Element element, String attribute, boolean isNecessary) throws TemplateParsingException {
        String result = element.getAttribute(attribute);

        if (result == null && isNecessary) {
            throw new TemplateParsingException();
        }

        return result;
    }

    private String getStringValue(Element element, String attribute) throws TemplateParsingException {
        return getStringValue(element, attribute, true);
    }

    private Template.UIType getUiTypeValue(Element element, String attribute) throws TemplateParsingException {

        String result = element.getAttribute(attribute);

        if (result == null) {
            throw new TemplateParsingException();
        }

        if (result.equals(UI_TYPE_TAB)) {
            return Template.UIType.TAB;

        } else if (result.equals(UI_TYPE_TREE)) {
            return Template.UIType.TREE;

        } else if (result.equals(UI_TYPE_SEARCH)) {
            return Template.UIType.SEARCH;
        }

        return null;
    }

    private boolean isMonomerGroup(String groupName) {
        return groupName.equals(MONOMER_GROUP_TAG);
    }

    private boolean isFragmentGroup(String groupName) {
        return groupName.equals(FRAGMENT_GROUP_TAG);
    }

    private Node getElementNode(Node node) {

        Short nodeType = Short.valueOf(node.getNodeType());
        while (!ELEMENT_NODE.equals(nodeType)) {
            if (ELEMENT_NODE.equals(nodeType)) {
                return node;
            }

            node = node.getNextSibling();

            if (node == null) {
                return null;
            }

            nodeType = Short.valueOf(node.getNodeType());
        }

        return node;
    }
}
