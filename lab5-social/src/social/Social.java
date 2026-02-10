package social;

import java.util.*;
import java.util.stream.Collectors;
import social.JPAUtil.ThrowingSupplier;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.UUID;


@Entity
@Table(name = "SOCIAL_GROUP")
class Group {
    @Id
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Person> members = new HashSet<>();

    Group() {}

    Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Person> getMembers() {
        return members;
    }
    
    public void addMember(Person person) {
        this.members.add(person);
    }
}

@Entity
class Post {
    @Id
    private String pid; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_code")
    private Person author;
    
    private String content;
    private long timestamp;

    Post() {}

    Post(String pid, Person author, String content, long timestamp) {
        this.pid = pid;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getPid() {
        return pid;
    }

    public Person getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

class GroupRepository extends GenericRepository<Group, String> {

    public GroupRepository() {
        super(Group.class);
    }
    
    public boolean existsByName(String name) {
        return findById(name).isPresent();
    }
}

class PostRepository extends GenericRepository<Post, String> {

    public PostRepository() {
        super(Post.class);
    }
    
    public List<String> findPaginatedUserPosts(String authorCode, int pageNo, int pageLength) {
        int firstResult = (pageNo - 1) * pageLength;

        return JPAUtil.withEntityManager(em -> {
            TypedQuery<String> query = em.createQuery(
                "SELECT p.pid FROM Post p WHERE p.author.code = :authorCode ORDER BY p.timestamp DESC", 
                String.class
            );
            query.setParameter("authorCode", authorCode);
            query.setFirstResult(firstResult);
            query.setMaxResults(pageLength);
            return query.getResultList();
        });
    }

    public List<Object[]> findPaginatedFriendPosts(List<String> friendCodes, int pageNo, int pageLength) {
        int firstResult = (pageNo - 1) * pageLength;

        if (friendCodes.isEmpty()) {
            return List.of();
        }

        return JPAUtil.withEntityManager(em -> {
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT p.author.code, p.pid FROM Post p WHERE p.author.code IN :friendCodes ORDER BY p.timestamp DESC", 
                Object[].class
            );
            query.setParameter("friendCodes", friendCodes);
            query.setFirstResult(firstResult);
            query.setMaxResults(pageLength);
            return query.getResultList();
        });
    }
    
    public Optional<Post> findByPid(String pid) {
        return findById(pid);
    }
}



public class Social {

  private final PersonRepository personRepository = new PersonRepository();
  private final GroupRepository groupRepository = new GroupRepository();
  private final PostRepository postRepository = new PostRepository();
  
  
  private String generatePostId() {
      return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
  }


  public void addPerson(String code, String name, String surname) throws PersonExistsException {
    if (personRepository.findById(code).isPresent()){
        throw new PersonExistsException();
    }
    Person person = new Person(code, name, surname);
    personRepository.save(person);
  }

 
  public String getPerson(String code) throws NoSuchCodeException {
    Person person = personRepository.findById(code)
            .orElseThrow(() -> new NoSuchCodeException());
    return person.getCode() + " " + person.getName() + " " + person.getSurname();
  }

  public void addFriendship(String codePerson1, String codePerson2)
      throws NoSuchCodeException {
    if (codePerson1.equals(codePerson2)) return;

    try {
        JPAUtil.executeInTransaction(() -> {
            Person person1 = personRepository.findById(codePerson1)
                    .orElseThrow(() -> new NoSuchCodeException());
            Person person2 = personRepository.findById(codePerson2)
                    .orElseThrow(() -> new NoSuchCodeException());
            
            person1.addFriend(person2);
            person2.addFriend(person1);
            
            personRepository.update(person1);
            personRepository.update(person2);
        });
    } catch (NoSuchCodeException e) {
        throw e;
    } catch (Exception e) {
        if (e.getCause() instanceof NoSuchCodeException) {
            throw (NoSuchCodeException) e.getCause();
        }
        throw new RuntimeException("Error executing addFriendship transaction", e);
    }
  }

  public Collection<String> listOfFriends(String codePerson)
      throws NoSuchCodeException {
    return JPAUtil.executeInContext((ThrowingSupplier<Collection<String>, NoSuchCodeException>) () -> {
      Person person = personRepository.findById(codePerson)
              .orElseThrow(() -> new NoSuchCodeException());
      
      return person.getFriends().stream()
              .map(Person::getCode)
              .collect(Collectors.toList());
    });
  }

  public void addGroup(String groupName) throws GroupExistsException {
    if (groupRepository.existsByName(groupName)) {
        throw new GroupExistsException();
    }
    Group group = new Group(groupName);
    groupRepository.save(group);
  }

  public void deleteGroup(String groupName) throws NoSuchCodeException {
    
    JPAUtil.executeInTransaction(() -> {
        Group group = groupRepository.findById(groupName)
            .orElseThrow(() -> new NoSuchCodeException());

        Set<Person> currentMembers = new HashSet<>(group.getMembers());
        
        for(Person member : currentMembers){
            Person attachedMember = personRepository.findById(member.getCode()).get(); 
            attachedMember.getGroups().remove(group);
            personRepository.update(attachedMember);
        }
        
        groupRepository.delete(group);
    });
  }

