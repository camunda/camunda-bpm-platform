package com.camunda.fox.cycle.impl.connector.signavio;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.Form;


public interface SignavioClient {

  
  @POST
  @Path("/login/")
  public Response login(@Form SignavioLoginForm form);
  
  @GET
  @Path("/directory{dir}")
  @Consumes(MediaType.APPLICATION_JSON)
  public String getChildren(@PathParam("dir") String dir);
  
  @GET
  @Path("/model/{model}/bpmn2_0_xml")
  @Consumes(MediaType.APPLICATION_XML)
  public InputStream getContent(@PathParam("model") String model);
  
}
