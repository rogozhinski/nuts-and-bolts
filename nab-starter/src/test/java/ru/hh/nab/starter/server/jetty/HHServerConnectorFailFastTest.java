package ru.hh.nab.starter.server.jetty;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import ru.hh.nab.common.properties.FileSettings;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ru.hh.nab.starter.server.jetty.JettyServerFactory.createJettyThreadPool;

public class HHServerConnectorFailFastTest {
  private static final int WORKERS = 10;

  private static final ExecutorService executorService = Executors.newCachedThreadPool();
  private static final SimpleAsyncHTTPClient httpClient = new SimpleAsyncHTTPClient(executorService);

  private ThreadPool threadPool;
  private JettyServer server;

  @Before
  public void beforeTest() throws Exception {
    var properties = new Properties();
    properties.setProperty("host", "localhost");
    properties.setProperty("port", "0");
    properties.setProperty("minThreads", String.valueOf(WORKERS));
    properties.setProperty("maxThreads", String.valueOf(WORKERS));
    properties.setProperty("queueSize", "5");
    properties.setProperty("stopTimeoutMs", "0");
    properties.setProperty("acceptQueueSize", "1");
    properties.setProperty("lowResourceMonitorPeriodMs", "200");
    properties.setProperty("lowResourcesIdleTimeoutMs", "10");

    var fileSettings = new FileSettings(properties);
    var servletHandler = new ServletHandler();
    var servletContextHandler = new ServletContextHandler();

    servletHandler.addServletWithMapping(new ServletHolder("MainServlet", new WaitingServlet()), "/*");
    servletContextHandler.setServletHandler(servletHandler);

    threadPool = createJettyThreadPool(fileSettings);
    server = new JettyServer(threadPool, fileSettings, servletContextHandler);
    server.start();
  }

  @After
  public void afterTest() {
    server.stop();
  }

  @AfterClass
  public static void afterClass() {
    executorService.shutdown();
  }

  @Test
  public void testHHServerConnectorResetsNewIncomingConnectionIfLowOnThreads() throws Exception {
    int requests = WORKERS * 3;
    int successes = 0;
    int failures = 0;

    List<Socket> sockets = new ArrayList<>(requests);

    for (int i = 0; i < requests; i++) {
      try {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", server.getPort()), 100);
        sockets.add(socket);

        System.out.println(String.format("making request #%s", i + 1));
        httpClient.request(socket);

        Thread.sleep(200); // give {@link org.eclipse.jetty.server.LowResourceMonitor} time to launch
        successes++;
      } catch (SocketTimeoutException e) {
        failures++;
      }
    }

    assertFalse(((ServerConnector) server.getServer().getConnectors()[0]).isAccepting());
    assertTrue(threadPool.isLowOnThreads());
    assertTrue(successes > 0);
    assertTrue(failures > 0);

    for (Socket socket : sockets) {
      socket.close();
    }
  }

  static class WaitingServlet extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) {
      try {
        Thread.sleep(10_000);
      } catch (InterruptedException e) {
        //
      }
    }
  }
}
