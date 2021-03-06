package ru.hh.nab.datasource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hh.metrics.StatsDSender;
import ru.hh.nab.datasource.monitoring.MetricsTrackerFactoryProvider;

@Configuration
public class NabDataSourceProdConfig {
  @Bean
  DataSourceFactory dataSourceFactory(String serviceName, StatsDSender statsDSender) {
    return new DataSourceFactory(new MetricsTrackerFactoryProvider(serviceName, statsDSender));
  }
}
