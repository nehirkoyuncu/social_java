package social;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OrderBy;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;

@Entity
class Person {
  @Id
  private String code;
  private String name;
  private String surname;

  // Düzeltme: R4 istatistikleri için EAGER yapıldı
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "FRIENDSHIP",
    joinColumns = @JoinColumn(name = "PERSON_CODE"),
    inverseJoinColumns = @JoinColumn(name = "FRIEND_CODE")
  )
  private Set<Person> friends = new HashSet<>();

  // Düzeltme: R4 istatistikleri için EAGER yapıldı
  @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER) 
  private Set<Group> groups = new HashSet<>();
  
  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("timestamp DESC")
  private List<Post> posts;

  Person() {
  }

  Person(String code, String name, String surname) {
    this.code = code;
    this.name = name;
    this.surname = surname;
  }

  String getCode() {
    return code;
  }

  String getName() {
    return name;
  }

  String getSurname() {
    return surname;
  }

  public Set<Person> getFriends() {
    return friends;
  }

  public Set<Group> getGroups() {
    return groups;
  }
  
  public List<Post> getPosts() {
	  return posts;
  }
  
  public void addFriend(Person friend) {
      this.friends.add(friend);
  }
  
  public void addToGroup(Group group) {
      this.groups.add(group);
  }
}