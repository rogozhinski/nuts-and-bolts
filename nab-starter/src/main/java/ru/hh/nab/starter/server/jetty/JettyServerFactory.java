package ru.hh.nab.starter.server.jetty;

import static java.util.Optional.ofNullable;
import java.util.Collections;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import ru.hh.nab.common.properties.FileSettings;
import ru.hh.nab.starter.server.ServerContext;
import ru.hh.nab.starter.servlet.JerseyServletContextInitializer;
import ru.hh.nab.starter.servlet.ServletConfig;

import javax.servlet.Servlet;

public final class JettyServerFactory {

  public static JettyServer create(
      ServerContext jettyContext,
      ResourceConfig resourceConfig,
      ServletConfig servletConfig,
      JerseyServletContextInitializer servletContextInitializer) {

    ServletContainer servletContainer = createServletContainer(resourceConfig, servletConfig);
    ServletContextHandler contextHandler = createWebAppContextHandler(servletContainer, servletConfig, jettyContext, servletContextInitializer);
    return new JettyServer(jettyContext, contextHandler);
  }

  private static ServletContextHandler createWebAppContextHandler(Servlet mainServlet,
                                                                  ServletConfig servletConfig,
                                                                  ServerContext jettyContext,
                                                                  JerseyServletContextInitializer servletContextInitializer) {
    final FileSettings jettySettings = jettyContext.getSettings();
    boolean sessionEnabled = ofNullable(jettySettings.getBoolean("session-manager.enabled")).orElse(false);
    final ServletContextHandler contextHandler = new JettyWebAppContext(servletContextInitializer, sessionEnabled);
    final ServletHolder servletHolder = new ServletHolder("mainServlet", mainServlet);

    final ServletHandler servletHandler = new ServletHandler();
    servletHandler.addServletWithMapping(servletHolder, servletConfig.getServletMapping());
    contextHandler.setServletHandler(servletHandler);
    return contextHandler;
  }

  private static ServletContainer createServletContainer(ResourceConfig resourceConfig, ServletConfig servletConfig) {
    resourceConfig.addProperties(Collections.singletonMap(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE));
    servletConfig.registerResources(resourceConfig);
    return new ServletContainer(resourceConfig);
  }

  private JettyServerFactory() {
  }
}
