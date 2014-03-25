package org.helm.editor.data;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.helm.editor.editor.MacromoleculeEditor;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.MonomerStore;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.DeepCopy;
import org.jdom.JDOMException;

/**
 * Describes a database containing 2 monomer stores: 1. Store that is held by
 * MonomerFactory. 2. XHELM Store (external Monomers).
 * 
 * @author M.Lanig
 * 
 */
public class MonomerStoreCache {
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
		if (this.externalMonomerStore != null)
			for (String polymerType : this.externalMonomerStore.getMonomerDB()
					.keySet()) {
				for (Monomer mon : this.externalMonomerStore.getMonomers(
						polymerType).values()) {
					try {
						combinedMonomerStore.addNewMonomer(mon);
					} catch (Exception e) {
						notAdded++;
					}
				}
			}
		// internals when their alternate id is not contained in externals
		for (String polymerType : this.internalMonomerStore.getMonomerDB()
				.keySet()) {
			for (Monomer mon : this.internalMonomerStore.getMonomers(
					polymerType).values()) {
				try {
					if (!combinedMonomerStore.hasMonomer(polymerType,
							mon.getAlternateId())) {
						combinedMonomerStore.addNewMonomer(mon);
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
	public boolean addExternalMonomers(Component owner, MonomerStore store)
			throws IOException, MonomerException, IllegalArgumentException {
		if (this.externalMonomerStore == null
				|| this.externalMonomerStore.getMonomerDB() == null) {
			setExternalMonomers(store);
			return true;
		}

		LinkedList<String> conflicts = findConflictingMonomers(store);
		if (conflicts.size() > 0) {
			JOptionPane.showMessageDialog(owner,
					"Conflicting monomers were found: " + conflicts.toString());
			return false;
		}

		for (String polymerType : store.getMonomerDB().keySet()) {
			for (Monomer newMonomer : store.getMonomers(polymerType).values()) {
				this.externalMonomerStore.addMonomer(newMonomer);
			}
		}

		combineMonomerStores();

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
	private LinkedList<String> findConflictingMonomers(MonomerStore store) {
		LinkedList<String> conflictingIDs = new LinkedList<String>();

		Monomer savedMonomerToCheck = null;

		// compare this parameter to storage
		for (String polymerType : store.getMonomerDB().keySet()) {
			for (Monomer newMonomer : store.getMonomers(polymerType).values()) {
				// When alternateID is already registered -> check for equal
				// smiles
				if (this.externalMonomerStore.hasMonomer(polymerType,
						newMonomer.getAlternateId())) {
					savedMonomerToCheck = this.externalMonomerStore.getMonomer(
							polymerType, newMonomer.getAlternateId());
					if (savedMonomerToCheck != null) {
						if (!savedMonomerToCheck.getCanSMILES().equals(
								newMonomer.getCanSMILES())) {
							// add conflict when alternateIDs equal and
							// smiles/structure differs
							conflictingIDs.add(newMonomer.getAlternateId());
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
		if (this.externalMonomerStore != null)
			this.externalMonomerStore.clearMonomers();
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

}
