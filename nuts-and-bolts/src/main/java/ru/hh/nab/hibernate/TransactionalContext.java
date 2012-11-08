package ru.hh.nab.hibernate;

import com.google.common.base.Preconditions;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import java.util.concurrent.Callable;

class TransactionalContext {
  private final EntityManager em;
  private boolean readOnly = false;
  private EntityTransaction jpaTx = null; // null when not in transaction
  private PostCommitHooks postCommitHooks = null;
  private boolean runningPostCommitHooks = false;

  public TransactionalContext(EntityManager em) {
    this.em = em;
  }

  EntityManager getEntityManager() {
    // post commit hooks should never expect EntityManager to be available
    Preconditions.checkState(!runningPostCommitHooks, "Trying to get EntityManager while running commit hooks");
    return em;
  }

  PostCommitHooks getPostCommitHooks() {
    return postCommitHooks;
  }

  public void enter(Transactional ann) {
    if (!jpaTx.getRollbackOnly() && ann.rollback()) {
      throw new IllegalStateException("Can't execute (rollback() == true) tx while in (rollback() == false) tx");
    }
    if (readOnly && !ann.readOnly()) {
      em.setFlushMode(FlushModeType.AUTO);
      readOnly = false;
    }
  }

  public <T> T runInTransaction(Transactional ann, Callable<T> invocation) throws Exception {
    boolean committed = false;
    try {
      begin(ann);
      T result = invocation.call();
      commit();
      committed = true;
      return result;
    } catch (Exception e) {
      rollbackIfActive();
      throw e;
    } finally {
      try {
        if (committed) {
          runningPostCommitHooks = true;
          postCommitHooks.execute();
        }
      } finally {
        runningPostCommitHooks = false;
        reset();
      }
    }
  }

  public boolean inTransaction() {
    return jpaTx != null;
  }

  private void begin(Transactional ann) {
    postCommitHooks = new PostCommitHooks();
    readOnly = ann.readOnly();
    jpaTx = em.getTransaction();
    em.setFlushMode(readOnly ? FlushModeType.COMMIT : FlushModeType.AUTO);
    if (ann.rollback()) {
      jpaTx.setRollbackOnly();
    }
    jpaTx.begin();
  }

  private void commit() {
    jpaTx.commit();
  }

  private void rollbackIfActive() {
    if (jpaTx.isActive()) {
      jpaTx.rollback();
    }
  }

  private void reset() {
    jpaTx = null;
    em.setFlushMode(FlushModeType.AUTO);
    postCommitHooks = new PostCommitHooks();
  }
}
