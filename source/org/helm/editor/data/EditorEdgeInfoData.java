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
package org.helm.editor.data;
import org.helm.notation.model.Attachment;

/**
 *
 * @author lih25
 */
public class EditorEdgeInfoData extends AbstractEdgeInfo {
	protected boolean isPair = false;
    	
	
    public EditorEdgeInfoData(Attachment sourceNodeAtt, Attachment targetNodeAtt){
        super(sourceNodeAtt, targetNodeAtt);
        
        if(sourceNodeAtt != null && sourceNodeAtt.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)
                && targetNodeAtt != null && targetNodeAtt.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)){
            isPair = true;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(sourceNodeAttachment == null){
            sb.append("");
        }else{
            sb.append(sourceNodeAttachment.toString());
        }
        sb.append(" , ");
        if(targetNodeAttachment == null){
            sb.append("");
        }else{
            sb.append(targetNodeAttachment.toString());
        }
        return sb.toString();
    }

	public boolean isPair() {
	    return isPair;
	}

	public void setIsPair(boolean isPair) {
	    this.isPair = isPair;
	}
}
