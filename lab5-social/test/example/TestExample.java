package example;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import social.*;

import static org.junit.jupiter.api.Assertions.*;

class TestExample {
  private static final String MARIO99 = "Mario99";

  @BeforeAll
  static void populateDb() throws PersonExistsException, NoSuchCodeException, GroupExistsException {
    JPAUtil.setTestMode();
    Social f = new Social();

    f.addPerson(MARIO99, "Mario", "Rossi");
    f.addPerson("Mario88", "Mario", "Verdi");
    f.addPerson("Elena66", "Elena", "Aresti");
    f.addPerson("BigLupo", "Lupo", "Bianchi");
    f.addPerson("FFA", "Franca", "Rosetti");
    f.addPerson("Sally", "Sandra", "Sandroni");

    f.addFriendship(MARIO99, "BigLupo");
    f.addFriendship(MARIO99, "Elena66");
    f.addFriendship("Elena66", "FFA");
    f.addFriendship("Elena66", "Sally");
    f.addFriendship("BigLupo", "FFA");
    f.addFriendship("Mario88", "FFA");
    f.addFriendship("Mario88", "BigLupo");

    f.addGroup("torino");
    f.addGroup("poli");
    f.addGroup("acolyte");

    f.addPersonToGroup(MARIO99, "torino");
    f.addPersonToGroup("Elena66", "torino");
    f.addPersonToGroup(MARIO99, "poli");
    f.addPersonToGroup("FFA", "poli");
    f.addPersonToGroup("Sally", "acolyte");
  }

  @AfterAll
  static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  void testR1() throws NoSuchCodeException {
    Social f = new Social();

    String s = f.getPerson(MARIO99); // "Mario99 Mario Rossi"

    assertNotNull(s, "Missing person");
    assertTrue(s.contains(MARIO99));
    assertTrue(s.contains("Rossi"));
  }

  @Test
  void testR2() throws NoSuchCodeException {
    Social f = new Social();
    Collection<String> friends = f.listOfFriends(MARIO99); // "Elena66" "BigLupo"

    assertNotNull(friends, "Missing list of friends");
    assertTrue(friends.contains("Elena66"));
    assertTrue(friends.contains("BigLupo"));
  }

  @Test
  void testR3() {
    Social f = new Social();
    Collection<String> s = f.listOfPeopleInGroup("poli");

    assertNotNull(s, "Missing list of people in group");
    assertTrue(s.contains(MARIO99));
    assertTrue(s.contains("FFA"));
  }

  @Test
  void testR4() {
    Social f = new Social();
 
    String id = f.personInLargestNumberOfGroups();
    assertEquals(MARIO99, id, "Wrong person with most groups");
  }

  @Test
  void testR5() throws InterruptedException {
    Social f = new Social();
    long t0 = System.currentTimeMillis();
    Thread.sleep(5);
    String text = "Hello world!";
    String pid = f.post(MARIO99, text);

    assertNotNull(pid, "Missing post id");

    assertEquals(text, f.getPostContent(pid));
    long t = f.getTimestamp(pid);
    assertTrue(t > t0);

    f.post(MARIO99, 2 + text);
    Thread.sleep(5);
    f.post(MARIO99, 3 + text);
    Thread.sleep(5);
    f.post(MARIO99, 4 + text);
    Thread.sleep(5);
    String pid1 = f.post(MARIO99, 5 + text);

    List<String> posts = f.getPaginatedUserPosts(MARIO99, 1, 3);
    assertNotNull(posts);
    assertEquals(3, posts.size());
    assertEquals(pid1, posts.get(0));
  }

}