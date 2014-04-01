/**
 * *****************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *****************************************************************************
 */
package org.helm.editor.sample;

import java.util.Iterator;
import org.helm.editor.utility.xmlparser.data.Group;
import org.helm.editor.utility.xmlparser.data.Polymer;
import org.helm.editor.utility.xmlparser.data.Template;
import org.helm.editor.utility.xmlparser.data.XmlFragment;
import org.helm.editor.utility.xmlparser.data.XmlMonomer;
import org.helm.editor.utility.xmlparser.parser.DomParser;
import org.helm.editor.utility.xmlparser.parser.TemplateParser;
import org.helm.editor.utility.xmlparser.parser.TemplateParsingException;
import org.helm.editor.utility.xmlparser.validator.UITemplateValidator;
import org.helm.editor.utility.xmlparser.validator.ValidationTemplateExcaption;

/**
 *
 * @author ZHANGTIANHONG
 */
public class DomParserSample {

    public static void main(String[] args) {

        UITemplateValidator validator = new UITemplateValidator();

        try {
            validator.setSchema("./conf/MonomerCategorizationSchema.xsd");
            System.out.println("Validation result: " + validator.validate("./conf/DefaultMonomerCategorizationTemplate.xml"));
        } catch (ValidationTemplateExcaption e1) {
            e1.printStackTrace();
        }

        TemplateParser parser = new DomParser();
        try {
            Template template = parser.parse("./conf/DefaultMonomerCategorizationTemplate.xml");
            System.out.println("Template name: " + template.getName() + "," + " template type: " + template.getUiType());

            Iterator<Polymer> polymerIterator = template.getPolymersInterator();
            while (polymerIterator.hasNext()) {
                Polymer currPolymer = polymerIterator.next();
                System.out.println("++++++++++++++++");
                System.out.println("Polymer name: " + currPolymer.getName());

                Iterator<Group<XmlMonomer>> mgInterator = currPolymer.getMonomerGroupsIterator();
                while (mgInterator.hasNext()) {
                    Group<XmlMonomer> currGroup = mgInterator.next();
                    System.out.println("Monomer group name: " + currGroup.getName() + " group parent: " + currGroup.getParent());
                    System.out.println("Nodes shape: " + currGroup.getShape());

                    Iterator<XmlMonomer> monItr = currGroup.getGroupIterator();
                    while (monItr.hasNext()) {
                        XmlMonomer curMon = monItr.next();
                        System.out.println("Monomer: " + curMon.getName());
                        System.out.println("Font color: " + curMon.getFontColor() + " Background color: " + curMon.getBackgroundColor());
                    }
                }

                Iterator<Group<XmlFragment>> fgInterator = currPolymer.getFragmentGroupsIterator();
                while (fgInterator.hasNext()) {
                    Group<XmlFragment> currGroup = fgInterator.next();

                    System.out.println("Fragment group name: " + currGroup.getName() + " group parent: " + currGroup.getParent());
                    System.out.println("Nodes shape: " + currGroup.getShape());

                    Iterator<XmlFragment> fragIterator = currGroup.getGroupIterator();
                    while (fragIterator.hasNext()) {
                        XmlFragment currFrag = fragIterator.next();
                        System.out.println("Fragment: " + currFrag.getName() + " notation: " + currFrag.getNotation());
                        System.out.println("Font color: " + currFrag.getFontColor() + " Background color: " + currFrag.getBackgroundColor());
                    }
                }

                System.out.println("++++++++++++++++");
            }


        } catch (TemplateParsingException e) {
            e.printStackTrace();
        }
    }
}
