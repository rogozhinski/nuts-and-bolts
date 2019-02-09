package ru.hh.nab.starter.server.jetty;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * ServerConnector that:<br/>
 * - immediately closes new incoming connections if there is no idle thread in the main thread pool;<br/>
 * - waits for current requests to end before completing shutdown;<br/>
 */
public final class HHServerConnector extends ServerConnector {
  public HHServerConnector(@Name("server") Server server) {
    super(server);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("acceptors") int acceptors,
                           @Name("selectors") int selectors) {
    super(server, acceptors, selectors);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("acceptors") int acceptors,
                           @Name("selectors") int selectors,
                           @Name("factories") ConnectionFactory... factories) {
    super(server, acceptors, selectors, factories);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("factories") ConnectionFactory... factories) {
    super(server, factories);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("sslContextFactory") SslContextFactory sslContextFactory) {
    super(server, sslContextFactory);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("acceptors") int acceptors,
                           @Name("selectors") int selectors,
                           @Name("sslContextFactory") SslContextFactory sslContextFactory) {
    super(server, acceptors, selectors, sslContextFactory);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("sslContextFactory") SslContextFactory sslContextFactory,
                           @Name("factories") ConnectionFactory... factories) {
    super(server, sslContextFactory, factories);
  }

  public HHServerConnector(@Name("server") Server server,
                           @Name("executor") Executor executor,
                           @Name("scheduler") Scheduler scheduler,
                           @Name("bufferPool") ByteBufferPool bufferPool,
                           @Name("acceptors") int acceptors,
                           @Name("selectors") int selectors,
                           @Name("factories") ConnectionFactory... factories) {
    super(server, executor, scheduler, bufferPool, acceptors, selectors, factories);
  }

  @Override
  public Future<Void> shutdown() {
    super.shutdown();

    CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
    new ChannelsReadyChecker(shutdownFuture, this::getConnectedEndPoints, getScheduler()).run();
    return shutdownFuture;
  }
}
