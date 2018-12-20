package ru.hh.nab.example;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@Path("/")
public class ExampleResource {

  @GET
  @Path("/hello")
  public String hello(@DefaultValue("world") @QueryParam("name") String name) {
    return String.format("Hello, %s!", name);
  }

  @GET
  @Path("/requestTimeout")
  @Produces({"text/plain"})
  public Response requestTimeout() throws InterruptedException {
    Thread.sleep(10000);
    return Response.ok(StringUtils.repeat("fsdfsdfsdfsdfsd", 10000)).build();
  }

  @XmlRootElement(name = "dto")
  public static class DTO {
    @XmlValue
    public String string;

    public DTO() {}

    public DTO(String string) {
      this.string = string;
    }
  }
}
