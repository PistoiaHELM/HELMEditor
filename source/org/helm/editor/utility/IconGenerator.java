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
package org.helm.editor.utility;

import java.awt.Image;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author ZHANGTIANHONG
 */
public class IconGenerator {
    public static final String HELM_APP_ICON_RESOURCE_URL = "/org/helm/editor/editor/resource/Icon-HELM.png";
    public static final String FORWARD_ARROW_ICON_RESOURCE_URL = "/org/helm/editor/editor/resource/Forward16.gif";

    public static Image getImage(String resouceURL) {
        try {
            URL imageURL = IconGenerator.class.getResource(resouceURL);
            return ImageIO.read(imageURL);
        } catch (Exception ex) {
            Logger.getLogger(IconGenerator.class.getName()).log(Level.WARNING, "Unable to retrieve image at "+resouceURL, ex);
        }
        return null;
    }

    public static Icon getIcon(String resouceURL) {
        try {
            URL imageURL = IconGenerator.class.getResource(resouceURL);
            Image image = ImageIO.read(imageURL);
            return new ImageIcon(image);
        } catch (Exception ex) {
            Logger.getLogger(IconGenerator.class.getName()).log(Level.WARNING, "Unable to retrieve icon at "+resouceURL, ex);
        }
        return null;
    }

}
