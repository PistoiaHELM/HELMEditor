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
package org.helm.editor.componentPanel.sequenceviewpanel;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import javax.swing.JScrollPane;
import y.view.Graph2DView;

/**
 * @author Alexander Makarov
 */
public class ViewMetrics {

	private int _aligmentType;
	// enable autoresize mode in Graph2DView
	private boolean _enableAutoResize = true;
	private Graph2DView _view;
	// Nodes positiion section
	// default values for distance between nodes
	public final static int INTERNAL_DISTANCE_V = 45;
	public final static int EXTERNAL_DISTANCE_H = 33;
	public final static int EXTERNAL_DISTANCE_V = 60;
	public final static int INTERNAL_DISTANCE_H = 60;
	// configurable components
	private int _hDistanceExt;
	private int _hDistanceInt;
	private int _vDsitanceExt;
	private int _vDsitanceInt;
	public static final double DEFAULT_STEP = 20.0;
	public static final double DEFAULT_GAP = 20.0;
	public static final double DEFAULT_CHEM_OFFSET = 10.0;
	private double _chemEdgeOffset;
	public static final double DEFAULT_OFFSET = 15;
	private double _xEdgeOffset;
	public static final int MODIFIED_START_LABEL_STEP = -15;// -10;
	private int _modifiedStartLabelStep;
	private double _xStep;
	private double _yStep;
	private static final double DEFAULT_ZOOM_LEVEL = 1.0;
	private double _zoomLevel;
	private static final double LEFT_Y_VIEW_PORT = -30;
	private static final double LEFT_X_VIEW_PORT = 0;
	private double _leftYViewPort;
	private double _leftXViewPort;
	//
	// Label section
	public static final int LABEL_FONT_SIZE = 15;// 10;
	private int _labelFontSize;
	// phosphate label parameters
	public static final int PHOSPHATE_LABEL_FONT_SIZE = 10;
	public static final int PHOSPHATE_LABEL_LEFT_OFFSET = -12;
	public static final int PHOSPHATE_LABEL_RIGHT_OFFSET = 22;
	private int _lPhosphateOffset;
	private int _rPhosphateOffset;
	private int _phosphateLabelFont;
	// 5' label parameters
	public static final int STARTING_LABEL_X_OFFSET = -15;// -10;
	public static final int STARTING_LABEL_Y_OFFSET = -22;// -12;
	private int _xStartLabelOffset;
	private int _yStartLabelOffset;
	// number label offsets
	public static final int NUMBER_LABEL_X_OFFSET = 5;
	public static final int NUMBER_LABEL_Y_OFFSET = -22;
	private double _numberLabelXOffset;
	private double _numberLabelYOffset;
	// Node section
	// node parameters
	public final static int NODE_SIZE = 20;
	private int _nodeSize;
	public static final int FONT_SIZE = 20;
	private int _fontSize;
	// part only for chem nodes
	public final static int CHEM_NODE_SIZE = 31;
	private int _chemNodeSize;
	public final static int CHEM_NODE_FONT = 15;
	private int _chemNodeFontSize;
	// y step in component cell
	private int _yBound;
	private static final int Y_BOUND = -8;
	private static final int NUMBER_LABEL_FONT_SIZE = 16;
	private int _numberLabelFontSize;
	private static final double DEFAULT_LABEL_MODIFIED_WIDTH = 3;
	private double _leftModifiedLabelWidth;
	private static final double DEFAULT_LEFT_MODIFIED_LABEL_OFFSET = -7;
	private double _leftModifiedLabelXOffset;
	private double _rightModifiedLabelWidth;
	private static final double DEFAULT_RIGHT_MODIFIED_LABEL_OFFSET = 22;
	private double _rightModifiedLabelXOffset;

	public ViewMetrics(Graph2DView view) {

		_view = view;

		_view.setFitContentOnResize(_enableAutoResize);
		_view.setMaximumSize(new Dimension(20, 20));

		setDefaultParameters();
	}

	private void setDefaultParameters() {
		setAlignment(SequenceViewLayout.CENTRAL_ALIGNMENT, -1);
		setNumberLabelFontSize(NUMBER_LABEL_FONT_SIZE);

		setChemEdgeOffset(DEFAULT_CHEM_OFFSET);

		setModifiedStartLabelStep(MODIFIED_START_LABEL_STEP);

		setLeftXViewPort(LEFT_X_VIEW_PORT);
		setLeftYViewPort(LEFT_Y_VIEW_PORT);

		setYBound(Y_BOUND);

		setZoom(DEFAULT_ZOOM_LEVEL);

		nodeDefaultParameters();

		setLeftModifiedLabelWidth(DEFAULT_LABEL_MODIFIED_WIDTH);
		setRightModifiedLabelWidth(DEFAULT_LABEL_MODIFIED_WIDTH);

		setLeftModifiedLabelXOffset(DEFAULT_LEFT_MODIFIED_LABEL_OFFSET);
		setRightModifiedLabelXOffset(DEFAULT_RIGHT_MODIFIED_LABEL_OFFSET);

		setNumberLabelXOffset(NUMBER_LABEL_X_OFFSET);
		setNumberLabelYOffset(NUMBER_LABEL_Y_OFFSET);

		setLPhosphateOffset(PHOSPHATE_LABEL_LEFT_OFFSET);
		setRPhosphateOffset(PHOSPHATE_LABEL_RIGHT_OFFSET);

		setXStartLabelOffset(STARTING_LABEL_X_OFFSET);
		setYStartLabelOffset(STARTING_LABEL_Y_OFFSET);

		setXOffset(DEFAULT_OFFSET);
		setChemNodeSize(CHEM_NODE_SIZE);
		setChemNodeFontSize(CHEM_NODE_FONT);
	}

