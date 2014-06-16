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
package org.helm.editor.utility.xmlparser.validator;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Realization of .xml validator Using http://www.w3.org/2001/XMLSchema standard
 * 
 * @author Alexander Makarov
 * @see Validator
 */
public class UITemplateValidator implements TemplateValidator {

	private String schemaPath;

	private SchemaFactory schemaFactory = SchemaFactory
			.newInstance("http://www.w3.org/2001/XMLSchema");
	private Validator validator;

	public void setSchema(String schemaPath) throws ValidationTemplateExcaption {
		this.schemaPath = schemaPath;

		File schemaLocation = new File(schemaPath);
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(schemaLocation);
		} catch (SAXException e) {
			throw new ValidationTemplateExcaption(e);
		}
		validator = schema.newValidator();
	}

	public String getCurrentSchemaLocation() {
		return schemaPath;
	}

	public boolean validate(String xmlPath) throws ValidationTemplateExcaption {

		File validationXmlLocation = new File(xmlPath);
		Source sourceForValidation = new StreamSource(validationXmlLocation);

		try {
			validator.validate(sourceForValidation);
		} catch (SAXException e) {
			throw new ValidationTemplateExcaption(e);
		} catch (IOException e) {
			throw new ValidationTemplateExcaption(e);
		}

		return true;
	}

}
