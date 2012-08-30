package com.camunda.fox.cycle.web.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This controller exposes the views offered by the application 
 * under <code>app/secured/view</code>. 
 * 
 * @author nico.rehwaldt
 */
@Path("secured/view")
public class ViewService extends AbstractRestService {
  
  @GET
	@Path("{name:[\\S]+}")
  @Produces(MediaType.TEXT_HTML)
	public String template(@PathParam("name") String name) {
    if (name.endsWith(".html")) {
      name = name.substring(0, name.lastIndexOf(".html"));
    }
    return "app/" + name;
  }

  
  // TODO: Rewrite change language via filter?
  //  private void changeLanguage(String language) {
  //    String[] localeSplit = getLangTag(request, language).split("-", 2);
  //    return templateEngine.process(name, getTemplateContext(variables, new Locale(localeSplit[0], localeSplit[1]))); 
  //    }
  //  }	
  //	private String getLangTag(HttpServletRequest request, String override) {
  //		String langTag = null;
  //		if (override != null) {
  //			langTag = override;
  //		}else {
  //			try {
  //				langTag = request.getHeader("Accept-Language").split(";")[0].split(",")[0];
  //			}catch(Exception e) {
  //				langTag = "de-DE";
  //			}
  //		}
  //		return langTag;
  //	}
}
