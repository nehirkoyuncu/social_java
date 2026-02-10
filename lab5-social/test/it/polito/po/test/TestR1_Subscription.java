package it.polito.po.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import social.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;

class TestR1_Subscription {

  @BeforeAll
  static void populateDb() throws PersonExistsException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
  }

  @AfterAll
  static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  void testR11AddPerson() throws NoSuchCodeException {
    Social m = new Social();
    String s = m.getPerson("ABCD");
    assertEquals("ABCD Ricardo Kaka", s, "Wrong person information");
  }

  @Test
  void testR13PersonDoesNotExist() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.getPerson("ZZ"), "Non existing person should throw exception");
  }

  @Test
  void testR14PersonExists() {
    Social m = new Social();
    assertThrows(PersonExistsException.class, () -> m.addPerson("ABCD", "Alex", "Pato"), "Duplicate code should throw exception");
  }
}
