package org.roche.antibody.ui.filechooser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@code FileUtils} contains methods to check the file format
 * 
 * @author <a href="mailto:Stefan.Klostermann@roche.com">Stefan Klostermann</a>, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Penzberg
 * @author <a href="mailto:fichtner@quattro-research.com">Jutta Fichtner</a>
 * 
 * @version $Id: FileUtils.java 13457 2014-11-11 14:06:30Z schirmb $
 */
public class FileUtils {

  /** The Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

  private final static String PATTERN_HEADER =
      "(LOCUS\\p{Blank}+)([A-Za-z0-9\\p{Punct}]+)(\\p{Blank}+)([0-9]+)(\\p{Blank}+)(bp|aa)(\\p{Blank}+)(ds-DNA|DNA)?(\\p{Blank}*)(circular)?(.*)";

  /**
   * Checks if the given file is a VNTFile that contains DNA by reference to the LOCUS tag in the .gb file
   * 
   * @param file the file to be checked.
   * @return true if the given file is a genbank file that contains DNA.
   * @throws IOException
   */
  public static boolean isVNTDNAFile(String file) throws IOException {
    Pattern pattern = Pattern.compile(PATTERN_HEADER);
    BufferedReader readbuffer = null;
    try {
      readbuffer = new BufferedReader(new StringReader(file));
      String strRead;
      strRead = readbuffer.readLine();
      Matcher matcher = pattern.matcher(strRead);

      return (matcher.matches() && matcher.group(6).equalsIgnoreCase("bp") && (matcher.group(8).equalsIgnoreCase("DNA") || matcher.group(8).equalsIgnoreCase("ds-DNA")));
    } finally {
      readbuffer.close();
    }
  }

  /**
   * Checks if the given file is a VNTFile that contains a protein by reference to the LOCUS tag in the .gb file
   * 
   * @param file the file to be checked.
   * @return true if the given file is a genbank file that contains a protein.
   * @throws IOException
   */
  public static boolean isVNTProteinFile(String file) throws IOException {
    Pattern pattern = Pattern.compile(PATTERN_HEADER);
    BufferedReader readbuffer = null;
    try {
      readbuffer = new BufferedReader(new StringReader(file));
      String strRead;
      strRead = readbuffer.readLine();
      Matcher matcher = pattern.matcher(strRead);

      return (matcher.matches() && matcher.group(6).equalsIgnoreCase("aa"));
    } finally {
      readbuffer.close();
    }
  }

  /**
   * Checks if the input really starts with a '>' character. Has to be refined eventually
   * 
   * @param arg the uploaded sequence
   * @return true or false
   */
  public static boolean isFastaFile(String arg) {
    return (arg.trim().indexOf(">") == 0);
  }

}
