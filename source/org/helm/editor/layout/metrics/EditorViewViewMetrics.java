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
package org.helm.editor.layout.metrics;


public class EditorViewViewMetrics implements IViewMetrics {
	//distance in vertical direction
    public final static int DISTANCE_V = 60;
    //distance in horizontal direction
    public final static int DISTANCE_H = 60;

    public final static int INTERNAL_DISTANCE_V = 45;
//    public final static int EXTERNAL_DISTANCE_H = 33;
//    public final static int EXTERNAL_DISTANCE_V = 60;
    public final static int INTERNAL_DISTANCE_H = 60;

    
    public int getShiftForFlippedNucleotideSequence() {
		return 2 * DISTANCE_H;
	}

	public int getShiftForFlippedPeptideSequence() {
		return DISTANCE_H;
	}
    
    public int getHDistanceExt() {
        return DISTANCE_H;
    }

    public int getVDistanceExt() {
        return DISTANCE_V;
    }

    public int getHDistanceInt() {
        return INTERNAL_DISTANCE_H;
    }

    public int getVDistanceInt() {
        return INTERNAL_DISTANCE_V;
    }

	public int getChemEdgePathHorisontalStep() {
		return INTERNAL_DISTANCE_H / 2;
	}

	public int getChemEdgePathVerticalStep() {
		return INTERNAL_DISTANCE_V;
	}

	public int getChemNodeToBackboneNodeDistance() {
		return DISTANCE_V;
	}

}
