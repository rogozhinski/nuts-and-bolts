package ru.hh.nab.grizzly;

import com.google.common.collect.ImmutableSet;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.util.Set;
import ru.hh.nab.health.limits.LeaseToken;
import ru.hh.nab.health.limits.Limit;

public class HandlerDecorator implements RequestHandler {
  private final RequestHandler target;
  private final Set<HttpMethod> methods;
  private final Limit limit;

  public HandlerDecorator(RequestHandler target, HttpMethod[] methods, Limit limit) {
    this.target = target;
    this.methods = ImmutableSet.copyOf(methods);
    this.limit = limit;
  }

  public LeaseToken tryBegin() {
    return limit.acquire();
  }

  @Override
  public void handle(GrizzlyRequest request, GrizzlyResponse response) throws Exception {
    if (methods.contains(HttpMethod.valueOf(request.getRequest().method().getString())))
      target.handle(request, response);
    else
      SimpleGrizzlyAdapterChain.abstain(request);
  }
}
