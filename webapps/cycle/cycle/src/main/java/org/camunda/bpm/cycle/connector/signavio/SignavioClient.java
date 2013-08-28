package org.camunda.bpm.cycle.connector.signavio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.camunda.bpm.cycle.http.conn.ssl.X509HostnameVerifier;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.http.HttpException;
import org.camunda.bpm.cycle.http.HttpHost;
import org.camunda.bpm.cycle.http.HttpRequest;
import org.camunda.bpm.cycle.http.HttpRequestInterceptor;
import org.camunda.bpm.cycle.http.HttpResponse;
import org.camunda.bpm.cycle.http.HttpResponseInterceptor;
import org.camunda.bpm.cycle.http.ParseException;
import org.camunda.bpm.cycle.http.auth.AuthScope;
import org.camunda.bpm.cycle.http.auth.UsernamePasswordCredentials;
import org.camunda.bpm.cycle.http.client.HttpRequestRetryHandler;
import org.camunda.bpm.cycle.http.client.fluent.Content;
import org.camunda.bpm.cycle.http.client.fluent.Executor;
import org.camunda.bpm.cycle.http.client.fluent.Form;
import org.camunda.bpm.cycle.http.client.fluent.Request;
import org.camunda.bpm.cycle.http.client.utils.HttpClientUtils;
import org.camunda.bpm.cycle.http.client.utils.URIBuilder;
import org.camunda.bpm.cycle.http.conn.ConnectTimeoutException;
import org.camunda.bpm.cycle.http.conn.params.ConnRoutePNames;
import org.camunda.bpm.cycle.http.conn.scheme.Scheme;
import org.camunda.bpm.cycle.http.conn.scheme.SchemeRegistry;
import org.camunda.bpm.cycle.http.conn.ssl.SSLSocketFactory;
import org.camunda.bpm.cycle.http.entity.ContentType;
import org.camunda.bpm.cycle.http.entity.mime.HttpMultipartMode;
import org.camunda.bpm.cycle.http.entity.mime.MultipartEntity;
import org.camunda.bpm.cycle.http.entity.mime.content.FileBody;
import org.camunda.bpm.cycle.http.entity.mime.content.InputStreamBody;
import org.camunda.bpm.cycle.http.entity.mime.content.StringBody;
import org.camunda.bpm.cycle.http.impl.NoConnectionReuseStrategy;
import org.camunda.bpm.cycle.http.impl.client.DefaultHttpClient;
import org.camunda.bpm.cycle.http.impl.conn.PoolingClientConnectionManager;
import org.camunda.bpm.cycle.http.impl.conn.SchemeRegistryFactory;
import org.camunda.bpm.cycle.http.params.HttpConnectionParams;
import org.camunda.bpm.cycle.http.params.SyncBasicHttpParams;
import org.camunda.bpm.cycle.http.protocol.HttpContext;
import org.camunda.bpm.cycle.http.util.EntityUtils;
import org.camunda.bpm.cycle.util.IoUtil;

/**
 * Encapsulates all HTTP calls to Signavio/camunda modeler REST API using Apache HTTP 4 client.
 *  
 * @author christian.lipphardt@camunda.com
 */
public class SignavioClient {

  private static final Logger logger = Logger.getLogger(SignavioClient.class.getName());
  
  static final String SLASH_CHAR = "/";
  private static final String UTF_8 = "UTF-8";
  
  private static final int MAX_OPEN_CONNECTIONS_TOTAL = 20;
  private static final int MAX_OPEN_CONNECTIONS_PER_ROUTE = 5;
  private static final int CONNECTION_IDLE_CLOSE = 2000;
  private static final int CONNECTION_TIMEOUT = 3000;
  private static final int CONNECTION_TTL = 5000;
  private static final int RETRIES_CONNECTION_EXCEPTION = 1;

  private static final String WARNING_SNIPPET = "<div id=\"warning\">([^<]+)</div>";
  
  private static final String LOGIN_URL_SUFFIX = "login";
  private static final String REPOSITORY_BACKEND_URL_SUFFIX = "p";
  public static final String MODEL_URL_SUFFIX = "model";
  public static final String DIRECTORY_URL_SUFFIX = "directory";
  private static final String BPMN2_0_IMPORT_SUFFIX = "bpmn2_0-import";
  private static final String SGX_IMPORT_SUFFIX = "zip-import";
  
