package com.quattroresearch.antibody;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Message prompt for sequence input <p> Input dialog for sequence input which also calculates the number of Cysteins
 * and the sequence length
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author Anne Mund, quattro research GmbH
 * 
 */
public class SequenceEditor extends AbstractCellEditor implements
		TableCellEditor, ActionListener {

	/** Generated UID */
	private static final long serialVersionUID = 2460784291731083999L;
	EnterDomainDialog dialog;
	JButton button;
	String currentSequence;
	int row;

	public SequenceEditor(EnterDomainDialog dialog) {
		this.dialog = dialog;
		button = new JButton();
		button.setActionCommand("edit");
		button.addActionListener(this);
		button.setBorderPainted(false);

	}

	@Override
	/** {@inheritDoc}*/
	public Object getCellEditorValue() {
		return currentSequence;
	}

	@Override
	/** {@inheritDoc}*/
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.row = row;
		currentSequence = (String) value;
		return button;
	}

	@Override
	/**
	 * Prompt for sequence String
	 * <p>
	 * Checks whether sequence only contains letters
	 * Also calculates length and Cystein-count.
	 */
	public void actionPerformed(ActionEvent e) {
		if ("edit".equals(e.getActionCommand())) {
			Pattern pattern = Pattern.compile("^[a-zA-Z]+");
			String input = (String) JOptionPane.showInputDialog(
					"Enter Sequence", currentSequence);
			if (input == null) {
				fireEditingStopped();
				return;
			}
			Matcher matcher = pattern.matcher(input);
			while (!matcher.matches()) {
				input = (String) JOptionPane.showInputDialog(
						"The Sequence can only contain letters!", input);
				if (input == null) {
					fireEditingStopped();
					return;
				}
				matcher = pattern.matcher(input);
			}
			currentSequence = input.toUpperCase();
			dialog.calculateCys(currentSequence, row);

			fireEditingStopped();
		}
	}

}
