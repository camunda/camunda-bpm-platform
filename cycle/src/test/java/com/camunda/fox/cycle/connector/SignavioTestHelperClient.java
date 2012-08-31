package com.camunda.fox.cycle.connector;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.Form;

import com.camunda.fox.cycle.impl.connector.signavio.SignavioClient;


public interface SignavioTestHelperClient extends SignavioClient {

  @POST
  @Path("/directory/")
  @Consumes(MediaType.APPLICATION_JSON)
  public String createFolder(@Form CreateSignavioFolderForm form);

  @POST
  @Path("/model/")
  @Consumes(MediaType.APPLICATION_JSON)
  public String createModel(@Form CreateSignavioModelForm form);
  
  @DELETE
  @Path("/directory{dir}")
  @Consumes(MediaType.APPLICATION_JSON)
  public String deleteFolder(@PathParam("dir") String dir);
  
}
