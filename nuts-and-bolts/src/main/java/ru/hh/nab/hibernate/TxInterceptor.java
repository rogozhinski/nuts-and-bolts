package ru.hh.nab.hibernate;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import java.util.concurrent.Callable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TxInterceptor implements MethodInterceptor {

  private ThreadLocal<TransactionalContext> txHolder = new ThreadLocal<TransactionalContext>();

  private Provider<EntityManagerFactory> emf;

  public TxInterceptor(Provider<EntityManagerFactory> emf) {
    this.emf = emf;
  }

  public <T> T invoke(Transactional ann, Callable<T> invocation) throws Exception {
    TransactionalContext tx = txHolder.get();
    // Is transaction context already initialized (i.e. have we already
    // encountered Transactional annotation) ?
    if (tx != null) {
      if (tx.inTransaction()) {
        // continue previously started transaction
        tx.enter(ann);
        return invocation.call();
      } else if (ann.optional()) {
        // not in transaction, and no need to start transaction
        return invocation.call();
      } else {
        // not in transaction, need to start transaction
        return tx.runInTransaction(ann, invocation);
      }
    }

    EntityManager em = null;
    try {
      // create entity manager instance and init new context
      em = emf.get().createEntityManager();
      tx = new TransactionalContext(em);
      txHolder.set(tx);

      // call the callback...
      if (ann.optional()) {
        // ...without transaction
        return invocation.call();
      } else {
        // ...with transaction
        return tx.runInTransaction(ann, invocation);
      }

    } finally {
      // release entity manager and remove transaction context object
      // if we have created them in this call
      if (em != null) {
        txHolder.remove();
        em.close();
      }
    }
  }

  @Override
  public Object invoke(final MethodInvocation invocation) throws Throwable {
    return invoke(
        invocation.getMethod().getAnnotation(Transactional.class),
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            try {
              return invocation.proceed();
            } catch (Throwable throwable) {
              if (throwable instanceof Exception)
                throw (Exception)throwable;
              else
                throw new RuntimeException(throwable);
            }
          }
        });
  }

  public EntityManager currentEntityManager() {
    TransactionalContext tx = txHolder.get();
    Preconditions.checkState(tx != null, "Not under transaction annotation");
    return tx.getEntityManager();
  }

  public PostCommitHooks currentPostCommitHooks() {
    TransactionalContext tx = txHolder.get();
    Preconditions.checkState(tx != null && tx.inTransaction(), "Not in transaction");
    return tx.getPostCommitHooks();
  }

}