  public void updateGroupName(String groupName, String newName) throws NoSuchCodeException, GroupExistsException {
    if (groupRepository.existsByName(newName)) {
        throw new GroupExistsException();
    }
    
    JPAUtil.executeInTransaction(() -> {
        Group oldGroup = groupRepository.findById(groupName)
            .orElseThrow(() -> new NoSuchCodeException());

        Set<Person> currentMembers = new HashSet<>(oldGroup.getMembers());
        
        for(Person member : currentMembers){
            Person attachedMember = personRepository.findById(member.getCode()).get();
            attachedMember.getGroups().remove(oldGroup);
            personRepository.update(attachedMember);
        }
      
        groupRepository.delete(oldGroup);

        Group newGroup = new Group(newName);
        groupRepository.save(newGroup);
        
        for(Person member : currentMembers){
            Person attachedMember = personRepository.findById(member.getCode()).get();
            attachedMember.getGroups().add(newGroup);
            personRepository.update(attachedMember);
        }
        
        newGroup.getMembers().addAll(currentMembers);
        groupRepository.update(newGroup);
    });
  }

  public Collection<String> listOfGroups() {
    return groupRepository.findAll().stream()
            .map(Group::getName)
            .collect(Collectors.toList());
  }

  public void addPersonToGroup(String codePerson, String groupName) throws NoSuchCodeException {
    
    try {
        JPAUtil.executeInTransaction(() -> {
            Person person = personRepository.findById(codePerson)
                    .orElseThrow(() -> new NoSuchCodeException());
            Group group = groupRepository.findById(groupName)
                    .orElseThrow(() -> new NoSuchCodeException());

            person.addToGroup(group);
            group.addMember(person);
            
            personRepository.update(person);
            groupRepository.update(group);
        });
    } catch (NoSuchCodeException e) {
        throw e; 
    } catch (Exception e) {
        if (e.getCause() instanceof NoSuchCodeException) {
            throw (NoSuchCodeException) e.getCause();
        }
        throw new RuntimeException("Error executing addPersonToGroup transaction", e);
    }
  }

  public Collection<String> listOfPeopleInGroup(String groupName) {
    return JPAUtil.executeInContext((ThrowingSupplier<Collection<String>, RuntimeException>) () -> {
        return groupRepository.findById(groupName)
            .map(group -> group.getMembers().stream()
                    .map(Person::getCode)
                    .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    });
  }

 
  public String personWithLargestNumberOfFriends() { 
    return JPAUtil.executeInContext((ThrowingSupplier<String, RuntimeException>) () -> {
        return personRepository.findAll().stream()
                .max(Comparator.comparingInt(p -> p.getFriends().size()))
                .map(Person::getCode)
                .orElse(null);
    });
  }

  public String largestGroup() {
    return JPAUtil.executeInContext((ThrowingSupplier<String, RuntimeException>) () -> {
        return groupRepository.findAll().stream()
                .max(Comparator.comparingInt(g -> g.getMembers().size()))
                .map(Group::getName)
                .orElse(null);
    });
  }


  public String personInLargestNumberOfGroups() {
    return JPAUtil.executeInContext((ThrowingSupplier<String, RuntimeException>) () -> {
      return personRepository.findAll().stream()
              .max(Comparator.comparingInt(p -> p.getGroups().size()))
              .map(Person::getCode)
              .orElse(null);
    });
  }

  public String post(String authorCode, String text) {
    Person author = personRepository.findById(authorCode)
        .orElse(null);
    
    if (author == null) {
        return null;
    }
    
    String pid = generatePostId();
    long timestamp = System.currentTimeMillis();
    
    Post post = new Post(pid, author, text, timestamp);
    
    JPAUtil.executeInTransaction(() -> {
        postRepository.save(post);
    });
    
    return pid;
  }

  public String getPostContent(String pid) {
    return postRepository.findByPid(pid)
            .map(Post::getContent)
            .orElse(null);
  }


  public long getTimestamp(String pid) {
    return postRepository.findByPid(pid)
            .map(Post::getTimestamp)
            .orElse(-1L);
  }

  public List<String> getPaginatedUserPosts(String author, int pageNo, int pageLength) {
    if (pageNo < 1 || pageLength <= 0) return Collections.emptyList();
    
    return postRepository.findPaginatedUserPosts(author, pageNo, pageLength);
  }

  public List<String> getPaginatedFriendPosts(String author, int pageNo, int pageLength) {
    if (pageNo < 1 || pageLength <= 0) return Collections.emptyList();
    
    return JPAUtil.executeInContext((ThrowingSupplier<List<String>, RuntimeException>) () -> {
        Person person = personRepository.findById(author)
                .orElse(null);
        
        if (person == null) {
            return Collections.emptyList();
        }
        
        List<String> friendCodes = person.getFriends().stream()
                .map(Person::getCode)
                .collect(Collectors.toList());
        
        if (friendCodes.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Object[]> friendPosts = postRepository.findPaginatedFriendPosts(friendCodes, pageNo, pageLength);
        
        return friendPosts.stream()
                .map(obj -> obj[0] + ":" + obj[1])
                .collect(Collectors.toList());
    });
  }
}