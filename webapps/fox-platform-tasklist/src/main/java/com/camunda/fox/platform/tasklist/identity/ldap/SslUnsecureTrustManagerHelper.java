package com.camunda.fox.platform.tasklist.identity.ldap;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslUnsecureTrustManagerHelper {

  /**
   * set global environment to ignore missing self signed certificate. Aceepts
   * ALL certificates now. I guess server-wide. Dangerous!!
   */
  public static void accepptAllSSLCertificates() {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
      SSLContext.setDefault(sslContext);
    } catch (Exception ex) {
      throw new RuntimeException("Could not change SSL TrustManager to accept arbitray certificates", ex);
    }
  }

  private static class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

  }
}
