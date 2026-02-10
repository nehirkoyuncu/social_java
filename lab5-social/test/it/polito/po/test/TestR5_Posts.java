package it.polito.po.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import social.JPAUtil;
import social.Social;

import java.util.List;

class TestR5_Posts {

  @BeforeAll
  static void setUp() throws Exception {
    JPAUtil.setTestMode();
    Social f = new Social();

    f.addPerson("Mario99", "Mario", "Rossi");
    f.addPerson("Mario88", "Mario", "Verdi");
    f.addPerson("Elena66", "Elena", "Aresti");
    f.addPerson("BigLupo", "Lupo", "Bianchi");
    f.addPerson("FFA", "Franca", "Rosetti");
    f.addPerson("Sally", "Sandra", "Sandroni");

    f.addFriendship("Mario99", "BigLupo");
    f.addFriendship("Mario99", "Elena66");
    f.addFriendship("Elena66", "FFA");
    f.addFriendship("Elena66", "Sally");
    f.addFriendship("BigLupo", "FFA");
  }

  @AfterAll
  static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  void testPost() {
    Social f = new Social();
    Social r = new Social();
    String pid = f.post("BigLupo", "Hi everybody!");

    assertNotNull(pid, "Missing post id");
    assertTrue(!pid.isEmpty(), "Empty post id");

    String postContent = r.getPostContent(pid);
    assertNotNull(postContent, "Missing post content");
    assertEquals("Hi everybody!", postContent, "Incorrect post content");
  }

  @Test
  void testPostIdUnique() {
    Social f = new Social();
    String pid = f.post("BigLupo", "Hi everybody!");
    String pid2 = f.post("BigLupo", "Hi everybody!");

    assertNotNull(pid, "Missing post id");
    assertNotEquals(pid, pid2, "Post Ids must be unique");
  }

  @Test
  void testPostData() {
    Social f = new Social();
    Social r = new Social();
    String author = "BigLupo";
    long t0 = System.currentTimeMillis();
    String text = "Hi everybody!";
    String pid = f.post(author, text);

    assertNotNull(pid, "Missing post id");

    String c = r.getPostContent(pid);
    long t = r.getTimestamp(pid);

    long t1 = System.currentTimeMillis();

    assertNotNull(c, "Missing post content");
    assertEquals(text, c, "Wrong content");

    assertTrue(t >= 0, "Missing post timestamp");
    assertTrue(t >= t0 && t <= t1, "Wrong author");

  }

  @Test
  void testPaginatedUserPosts() throws InterruptedException {
    Social f = new Social();
    Social r = new Social();
    String author = "FFA";
    String text = "Hi everybody!";
    f.post(author, text);
    Thread.sleep(5);
    String pid2 = f.post(author, 2 + text);
    Thread.sleep(5);
    f.post(author, 3 + text);
    Thread.sleep(5);
    f.post(author, 4 + text);
    Thread.sleep(5);
    String pid1 = f.post(author, 5 + text);

    List<String> posts = r.getPaginatedUserPosts(author, 1, 3);
    assertNotNull(posts, "Missing paginated post for user " + author);
    assertEquals(3, posts.size(), "Wrong number of posts in page");
    assertEquals(pid1, posts.get(0), "First post should be most recent");

    posts = f.getPaginatedUserPosts(author, 2, 3);
    assertNotNull(posts, "Missing paginated post for user " + author);
    assertEquals(2, posts.size(), "Wrong number of posts in second page");
    assertEquals(pid2, posts.get(0), "First post on page should be most recent");

  }

  @Test
  void testPaginatedFriendPosts() throws InterruptedException {
    Social f = new Social();
    Social r = new Social();
    String author = "Mario88";
    String text = "Hi everybody!";

    f.post(author, text); // not a friend
    Thread.sleep(5);
    String pid2 = f.post("Sally", text);
    Thread.sleep(5);
    f.post("Mario99", text);
    Thread.sleep(5);
    String pid1 = f.post("Sally", 2 + text);
    Thread.sleep(5);
    f.post("Elena66", text); // self post, not included

    List<String> posts = r.getPaginatedFriendPosts("Elena66", 1, 2);

    assertNotNull(posts, "Missing paginated post for user " + author);
    assertEquals(2, posts.size(), "Wrong number of posts in page");
    String[] post = posts.get(0).split("\\s*:\\s*");
    assertEquals(2, post.length, "Wrong post format");
    assertEquals(pid1, post[1], "First post should be most recent");
    assertEquals("Sally", post[0], "First post should be most recent");

    posts = f.getPaginatedFriendPosts("Elena66", 2, 2);
    assertNotNull(posts, "Missing paginated post for user " + author);
    assertEquals(1, posts.size(), "Wrong number of posts in second page");
    post = posts.get(0).split("\\s*:\\s*");
    assertEquals(pid2, post[1], "First post on page should be most recent");

  }

}
