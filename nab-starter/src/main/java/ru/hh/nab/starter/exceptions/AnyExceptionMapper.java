package ru.hh.nab.starter.exceptions;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.io.EofException;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.sql.SQLTransientConnectionException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.REQUEST_TIMEOUT;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static ru.hh.nab.starter.exceptions.NabExceptionMapper.LOW_PRIORITY;

@Provider
@Priority(LOW_PRIORITY)
public class AnyExceptionMapper extends NabExceptionMapper<Exception> {
  public AnyExceptionMapper() {
    super(INTERNAL_SERVER_ERROR, LoggingLevel.ERROR_WITH_STACK_TRACE);
  }

  @Override
  public Response toResponse(Exception exception) {
    List<Throwable> causes = ExceptionUtils.getThrowableList(exception);
    Throwable rootCause = causes.size() > 0 ? causes.get(causes.size() - 1) : null;

    if (causes.stream().anyMatch(e -> e instanceof EofException)) {
      statusCode = REQUEST_TIMEOUT;
      loggingLevel = LoggingLevel.INFO_WITHOUT_STACK_TRACE;

    } else if (exception instanceof SQLTransientConnectionException || rootCause instanceof SQLTransientConnectionException) {
      statusCode = SERVICE_UNAVAILABLE;
      loggingLevel = LoggingLevel.WARN_WITHOUT_STACK_TRACE;
    }

    return super.toResponse(exception);
  }
}
