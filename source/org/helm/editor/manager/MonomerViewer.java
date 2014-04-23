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
package org.helm.editor.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import chemaxon.marvin.beans.MSketchPane;
import chemaxon.marvin.beans.MViewPane;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;

import org.helm.editor.data.MonomerStoreCache;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.MonomerParser;
import org.helm.notation.tools.SimpleNotationParser;
import org.helm.notation.tools.StructureParser;

import java.awt.Component;
import java.util.Collections;

import javax.swing.DefaultCellEditor;
import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author lih25
 */
public class MonomerViewer extends JPanel {

	private MSketchPane msketchPane;
	private JButton editButton;
	private MViewPane mviewPane;
	private JTextField idTextField;
	private JTextField nameTextField;
	private JComboBox monomerTypeComboBox;
	private JComboBox polymerTypeComboBox;
	private JTextField naturalAnalogTextField;
	private JTable attachmentTable;
	private AttachmentTableModel model;
	private boolean modifiable = true;
	private static Map<String, MonomerViewer> namedInstances = new HashMap<String, MonomerViewer>();
	private static MonomerViewer instance;
	
	private Monomer monomer;
	
	private MonomerViewer() {
		this(true);
	}

	public synchronized static MonomerViewer getInstance() {
		if (instance == null) {
			instance = new MonomerViewer();
		}

		return instance;
	}

	public static MonomerViewer getNamedInstance(String name) {
		MonomerViewer inst = namedInstances.get(name);
		if (inst == null) {
			inst = new MonomerViewer();
			namedInstances.put(name, inst);
		}

		return inst;
	}

	private MonomerViewer(boolean modifiable) {
		this.modifiable = modifiable;

		JPanel viewerPanel = createViewerPanel();
		viewerPanel.setBorder(BorderFactory.createEtchedBorder());

		JPanel allDataPanel = createOverallDataPanel();
		allDataPanel.setBorder(BorderFactory.createEtchedBorder());

		setLayout(new BorderLayout());
		add(viewerPanel, BorderLayout.CENTER);
		add(allDataPanel, BorderLayout.EAST);

		initComboBoxes();
		setModifiableStatus(modifiable);
	}

	/**
	 * mviewPane will always be enabled, otherwise, the menu bar for the
	 * application will be disabled (Marvin did this)
	 * 
	 * @param modifiable
	 */
	public void setModifiableStatus(boolean modifiable) {
		this.modifiable = modifiable;
		editButton.setEnabled(modifiable);
		// mviewPane.setEnabled(modifiable);
		idTextField.setEditable(modifiable);
		nameTextField.setEditable(modifiable);
		monomerTypeComboBox.setEnabled(modifiable);
		polymerTypeComboBox.setEnabled(modifiable);
		naturalAnalogTextField.setEditable(modifiable);
		attachmentTable.setEnabled(modifiable);
	}
	
	public void setIdEditable(boolean modifiable){
		idTextField.setEditable(modifiable);
	}

	public void setNameEditable(boolean modifiable){
		nameTextField.setEditable(modifiable);
	}
	
	public void setNaturalAnalogEditable(boolean modifiable){
		naturalAnalogTextField.setEditable(modifiable);
	}
	
