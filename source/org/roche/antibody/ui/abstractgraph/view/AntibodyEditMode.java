/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.roche.antibody.ui.abstractgraph.view;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.services.AbConst;
import org.roche.antibody.ui.components.AntibodyEditorPane;

import y.base.DataProvider;
import y.base.Node;
import y.view.EditMode;
import y.view.Graph2D;

/**
 * View Model Class for Graph2DView used in the routines. Current class supports both NodeRealizers and EdgeRealizers
 * for "Large Graph View" and "Nice Graph View" and only provides the most general functionality for both Swing/YFiles
 * elements
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author raharjap
 * 
 */
public class AntibodyEditMode extends EditMode {

  private AntibodyEditorPane viewDialog;

  public AntibodyEditMode(AntibodyEditorPane viewDialog) {
    this.viewDialog = viewDialog;
    this.allowNodeCreation(false);
    this.allowBendCreation(false);
    this.setMixedSelectionEnabled(false);
    this.setCreateEdgeMode(new CysteinBridgeEdgeMode());
    this.setPopupMode(new GraphPopupMode(viewDialog));
  }

  @Override
  // we show/update the property-table if a domain was selected
  protected void nodeClicked(Graph2D graph, Node node, boolean wasSelected, double x, double y, boolean modifierSet) {
    DataProvider nodeToDomain = graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    if (nodeToDomain.get(node) instanceof Domain) {
      viewDialog.updatePane(((Domain) nodeToDomain.get(node)));
    }
    else {
      viewDialog.updatePane(null);
    }
    super.nodeClicked(graph, node, wasSelected, x, y, modifierSet);
  }

  /**
   * we clear the property table if nothing something was selected, which is not a node {@inheritDoc}
   */
  @Override
  public void mouseClicked(double x, double y) {
    super.mouseClicked(x, y);
    if (getHitInfo(x, y).getHitNode() == null) {
      viewDialog.updatePane(null);
    }
  }

}
