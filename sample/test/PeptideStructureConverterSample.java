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
package test;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.StructureException;
import org.helm.notation.model.Monomer;
import org.helm.notation.peptide.PeptideStructureParser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;

/**
 *
 * @author ZHANGTIANHONG
 */
public class PeptideStructureConverterSample {

    public static void main(String[] args) throws StructureException {
        try {
            String inFilePath = "C:/Documents and Settings/zhangtianhong/Desktop/new-peptide-compounds.txt";
            String outFilePath = "C:/Documents and Settings/zhangtianhong/Desktop/new-peptide-compounds-notation.txt";
            if (args.length == 2) {
                inFilePath = args[0];
                outFilePath = args[1];
            }
            
            Map<String, Map<String, Monomer>> monomers = MonomerFactory.getInstance().getMonomerDB();
            PeptideStructureParser.getInstance().initAminoAcidLists();
            

            System.out.println("Reading compounds from file ...");
            List<Compound> cmpdList = read(inFilePath);
            System.out.println("Reading compounds from file ... done, total count:"+cmpdList.size());

            int count = 0;
            System.out.println("Starting conversion ...");
            for (Compound cmpd : cmpdList) {
                process(cmpd);
                count++;
                System.out.println("\tNumber of compounds processed: "+count);
            }
            System.out.println("Conversion ...done");

            System.out.println("Writing result to file ...");
            write(outFilePath, cmpdList);
            System.out.println("Writing result to file ... done, total count:"+cmpdList.size());

        } catch (MonomerException ex) {
            Logger.getLogger(PeptideStructureConverterSample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            Logger.getLogger(PeptideStructureConverterSample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PeptideStructureConverterSample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeptideStructureConverterSample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<Compound> read(String fileName) throws FileNotFoundException, IOException {
        List<Compound> l = new ArrayList<Compound>();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (null != line) {
            if (line.trim().length() > 0) {
                Compound c = new Compound();
                c.setCompoundNumber(line.trim());
                l.add(c);
            }
            line = br.readLine();
        }
        br.close();
        fr.close();

        return l;
    }

    private static void write(String fileName, List<Compound> compoundList) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        fw.write("PF Number\t");
        fw.write("SMILES\t");
        fw.write("Notation\t");
        fw.write("Status\t");
        fw.write("Message\n");

        for (Compound compound : compoundList) {
            if (null != compound.getCompoundNumber()) {
                fw.write(compound.getCompoundNumber());
            }
            fw.write("\t");

            if (null != compound.getSmiles()) {
                fw.write(compound.getSmiles());
            }
            fw.write("\t");

            if (null != compound.getNotation()) {
                fw.write(compound.getNotation());
            }
            fw.write("\t");


            if (null != compound.getStatus()) {
                fw.write(compound.getStatus());
            }
            fw.write("\t");

            if (null != compound.getMessage()) {
                fw.write(compound.getMessage());
            }

            fw.write("\n");
        }

        fw.close();
    }

    private static void process(Compound compound) {
        String cmpdNumber = compound.getCompoundNumber();
        String molfile = null;
        String smiles = null;
        String notation = null;


        try {
            molfile = MolfileRetriever.getMolfile(cmpdNumber);
        } catch (Exception ex) {
            compound.setStatus("Molfile Retrieval Failure");
            compound.setMessage(ex.getMessage());
        }

        if (null != molfile) {
            try {
                InputStream is = new ByteArrayInputStream(molfile.getBytes());
                MolImporter importer = new MolImporter(is);
                Molecule molecule = importer.read();
                smiles = molecule.toFormat("smiles:u");
            } catch (Exception ex) {
                compound.setStatus("mol2smi Conversion Failure");
                compound.setMessage(ex.getMessage());
            }
        }

        if (null != smiles) {
            compound.setSmiles(smiles);
            try {
                notation = PeptideStructureParser.getInstance().smiles2notation(smiles);
                compound.setNotation(notation);
            } catch (Exception ex) {
                compound.setStatus("smi2notation Conversion Failure");
                compound.setMessage(ex.getMessage());
            }
        }
    }

    static class Compound {

        private String compoundNumber;
        private String smiles;
        private String notation;
        private String status;
        private String message;

        public String getCompoundNumber() {
            return compoundNumber;
        }

        public void setCompoundNumber(String compoundNumber) {
            this.compoundNumber = compoundNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSmiles() {
            return smiles;
        }

        public void setSmiles(String smiles) {
            this.smiles = smiles;
        }

        public String getNotation() {
            return notation;
        }

        public void setNotation(String notation) {
            this.notation = notation;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
