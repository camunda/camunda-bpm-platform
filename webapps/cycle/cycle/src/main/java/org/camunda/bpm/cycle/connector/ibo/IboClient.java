package org.camunda.bpm.cycle.connector.ibo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.signavio.SignavioClient;
import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.http.HttpException;
import org.camunda.bpm.cycle.http.HttpHost;
import org.camunda.bpm.cycle.http.HttpRequest;
import org.camunda.bpm.cycle.http.HttpRequestInterceptor;
import org.camunda.bpm.cycle.http.HttpResponse;
import org.camunda.bpm.cycle.http.HttpResponseInterceptor;
import org.camunda.bpm.cycle.http.auth.AuthScope;
import org.camunda.bpm.cycle.http.auth.UsernamePasswordCredentials;
import org.camunda.bpm.cycle.http.client.HttpRequestRetryHandler;
import org.camunda.bpm.cycle.http.client.fluent.Executor;
import org.camunda.bpm.cycle.http.client.fluent.Form;
import org.camunda.bpm.cycle.http.client.fluent.Request;
import org.camunda.bpm.cycle.http.client.utils.HttpClientUtils;
import org.camunda.bpm.cycle.http.conn.ConnectTimeoutException;
import org.camunda.bpm.cycle.http.conn.params.ConnRoutePNames;
import org.camunda.bpm.cycle.http.conn.scheme.Scheme;
import org.camunda.bpm.cycle.http.conn.scheme.SchemeRegistry;
import org.camunda.bpm.cycle.http.conn.ssl.SSLSocketFactory;
import org.camunda.bpm.cycle.http.conn.ssl.X509HostnameVerifier;
import org.camunda.bpm.cycle.http.impl.NoConnectionReuseStrategy;
import org.camunda.bpm.cycle.http.impl.client.DefaultHttpClient;
import org.camunda.bpm.cycle.http.impl.conn.PoolingClientConnectionManager;
import org.camunda.bpm.cycle.http.impl.conn.SchemeRegistryFactory;
import org.camunda.bpm.cycle.http.params.HttpConnectionParams;
import org.camunda.bpm.cycle.http.params.SyncBasicHttpParams;
import org.camunda.bpm.cycle.http.protocol.HttpContext;
import org.camunda.bpm.cycle.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.DatatypeConverter;

class IboClient {
	private static final Logger logger = Logger.getLogger(SignavioClient.class.getName());

	private static final String UTF_8 = "UTF-8";

	private static final int MAX_OPEN_CONNECTIONS_TOTAL = 20;
	private static final int MAX_OPEN_CONNECTIONS_PER_ROUTE = 5;
	private static final int CONNECTION_IDLE_CLOSE = 2000;
	private static final int CONNECTION_TIMEOUT = 3000;
	private static final int CONNECTION_TTL = 5000;
	private static final int RETRIES_CONNECTION_EXCEPTION = 1;

	private DefaultHttpClient apacheHttpClient;
	private Executor requestExecutor;

	private String configurationName;
	private String iboBaseUrl;
	private String proxyUrl;
	private String proxyUsername;
	private String proxyPassword;
	private String defaultCommitMessage;

	private String username;
	private String password;

	private String sessionId;

	public IboClient(String configurationName, String iboBaseUrl, String proxyUrl, String proxyUsername,
			String proxyPassword, String defaultCommitMessage) throws URISyntaxException {
		this.configurationName = configurationName;
		this.iboBaseUrl = iboBaseUrl;
		this.proxyUrl = proxyUrl;
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
		this.defaultCommitMessage = defaultCommitMessage;

		initHttpClient();
	}

	public boolean relogin() {
		if (username != null && password != null) {
			return loginWithHashPassword(username, password);
		} else {
			return false;
		}
	}

