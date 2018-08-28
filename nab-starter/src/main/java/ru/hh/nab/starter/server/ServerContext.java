package ru.hh.nab.starter.server;

import org.eclipse.jetty.util.thread.ThreadPool;
import ru.hh.nab.common.properties.FileSettings;
import static ru.hh.nab.starter.server.jetty.JettyThreadPoolFactory.createJettyThreadPool;

public class ServerContext {
  private static final String JETTY_SETTINGS_PREFIX = "jetty";

  private final FileSettings settings;
  private final ThreadPool threadPool;

  public ServerContext(FileSettings fileSettings) {
    this.settings = fileSettings.getSubSettings(JETTY_SETTINGS_PREFIX);

    threadPool = createJettyThreadPool(fileSettings);
  }

  public FileSettings getSettings() {
    return settings;
  }

  public ThreadPool getThreadPool() {
    return threadPool;
  }
}
