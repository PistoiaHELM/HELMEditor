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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Alexander Makarov
 *
 * @param <T>
 */
public class Group<T extends XmlElement> {

	private List<T> data;
	
	private String parent;
	private String name;
	
	private String shape;
	
	private XmlElementsComparator comparator;
	
	public Group() {
		data = new LinkedList<T>();
		comparator = new XmlElementsComparator();
	}	
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public String getParent(){
		return parent;
	}
	
	public void setParent(String parent){
		this.parent =parent;
	}
	
	public void setShape(String shape){
		this.shape = shape;
	}
	
	public String getShape(){
		return shape;
	}
	
	public void addElement(T element){
		data.add(element);
	}
	
	public Iterator<T> getGroupIterator(){
		return data.iterator();
	}
	
	public boolean isElementExists(String elementName){
		
		for(T currElement : data){
			if (elementName.equals(currElement.getName())){ 
				return true;			
			}
		}
		
		return false;
	}
	
	public XmlElement getElementNyName(String elementName) {
		for(T currElement : data){
			if (elementName.equals(currElement.getName())){ 
				return currElement;			
			}
		}
		
		return null;

	}

	public void sort(){
		Collections.sort(data, comparator);
	}
	
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	private class XmlElementsComparator implements Comparator<T> {

		public int compare(XmlElement o1, XmlElement o2) {
			return o1.getName().compareTo(o2.getName());
//			if (o1.getNotation() == null || o2.getNotation() == null) {
//			}
//			
//			return o1.getNotation().compareTo(o2.getNotation());
		}
		
	}
}
