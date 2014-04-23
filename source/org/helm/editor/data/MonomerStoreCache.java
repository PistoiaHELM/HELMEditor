package org.helm.editor.data;

import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.NotationException;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.DeepCopy;
import org.jdom.JDOMException;

import java.awt.event.*;

/**
 * Describes a database containing 2 monomer stores: 1. Store that is held by
 * MonomerFactory. 2. XHELM Store (external Monomers).
 * 
 * @author M.Lanig
 * 
 */
public class MonomerStoreCache {

	public enum ModalResult {
		OK, CANCEL
	}

	private static MonomerStoreCache _instance = null;

	private MonomerStore internalMonomerStore;
	private MonomerStore externalMonomerStore;
	/**
	 * Contains both stores together. Needs to be synched, when changing one of
	 * the others.
	 */
	private MonomerStore combinedMonomerStore;

	private MonomerStoreCache() {
	}

	/**
	 * Singleton instance.
	 * 
	 * @return instance.
	 */
	public static MonomerStoreCache getInstance() {
		if (_instance == null)
			_instance = new MonomerStoreCache();

		return _instance;
	}

	/**
	 * get the monomer store a monomer belongs to
	 * @param monomer
	 * @return
	 */
	public MonomerStore getMonomerStore( Monomer monomer) {
		String polymerType = monomer.getPolymerType();
		String alternateId = monomer.getAlternateId();
		
		if ( this.externalMonomerStore!= null && this.externalMonomerStore.hasMonomer(polymerType, alternateId)) {
			return this.externalMonomerStore;
		}
		else if ( this.internalMonomerStore.hasMonomer(polymerType, alternateId)) {
			return this.internalMonomerStore;
		}
		else {
			return null;
		}
	}
	
	
	public MonomerStore getExternalStore(){
		return this.externalMonomerStore;
	}
	
	
	
	public MonomerStore getUnregisteredMonomers() {
		MonomerStore store=new MonomerStore();
		if (this.externalMonomerStore != null) {			
			for (String polymerType :this.externalMonomerStore.getPolymerTypeSet()){
				for (Monomer m: this.externalMonomerStore.getMonomers(polymerType).values()){
					if (this.internalMonomerStore.getMonomer(polymerType, m.getAlternateId())==null){
						
						try {
							store.addMonomer(m);
						} catch (Exception e) {
							e.printStackTrace();
							Logger.getLogger(MonomerStoreCache.class.getName())
							.log(Level.WARNING,
									"Error ocurred when adding "+m.getAlternateId()+"to monomer store for merging");

						}
						
					}
				}
				
			}
		}
		if (this.internalMonomerStore !=null){
			for (String polymerType :this.internalMonomerStore.getPolymerTypeSet()){
			for (Monomer m: this.internalMonomerStore.getMonomers(polymerType).values()){
				if (m.isNewMonomer()){						
					try {
						store.addMonomer(m);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.getLogger(MonomerStoreCache.class.getName())
						.log(Level.WARNING,
								"Error ocurred when adding "+m.getAlternateId()+"to monomer store for merging");

					}
					
				}
			}
			
		}
			
		}	
		
			
			
		return store;

	}
	
	/**
	 * Store synchronization. All external monomers are included, together with
	 * every internal monomer with an alternate id that is not yet included in
	 * the externals. This ensures that every id is used only once.
	 */
	private void combineMonomerStores() {
		try {
			this.internalMonomerStore = MonomerFactory.getInstance()
					.getMonomerStore();
			if (this.internalMonomerStore == null)
				return;
		} catch (Exception e1) {
			Logger.getLogger(MonomerStoreCache.class.getName()).log(
					Level.SEVERE, e1.getMessage());
			return;
		}

		combinedMonomerStore = new MonomerStore();

		int notAdded = 0;

		// all externals when applicable
		if (this.externalMonomerStore != null) {
			for (String polymerType : this.externalMonomerStore.getMonomerDB()
					.keySet()) {
				Map<String, Monomer> externalMonomerMap = this.externalMonomerStore.getMonomers(
						polymerType);
				for (Monomer mon : externalMonomerMap.values()) {
					try {
						//combinedMonomerStore.addNewMonomer(mon);						
						combinedMonomerStore.addMonomer(mon);
						
					} catch (Exception e) {
						notAdded++;
					}
				}
			}
		}
		// internals when their alternate id is not contained in externals
		for (String polymerType : this.internalMonomerStore.getMonomerDB()
				.keySet()) {
			Map<String, Monomer> internalMonomerMap = this.internalMonomerStore.getMonomers(
					polymerType);
			for (Monomer mon : internalMonomerMap.values()) {
				try {
					if (!combinedMonomerStore.hasMonomer(polymerType,
							mon.getAlternateId())) {						
						//combinedMonomerStore.addNewMonomer(mon);
						combinedMonomerStore.addMonomer(mon);
					}
				} catch (Exception e) {
					notAdded++;
				}
			}
		}

		if (notAdded > 0)
			Logger.getLogger(MonomerStoreCache.class.getName())
					.log(Level.WARNING,
							notAdded
									+ " monomers could not be added to combined monomer library.");

		MonomerFactory.resetDBChanged();

	}