	public boolean login(String username, String password) {

		try {
			byte[] passwordHash = password.getBytes(UTF_8);
			MessageDigest md = MessageDigest.getInstance("MD5");
			passwordHash = md.digest(passwordHash);
			password = Hex.encodeHexString(passwordHash);
			password = username.toUpperCase() + ":" + password;

			passwordHash = password.getBytes(UTF_8);
			passwordHash = md.digest(passwordHash);

			password = Hex.encodeHexString(passwordHash);
			return loginWithHashPassword(username, password);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean loginWithHashPassword(String username, String password) {
		this.username = username;
		this.password = password;

		Form form = Form.form();
		form.add("applicationName", "CamundaCycle");
		form.add("culture", "de-DE");
		form.add("username", username);
		form.add("password", password);

		String responseText = ExecuteCommand(form, "PAP", "LoginWithSessionStart");

		boolean success = false;

		try {
			JSONObject json = new JSONObject(responseText);
			success = IboJson.extractSuccess(json);
			sessionId = IboJson.extractSessionId(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return success;
	}

	protected HttpResponse executeAndGetResponse(Request request) {
		try {
			return requestExecutor.execute(request).returnResponse();
		} catch (Exception e) {
			throw new CycleException("Connector '" + configurationName + "'", e);
		}
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
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			};

			// set up a TrustManager that trusts everything
			sslContext.init(new KeyManager[0], new TrustManager[] { trustAllManager }, new SecureRandom());
			SSLContext.setDefault(sslContext);

			SSLSocketFactory sslSF = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// set up an own X509HostnameVerifier because default one is still
			// too strict.
			sslSF.setHostnameVerifier(new X509HostnameVerifier() {
				@Override
				public void verify(String s, SSLSocket sslSocket) throws IOException {
				}

				@Override
				public void verify(String s, X509Certificate x509Certificate) throws SSLException {
				}

				@Override
				public void verify(String s, String[] strings, String[] strings2) throws SSLException {
				}

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
		final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry,
				CONNECTION_TTL, TimeUnit.MILLISECONDS);
		connectionManager.setDefaultMaxPerRoute(MAX_OPEN_CONNECTIONS_PER_ROUTE);
		connectionManager.setMaxTotal(MAX_OPEN_CONNECTIONS_TOTAL);

		// configure and initialize apache httpclient
		apacheHttpClient = new DefaultHttpClient(connectionManager, params);

		configureProxy();

		// configure proxy stuff
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

		// close expired / idle connections and add securityToken header for
		// each request
		apacheHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext ctx) throws HttpException, IOException {
				connectionManager.closeExpiredConnections();
				connectionManager.closeIdleConnections(CONNECTION_IDLE_CLOSE, TimeUnit.MILLISECONDS);

				String uri = request.getRequestLine().getUri().toString();
				logger.fine("Sending request to " + uri);
				logger.fine("RequestHeaders: " + request.getAllHeaders());
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

	private String getRequestUrl(String programmCode, String cmd) {
		StringBuilder sb = new StringBuilder();

		sb.append(iboBaseUrl);

		if (!iboBaseUrl.endsWith("/")) {
			sb.append("/");
		}
		sb.append("ExecuteCommand");
		sb.append("?");
		sb.append("programmCode=").append(programmCode);
		sb.append("&");
		sb.append("command=").append(cmd);

		return sb.toString();
	}

	private String extractResponseResult(HttpResponse response) {
		try {
			String payload = EntityUtils.toString(response.getEntity(), Charset.forName(UTF_8));
			if (payload.contains("An error occurred (unauthorized)")) {
				throw new CycleException("Could not login into connector '" + configurationName
						+ "'. The user name and/or password might be incorrect.");
			}
			return payload;
		} catch (IOException e) {
			throw new CycleException(e.getMessage(), e);
		} finally {
			HttpClientUtils.closeQuietly(response);
		}
	}

	public String getChildren(ConnectorNode parent) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("id", parent.getId());

		return ExecuteCommand(form, "PR", "RestGetChildren");
	}

	public InputStream getPngFile(String id) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("id", id);

		String data = ExecuteCommand(form, "PR", "RestGetPng");

		try {
			JSONObject obj = new JSONObject(data);
			if (obj.has("base64Data")) {
				byte[] imageByte = DatatypeConverter.parseBase64Binary(obj.getString("base64Data"));
				return new ByteArrayInputStream(imageByte);
			}
		} catch (JSONException ex) {
			return null;
		}
		return null;
	}

	public String getRootNode() {
		Form form = Form.form();
		form.add("sessionId", sessionId);

		return ExecuteCommand(form, "PR", "RestGetRoot");
	}

	public String getContentInformation(ConnectorNode node) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("id", node.getId());

		return ExecuteCommand(form, "PR", "RestGetContentInformation");
	}

	private String ExecuteCommand(Form form, String programmCode, String command) {
		return ExecuteCommand(form, programmCode, command, true);
	}

	private String ExecuteCommand(Form form, String programmCode, String command, boolean doRelogin) {
		Request request = Request.Post(getRequestUrl(programmCode, command)).bodyForm(form.build(), Charset.forName(UTF_8));

		HttpResponse response = executeAndGetResponse(request);
		String responseText = extractResponseResult(response);
		if (response.getStatusLine().getStatusCode() == 500) {
			try {
				JSONObject obj = new JSONObject(responseText);
				if (obj.has("errorType")) {
					if (obj.get("errorType").equals("SessionNotFound")) {
						if (doRelogin && relogin()) {
							return ExecuteCommand(form, programmCode, command, false);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		return responseText;
	}

	public InputStream getXmlFile(String id) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("id", id);

		String data = ExecuteCommand(form, "PR", "RestGetXml");

		try {
			JSONObject obj = new JSONObject(data);
			if (obj.has("xmlData")) {
				byte[] xmlBytes = DatatypeConverter.parseBase64Binary(obj.getString("xmlData"));
				return new ByteArrayInputStream(xmlBytes);
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	private void configureProxy() throws URISyntaxException {
		if (proxyUrl != null && !proxyUrl.isEmpty()) {
			URI proxyURI = new URI(proxyUrl);
			String proxyHost = proxyURI.getHost();
			int proxyPort = proxyURI.getPort();
			apacheHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, proxyPort));

			if (proxyUsername != null && !proxyUsername.isEmpty() && proxyPassword != null && !proxyPassword.isEmpty()) {
				apacheHttpClient.getCredentialsProvider().setCredentials(new AuthScope(proxyHost, proxyPort),
						new UsernamePasswordCredentials(proxyUsername, proxyPassword));
			}
			logger.fine("Configured ibo client with proxy settings: url: " + proxyUrl + ", proxyUsername: " + proxyUsername);
		}
	}

	public String updateContent(ConnectorNode node, InputStream newContent, String message) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		if (message == null || message.isEmpty())
			message = defaultCommitMessage;
		String nodeStr = convertConnectorNodeToJsonObject(node).toString();
		form.add("node", nodeStr);

		try {
			byte[] content = IOUtils.toByteArray(newContent);
			String strContent = DatatypeConverter.printBase64Binary(content);
			form.add("newContent", strContent);
		} catch (Exception e) {
			e.printStackTrace();
		}

		form.add("message", message);
		return ExecuteCommand(form, "PR", "RestUpdateContent");
	}

	private JSONObject convertConnectorNodeToJsonObject(ConnectorNode node) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("connectorNodeType", node.getType().toString());
			obj.put("id", node.getId());
			obj.put("label", node.getLabel());
			obj.put("message", node.getMessage());

			Date created = node.getCreated();
			if (created == null) {
				obj.put("created", JSONObject.NULL);
			} else {
				obj.put("created", created.getTime());
			}

			Date lastModified = node.getLastModified();
			if (lastModified == null) {
				obj.put("lastModified", JSONObject.NULL);
			} else {
				obj.put("lastModified", lastModified.getTime());
			}

			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String createFolder(String parentId, String label, String message) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("parentId", parentId);
		form.add("label", label);
		if (message == null || message.isEmpty())
			message = defaultCommitMessage;
		form.add("message", message);

		return ExecuteCommand(form, "PR", "RestCreateFolder");
	}

	public String createFile(String parentId, String label, String message) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		form.add("parentId", parentId);
		form.add("label", label);

		if (message == null || message.isEmpty())
			message = defaultCommitMessage;
		form.add("message", message);

		return ExecuteCommand(form, "PR", "RestCreateFile");
	}

	public void deleteFolder(String id, String message) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		if (message == null || message.isEmpty())
			message = defaultCommitMessage;
		form.add("message", message);
		form.add("id", id);

		ExecuteCommand(form, "PR", "RestDeleteFolder");
	}

	public void deleteFile(String id, String message) {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		if (message == null || message.isEmpty())
			message = defaultCommitMessage;
		form.add("message", message);
		form.add("id", id);

		ExecuteCommand(form, "PR", "RestDeleteFile");
	}

	public void endSession() {
		Form form = Form.form();
		form.add("sessionId", sessionId);
		ExecuteCommand(form, "PAP", "EndSession");
	}

	public void dispose() {
		apacheHttpClient.getConnectionManager().shutdown();
		requestExecutor = null;
	}

}