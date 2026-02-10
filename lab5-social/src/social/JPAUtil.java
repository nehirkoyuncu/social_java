package social;

import java.sql.SQLException;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * Utility class to interact with the JPA factory classes.
 * <p>
 * This class is used by {@link GenericRepository} to create
 * and release the the {@link jakarta.persistence.EntityManager}
 * for the db.
 */
public class JPAUtil {

  private static final String TEST_PU_NAME = "socialPUTest";
  private static final String PU_NAME = "socialPU";

  private static EntityManagerFactory emf;
  private static String currentPUName = JPAUtil.PU_NAME;

  private static final ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(()->false);
  private static final ThreadLocal<EntityManager> currentManager = ThreadLocal.withInitial(()->null);

  private JPAUtil(){ /* Cannot instantiate! */}

  private static EntityManagerFactory getCurrentFactory() {
    if (emf == null || !emf.isOpen()) {
      emf = Persistence.createEntityManagerFactory(currentPUName);
    }
    return emf;
  }

  /**
   * Enable test mode: the db is deleted on close.
   */
  public static void setTestMode() {
    try {
      org.h2.tools.Server.createWebServer("-web","-webDaemon");
      IO.println("H2 console monitor available at http://localhost:8082");
    } catch (SQLException e) {
        IO.println("Could not start H2 monitor: " + e.getMessage());
    }
    currentPUName = JPAUtil.TEST_PU_NAME;
  }


  /**
   * Retrieves an entity manager to perform persistence operations.
   * 
   * @return entity manager object
   */
  public static EntityManager getEntityManager() {
    EntityManager currentEm = currentManager.get();
    if(currentEm == null || !currentEm.isOpen()){
      IO.println("*** new EntityManager");
      currentEm = getCurrentFactory().createEntityManager();
      currentManager.set(currentEm);
    }
    return currentEm;
  }

  public static void closeEntityManager(){
    EntityManager currentEm = currentManager.get();
    if(currentEm!=null && currentEm.isOpen() && !inTransaction.get()){
      currentEm.close();
      currentManager.remove();
    }
  }

  /**
   * Performs an action potentially returning a value with the provided {@link EntityManager}.
   * This method is intended to be used by the repositories to perform their activities.
   * <p>
   * Unless in a transaction or in a context closes the entity manager at the end.
   * 
   * @param <T> the result type if any, otherwise it is assumed to be {@link Void}
   * @param action  the action to be performed with the provided entity manager
   * @return the result of the action
   */
  public static <T> T withEntityManager(Function<EntityManager, T> action){
    EntityManager em = getEntityManager();
    try{
      return action.apply(em);
    }finally{
      closeEntityManager();
    }
  }

  /**
   * Represents an operation that consumes a value and can possibly throw an exception.
   *
   * <p>This is a functional interface whose functional method is {@link #accept}.
   * 
   * @param <T> the type of results supplied by this supplier
   * @param <X> the type of possible exception thrown by this supplier
   */
  @FunctionalInterface
  public interface ThrowingConsumer<T,X extends Exception> {
    void accept(T t) throws X;
  }
  /**
   * Performs an action within a transaction with the provided {@link EntityManager}.
   * <p>
   * Unless in a transaction or in a context closes the entity manager at the end.
   * 
   * @param action the action to be performed within the transaction
   */
  public static <X extends Exception> void transaction(ThrowingConsumer<EntityManager,X> action) throws X {
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();
    if(!inTransaction.get()){
      inTransaction.set(true);
      try(em){
          tx.begin();
          action.accept(em);
          tx.commit();
      } catch (Exception ex) {
        if (tx.isActive())
          tx.rollback();
        throw ex;
      } finally {
        inTransaction.remove();
      }
    }else{
      action.accept(em);
    }
  }

  /**
   * Represents an operation that does not return a result and can possibly throw an exception.
   * 
   * <p>This is a functional interface whose functional method is {@link #run}.
   * 
   * @param <X> the type of possible exception thrown by this runnable
   */
  @FunctionalInterface
  public interface ThrowingRunnable<X extends Exception> {
    void run() throws X;
  }

  /**
   * Allows executing a series of operations all within a single transaction,
   * with a unique entity manager.
   * <p>
   * The entity manager begins before performing the {@code action} and it is
   * committed when it is completed.
   * <p>
   * This method is intended to be used by the business logic to carry
   * on a set of related operations within a unique transaction, while 
   * keeping all entities attached.
   * 
  * @param <E> the possible exception thrown by the action
   * @param action the action to be performed within the transaction
   * @throws E it is re-thrown from the action, if any
   */
  public static <E extends Exception> void executeInTransaction(ThrowingRunnable<E> action) throws E {
    if(inTransaction.get()){
      throw new IllegalStateException("Nested call to executeInTransaction");
    }
    inTransaction.set(true);
    EntityManager em = getEntityManager();
    EntityTransaction tx = em.getTransaction();
    try{
      tx.begin();
      action.run();
      tx.commit();
    } catch(Exception e){
      if (tx.isActive())
        tx.rollback();
      throw e;
    } finally {
      inTransaction.remove();
      closeEntityManager();
    }
  }

  /**
   * Represents a supplier of results that can possibly throw an exception.
   * 
   * <p>This is a functional interface whose functional method is {@link #get()}.
   * 
   * @param <T> the type of results supplied by this supplier
   * @param <X> the type of possible exception thrown by this supplier
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T, X extends Exception> {
    T get() throws X;
  }
  /**
   * Allows executing a series of operations with a unique entity manager.
   * <p>
   * The entity manager is closed when the {@code} is completed.
   * <p>
   * This method is intended to be used by the business logic to carry
   * on a set of operations, while keeping all entities attached an
   * allowing lazy loading.
   * 
   * @param <T> the possible return type of the action
   * @param <E> the possible exception thrown by the action
   * @param action the action to be performed within the transaction
   * @throws E it is re-thrown from the action, if any
   */

  public static <T, E extends Exception> T executeInContext(ThrowingSupplier<T,E> action) throws E {
    EntityManager em = getEntityManager();
    if(!inTransaction.get()){
      inTransaction.set(true);
      try(em) {
          return action.get();
      } finally {
        inTransaction.remove();
      }
    }else{
      return action.get();
    }
  }

  /**
   * Closes the entity manager factory
   */
  public static void close() {
    if (emf!=null && emf.isOpen()) {
      emf.close();
    }
  }
}