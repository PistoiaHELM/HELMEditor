package com.quattroresearch.antibody;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Drag&Drop Listener for AntibodyEditor only accepts copy drops, other methods unused
 * 
 * @author Anne Mund, quattro research GmbH
 *
 */
public class AntibodyDragDropListener implements DropTargetListener{

	private AntibodyEditor antibodyEditor;
	
	public AntibodyDragDropListener(AntibodyEditor aE) {
		this.antibodyEditor = aE;
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		Transferable transferable = dtde.getTransferable();
		//get data formats
		DataFlavor[] formats = transferable.getTransferDataFlavors();
		for (DataFlavor format : formats) {
			try {
				//dropped items could be files
				if (format.isFlavorJavaFileListType()) {
					//make list of all found chains in textfiles
					List<String> foundnames = new ArrayList<String>();
					List<String> foundchains = new ArrayList<String>();
					List<List<String>> result = new ArrayList<List<String>>();
					
					//Accept copy drops
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					List <File> files = (List<File>) transferable.getTransferData(format);
					for(File file: files){
						result = antibodyEditor.readFile(file);

						foundnames.addAll(result.get(0));
						foundchains.addAll(result.get(1));
					}
					antibodyEditor.setChains(foundnames,foundchains);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dtde.dropComplete(true);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		
	}

}
