package ru.hh.nab.hibernate.interceptor;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import ru.hh.nab.common.util.MDC;

public class ControllerPassingInterceptorTest {
  private final ControllerPassingInterceptor interceptor = new ControllerPassingInterceptor();

  @After
  public void tearDown() {
    MDC.deleteKey(MDC.CONTROLLER_MDC_KEY);
  }

  @Test
  public void controllerExistShouldReturnWithComment() {
    MDC.setKey(MDC.CONTROLLER_MDC_KEY, "resume");

    String originalSql = "select * from resume;";

    String sqlAfterPrepareStatement = interceptor.onPrepareStatement(originalSql);

    assertEquals("/* resume */" + originalSql, sqlAfterPrepareStatement);
  }

  @Test
  public void controllerExistAndHasStarShouldReturnWithComment() {
    MDC.setKey(MDC.CONTROLLER_MDC_KEY, "resume*");

    String originalSql = "select * from resume;";

    String sqlAfterPrepareStatement = interceptor.onPrepareStatement(originalSql);

    assertEquals("/* resume_ */" + originalSql, sqlAfterPrepareStatement);
  }

  @Test
  public void controllerDoesNotExistShouldReturnWithoutComment() {
    assertFalse(MDC.getController().isPresent());

    String originalSql = "select * from resume;";

    String sqlAfterPrepareStatement = interceptor.onPrepareStatement(originalSql);

    assertEquals(originalSql, sqlAfterPrepareStatement);
  }
}