  private static final String HEADER_SIGNAVIO_SECURITY_TOKEN = "x-signavio-id";
  
  private String configurationName;
  private String signavioBaseUrl;
  private String proxyUrl;
  private String proxyUsername;
  private String proxyPassword;
  
  private DefaultHttpClient apacheHttpClient;
  private Executor requestExecutor;
  
  private String securityToken;

  private String defaultCommitMessage;

  
  public SignavioClient(String configurationName, String signavioBaseUrl, String proxyUrl, String proxyUsername, String proxyPassword, String defaultCommitMessage) throws URISyntaxException {
    this.configurationName = configurationName;
    this.signavioBaseUrl = signavioBaseUrl;
    this.proxyUrl = proxyUrl;
    this.proxyUsername = proxyUsername;
    this.proxyPassword = proxyPassword;
    this.defaultCommitMessage = defaultCommitMessage;
    initHttpClient();
  }
  
  public boolean login(String username, String password) {
    Form loginForm = Form.form();
    loginForm.add("name", username);
    loginForm.add("password", password);
    loginForm.add("tokenonly", "true");
    
    Request request = Request.Post(requestUrl(LOGIN_URL_SUFFIX))
                             .bodyForm(loginForm.build(), Charset.forName(UTF_8));
    HttpResponse response = executeAndGetResponse(request);
    
    String responseResult = extractResponseResult(response);
    if (responseResult == null || responseResult.equals("")) {
      throw new CycleException("Could not login into connector '" + configurationName + "'. The user name and/or password might be incorrect.");
    }
    Matcher matcher = Pattern.compile(WARNING_SNIPPET).matcher(responseResult);
    if (matcher.find()) {
      String errorMessage = matcher.group(1);
      throw new CycleException("Could not login into connector '" + configurationName + "'. " + errorMessage);
    }
    
    if (responseResult.matches("[a-f0-9]{32}")) {
      securityToken = responseResult;
      logger.fine("SecurityToken: " + securityToken);
    }
    
    return true;
  }
  
  public String getChildren(String dir) {
    Request request = Request.Get(requestUrl(DIRECTORY_URL_SUFFIX, dir))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType());
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public InputStream getXmlContent(String model) {
    try {
      Request request = Request.Get(requestUrl(MODEL_URL_SUFFIX, model, "bpmn2_0_xml"))
                               .addHeader("accept", ContentType.APPLICATION_XML.getMimeType());
      InputStream in = executeAndGetContent(request).asStream();
      return in;
    } catch (Exception e) {
      throw new CycleException(e.getMessage(), e);
    }
  }
  
  public InputStream getPngContent(String model) {
    try {
      Request request = Request.Get(requestUrl(MODEL_URL_SUFFIX, model, "png"))
                               .addHeader("accept", ContentType.APPLICATION_XML.getMimeType());
      InputStream in = executeAndGetContent(request).asStream();
      return in;
    } catch (Exception e) {
      throw new CycleException(e.getMessage(), e);
    }
  }
  
  public String getInfo(String type, String id) {
    Request request = Request.Get(requestUrl(type, id, "info"))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType());
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String getModelAsJson(String id) {
    Request request = Request.Get(requestUrl(MODEL_URL_SUFFIX, id, "json"))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType());
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String getModelAsSVG(String id) {
    Request request = Request.Get(requestUrl(MODEL_URL_SUFFIX, id, "svg"))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType());
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String delete(String type, String id) {
    Request request = Request.Delete(requestUrl(type, id))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType());
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String createModel(String parentId, String label, String message) {
    if(message == null || message.length() == 0) {
      message = defaultCommitMessage;
    }
    Form createModelForm = constructModelForm(constructCreateModelParams(parentId, label, message));
    Request request = Request.Post(requestUrl(MODEL_URL_SUFFIX))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType())
                             .bodyForm(createModelForm.build(), Charset.forName(UTF_8));
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String updateModel(String id, String label, String json, String svg, String parentId, String message) throws JSONException {
    if(message == null || message.length() == 0) {
      message = defaultCommitMessage;
    }
    Form updateModelForm = constructModelForm(constructUpdateModelParams(id, label, json, svg, parentId, message));
    Request request = Request.Put(requestUrl(MODEL_URL_SUFFIX, id))
            .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType())
            .bodyForm(updateModelForm.build(), Charset.forName(UTF_8));
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  private Map<String,String> constructCreateModelParams(String parentId, String label, String message) {
    HashMap<String, String> createModelParams = new HashMap<String, String>();
    InputStream emptyJson = null;
    
    try {
      createModelParams.put("id", UUID.randomUUID().toString().replace("-", ""));
      createModelParams.put("name", label);
      createModelParams.put("comment", message);
      createModelParams.put("description", "");
      createModelParams.put("parent", parentId);
  
      emptyJson = getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/cycle/connector/emptyProcessModelTemplate.json");
      createModelParams.put("json_xml", new String(IoUtil.readInputStream(emptyJson, "emptyProcessModelTemplate.json"), UTF_8));
      
      createModelParams.put("svg_xml", "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>");
      
      
    } catch (UnsupportedEncodingException e) {
      // nop
    } finally {
      IoUtil.closeSilently(emptyJson);
    }
    
    return createModelParams;
  }
  
