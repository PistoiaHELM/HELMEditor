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
package org.helm.editor.monomerui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.helm.editor.services.PreferensesService;
import org.helm.editor.utility.xmlparser.data.Template.UIType;

/**
 * @author Alexander Makarov
 * 
 */
public class PropertyManager implements PreferensesService {

    public static final String XML_FILE_TYPE = "XML";
    public static final String MONOMER_LAYOUT_TEMPLATE_TYPE = "Monomer Layout";
    private static PropertyManager instance;
    private Properties properties;
    private String propertyFilePath = "";
    public static final String USER_PROPERTY = "user.home";
    public static final String PROPERTY_FOLDER = System.getProperty(USER_PROPERTY) + "/.helm/editor/";
    public static final String PROPERTY_FILE = PROPERTY_FOLDER + "user.property";
    public static final String SCHEMA_FILE = PROPERTY_FOLDER + "MonomerCategorizationSchema.xsd";
    public static final String DEFAULT_MONOMER_CATEGORIZATION_TEMPLATE = "DefaultMonomerCategorizationTemplate.xml";
    public static final String UI_PROPERTY = "ui.type";
    public static final String UI_XML = "ui.xml";
    private static final String HIDDEN_PREFIX = ".";
    private static final String CONFIG_FOLDER = "conf";
    private static ClassLoader classLoader;
    private static File propertyFolder;

    public static PropertyManager getInstance() throws IOException {

        if (instance == null) {
            instance = new PropertyManager();
        }

        return instance;
    }

    public UIType getUIType() {
        String uiType = loadUserPreference(UI_PROPERTY);
        return UIType.stringValue(uiType);
    }

    public String getUIFilePath() {
        String uiXml = PROPERTY_FOLDER + loadUserPreference(UI_XML);
        File f = new File(uiXml);
        if (f.exists()) {
            return uiXml;
        } else {
            try {
                saveUserPreference(UI_XML, DEFAULT_MONOMER_CATEGORIZATION_TEMPLATE);
            } catch (IOException ex) {
                Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return PROPERTY_FOLDER +DEFAULT_MONOMER_CATEGORIZATION_TEMPLATE;
        }
    }

    public String loadUserPreference(String preferenceName) {
        return properties.getProperty(preferenceName);
    }

    public boolean saveUserPreference(String preferenceName, String newValue)
            throws IOException {
        properties.setProperty(preferenceName, newValue);

        File file = new File(propertyFilePath);
        FileOutputStream outputStream = new FileOutputStream(file);
        properties.store(outputStream, null);

        outputStream.close();

        return true;
    }

    private static void configFolderProcessing() throws IOException {
        propertyFolder = new File(PROPERTY_FOLDER);

        // check folder
        if (!propertyFolder.exists()) {
            propertyFolder.mkdir();
        }

        classLoader = PropertyManager.class.getClassLoader();

        URLConnection connection = classLoader.getResource(CONFIG_FOLDER).openConnection();
        if (connection instanceof JarURLConnection) {
            // load from jar file
            Enumeration<JarEntry> entries =
                    ((JarURLConnection) connection).getJarFile().entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(CONFIG_FOLDER + "/")) {
                    processConfigFile(entryName.substring(5));
                }
            }
        } else {
            // load from directory, not from jar
            InputStream is = classLoader.getResourceAsStream(CONFIG_FOLDER);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String[] fileNames = (new String(buffer)).split("\n");

            for (String fileName : fileNames) {
                processConfigFile(fileName);
            }
        }
    }

    private static void processConfigFile(String fileName) throws IOException {
        if (fileName.startsWith(HIDDEN_PREFIX)) {
            // skip "hidden files"
            return;
        }

        File configFile = new File(propertyFolder, fileName);
        if (configFile.exists() && PROPERTY_FILE.endsWith(fileName)) {
            // if user.property file already exists, skip it
            return;
        }

        InputStream is =
                classLoader.getResourceAsStream(CONFIG_FOLDER + "/" + fileName);
        FileOutputStream fout = new FileOutputStream(configFile);
        byte[] buffer = new byte[16 * 1024];

        while (is.available() > 0) {
            int size = is.read(buffer);
            fout.write(buffer, 0, size);
        }
        fout.close();
        is.close();
    }

    private PropertyManager() throws IOException {
        configFolderProcessing();

        propertyFilePath = PROPERTY_FILE;

        FileInputStream propertyInputStream = new FileInputStream(
                propertyFilePath);

        properties = new Properties();
        properties.load(propertyInputStream);
    }

    public void saveFileContent(String fileName, String xmlContent) throws FileNotFoundException, IOException {
        String path = PROPERTY_FOLDER + fileName;
        File file = new File(path);
        FileWriter writer = new FileWriter(file);
        writer.write(xmlContent);
        writer.close();
    }
}
