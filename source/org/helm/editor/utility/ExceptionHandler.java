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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.jdom.JDOMException;

import org.helm.notation.MonomerException;
import org.helm.notation.NotationException;
import org.helm.editor.editor.MacromoleculeEditor;

public class ExceptionHandler {
	public static void handleException(Exception ex) {
		if (ex instanceof NotationException) {
			showWarning(ex, "Invalid Notation!");
		} else if (ex instanceof MonomerException) {
			showWarning(ex, "Invalid Monomer!");
		} else if (ex instanceof IOException) {
			showWarning(ex, "IOException!");
		} else if (ex instanceof JDOMException) {
			showWarning(ex, "JDOMException!");
		} else {
			showWarning(ex, "Internal Error");
		}
	}

	private static String getUserMessage(Throwable t) {
		String message = getMessage(t);
		if (message.length() > 0) {
			return "Sorry, internal error occured: " + message;
		}
		return "Sorry, internal error occured. See logs for details";
	}

	private static String getLogMessage(Throwable t) {
		String message = getMessage(t) + t.getClass().getName();
		return message + "\n " + getStackTraceAsString(t);
	}

	private static String getStackTraceAsString(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		StringBuffer error = stringWriter.getBuffer();
		return error.toString();
	}

	private static String getMessage(Throwable t) {
		if ((t.getMessage() != null) && (t.getMessage().length() > 0)) {
			return t.getMessage();
		}
		return "";
	}

	private static void showWarning(Throwable t, String title) {
		JOptionPane.showMessageDialog(null, getUserMessage(t), title,
				JOptionPane.WARNING_MESSAGE);
		Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE,
				getLogMessage(t));
	}

}