  private Map<String, String> constructUpdateModelParams(String id, String name, String json, String svg, String parentId, String message) throws JSONException {
    HashMap<String, String> updateModelParams = new HashMap<String, String>();
    
    if (id.startsWith(SLASH_CHAR)) {
      id = id.substring(1);
    }
    updateModelParams.put("id", id);
    if (name == null) {
      name = "";
    }
    updateModelParams.put("name", name);
    updateModelParams.put("json_xml", new JSONObject(json).toString());
    if (svg == null || svg.isEmpty()) {
      svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>";
    }
    updateModelParams.put("svg_xml", svg);
    updateModelParams.put("comment", message);
    updateModelParams.put("description", "");
    updateModelParams.put("parent", parentId);
    
    return updateModelParams;
  }
  
  private Form constructModelForm(Map<String, String> modelParams) {
    Form createModelForm = Form.form();
    
    if (modelParams.containsKey("id")) {
      createModelForm.add("id", modelParams.get("id"));
    }
    createModelForm.add("name", modelParams.get("name"));
    createModelForm.add("comment", modelParams.get("comment"));
    createModelForm.add("description", modelParams.get("description"));
    createModelForm.add("json_xml", modelParams.get("json_xml"));
    createModelForm.add("svg_xml", modelParams.get("svg_xml"));
    
    createModelForm.add("parent", SLASH_CHAR + DIRECTORY_URL_SUFFIX + modelParams.get("parent"));
    
    createModelForm.add("glossary_xml", new JSONArray().toString());
    createModelForm.add("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
    createModelForm.add("type", "BPMN 2.0");
    
    return createModelForm;
  }

  public String createFolder(String name, String parent) {
    Form createFolderForm = Form.form();
    createFolderForm.add("name", name);
    createFolderForm.add("parent", SLASH_CHAR + DIRECTORY_URL_SUFFIX + parent);
    createFolderForm.add("description", "");
    
    Request request = Request.Post(requestUrl(DIRECTORY_URL_SUFFIX))
                             .addHeader("accept", ContentType.APPLICATION_JSON.getMimeType())
                             .bodyForm(createFolderForm.build(), Charset.forName(UTF_8));
    HttpResponse response = executeAndGetResponse(request);
    return extractResponseResult(response);
  }
  
  public String importBpmnXml(String parentFolderId, String content, String modelName) throws ParseException, IOException {
    MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    multipartEntity.addPart(DIRECTORY_URL_SUFFIX, new StringBody(SLASH_CHAR + DIRECTORY_URL_SUFFIX + parentFolderId , Charset.forName(UTF_8)));
    InputStream in = new ByteArrayInputStream(content.getBytes(UTF_8));
    InputStreamBody isb = new InputStreamBody(in, modelName);
    multipartEntity.addPart("bpmn2_0file", isb);
    
    Request request = Request.Post(requestUrl(BPMN2_0_IMPORT_SUFFIX))
                             .addHeader("accept", ContentType.MULTIPART_FORM_DATA.getMimeType())
                             .body(multipartEntity);
    HttpResponse response = executeAndGetResponse(request);
    
    // check if something went wrong on Signavio side
    if (response.getStatusLine().getStatusCode() >= 400) {
      logger.severe("Import of BPMN XML failed in Signavio.");
      logger.severe("Error response from server: " + EntityUtils.toString(response.getEntity(), UTF_8));
      throw new CycleException("BPMN XML could not be imported: " + content);
    }
    
    String responseStream = extractResponseResult(response);
    
    if (responseStream != null && !responseStream.startsWith("[true]") && !responseStream.contains("\"errors\":[]")) {
      throw new CycleException("BPMN XML could not be imported because of model errors: " + responseStream); 
    }
    
    return responseStream;
  }
  
  public String importSignavioArchive(String parentFolderId, String signavioArchive) throws ParseException, IOException {
    MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    
    multipartEntity.addPart(DIRECTORY_URL_SUFFIX, new StringBody(SLASH_CHAR + DIRECTORY_URL_SUFFIX + parentFolderId , Charset.forName(UTF_8)));
    multipartEntity.addPart("signavio-id", new StringBody(UUID.randomUUID().toString(), Charset.forName(UTF_8)));
    multipartEntity.addPart("file", new FileBody(new File(signavioArchive)));
    
    Request request = Request.Post(requestUrl(SGX_IMPORT_SUFFIX))
                             .addHeader("accept", ContentType.MULTIPART_FORM_DATA.getMimeType())
                             .body(multipartEntity);
    HttpResponse response = executeAndGetResponse(request);
    
    // check if something went wrong on Signavio side
    if (response.getStatusLine().getStatusCode() >= 400) {
      logger.severe("Import of BPMN XML failed in Signavio.");
      logger.severe("Error response from server: " + EntityUtils.toString(response.getEntity(), Charset.forName(UTF_8)));
      throw new CycleException("BPMN XML could not be imported: " + signavioArchive);
    }
    
    return extractResponseResult(response);
  }
  
  private void initHttpClient() throws URISyntaxException {
    // trust all certificates
    SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
    try {
      SSLContext sslContext = SSLContext.getInstance("SSL");

      X509TrustManager trustAllManager = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
      };
      
      // set up a TrustManager that trusts everything
      sslContext.init(new KeyManager[0], new TrustManager[] { trustAllManager }, new SecureRandom());
      SSLContext.setDefault(sslContext);
      
      SSLSocketFactory sslSF = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

      // set up an own X509HostnameVerifier because default one is still too strict.
      sslSF.setHostnameVerifier(new X509HostnameVerifier() {
        @Override
        public void verify(String s, SSLSocket sslSocket) throws IOException {}
        @Override
        public void verify(String s, X509Certificate x509Certificate) throws SSLException {}
        @Override
        public void verify(String s, String[] strings, String[] strings2) throws SSLException {}
        @Override
        public boolean verify(String s, SSLSession sslSession) {
          return true;
        }
      });

      schemeRegistry.register(new Scheme("https", 443, sslSF));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to modify SSLSocketFactory to allow self-signed certificates.", e);
    }

    // configure connection params
    SyncBasicHttpParams params = new SyncBasicHttpParams();
    DefaultHttpClient.setDefaultHttpParams(params);
    HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
    HttpConnectionParams.setStaleCheckingEnabled(params, true);
    HttpConnectionParams.setLinger(params, 5000);
    
    // configure thread-safe client connection management
    final PoolingClientConnectionManager connectionManager = 
            new PoolingClientConnectionManager(schemeRegistry, CONNECTION_TTL, TimeUnit.MILLISECONDS);
    connectionManager.setDefaultMaxPerRoute(MAX_OPEN_CONNECTIONS_PER_ROUTE);
    connectionManager.setMaxTotal(MAX_OPEN_CONNECTIONS_TOTAL);
    
    // configure and initialize apache httpclient
    apacheHttpClient = new DefaultHttpClient(connectionManager, params);
    
    // configure proxy stuff
    configureProxy();
    
    apacheHttpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
      private int retries = RETRIES_CONNECTION_EXCEPTION;
      
      @Override
      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (exception == null) {
          throw new IllegalArgumentException("Exception parameter may not be null");
        }
        if (context == null) {
          throw new IllegalArgumentException("HTTP context may not be null");
        }
        if (exception instanceof ConnectTimeoutException && retries > 0) {
          // Timeout
          retries--;
          return true;
        }

        return false;
      }
    });
    
    // close expired / idle connections and add securityToken header for each request
    apacheHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
      @Override
      public void process(HttpRequest request, HttpContext ctx) throws HttpException, IOException {
        connectionManager.closeExpiredConnections();
        connectionManager.closeIdleConnections(CONNECTION_IDLE_CLOSE, TimeUnit.MILLISECONDS);
        
        String uri = request.getRequestLine().getUri().toString();
        logger.fine("Sending request to " + uri);
        logger.fine("RequestHeaders: " + request.getAllHeaders());
        
        if (securityToken != null && !request.containsHeader(HEADER_SIGNAVIO_SECURITY_TOKEN)) {
          request.addHeader(HEADER_SIGNAVIO_SECURITY_TOKEN, securityToken);
        }
      }
    });
    
    apacheHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
      @Override
      public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        logger.fine("Received response with status " + response.getStatusLine().getStatusCode());
      }
    });
    
    apacheHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
    
    requestExecutor = Executor.newInstance(apacheHttpClient);
  }
  
  private void configureProxy() throws URISyntaxException {
    if (proxyUrl != null && !proxyUrl.isEmpty()) {
      URI proxyURI = new URI(proxyUrl);
      String proxyHost = proxyURI.getHost();
      int proxyPort = proxyURI.getPort();
      apacheHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
              new HttpHost(proxyHost, proxyPort));
      
      if (proxyUsername != null && !proxyUsername.isEmpty() && proxyPassword != null && !proxyPassword.isEmpty()) {
        apacheHttpClient.getCredentialsProvider().setCredentials(
                new AuthScope(proxyHost, proxyPort),
                new UsernamePasswordCredentials(proxyUsername, proxyPassword));
      }
      logger.fine("Configured signavio client with proxy settings: url: " + proxyUrl + ", proxyUsername: " + proxyUsername);
    }
    
    // enable usage of jvm specified proxy settings, e.g. -Dhttp.proxyHost=<my proxy> -Dhttp.proxyPort=<my proxy port>
