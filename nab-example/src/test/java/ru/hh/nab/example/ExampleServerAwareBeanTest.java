package ru.hh.nab.example;

import java.util.function.Function;
import javax.inject.Inject;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import ru.hh.nab.testbase.NabTestBase;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(
  classes = {
      ExampleTestConfig.class,
      ExampleServerAwareBeanTest.Config.class
  },
  loader = NabTestBase.ContextInjectionAnnotationConfigWebContextLoader.class)
public class ExampleServerAwareBeanTest extends NabTestBase {

  @Inject
  private Function<String, String> serverPortAwareBean;

  @Test
  public void testBeanWithNabTestContext() {
    try (Response response = createRequestFromAbsoluteUrl(serverPortAwareBean.apply("/hello")).get()) {
      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      assertEquals("Hello, world!", response.readEntity(String.class));
    }
  }

  static class Config {
    @Bean
    Function<String, String> serverPortAwareBean(NabTestBase.NabTestContext ctx) {
      return path -> ctx.baseUrl() + path;
    }
  }
}
