package ru.hh.nab.starter.exceptions;

import org.glassfish.jersey.server.internal.process.MappableException;

import javax.ws.rs.core.Response;

public class EofExceptionMapper extends NabExceptionMapper<MappableException> {
  public EofExceptionMapper() {
    super(Response.Status.REQUEST_TIMEOUT, LoggingLevel.WARN_WITHOUT_STACK_TRACE);
  }
}
