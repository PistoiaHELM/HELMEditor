package org.roche.antibody.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.roche.antibody.model.antibody.Antibody;
import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.model.antibody.Connection;
import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.services.antibody.AntibodyService;

/**
 * 
 * {@code TestSuite}
 * 
 * @author raharjap
 * 
 * @version $Id: TestSuite.java 13993 2014-12-12 12:30:53Z schirmb $
 */
@RunWith(Suite.class)
@SuiteClasses({DomainServiceTest.class, XmlAntbodyServiceTest.class, HELMNotationTest.class})
public class TestSuite {

  /**
   * @return testPeptide
   * @throws Exception
   */
  public static Peptide getTestPeptide() throws Exception {
    List<Peptide> pepList = new ArrayList<Peptide>();
    Peptide pep = new Peptide();
    pep.setSequence("APRILCLARSCC");
    Domain dom = new Domain("April|Lars", pep, 1, 12, 1, 12);
    pep.setDomains(new Domain[] {dom});
    // ME 2014-10-10: changed the constructor call.
    ChemElement sm = new ChemElement("Az", "[*]C(=O)CCCN=[N+]=[N-] |$_R1;;;;;;;;$|",
        "\n" +
            "  ACCLDraw10281407312D\n" +
            "\n" +
            "  8  7  0  0  0  0  0  0  0  0999 V2000\n" +
            "   14.5592  -11.0695    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   15.7403  -11.0695    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   13.9687  -12.0923    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   12.7876  -12.0923    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   12.1970  -13.1152    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   11.0159  -13.1152    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
            "   10.4254  -14.1380    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0\n" +
            "    9.8348  -15.1610    0.0000 N   0  5  0  0  0  0  0  0  0  0  0  0\n" +
            "  1  2  2  0  0  0  0\n" +
            "  1  3  1  0  0  0  0\n" +
            "  3  4  1  0  0  0  0\n" +
            "  4  5  1  0  0  0  0\n" +
            "  5  6  1  0  0  0  0\n" +
            "  6  7  2  0  0  0  0\n" +
            "  7  8  2  0  0  0  0\n" +
            "M  CHG  2   7   1   8  -1\n" +
            "M  END");

    pepList.add(pep);
    Antibody ab = AntibodyService.getInstance().create(pepList);
    Connection conn = new CysteinConnection(6, 11, pep);
    AntibodyService.getInstance().addSequence(sm, ab);
    GeneralConnection gc = new GeneralConnection(pep, sm, 12, 1, "R3", "R1");
    ab.addConnection(conn);
    ab.addConnection(gc);
    return pep;
  }

  public static Antibody getTestAntibody() throws Exception {
    return getTestPeptide().getAntibody();
  }

}
