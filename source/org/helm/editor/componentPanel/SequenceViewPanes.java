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
package org.helm.editor.componentPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.helm.editor.componentPanel.componentviewpanel.ComponentTableView;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewController;
import org.helm.editor.componentPanel.sequenceviewpanel.SequenceViewControllerImpl;
import org.helm.editor.data.DataListener;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.NotationUpdateEvent;
import org.helm.editor.utility.ExceptionHandler;

import y.view.Graph2D;
import y.view.Graph2DView;

/**
 * 
 * @author lih25
 */
public class SequenceViewPanes extends JPanel implements DataListener {

	private JTabbedPane tabbedPane;
	private SequenceViewController sequenceViewController;
	private ComponentTableView componentTableView;
	private String _owner;

	public SequenceViewPanes(String owner) {
		_owner = owner;

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(100, 10));
		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(100, 50));

		sequenceViewController = new SequenceViewControllerImpl();
		JPanel sequenceView = new JPanel();
		sequenceView.setLayout(new BorderLayout());
		sequenceView.add(sequenceViewController.getSequenceView(),
				BorderLayout.CENTER);
		sequenceView.add(new LegendPanel(), BorderLayout.SOUTH);
		componentTableView = new ComponentTableView();

		tabbedPane.add("Sequence View", sequenceView);
		tabbedPane.add("Component View", componentTableView);

		add(tabbedPane, BorderLayout.CENTER);
	}

	public void init(Graph2D graph, GraphManager graphManager) {
		if (graph == null || graph.isEmpty()) {
			sequenceViewController.refreshSequenceView();

			componentTableView.reset();
			componentTableView.repaint();

			return;
		}
	}

	public void setNotation(String notation) {

		try {
			sequenceViewController.setNotation(notation);
		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
		}

		try {
			componentTableView.setNotation(notation);
		} catch (Exception ex) {
			ExceptionHandler.handleException(ex);
		}
	}

	public Graph2DView getSequenceView() {
		return sequenceViewController.getView();
	}

	public void onUpdate(NotationUpdateEvent event) {

		if (!event.getOwner().equals(_owner)) {
			return;
		}

		String notation = event.getData();
		setNotation(notation);
	}

	/**
	 * Added by Roche in order to extend the lower tab pane
	 * 
	 * @return
	 */
	public JTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}
}
