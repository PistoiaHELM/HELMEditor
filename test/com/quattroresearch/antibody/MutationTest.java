package com.quattroresearch.antibody;

import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.DomainLibraryValues;

public class MutationTest {

  @Test
  public void testIsMutated() {

    // domainLibraryValues
    DomainLibraryValues dlv1 = new DomainLibraryValues();
    dlv1.setName("IGHG1_CH2_HUMAN");

    DomainLibraryValues dlv2 = new DomainLibraryValues();
    dlv2.setName("IGHG1_CH3_HUMAN");

    // domains
    Domain domain1 = new Domain();
    domain1.setLibraryValues(dlv1);
    domain1.setSequence("FFFFFFFFFFDDDDDDDDDDFFAFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDA");

    Domain domain2 = new Domain();
    domain2.setLibraryValues(dlv2);
    domain2.setSequence("FFFFFFFFFFDDDCDDDDDDFFFFFWFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDADDDDD");

    // domainlist

    LinkedList<Domain> domainlist = new LinkedList<Domain>();
    domainlist.add(domain1);
    domainlist.add(domain2);

    // mutations
    Mutation complexMutation = new Mutation("3A");
    SingleMutation sm1 = new SingleMutation("IGHG1_CH2_HUMAN", 23, 'I',
        'A', complexMutation, "3A", 0, null);
    SingleMutation sm2 = new SingleMutation("IGHG1_CH2_HUMAN", 80, 'H',
        'A', complexMutation, "3A", 0, null);
    SingleMutation sm3 = new SingleMutation("IGHG1_CH3_HUMAN", 95, 'H',
        'A', complexMutation, "3A", 0, null);
    complexMutation.getSingleMutations().add(sm1);
    complexMutation.getSingleMutations().add(sm2);
    complexMutation.getSingleMutations().add(sm3);

    Mutation knob = new Mutation("Knob");
    SingleMutation sm11 = new SingleMutation("IGHG1_CH3_HUMAN", 14, 'S',
        'C', knob, "K", 1, "1");
    SingleMutation sm12 = new SingleMutation("IGHG1_CH3_HUMAN", 26, 'T',
        'W', knob, "K", 1, null);
    knob.getSingleMutations().add(sm11);
    knob.getSingleMutations().add(sm12);

    Mutation sple = new Mutation("SPLE");
    SingleMutation sm21 = new SingleMutation("IGHG4_HINGE_HUMAN", 10, 'S',
        'P', sple, "S", 0, null);
    SingleMutation sm22 = new SingleMutation("IGHG4_CH2_HUMAN", 5, 'L',
        'E', sple, "S", 0, null);
    sple.getSingleMutations().add(sm21);
    sple.getSingleMutations().add(sm22);

    // test function
    boolean result = complexMutation.isMutated(domainlist);
    Assert.assertEquals(true, result);

    result = knob.isMutated(domainlist);
    Assert.assertEquals(true, result);

    result = sple.isMutated(domainlist);
    Assert.assertEquals(false, result);

  }
  
  @Test
  public void testContainsSingleMutationDomain() {

    // SingleMutations
    Mutation complexMutation = new Mutation("3A");
    SingleMutation sm1 = new SingleMutation("IGHG1_CH2_HUMAN", 23, 'I',
        'A', complexMutation, "3A", 0, null);
    Mutation knob = new Mutation("Knob");
    SingleMutation sm2 = new SingleMutation("IGHG1_CH3_HUMAN", 14, 'S',
        'C', knob, "K", 1, "1");
    Mutation sple = new Mutation("SPLE");
    SingleMutation sm3 = new SingleMutation("IGHG4_HINGE_HUMAN", 10, 'S',
        'P', sple, "S", 0, null);

    // domainLibraryValues
    DomainLibraryValues dlv1 = new DomainLibraryValues();
    dlv1.setName("IGHG1_CH2_HUMAN");

    DomainLibraryValues dlv2 = new DomainLibraryValues();
    dlv2.setName("IGHG4_HINGE_HUMAN");

    // domains
    Domain domain1 = new Domain();
    domain1.setLibraryValues(dlv1);

    Domain domain2 = new Domain();
    domain2.setLibraryValues(dlv2);

    // domainlist
    LinkedList<Domain> domainlist = new LinkedList<Domain>();
    domainlist.add(domain1);
    domainlist.add(domain2);

    boolean result = complexMutation.containsSingleMutationDomain(sm1, domainlist);
    Assert.assertEquals(true, result);

    result = knob.containsSingleMutationDomain(sm2, domainlist);
    Assert.assertEquals(false, result);

    result = sple.containsSingleMutationDomain(sm3, domainlist);
    Assert.assertEquals(true, result);
  }
}