	private void nodeDefaultParameters() {
		setDistance(EXTERNAL_DISTANCE_V, INTERNAL_DISTANCE_V,
				EXTERNAL_DISTANCE_H, INTERNAL_DISTANCE_H);
		setNodeSize(NODE_SIZE);
		setFontSize(FONT_SIZE);
		setLabelFontSize(LABEL_FONT_SIZE);
		setPhosphateLabelFontSize(PHOSPHATE_LABEL_FONT_SIZE);
	}

	public double getChemEdgeOffset() {
		return _chemEdgeOffset;
	}

	public void setChemEdgeOffset(double edgeOffset) {
		_chemEdgeOffset = edgeOffset;
	}

	public double calculateLabelOffset(boolean labelPosition) {
		if (labelPosition == LabelConstructor.LEFT_PHOSPHATE_LABEL_POSTION) {
			return _lPhosphateOffset;
		}

		return _rPhosphateOffset;
	}

	public void setViewSize(int width, int hight) {

		if (_enableAutoResize) {
			_view.fitContent();
		}
	}

	public double getViewArea() {
		Dimension square = _view.getCanvasSize();
		return square.getHeight() * square.getWidth();
	}

	public void updateView() {
		if (_enableAutoResize) {
			_view.fitContent();
		}

		_view.updateView();
	}

	public double getLeftModifiedLabelWidth() {
		return _leftModifiedLabelWidth;
	}

	public void setLeftModifiedLabelWidth(double modifiedLabelWidth) {
		_leftModifiedLabelWidth = modifiedLabelWidth;
	}

	public double getRightModifiedLabelWidth() {
		return _rightModifiedLabelWidth;
	}

	public void setRightModifiedLabelWidth(double modifiedLabelWidth) {
		_rightModifiedLabelWidth = modifiedLabelWidth;
	}

	public double getLeftModifiedLabelXOffset() {
		return _leftModifiedLabelXOffset;
	}

	public void setLeftModifiedLabelXOffset(double modifiedLabelXOffset) {
		_leftModifiedLabelXOffset = modifiedLabelXOffset;
	}

	public double getRightModifiedLabelXOffset() {
		return _rightModifiedLabelXOffset;
	}

	public void setRightModifiedLabelXOffset(double modifiedLabelXOffset) {
		_rightModifiedLabelXOffset = modifiedLabelXOffset;
	}

	public int getXStartLabelOffset() {
		return _xStartLabelOffset;
	}

	public void setXStartLabelOffset(int startLabelOffset) {
		_xStartLabelOffset = startLabelOffset;
	}

	public int getYStartLabelOffset() {
		return _yStartLabelOffset;
	}

	public void setYStartLabelOffset(int startLabelOffset) {
		_yStartLabelOffset = startLabelOffset;
	}

	public double getNumberLabelXOffset() {
		return _numberLabelXOffset;
	}

	public double getLeftYViewPort() {
		return _leftYViewPort;
	}

	public void setLeftYViewPort(double leftYViewPort) {
		this._leftYViewPort = leftYViewPort;
	}

	public double getLeftXViewPort() {
		return _leftXViewPort;
	}

	public void setLeftXViewPort(double leftXViewPort) {
		this._leftXViewPort = leftXViewPort;
	}

	public void setNumberLabelXOffset(double labelXOffset) {
		_numberLabelXOffset = labelXOffset;
	}

	public double getNumberLabelYOffset() {
		return _numberLabelYOffset;
	}

	public void setNumberLabelYOffset(double labelYOffset) {
		_numberLabelYOffset = labelYOffset;
	}

	public int getNumberLabelFontSize() {
		return _numberLabelFontSize;
	}

	public void setNumberLabelFontSize(int labelFontSize) {
		_numberLabelFontSize = labelFontSize;
	}

	public int getLPhosphateOffset() {
		return _lPhosphateOffset;
	}

	public void setLPhosphateOffset(int phosphateOffset) {
		_lPhosphateOffset = phosphateOffset;
	}

	public int getRPhosphateOffset() {
		return _rPhosphateOffset;
	}

	public void setRPhosphateOffset(int phosphateOffset) {
		_rPhosphateOffset = phosphateOffset;
	}

	public int getYBound() {
		return _yBound;
	}

	public void setYBound(int bound) {
		_yBound = bound;
	}

