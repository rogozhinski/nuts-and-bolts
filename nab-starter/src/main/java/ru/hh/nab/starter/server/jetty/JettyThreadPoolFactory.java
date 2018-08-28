package ru.hh.nab.starter.server.jetty;

import static java.util.Optional.ofNullable;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import ru.hh.nab.common.properties.FileSettings;

public final class JettyThreadPoolFactory {

  public static ThreadPool createJettyThreadPool(FileSettings jettySettings) {
    int minThreads = ofNullable(jettySettings.getInteger("minThreads")).orElse(4);
    int maxThreads = ofNullable(jettySettings.getInteger("maxThreads")).orElse(12);
    int idleTimeoutMs = ofNullable(jettySettings.getInteger("threadPoolIdleTimeoutMs")).orElse(60_000);
    try {
      QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeoutMs, new BlockingArrayQueue<>(maxThreads));
      threadPool.start();
      return threadPool;
    } catch (Exception e) {
      throw new JettyServerException("Unable to create jetty thread pool", e);
    }
  }

  private JettyThreadPoolFactory() {
  }
}
