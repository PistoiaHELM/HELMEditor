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

import org.helm.notation.model.Monomer;
import java.awt.Color;

/**
 *
 * @author lih25
 */
public class ColorMap {
    public static Color getNucleotidesColor(String type){
        if(type.equalsIgnoreCase("A")){
            return Color.GREEN;
        }else if(type.equalsIgnoreCase("G")){
            return Color.ORANGE;
        }else if(type.equalsIgnoreCase("C")){
            return Color.RED;
        }else if(type.equalsIgnoreCase("U")){
        	return Color.CYAN;
        }else if (type.equalsIgnoreCase("T")){
        	return Color.CYAN;
        }else if(type.equalsIgnoreCase("R") ||type.equalsIgnoreCase("P") ){
            return new Color(204, 204, 255);
            
        }else {
            return Color.PINK;
        }
        
    }
    
    public static Color getAminoAcidColor(String type){
    	return new Color(0, 195, 255);
//        if(type.equalsIgnoreCase(Monomer.ID_ALA)){
//            return Color.ORANGE;
//        }else if(type.equalsIgnoreCase(Monomer.ID_ARG)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_ASP)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_ASN)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_CYS)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_GLN)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_GLU)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_GLY)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_HIS)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_ILE)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_LEU)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_LYS)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_MET)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_PHE)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_PRO)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_SER)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_THR)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_TRP)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_TYR)){
//            return Color.CYAN;
//        }else if(type.equalsIgnoreCase(Monomer.ID_VAL)){
//            return Color.CYAN;
//        }else{
//            return Color.cyan;
//        }
    }
    

}