//    ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
//            apacheHttpClient.getConnectionManager().getSchemeRegistry(),
//            ProxySelector.getDefault());  
//    apacheHttpClient.setRoutePlanner(routePlanner);
  }
  
  protected Content executeAndGetContent(Request request) {
    try {
      return requestExecutor.execute(request).returnContent();
    } catch (Exception e) {
      throw new CycleException("Connector '" + configurationName + "'", e);
    }
  }
  
  protected HttpResponse executeAndGetResponse(Request request) {
    try {
      return requestExecutor.execute(request).returnResponse();
    } catch (Exception e) {
      throw new CycleException("Connector '" + configurationName + "'", e);
    }
  }
  
  private URI signavioBackendBaseURI() throws URISyntaxException {
    URIBuilder builder = new URIBuilder(signavioBaseUrl);
    builder.setPath(SLASH_CHAR + REPOSITORY_BACKEND_URL_SUFFIX);
    return builder.build();
  }
  
  private URI requestUrl(String... pathArgs) {
    try {
      URIBuilder builder = new URIBuilder(signavioBackendBaseURI());
      StringBuffer sb = new StringBuffer();
      for (String pathArg : pathArgs) {
        if (!pathArg.startsWith(SLASH_CHAR)) {
          sb.append(SLASH_CHAR);
        }
        sb.append(pathArg);
      }
      builder.setPath(builder.getPath() + sb.toString());
      
      URI requestURI = builder.build();
      logger.fine(requestURI.toString());
      return requestURI;
    } catch (URISyntaxException e) {
      throw new CycleException("Failed to construct url for signavio request.", e);
    }
  }
  
  public void dispose() {
    apacheHttpClient.getConnectionManager().shutdown();
    requestExecutor = null;
  }
  
  private String extractResponseResult(HttpResponse response) {
    try {
      String payload = EntityUtils.toString(response.getEntity(), Charset.forName(UTF_8));
      if (payload.contains("An error occurred (unauthorized)")) {
        throw new CycleException("Could not login into connector '" + configurationName + "'. The user name and/or password might be incorrect.");
      }
      return payload;
    } catch (IOException e) {
      throw new CycleException(e.getMessage(), e);
    } finally {
      HttpClientUtils.closeQuietly(response);      
    }
  }
  
}
