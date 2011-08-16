package ru.hh.nab.security;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.apache.commons.lang.StringUtils;

public class SecurityFilter implements ResourceFilter {
  private final PermissionLoader permissions;
  static final String REQUEST_PROPERTY_KEY = "ru.hh.nab.security.SecurityFilter.permissions";

  @Inject
  public SecurityFilter(PermissionLoader permissions) {
    this.permissions = permissions;
  }

  public class SecurityRequestFilter implements ContainerRequestFilter {
    @Override
    public ContainerRequest filter(ContainerRequest request) {
      String apiKey = request.getHeaderValue("X-Hh-Api-Key");

      Permissions p = null;
      if (!StringUtils.isEmpty(apiKey))
        p = permissions.forKey(apiKey);
      if (p == null)
        p = permissions.anonymous();

      request.getProperties().put(REQUEST_PROPERTY_KEY, p);
      return request;
    }
  }

  private final SecurityRequestFilter FILTER_INSTANCE = new SecurityRequestFilter();

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return FILTER_INSTANCE;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    return null;
  }
}