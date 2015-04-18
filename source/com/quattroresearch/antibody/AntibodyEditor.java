package com.quattroresearch.antibody;

import java.awt.HeadlessException;
import java.awt.dnd.DropTarget;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FilenameUtils;

/**
 * Content Panel of "Antibody Sequence Editor" <p> Displays arbitrary number of chains to be searched against DB.
 * Accepts drag&drop File-Input.
 * 
 * @author Anne Mund, quattro research GmbH
 */
public class AntibodyEditor extends JPanel {

  /** Generated UID */
  private static final long serialVersionUID = -7323309152562400371L;

  private static final String HELP_TEXT = "Load files by OpenFileDialog or Drag&Drop: ";

  // declaration of variables
  private List<String> chainNames;

  private List<String> chainSequences;

  private AntibodyDragDropListener dragDropListener;

  public AntibodyEditor() {
    initDragDrop();
    initComponents();
  }

  private void initComponents() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    chainNames = new ArrayList<String>();
    chainSequences = new ArrayList<String>();
    placeChainField(HELP_TEXT, "");

  }

  private void initDragDrop() {
    dragDropListener = new AntibodyDragDropListener(this);
    new DropTarget(this, dragDropListener);
  }

  public List<String> getChains() {
    return chainSequences;
  }

  public List<String> getNames() {
    return chainNames;
  }

  public void setChains(List<String> namelist, List<String> sequencelist) {
    for (int i = 0; i < namelist.size(); i++) {
      placeChainField(namelist.get(i), sequencelist.get(i));
    }
  }

  public void clearAll() {
    this.removeAll();
    initComponents();
  }

  /**
   * Adds name and sequence of chain to the content Panel. <p> Method is called for every new chain added, sequence is
   * put in a JTextArea inside a JScrollPane.
   * 
   * @param name Name of chain; for gp-Files filename
   * @param sequence Sequence of amino acids
   */
  public void placeChainField(String name, String sequence) {
    if (chainNames.size() == 1 && chainNames.get(0).equals(HELP_TEXT)) {
      this.removeAll();
      chainNames = new ArrayList<String>();
      chainSequences = new ArrayList<String>();
    }

    // remember name + sequence
    chainNames.add(name);
    chainSequences.add(sequence);

    // display name + sequence
    JLabel label = new JLabel(name);
    this.add(label);
    JTextArea area = new JTextArea(3, 30);
    area.setText(sequence);
    area.setEditable(false);
    area.setLineWrap(true);
    JScrollPane scroll = new JScrollPane(area);
    this.add(scroll);
    this.validate();
    // this.repaint();

    // enable drag&drop in TextArea
    new DropTarget(area, dragDropListener);
  }

  /**
   * Takes a file and gives it to the appropriate parser. Opens Message Dialog on wrong file extension.
   * 
   * @param file .txt, .gp or .fa File to parse
   * @return (List of chainnames, List of chainsequences)
   * @throws IllegalArgumentException thrown when file type is unknown
   * @throws IOException
   * @throws HeadlessException
   */
  public List<List<String>> readFile(File file) throws IllegalArgumentException, HeadlessException, IOException {
    List<List<String>> result = null;
    if (org.roche.antibody.ui.filechooser.FileUtils.isFastaFile(getFileContent(file))) {
      result = readFastaFile(file);
    } else if (org.roche.antibody.ui.filechooser.FileUtils.isVNTProteinFile(getFileContent(file))) {
      result = readGPFile(file);
    } else {
      String exceptionText =
          "The content of the file is not a fasta nor a VNT protein format. Please use only these file formats.";
      JOptionPane.showMessageDialog(this, exceptionText);
      throw new IllegalArgumentException(exceptionText);
    }
    return result;
  }

  /**
   * Parses .fa Files <p> Parses all chains found in a fasta-formatted file. Sequence can be upper- or lower-case
   * letters, name is everything from ">".
   * 
   * @param file .fa-File to parse
   * @return (List of chainnames, List of chainsequences)
   */
  private List<List<String>> readFastaFile(File file) {
    List<String> foundNames = new ArrayList<String>();
    List<String> foundChains = new ArrayList<String>();
    int current = -1;
    BufferedReader br = null;

    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        String lineClean = line.trim();
        if (!lineClean.isEmpty()) {
          if (lineClean.startsWith(">")) {
            foundNames.add(lineClean.split(">")[1]);
            foundChains.add("");
            current++;
          } else {
            foundChains.set(current, foundChains.get(current) + lineClean);
          }
        }
      }
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(this, "File " + file.getPath() + " was not found.");
      e.printStackTrace();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Could not read file. (Line " + (current + 1) + ")");
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return new ArrayList<List<String>>(Arrays.asList(foundNames, foundChains));
  }

  /**
   * Parses .gp Files <p> Parses all chains found in a gp-formatted file (usually one). Sequence can be upper- or
   * lower-case letters, chainname is the filename.
   * 
   * @param file .gp-File to parse
   * @return (List of chainnames, List of chainsequences)
   */
  private List<List<String>> readGPFile(File file) {
    List<String> foundNames = new ArrayList<String>();
    List<String> foundChains = new ArrayList<String>();
    try {
      FileInputStream fis = new FileInputStream(file);
      DataInputStream in = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      StringBuilder buildChain = new StringBuilder();
      Pattern startofSequence = Pattern.compile("^ORIGIN.*");
      Pattern dataline = Pattern.compile("^(\\s*[A-Z]{2,}\\s{3,})|(//).*");
      Pattern chain = Pattern.compile("^\\s*\\d+\\s+([A-Za-z ]+)$");
      boolean readSequence = false;
      Matcher matcher;

      // extract all Chains from file
      while ((strLine = br.readLine()) != null) {
        matcher = startofSequence.matcher(strLine);
        if (matcher.matches()) {
          readSequence = true;
        }

        if (readSequence) {
          matcher = chain.matcher(strLine.replaceAll("\\*", ""));
          if (matcher.matches()) {
            buildChain.append(matcher.group(1).replaceAll(" ", ""));
          }

          matcher = dataline.matcher(strLine);
          if (matcher.matches()) {
            foundChains.add(buildChain.toString().toUpperCase());
            buildChain = new StringBuilder();
            readSequence = false;
          }
        }
      }

      String filename = FilenameUtils.removeExtension(file.getName());
      if (foundChains.size() == 1) {
        foundNames.add(filename);
      } else {
        for (int i = 0; i < foundChains.size(); i++) {
          foundNames.add(filename + String.valueOf(i));
        }
      }

      in.close();
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }
    return new ArrayList<List<String>>(Arrays.asList(foundNames, foundChains));
  }

  private String getFileContent(File file) throws IOException {
    String lineSeparator = System.getProperty("line.separator");
    StringBuilder fileContent = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null)
        fileContent.append(line);
      fileContent.append(lineSeparator);
      return fileContent.toString();

    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }
}