	private JPanel createViewerPanel() {
		msketchPane = new MSketchPane();
		msketchPane.setBorder(BorderFactory.createEtchedBorder());
		mviewPane = new MViewPane();
		mviewPane.setEnabled(true);
		mviewPane.setM(0, "");

		editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (isModifiable()) {
					msketchPane.setMol(mviewPane.getM(0));
					int result = JOptionPane.showOptionDialog(mviewPane,
							msketchPane, "Monomer Structure Editor",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, null, null);
					// int result = JOptionPane.showConfirmDialog(mviewPane,
					// msketchPane, "Monomer Structure Editor",
					// JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						mviewPane.setM(0, msketchPane.getMol());
					}
				}
			}
		});

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(Box.createHorizontalStrut(5));
		buttonBox.add(Box.createHorizontalStrut((int) (editButton
				.getPreferredSize().getWidth()) * 2));
		buttonBox.add(editButton);
		buttonBox.add(Box.createHorizontalStrut(5));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(mviewPane, BorderLayout.CENTER);
		panel.add(buttonBox, BorderLayout.SOUTH);
		return panel;
	}

	private JPanel createOverallDataPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JPanel dataPanel = createDataPanel();
		JScrollPane attachmentPane = createAttachmentPane();

		panel.add(Box.createHorizontalStrut(5));
		panel.add(dataPanel);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(attachmentPane);
		panel.add(Box.createHorizontalStrut(5));

		JPanel vSizablePanel = new JPanel();
		vSizablePanel.setLayout(new BoxLayout(vSizablePanel, BoxLayout.Y_AXIS));
		vSizablePanel.add(Box.createVerticalGlue());
		vSizablePanel.add(panel);
		vSizablePanel.add(Box.createVerticalGlue());

		return vSizablePanel;
	}

	private JPanel createDataPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

		JLabel idLabel = new JLabel("Monomer ID:  ");
		JLabel nameLabel = new JLabel("Monomer Name:  ");
		JLabel monomerTypeLabel = new JLabel("Monomer Type:  ");
		JLabel polymerTypeLabel = new JLabel("Polymer Type:  ");
		JLabel naturalAnalogLabel = new JLabel("Natural Analog:  ");
		JLabel[] labels = new JLabel[] { idLabel, nameLabel, monomerTypeLabel,
				polymerTypeLabel, naturalAnalogLabel };

		// Size all labels to be as wide as the widest
		JLabel longestLabel = monomerTypeLabel;
		Dimension labelDimension = new Dimension(
				longestLabel.getPreferredSize().width + 10,
				new JTextField().getPreferredSize().height);

		for (int i = 0; i < labels.length; i++) {
			labels[i].setMinimumSize(labelDimension);
			labels[i].setMaximumSize(labelDimension);
			labels[i].setPreferredSize(labelDimension);
		}

		idTextField = new JTextField(20);
		nameTextField = new JTextField(20);
		monomerTypeComboBox = new JComboBox();
		polymerTypeComboBox = new JComboBox();
		naturalAnalogTextField = new JTextField(20);

		Dimension fieldDimentsion = new Dimension(2 * (labelDimension.width),
				labelDimension.height);
		JTextField[] textFields = new JTextField[] { idTextField,
				nameTextField, naturalAnalogTextField };
		for (int i = 0; i < textFields.length; i++) {
			textFields[i].setMinimumSize(fieldDimentsion);
			textFields[i].setMaximumSize(fieldDimentsion);
			textFields[i].setPreferredSize(fieldDimentsion);
		}

		JComboBox[] comboBoxes = new JComboBox[] { monomerTypeComboBox,
				polymerTypeComboBox };
		for (int i = 0; i < comboBoxes.length; i++) {
			comboBoxes[i].setMinimumSize(fieldDimentsion);
			comboBoxes[i].setMaximumSize(fieldDimentsion);
			comboBoxes[i].setPreferredSize(fieldDimentsion);
		}

		Box idBox = Box.createHorizontalBox();
		idBox.add(Box.createHorizontalStrut(5));
		idBox.add(idLabel);
		idBox.add(Box.createHorizontalStrut(5));
		idBox.add(idTextField);
		idBox.add(Box.createHorizontalStrut(5));

		Box nameBox = Box.createHorizontalBox();
		nameBox.add(Box.createHorizontalStrut(5));
		nameBox.add(nameLabel);
		nameBox.add(Box.createHorizontalStrut(5));
		nameBox.add(nameTextField);
		nameBox.add(Box.createHorizontalStrut(5));

		Box monomerTypeBox = Box.createHorizontalBox();
		monomerTypeBox.add(Box.createHorizontalStrut(5));
		monomerTypeBox.add(monomerTypeLabel);
		monomerTypeBox.add(Box.createHorizontalStrut(5));
		monomerTypeBox.add(monomerTypeComboBox);
		monomerTypeBox.add(Box.createHorizontalStrut(5));

		Box polymerTypeBox = Box.createHorizontalBox();
		polymerTypeBox.add(Box.createHorizontalStrut(5));
		polymerTypeBox.add(polymerTypeLabel);
		polymerTypeBox.add(Box.createHorizontalStrut(5));
		polymerTypeBox.add(polymerTypeComboBox);
		polymerTypeBox.add(Box.createHorizontalStrut(5));

		Box naturalAnalogBox = Box.createHorizontalBox();
		naturalAnalogBox.add(Box.createHorizontalStrut(5));
		naturalAnalogBox.add(naturalAnalogLabel);
		naturalAnalogBox.add(Box.createHorizontalStrut(5));
		naturalAnalogBox.add(naturalAnalogTextField);
		naturalAnalogBox.add(Box.createHorizontalStrut(5));

		// dataPanel.add(Box.createVerticalStrut(1));
		dataPanel.add(idBox);
		// dataPanel.add(Box.createVerticalStrut(1));
		dataPanel.add(nameBox);
		// dataPanel.add(Box.createVerticalStrut(1));
		dataPanel.add(monomerTypeBox);
		// dataPanel.add(Box.createVerticalStrut(1));
		dataPanel.add(polymerTypeBox);
		// dataPanel.add(Box.createVerticalStrut(1));
		dataPanel.add(naturalAnalogBox);
		// dataPanel.add(Box.createVerticalStrut(1));

		return dataPanel;
	}

	private JScrollPane createAttachmentPane() {
		model = new AttachmentTableModel();
		attachmentTable = new JTable(model);
		AttachmentIDTableCellEditor editor = new AttachmentIDTableCellEditor(
				new JComboBox());
		attachmentTable.getColumnModel()
				.getColumn(AttachmentTableModel.ID_COLUMN_INDEX)
				.setCellEditor(editor);
		attachmentTable.setPreferredScrollableViewportSize(new Dimension(200,
				160));
		JScrollPane pane = new JScrollPane(attachmentTable);
		return pane;
	}

	private void initComboBoxes() {
		try {
			String[] polymerTypes = MonomerFactory.getInstance()
					.getPolymerTypes().toArray(new String[0]);
			polymerTypeComboBox
					.setModel(new DefaultComboBoxModel(polymerTypes));
			String[] monomerTypes = MonomerFactory.getInstance()
					.getMonomerTypes().toArray(new String[0]);
			monomerTypeComboBox
					.setModel(new DefaultComboBoxModel(monomerTypes));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
					"Error populating polymer type or monomer type combobox\n"
							+ ex.getMessage(),
					"MonomerViewer Initialization Error",
					JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(MonomerViewer.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}
	
	public Monomer getMonomer() {
		return this.monomer;
	}

	public Monomer getEditedMonomer() {
		String newSmiles = mviewPane.getM(0, "cxsmiles:u");
		String molfile = mviewPane.getM(0, "mol");
		String monomerId = idTextField.getText();
		String name = nameTextField.getText();
		String monomerType = (String) monomerTypeComboBox.getSelectedItem();
		String polymerType = (String) polymerTypeComboBox.getSelectedItem();
		String naturalAnalog = naturalAnalogTextField.getText();
		List<Attachment> attachments = getAttachmentList();

		// MonomerInfo tmpMI = new MonomerInfo(polymerType, monomerId);
		// tmpMI.setAttachmentList(attachments);

		Monomer tmpM = new Monomer(polymerType, monomerType, naturalAnalog,
				monomerId);
		tmpM.setName(name);
		tmpM.setCanSMILES(newSmiles);
		tmpM.setMolfile(molfile);
		tmpM.setAttachmentList(attachments);
		
		return tmpM;
	}

	public String getEditedMonomerXML() throws MonomerException {
		Monomer monomer = getEditedMonomer();
		Element monomerElement = MonomerParser.getMonomerElement(monomer);
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		return outputter.outputString(monomerElement);
	}

	private List<Attachment> getAttachmentList() {
		List<Attachment> aList = new ArrayList<Attachment>();

		List<AttachmentUIBean> beans = model.getData();
		for (AttachmentUIBean bean : beans) {
			if (null != bean.getId() && bean.getId().length() > 0) {
				Attachment att = new Attachment();
				att.setAlternateId(bean.getId());
				att.setLabel(bean.getLabel());
				aList.add(att);
			}
		}

		return aList;
	}

	public void setMonomer(Monomer monomer) {
		this.monomer = monomer;
		
		if (monomer != null) {
			if (monomer.getMolfile() != null) {
				mviewPane.setM(0, monomer.getMolfile());
			} else if (monomer.getCanSMILES() != null) {
				mviewPane.setM(0, monomer.getCanSMILES());
			} else {
				mviewPane.setM(0, "");
			}

			if (monomer.getAlternateId() != null) {
				idTextField.setText(monomer.getAlternateId());
			} else {
				idTextField.setText("");
			}

			if (monomer.getName() != null) {
				nameTextField.setText(monomer.getName());
			} else {
				nameTextField.setText("");
			}
			if (monomer.getMonomerType() != null) {
				monomerTypeComboBox.setSelectedItem(monomer.getMonomerType());
			}

			if (monomer.getPolymerType() != null) {
				polymerTypeComboBox.setSelectedItem(monomer.getPolymerType());
			}

			if (monomer.getNaturalAnalog() != null) {
				naturalAnalogTextField.setText(monomer.getNaturalAnalog());
			} else {
				naturalAnalogTextField.setText("");
			}

			List<Attachment> aList = monomer.getAttachmentList();
			model.updateAttachments(aList);
		} else {
			mviewPane.setM(0, "");
			idTextField.setText("");
			nameTextField.setText("");
			naturalAnalogTextField.setText("");

			model.updateAttachments(null);
		}
	}

	public boolean isModifiable() {
		return modifiable;
	}

	public boolean isValidNewMonomer() {
		try {
			Monomer m = getEditedMonomer();
			MonomerParser.validateMonomer(m);

			
			MonomerFactory monomerFactory = MonomerFactory.getInstance();
			Map<String, Map<String, Monomer>> monomerDB = monomerFactory.getMonomerDB();
			
//			Map<String, Map<String, Monomer>> monomerDB = MonomerStoreCache
//					.getInstance().getCombinedMonomerStore().getMonomerDB();

			Map<String, Monomer> idMap = monomerDB.get(m.getPolymerType());
			Monomer[] monomers = idMap.values().toArray(new Monomer[0]);
			for (int i = 0; i < monomers.length; i++) {
				if (monomers[i].getAlternateId().equals(m.getAlternateId())) {
					JOptionPane.showMessageDialog(
							this,
							"Monomer with the same ID of " + m.getAlternateId()
									+ " already exists in "
									+ m.getPolymerType(),
							"Monomer Validation Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}

			// for structure that contains any atom, it is ok for smiles and
			// attachment to be the same
			
			Map<String, Monomer> smilesMap = monomerFactory.getSmilesMonomerDB();
			 
			System.out.println(m.getCanSMILES());
//			Map<String, Monomer> smilesMap = MonomerStoreCache.getInstance()
//					.getCombinedMonomerStore().getSmilesMonomerDB();
			
			//iterate over smilesMap and compare only the smiles part without the rests
			String smiles=StructureParser.getSmilesFromExtendedSmiles(m.getCanSMILES());
			Iterator<Monomer> it = smilesMap.values().iterator();
			while (it.hasNext()) {
				Monomer existM = it.next();
				if	(existM.getCanSMILES()==null){
					continue;
				}
				String monomerSmiles = StructureParser.getSmilesFromExtendedSmiles(existM.getCanSMILES());
				
				
				
				if (monomerSmiles != null && monomerSmiles.compareTo(smiles) == 0) {
					
					 
					boolean isAdhocMonomer= false;
					if(this.monomer != null) {
						isAdhocMonomer = this.monomer.isAdHocMonomer();
					}

					boolean sameAttachment = m.attachmentEquals(existM);
					if (sameAttachment || isAdhocMonomer) {
						boolean containsAnyAtom = m.containAnyAtom();
						if (!containsAnyAtom) {
							JOptionPane.showMessageDialog(
									this,
									"Monomer with the same structure already exists in "
											+ existM.getPolymerType()
											+ " with monomer ID of "
											+ existM.getAlternateId(),
									"Monomer Validation Error",
									JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
				}
			}
			
//			if (smilesMap.containsKey(m.getCanSMILES())) {
//				Monomer existM = smilesMap.get(m.getCanSMILES());
//				boolean sameAttachment = m.attachmentEquals(existM);
//				if (sameAttachment) {
//					boolean containsAnyAtom = m.containAnyAtom();
//					if (!containsAnyAtom) {
//						JOptionPane.showMessageDialog(
//								this,
//								"Monomer with the same structure already exists in "
//										+ existM.getPolymerType()
//										+ " with monomer ID of "
//										+ existM.getAlternateId(),
//								"Monomer Validation Error",
//								JOptionPane.ERROR_MESSAGE);
//						return false;
//					}
//				}
//			}

			return true;

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(),
					"Monomer Validation Error", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(MonomerViewer.class.getName()).log(Level.WARNING,
					"isValidNewMonomer", ex);
			return false;
		}
	}

	class AttachmentTableModel extends AbstractTableModel {

		private String[] columns = { "Label", "Attachment ID" };
		private List<AttachmentUIBean> data;
		public static final int LABEL_COLUMN_INDEX = 0;
		public static final int ID_COLUMN_INDEX = 1;

		public AttachmentTableModel() {
			try {
				data = new ArrayList<AttachmentUIBean>();
				Map<String, List<String>> labelMap = MonomerFactory
						.getInstance().getAttachmentLabelIDs();

				// Sort labels
				List<String> labels = new ArrayList<String>();
				labels.addAll(labelMap.keySet());
				Collections.sort(labels);

				// build data for the model
				for (String label : labels) {
					AttachmentUIBean bean = new AttachmentUIBean();
					bean.setLabel(label);
					bean.setId("");
					List<String> l = labelMap.get(label);
					l.add(0, "");
					bean.setIds(l.toArray(new String[0]));
					data.add(bean);
				}
			} catch (Exception ex) {
				Logger.getLogger(MonomerViewer.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

		public int getRowCount() {
			return data.size();
		}

		public int getColumnCount() {
			return columns.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			AttachmentUIBean bean = getData().get(rowIndex);
			switch (columnIndex) {
			case 0:
				return bean.getLabel();
			case 1:
				return bean.getId();
			default:
				return "";

			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == ID_COLUMN_INDEX) {
				return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			AttachmentUIBean bean = getData().get(rowIndex);
			if (null != aValue && aValue instanceof String) {
				if (columnIndex == ID_COLUMN_INDEX) {
					bean.setId((String) aValue);
				}
			}
		}

		public List<AttachmentUIBean> getData() {
			return data;
		}

		public void updateAttachments(List<Attachment> attachments) {
			if (null == attachments) {
				attachments = new ArrayList<Attachment>();
			}

			for (AttachmentUIBean bean : data) {
				String uiLabel = bean.getLabel();
				String uiId = "";
				for (Attachment att : attachments) {
					String label = att.getLabel();
					String id = att.getAlternateId();
					if (uiLabel.equals(label)) {
						uiId = id;
						break;
					}
				}
				bean.setId(uiId);
			}
			fireTableDataChanged();
		}

		public String[] getIDsByRow(int row) {
			return getData().get(row).getIds();
		}
	}

	class AttachmentIDTableCellEditor extends DefaultCellEditor {

		private JComboBox comboBox = new JComboBox();

		public AttachmentIDTableCellEditor(JComboBox comboBox) {
			super(comboBox);
			this.comboBox = comboBox;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			String[] ids = ((AttachmentTableModel) table.getModel())
					.getIDsByRow(row);
			DefaultComboBoxModel model = new DefaultComboBoxModel(ids);
			comboBox.setModel(model);
			return comboBox;
		}
	}

	class AttachmentUIBean {

		private String label;
		private String id;
		private String[] ids;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String[] getIds() {
			return ids;
		}

		public void setIds(String[] ids) {
			this.ids = ids;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}
}
