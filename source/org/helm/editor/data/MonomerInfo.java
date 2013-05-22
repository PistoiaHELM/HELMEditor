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

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.tools.DeepCopy;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.MonomerParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;

/**
 * This class holds keys to get the monomer object from the monomer database.
 *   It also keeps the attachment list
 * @author Hongli Li
 */
public class MonomerInfo {

    private String polymerType;
    private String monomerID;
    private List<Attachment> attachmentList;
    Map<Attachment, Boolean> isConnectedMap;

    public MonomerInfo(String polymerType, String monomerID) {
        try {
            this.polymerType = polymerType;
            this.monomerID = monomerID;
            isConnectedMap = new HashMap<Attachment, Boolean>();
            attachmentList = new ArrayList<Attachment>();

            final MonomerFactory monomerFactory = MonomerFactory.getInstance();
            final Map<String, Map<String, Monomer>> monomerDB = monomerFactory.getMonomerDB();
            //do this only if monomer exists, Tianhong 7/3/08    
            Monomer monomer = monomerDB.get(polymerType).get(monomerID);
            if (null != monomer) {
                List<Attachment> idList = monomerDB.get(polymerType).get(monomerID).getAttachmentList();
                for (Attachment att : idList) {
                    if (att.getCapGroupSMILES() == null) {
                        MonomerParser.fillAttachmentInfo(att);
                    }
                }
                attachmentList = DeepCopy.copy(idList);
                //if this monomer is a base, then add a pairing attachment to this list
                if (polymerType.equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE) && monomer.getMonomerType().equalsIgnoreCase(Monomer.BRANCH_MOMONER_TYPE)) {
                    attachmentList.add(new Attachment(Attachment.PAIR_ATTACHMENT, ""));
                }

            }
            for (Attachment a : attachmentList) {
                isConnectedMap.put(a, false);
            }

        } catch (MonomerException ex) {
            Logger.getLogger(MonomerInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MonomerInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(MonomerInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MonomerInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMonomerID() {
        return monomerID;
    }

    public void setConnection(Attachment sourceAtt, boolean isConnected) {
        isConnectedMap.put(sourceAtt, isConnected);
    }

    public boolean isConnected(Attachment att) {
        return isConnectedMap.get(att);
    }

    public void setMonomerID(String monomerID) {
        this.monomerID = monomerID;
    }

    public String getPolymerType() {
        return polymerType;
    }

    public void setPolymerType(String polymerType) {
        this.polymerType = polymerType;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    /**
     * get the free attachment list, this list will not include the attachment for pairing
     * @return list of Attachment
     */
    public List<Attachment> getAvailableAttachmentList() {
        List<Attachment> availableAttachmentList = new ArrayList<Attachment>();
        for (Attachment att : attachmentList) {
            if (!isConnectedMap.get(att) 
                    && !att.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)) {
                availableAttachmentList.add(att);
            }
        }
        return availableAttachmentList;
    }
    
    // TY
    public List<Attachment> getUsedAttachmentList() {
        List<Attachment> availableAttachmentList = new ArrayList<Attachment>();
        for (Attachment att : attachmentList) {
            if (isConnectedMap.get(att) 
                    && !att.getLabel().equalsIgnoreCase(Attachment.PAIR_ATTACHMENT)) {
                availableAttachmentList.add(att);
            }
        }
        return availableAttachmentList;
    }

    public Attachment getAttachment(String label) {
        for (Attachment att : attachmentList) {
            if (att.getLabel().equalsIgnoreCase(label)) {
                return att;
            }
        }
        return null;
    }
}
