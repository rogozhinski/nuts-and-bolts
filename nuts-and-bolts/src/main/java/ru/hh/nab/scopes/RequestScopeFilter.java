package ru.hh.nab.scopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.health.monitoring.TimingsLogger;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class RequestScopeFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestScopeFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    if (!(req instanceof HttpServletRequest && resp instanceof HttpServletResponse)) {
      throw new ServletException("non-HTTP request or response");
    }

    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) resp;

    RequestScope.enter(request, response);

    try {
      chain.doFilter(request, response);
      if (!request.isAsyncStarted()) {
        RequestScope.currentTimingsLogger().setResponseContext(String.valueOf(response.getStatus()));
      } else {
        request.getAsyncContext().addListener(new AsyncListener() {
          @Override
          public void onComplete(AsyncEvent event) throws IOException {
            timingsLogger().setResponseContext(String.valueOf(response.getStatus()));
          }

          @Override
          public void onTimeout(AsyncEvent event) throws IOException {
            timingsLogger().setErrorState();
            timingsLogger().setResponseContext("timeout");
          }

          @Override
          public void onError(AsyncEvent event) throws IOException {
            timingsLogger().setErrorState();
            timingsLogger().setResponseContext("500");
          }

          @Override
          public void onStartAsync(AsyncEvent event) throws IOException {
            LOGGER.error("Starting async request while already running async request");
          }
        });
      }
    } catch (IOException | ServletException | RuntimeException e) {
      timingsLogger().setErrorState();
      throw e;
    } finally {
      RequestScope.leave();
    }
  }

  @Override
  public void destroy() {
  }

  private static TimingsLogger timingsLogger() {
    return RequestScope.currentTimingsLogger();
  }
}
