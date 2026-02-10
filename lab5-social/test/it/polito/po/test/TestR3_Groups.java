package it.polito.po.test;

import java.util.Collection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import social.*;

import static org.junit.jupiter.api.Assertions.*;

class TestR3_Groups {

  @BeforeAll
  static void populateDb() throws PersonExistsException, NoSuchCodeException, GroupExistsException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
    m.addPerson("XYZ", "Alex", "Pato");
    m.addPerson("GGG", "Gennaro", "Gattuso");
    m.addFriendship("ABCD", "XYZ");
    m.addFriendship("ABCD", "GGG");

    m.addGroup("milan");
    m.addGroup("juve");
    m.addGroup("brasile");
    m.addGroup("poli");

    m.addPersonToGroup("XYZ", "brasile");
    m.addPersonToGroup("ABCD", "brasile");
    m.addPersonToGroup("ABCD", "milan");
    m.addPersonToGroup("GGG", "milan");
  }

  @AfterAll
  static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  void testR31Group() {
    Social m = new Social();
    Collection<String> s = m.listOfGroups();
    assertNotNull(s, "Missing list of groups");
    assertTrue(s.contains("milan"));
    assertTrue(s.contains("juve"));
  }

  @Test
  void testR3_UpdateGroupSuccess() throws Exception {
    Social m = new Social();
    Social s = new Social();
    m.updateGroupName("milan", "renamedgroup");

    try{
      Collection<String> groups = s.listOfGroups();
      assertNotNull(groups, "Missing list of groups");

      assertFalse(groups.contains("milan"), "Old group name should not exist");
      assertTrue(groups.contains("renamedgroup"), "New group name should be present");
    }finally{
      m.updateGroupName("renamedgroup", "milan");
    }
  }

  @Test
  void testR3_UpdateGroup_GroupExistsException() {
    Social m = new Social();
    assertThrows(GroupExistsException.class, () -> m.updateGroupName("juve", "milan"), "Updating to an already existing group should fail");
  }

  @Test
  void testR3_UpdateGroup_NoSuchCodeException() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.updateGroupName("doesnotexist", "newname"), "Updating a non-existent group should fail");
  }

  @Test
  void testR3_DeleteGroupSuccess() throws Exception {
    Social m = new Social();
    Social s = new Social();
    Collection<String> groups;

    m.addGroup("tobedeleted");
    groups = s.listOfGroups();
    assertNotNull(groups, "Missing list of groups");

    assertTrue(groups.contains("tobedeleted"), "Group tobedeleted should be present");
    s.deleteGroup("tobedeleted");
    groups = m.listOfGroups();
    assertFalse(groups.contains("tobedeleted"), "Group tobedeleted should not be present");
  }

  @Test
  void testR3_DeleteGroup_NoSuchCodeException() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.deleteGroup("ghostgroup"), "Deleting a non-existent group should fail");
  }

  @Test
  void testR33GroupListing() {
    Social m = new Social();
    Collection<String> s = m.listOfPeopleInGroup("brasile");
    assertNotNull(s, "Missing list of groups");
    assertTrue(s.contains("XYZ"));
    assertTrue(s.contains("ABCD"));

    s = m.listOfPeopleInGroup("milan");
    assertTrue(s.contains("ABCD"));
    assertTrue(s.contains("GGG"));
  }

  @Test
  void testR3_MissingPerson() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.addPersonToGroup("NONEXISTENT", "brasil"), "When adding an unknown person to a group an exception is expected");
  }

  @Test
  void testR3_MissingGroup() {
    Social m = new Social();
    assertThrows(NoSuchCodeException.class, () -> m.addPersonToGroup("ABCD", "NO_GROUP"), "When adding to an unknown group an exception is expected");
  }

  @Test
  void testR3_EmptyGroup() {
    Social m = new Social();
    Collection<String> s = m.listOfPeopleInGroup("juve");
    assertNotNull(s, "Missing collection of members for empty group");
    assertEquals(0, s.size(), "Empty group should have no members");
  }

}