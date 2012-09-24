package com.camunda.fox.cycle.connector.signavio;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
  @Path("/model{model}/bpmn2_0_xml")
  @Consumes(MediaType.APPLICATION_XML)
  public InputStream getContent(@PathParam("model") String model);
  
  @GET
  @Path("/model{model}/png")
  @Consumes(MediaType.APPLICATION_XML)
  public InputStream getPngContent(@PathParam("model") String model);
  
  @GET
  @Path("/{type}{id}/info")
  @Consumes(MediaType.APPLICATION_JSON)
  public String getInfo(@PathParam("type") String type, @PathParam("id") String id);
  
  @GET
  @Path("/model{id}/json")
  @Consumes(MediaType.APPLICATION_JSON)
  public String getJson(@PathParam("id") String id);
  
  @GET
  @Path("/model{id}/svg")
  @Consumes(MediaType.APPLICATION_JSON)
  public String getSVG(@PathParam("id") String id);
  
  @DELETE
  @Path("/{type}{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public String delete(@PathParam("type") String type, @PathParam("id") String id);
  
  @PUT
  @Path("/model/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public String updateModel(@PathParam("id") String id, @Form SignavioCreateModelForm form);
  
  @POST
  @Path("/model/")
  @Consumes(MediaType.APPLICATION_JSON)
  public String createModel(@Form SignavioCreateModelForm form);
  
  @POST
  @Path("/directory/")
  @Consumes(MediaType.APPLICATION_JSON)
  public String createFolder(@Form SignavioCreateFolderForm form);
  
}
