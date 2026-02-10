package it.polito.po.test;

import java.util.Collection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import social.*;

import static org.junit.jupiter.api.Assertions.*;

class TestR2_Friends {

  @BeforeAll
  static void populateDb() throws PersonExistsException, NoSuchCodeException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
    m.addPerson("XYZ", "Alex", "Pato");
    m.addPerson("GGG", "Gennaro", "Gattuso");
    m.addFriendship("ABCD", "XYZ");
    m.addFriendship("ABCD", "GGG");

    m.addPerson("KABI", "Khaby", "Lame");

    m.addPerson("PPP", "Paolo", "Maldini");
    m.addPerson("AAA", "Andrea", "Pirlo");
    m.addFriendship("PPP", "GGG");
    m.addFriendship("AAA", "GGG");

    m.addFriendship("ABCD", "PPP");
  }

  @AfterAll
  static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  void testR21Friendship() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("ABCD");
    assertNotNull(friends, "Missing list of friends");
    assertTrue(friends.contains("XYZ"));
    assertTrue(friends.contains("GGG"));
  }

  @Test
  void testR22TwoWayFriendship() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("ABCD");
    assertNotNull(friends, "Missing list of friends");
    assertTrue(friends.contains("XYZ"));
    assertTrue(friends.contains("GGG"));
    Collection<String> friends2 = m.listOfFriends("GGG");
    assertTrue(friends2.contains("ABCD"));
    Collection<String> friends3 = m.listOfFriends("XYZ");
    assertTrue(friends3.contains("ABCD"));
  }

  @Test
  void testR23FriendshipNotExistingCode() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.addFriendship("ABCD", "UUUU"), "Expecting an exception for friendship with non existing code");
  }

  @Test
  void testR24FriendshipNotExistingCode2() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.listOfFriends("UUUU"), "Expecting an exception for friendship with non existing code");
  }

  @Test
  void testR25FriendshipNull() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("KABI");
    assertNotNull(friends, "Missing list of friends");
    assertEquals(0, friends.size(), "Expecting no friends for newly created person");
  }

}