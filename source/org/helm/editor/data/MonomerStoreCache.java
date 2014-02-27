package org.helm.editor.data;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		} catch (MonomerException | IOException | JDOMException e1) {
			Logger.getLogger(MonomerStoreCache.class.getName()).log(
					Level.SEVERE, e1.getMessage());
			return;
		}

		combinedMonomerStore = new MonomerStore();

		int notAdded = 0;

		// all externals when applicable
		if (this.externalMonomerStore != null)
			for (String polymerType : this.externalMonomerStore.getMonomerDB().keySet()) {
				for (Monomer mon : this.externalMonomerStore.getMonomers(polymerType)
						.values()) {
					try {
						combinedMonomerStore.addNewMonomer(mon);
					} catch (IOException | MonomerException e) {
						notAdded++;
					}
				}
			}
		// internals when their alternate id is not contained in externals
		for (String polymerType : this.internalMonomerStore.getMonomerDB().keySet()) {
			for (Monomer mon : this.internalMonomerStore.getMonomers(polymerType)
					.values()) {
				try {
					if (!combinedMonomerStore.hasMonomer(polymerType,
							mon.getAlternateId())) {
						combinedMonomerStore.addNewMonomer(mon);
					}
				} catch (IOException | MonomerException e) {
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

	@Deprecated
	/**
	 * Adds monomers to the external Monomer Database. Existing monomers won't
	 * be deleted. 
	 * 
	 * @param db monomer store to add
	 */
	public void addExternalMonomers(MonomerStore store) {
		// TODO merge
		this.externalMonomerStore = store;
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