	/**
	 * Returns the cached monomers. When internal monomer db has changed in the
	 * meantime, it gets refreshed first.
	 * 
	 * @return MonomerStore
	 */
	public MonomerStore getCombinedMonomerStore() {
		if (MonomerFactory.hasDBChanged())
			combineMonomerStores();

		return combinedMonomerStore;

	}

	/**
	 * Resets the cached external MonomerStore to given Parameter. Combined
	 * Store gets refreshed.
	 * 
	 * @param store
	 *            monomer store to set.
	 */
	public void setExternalMonomers(MonomerStore store) {
		this.externalMonomerStore = store;

		combineMonomerStores();
	}

	
	
	
	/**
	 * Adds monomers to the external Monomer Database. Existing monomers won't
	 * be deleted. A check for conflicting monomers is done before.
	 * 
	 * @param db
	 *            monomer store to add
	 * @return true when added successfully, else false.
	 * @throws MonomerException
	 *             may occur when trying to add a monomer to the database
	 * @throws IOException
	 *             may occur when trying to add a monomer to the database
	 * @throws IllegalArgumentException
	 *             when at least one conflict was found.
	 */
	public String addExternalMonomers(JFrame owner, MonomerStore store,
			String helmString) throws IOException, MonomerException,
			IllegalArgumentException {
		if (this.externalMonomerStore == null
				|| this.externalMonomerStore.getMonomerDB() == null) {
			setExternalMonomers(store);
			//return helmString;
		}
			
			
		
		

		LinkedList<Monomer> conflicts = findConflictingMonomers(store);
		LinkedList<String> newNames = new LinkedList<String>();

		if (conflicts.size() > 0) {

			boolean resolved = resolveConflicts(conflicts, newNames, store,
					owner);

			if (!resolved) {
				return null;
			}

		}
		System.out.println(store);
		System.out.println(conflicts);
		LinkedList<String> oldNames = new LinkedList<String>();
		int monomerIndex = -1;
		//add monomers in store to externalStore
		for (String polymerType : store.getMonomerDB().keySet()) {
			for (Monomer newMonomer : store.getMonomers(polymerType).values()) {
				//add monomer if there isn't a conflict
				monomerIndex = conflicts.lastIndexOf(newMonomer);
				if (monomerIndex >= 0) {
					oldNames.add(newMonomer.getAlternateId());
					newMonomer.setAlternateId(newNames.get(monomerIndex));
				}
				
				this.externalMonomerStore.addMonomer(newMonomer);
				

			}
		}

		combineMonomerStores();
		System.out.println(getCombinedMonomerStore());

		String newHelmString = helmString;
		for (int i = 0; i < conflicts.size(); i++) {
		 try {
			newHelmString = ComplexNotationParser.replaceMonomer(newHelmString,
					 conflicts.get(i).getPolymerType(), oldNames.get(i), newNames.get(i), getCombinedMonomerStore(), false);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

		System.out.println(newHelmString);

		return newHelmString;
	}
	/**
	 * for the list of conflicts the user is asked to enter alternate ids
	 * @param list
	 * @param newNames
	 * @param store
	 * @param owner
	 * @return
	 */
	private boolean resolveConflicts(LinkedList<Monomer> list,
			LinkedList<String> newNames, MonomerStore store, JFrame owner) {

		for (Monomer monomer : list) {
			EditConflictsDialog dialog = new EditConflictsDialog(owner,
					monomer.getAlternateId());
			dialog.setVisible(true);
			dialog.setLocationRelativeTo(owner);
			if (dialog.getResult() == ModalResult.OK) {
				newNames.add(dialog.getNewIdentifier());			

			} else {
				return false;
			}

		}
		return true;

	}

	/**
	 * This function checks for conflicts. The given store is compared to the
	 * already saved MonomerStore. When a monomer with same ID is contained in
	 * both stores, the smiles(structure) must be the same, else we have a
	 * conflict.
	 * 
	 * @param store
	 *            the store to check
	 * @return list of all conflicting alternateIDs. Empty list when no
	 *         conflicts were found.
	 */
	private LinkedList<Monomer> findConflictingMonomers(MonomerStore store) {
		LinkedList<Monomer> conflictingIDs = new LinkedList<Monomer>();

		Monomer savedMonomerToCheck = null;

		// compare this parameter to storage
		for (String polymerType : store.getMonomerDB().keySet()) {
			for (Monomer newMonomer : store.getMonomers(polymerType).values()) {
				// When alternateID is already registered -> check for equal
				// smiles
				if (this.internalMonomerStore.hasMonomer(polymerType,
						newMonomer.getAlternateId())) {
					savedMonomerToCheck = this.internalMonomerStore.getMonomer(
							polymerType, newMonomer.getAlternateId());
					if (savedMonomerToCheck != null) {
						if (!savedMonomerToCheck.getCanSMILES().equals(
								newMonomer.getCanSMILES())) {
							// add conflict when alternateIDs equal and
							// smiles/structure differs
							conflictingIDs.add(newMonomer);
						}
					}
				}
			}
		}

		return conflictingIDs;
	}

	/**
	 * Clears all custom monomers in the database.
	 */
	public void clearCustomMonomerDB() {
		if (this.externalMonomerStore != null){
			this.externalMonomerStore.clearMonomers();
			//reload combined Store
			MonomerFactory.setDBChanged(true);
		}
		
		
	}

	/**
	 * Checks whether monomers are contained in the custom database.
	 * 
	 * @return true when at least one monomer is contained, else false.
	 */
	/*
	 * public boolean isExternalMonomerDBEmpty() { return
	 * this.externalMonomerStore == null ||
	 * this.externalMonomerStore.isMonomerStoreEmpty(); }
	 */

	private class EditConflictsDialog extends JDialog {
		/** Generated UID */
		private static final long serialVersionUID = 7754084536776806279L;

		// private JTextField tfLeftBoundary;
		private JTextField tfNewIdentifier;

		private String newIdentifier;

		private ModalResult result;

		/**
		 * Builds the dialog including the layout
		 */
		public EditConflictsDialog(JFrame frame, String identifier) {
			super(frame, true);

			setLocationRelativeTo(frame);

			JButton btnOK = new JButton("Ok");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					okButtonActionPerformed(evt);
				}
			});
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					cancelButtonActionPerformed(evt);
				}
			});

			StringBuffer sb = new StringBuffer();
			sb.append(identifier);
			sb.append(" is already registered with a different structure");

			JLabel lblLeftBoundary = new JLabel(sb.toString());

			JLabel lblRightBoundary = new JLabel(
					"Please enter an alternative identifier");
			tfNewIdentifier = new JTextField("");
			tfNewIdentifier.setSize(50, 10);
			tfNewIdentifier.setMaximumSize(tfNewIdentifier.getSize());
			// tfRightBoundary.addFocusListener(buildFocusListener(tfRightBoundary));

			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup().addComponent(
									lblLeftBoundary))
					.addGroup(
							layout.createParallelGroup()
									.addComponent(lblRightBoundary)
									.addComponent(tfNewIdentifier))
					.addGroup(
							layout.createParallelGroup()
									.addComponent(btnCancel)
									.addComponent(btnOK)));
			layout.setHorizontalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup()
									.addComponent(lblLeftBoundary)
									.addComponent(lblRightBoundary)
									.addComponent(btnCancel))
					.addGroup(layout.createParallelGroup()

					.addComponent(tfNewIdentifier).addComponent(btnOK)));

			getContentPane().requestFocus();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setTitle("Resolve Conflict");
			setMinimumSize(new java.awt.Dimension(400, 140));

		}

		/**
		 * Action for OK Button.
		 * 
		 * @param evt
		 */
		private void okButtonActionPerformed(ActionEvent evt) {

			if (tfNewIdentifier.getText().length() > 0) {
				newIdentifier = tfNewIdentifier.getText();
				result = ModalResult.OK;

				this.dispose();
			}

		}

		/**
		 * Action for Cancel Button.
		 * 
		 * @param evt
		 */
		private void cancelButtonActionPerformed(ActionEvent evt) {
			result = ModalResult.CANCEL;
			this.dispose();
		}

		public String getNewIdentifier() {
			return newIdentifier;
		}

		public ModalResult getResult() {
			return result;
		}

	}

}
