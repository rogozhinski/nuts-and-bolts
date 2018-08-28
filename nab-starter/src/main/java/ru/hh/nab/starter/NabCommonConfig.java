package ru.hh.nab.starter;

import static java.util.Optional.ofNullable;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import com.timgroup.statsd.StatsDClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hh.metrics.StatsDSender;
import ru.hh.nab.common.executor.ScheduledExecutor;
import ru.hh.nab.common.properties.FileSettings;

@Configuration
public class NabCommonConfig {
  static final String SERVICE_NAME_PROPERTY = "serviceName";

  @Bean
  String serviceName(FileSettings fileSettings) {
    return ofNullable(fileSettings.getString(SERVICE_NAME_PROPERTY))
        .orElseThrow(() -> new RuntimeException(String.format("'%s' property is not found in file settings", SERVICE_NAME_PROPERTY)));
  }

  @Bean
  FileSettings fileSettings(Properties serviceProperties) {
    return new FileSettings(serviceProperties);
  }

  @Bean
  ScheduledExecutorService scheduledExecutorService() {
    return new ScheduledExecutor();
  }

  @Bean
  StatsDSender statsDSender(ScheduledExecutorService scheduledExecutorService, StatsDClient statsDClient) {
    return new StatsDSender(statsDClient, scheduledExecutorService);
  }

  @Bean
  AppMetadata appMetadata(String serviceName) {
    return new AppMetadata(serviceName);
  }
}
