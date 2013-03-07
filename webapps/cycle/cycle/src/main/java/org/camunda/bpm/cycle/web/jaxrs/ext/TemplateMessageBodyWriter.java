package org.camunda.bpm.cycle.web.jaxrs.ext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.spring3.context.SpringWebContext;
import org.xnap.commons.i18n.I18n;

/**
 * Renders Strings returned by controllers as templates when 
 * <code>text/html</code> or <code>text/xhtml+xml</code> is requested as a content type. 
 * 
 * Exceptions thrown by the templating engine are properly translated to responses via a {@link TemplateExceptionMapper}. 
 * 
 * @author nico.rehwaldt
 * 
 * @see TemplateExceptionMapper
 * @see TemplateMessageBodyWriter
 */
@Provider
@Produces({"text/html", "application/xhtml+xml"})
public class TemplateMessageBodyWriter implements MessageBodyWriter<String> {

  private static final String TPL_PREFIX = "tpl:";
  
  @Inject
  private TemplateEngine templateEngine;
  
  @Inject
  private ApplicationContext applicationContext;
  
  @Inject
  private I18n i18n;
  
  @Context
  private HttpServletRequest request;
  
  @Context
  private HttpServletResponse response;

  /**
   * We can process only objects for which templates exist.
   *
   * @param type
   * @param t
   * @param annotations
   * @param mediaType
   * @return
   */
  @Override
  public boolean isWriteable(Class<?> type, Type t, Annotation[] annotations, MediaType mediaType) {
    if (type.isAssignableFrom(String.class) && mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
      return true;
    }
    return false;
  }

  /**
   * Return the size of the response. We do not know upfront, that is why we return -1.
   *
   * @param t
   * @param type
   * @param type1
   * @param antns
   * @param mt
   * @return
   */
  @Override
  public long getSize(String t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
    return -1l;
  }

  @Override
  public void writeTo(String t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
    if (t.startsWith(TPL_PREFIX)) {
      String templateName = t.replaceFirst(TPL_PREFIX, "");
      templateName = removeEndSlash(templateName);
      writeTemplate(templateName, new HashMap<String, Object>(), new OutputStreamWriter(out));
    } else {
      writeString(t, new OutputStreamWriter(out));
    }
  }
  
  private String removeEndSlash(String templateName) {
    if (templateName.endsWith("/")) {
      return templateName.substring(0, templateName.length() - 1);
    }
    return templateName; 
  }
  
  private void writeString(String s, OutputStreamWriter writer) throws IOException {
    writer.write(s, 0, s.length());
    writer.flush();
  }
  
  private void writeTemplate(String name, Map<String, Object> model, OutputStreamWriter writer) throws IOException {
    
    // Always expose current url to model
    // Needed to encode that information for angular JS
    model.put("currentUrl", getRealRequestUri(request));
    model.put("contextPath", request.getContextPath());
    
    Locale locale = setLocale(request);
    
    IWebContext context = new SpringWebContext(
      request,
      response,
      request.getSession().getServletContext(),
      locale,
      model,
      applicationContext);

    templateEngine.process(name, context, writer);

    writer.flush();
  }
  
  /**
   * Get real request uri in case this is a forward.
   * 
   * @param request
   * @return 
   */
  private String getRealRequestUri(HttpServletRequest request) {
    String uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
    if (uri == null) {
      uri = request.getRequestURI();
    }
    
    return uri;
  }
  
  private Locale setLocale(HttpServletRequest request) {
    String langTag = getLangTag(request, request.getParameter("lang"));
    Locale locale = null;
    
    if (langTag != null && langTag.contains("-")) {
      String[] localeSplit = langTag.split("-");
      locale = new Locale(localeSplit[0], localeSplit[1]);
    }else if (langTag != null) {
      locale = new Locale(langTag);
    } else {
      locale = Locale.getDefault();
    }
    
    i18n.setLocale(locale);
    
    return locale;
  }

  private String getLangTag(HttpServletRequest request, String override) {
    if (override != null && override.equalsIgnoreCase("de")) {
      return "de-DE";
    }
    
    String langTag = null;
    if (override != null && override.contains("-")) {
      langTag = override;
    } else {
      if ( request.getHeader("Accept-Language") == null) {
        return "de-DE";
      }
      
      try {
        langTag = request.getHeader("Accept-Language").split(";")[0].split(",")[0];
      } catch (Exception e) {
        langTag = "de-DE";
      }
    }
    return langTag;
  }
  
}
