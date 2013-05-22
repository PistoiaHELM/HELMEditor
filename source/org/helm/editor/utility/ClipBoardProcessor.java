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
 * @author lih25
 */
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.*;

public class ClipBoardProcessor implements ClipboardOwner {

    private Clipboard myClipboard;

    public static void copy(BufferedImage bi) {
        ClipBoardProcessor cbp = new ClipBoardProcessor(bi);
    }

    public static void copy(String text){
         ClipBoardProcessor cbp = new ClipBoardProcessor(text);
    }

      public ClipBoardProcessor(String bi) {
        myClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        process(bi);
    }
    public ClipBoardProcessor(BufferedImage bi) {
        myClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        process(bi);
    }

    public void process(BufferedImage bi) {
        ClipImage ci = new ClipImage(bi);
        myClipboard.setContents(ci, this);
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     */
    public void process(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    private class ClipImage implements Transferable {

        private DataFlavor[] myFlavors;
        private Object myImage;

        public ClipImage(Object theImage) {
            myFlavors = new DataFlavor[]{DataFlavor.imageFlavor};
            myImage = theImage;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor != DataFlavor.imageFlavor ) {
                throw new UnsupportedFlavorException(flavor);
            }
            return myImage;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return myFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (flavor == DataFlavor.imageFlavor );
        }
    }
}