	public void updateWithMaximumSize() {
		_view.setFitContentOnResize(_enableAutoResize);
		_view.setMaximumSize(new Dimension(20, 20));

		updateView();
	}

	public int getAligmentType() {
		return _aligmentType;
	}

	public int getModifiedStartLabelStep() {
		return _modifiedStartLabelStep;
	}

	public void setModifiedStartLabelStep(int startLabelStep) {
		_modifiedStartLabelStep = startLabelStep;
	}

	public void minimizeGaps() {
		setLeftXViewPort(45);
		setZoom(0.53);
	}

	public void setAlignment(int viewType, double sequenceSize) {
		_aligmentType = viewType;
		updateAlignment(false);
	}

	public double getXOffset() {
		return _xEdgeOffset;
	}

	public void setXOffset(double xOffset) {
		_xEdgeOffset = xOffset;
	}

	public void updateAlignment(boolean isComposite) {
		_enableAutoResize = false;

		// only for composite sequences
		if (isComposite) {
			_vDsitanceExt = EXTERNAL_DISTANCE_H;
		}

		switch (_aligmentType) {
		case SequenceViewLayout.LEFT_ALIGNMENT:
			leftAlignment(isComposite);

			break;
		case SequenceViewLayout.RIGHT_ALIGNMENT:
			rightAlignment();

			break;
		default:
			_enableAutoResize = true;
			_view.fitContent();
		}

		_view.revalidate();
	}

	private void rightAlignment() {

		_view.fitContent();
		Point2D viewPoint = _view.getViewPoint2D();

		double xPoint = _view.getViewSize().getWidth() + viewPoint.getX()
				+ _xStep;
		_view.setViewPoint2D(-xPoint, viewPoint.getY());

		_view.setAutoscrolls(false);
		_view.setScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		_view.setFitContentOnResize(false);
	}

	private void leftAlignment(boolean isComposite) {
		_view.fitContent();

		_view.setZoom(1);

		double deltaX = _view.getViewPoint2D().getX()
				+ (_view.getSize().width - _view.getGraph2D().getBoundingBox()
						.getWidth()) / 2;
		deltaX = deltaX - 15;

		_view.setZoom(_zoomLevel);

		Point2D viewPoint = _view.getViewPoint2D();
		_view.setViewPoint2D(deltaX, viewPoint.getY());

		_view.setAutoscrolls(false);
		_view.setScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		_view.setFitContentOnResize(false);
	}

	public void setGraph2DView(Graph2DView view) {
		_view = view;
	}

	public int getHDistanceExt() {
		return _hDistanceExt;
	}

	public void setHDistanceExt(int distanceExt) {
		_hDistanceExt = distanceExt;
	}

	public int getHDistanceInt() {
		return _hDistanceInt;
	}

	public void setHDistanceInt(int distanceInt) {
		_hDistanceInt = distanceInt;
	}

	public int getVDsitanceExt() {
		return _vDsitanceExt;
	}

	public void setVDsitanceExt(int dsitanceExt) {
		_vDsitanceExt = dsitanceExt;
	}

	public int getVDsitanceInt() {
		return _vDsitanceInt;
	}

	public void setVDsitanceInt(int dsitanceInt) {
		_vDsitanceInt = dsitanceInt;
	}

	public int getLabelFontSize() {
		return _labelFontSize;
	}

	public void setLabelFontSize(int fontSize) {
		_labelFontSize = fontSize;
	}

	public int getPhosphateLabelFont() {
		return _phosphateLabelFont;
	}

	public void setPhosphateLabelFont(int labelFont) {
		_phosphateLabelFont = labelFont;
	}

	public int getNodeSize() {
		return _nodeSize;
	}

	public void setNodeSize(int size) {
		_nodeSize = size;
	}

	public int getFontSize() {
		return _fontSize;
	}

	public void setFontSize(int size) {
		_fontSize = size;
	}

	public double getXStep() {
		return _xStep;
	}

	public void setXStep(double step) {
		_xStep = step;
	}

	public double getYStep() {
		return _yStep;
	}

	public void setYStep(double step) {
		_yStep = step;
	}

	public int getChemNodeSize() {
		return _chemNodeSize;
	}

	public void setChemNodeSize(int nodeSize) {
		_chemNodeSize = nodeSize;
	}

	public int getChemNodeFontSize() {
		return _chemNodeFontSize;
	}

	public void setChemNodeFontSize(int nodeFontSize) {
		_chemNodeFontSize = nodeFontSize;
	}

	public double getZoom() {
		return _zoomLevel;
	}

	public void setZoom(double level) {
		_zoomLevel = level;
	}

	private void setDistance(int verticalExt, int verticalInt, int heightExt,
			int heightInt) {
		_hDistanceExt = heightExt;
		_hDistanceInt = heightInt;
		_vDsitanceExt = verticalExt;
		_vDsitanceInt = verticalInt;
	}

	private void setPhosphateLabelFontSize(int fontSize) {
		_phosphateLabelFont = fontSize;
	}
}
