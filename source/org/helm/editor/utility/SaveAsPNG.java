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

/**
 *
 * @author LIH25
 */
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class takes the image objects and saves as png. It creates the
 * BufferedImage of the given image and save it as .png
 * 
 * @author Rahul Sapkal(rahul@javareference.com)
 */
public class SaveAsPNG {

	public SaveAsPNG(Image img, String fileName) {
		try {

			BufferedImage bufferedImage = getBufferedImageFromImage(img);
			// Save as PNG using the ImageIO object
			File f = new File(fileName);

			f.getParentFile().mkdir();
			ImageIO.write(bufferedImage, "png", f);

		} catch (IOException ex) {
			Logger.getLogger(SaveAsPNG.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		// System.out.println("Image created Successfully");

	}

	/**
	 * This method takes the Image object and creates BufferedImage of it
	 * 
	 * @param img
	 * @return a BufferedImage
	 */
	public static BufferedImage getBufferedImageFromImage(Image img) {
		// This line is important, this makes sure that the image is
		// loaded fully
		img = new ImageIcon(img).getImage();

		// Create the BufferedImage object with the width and height of the
		// Image
		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_RGB);

		// Create the graphics object from the BufferedImage
		Graphics g = bufferedImage.createGraphics();

		// Draw the image on the graphics of the BufferedImage
		g.drawImage(img, 0, 0, null);

		// Dispose the Graphics
		g.dispose();

		// return the BufferedImage
		return bufferedImage;
	}

}
