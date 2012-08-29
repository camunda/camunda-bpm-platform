/**
 * Copyright (C) 2011 camunda services GmbH (www.camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.camunda.fox.cycle.impl.connector.signavio;


/**
 * Helper class to set some SSL related options on the client
 */
public class SslUtil {

//  /**
//   * Ugly method to get the HttpClientHelper from a client, needed for
//   * allowAllHostNamesOnClient
//   * 
//   * @param client
//   * @return HttpClientHelper of the client
//   */
//  private static HttpClientHelper getHelperFromClient(Object client) {
//    try {
//      Method privateHelperMethod = client.getClass().getDeclaredMethod("getHelper", new Class< ? >[] {});
//      privateHelperMethod.setAccessible(true);
//
//      HttpClientHelper returnValue = (HttpClientHelper) privateHelperMethod.invoke(client, new Object[] {});
//      return returnValue;
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    return null;
//  }
//
//  /**
//   * sets the HostnameVerifier of the SSLSocketFactory to
//   * SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
//   * 
//   * @param client
//   */
//  public static void allowAllHostNamesOnClient(Client client) {
//    HttpClient httpClient = getHttpClient(client);
//    ClientConnectionManager clientConnectionManager = httpClient.getConnectionManager();
//    SchemeRegistry schemeRegistry = clientConnectionManager.getSchemeRegistry();
//    Scheme scheme = schemeRegistry.getScheme("https");
//    SSLSocketFactory sslSocketFactory = (SSLSocketFactory) scheme.getSocketFactory();
//    sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//  }
//
//  /**
//   * Retrieves the Apache HTTP Client from a Restlet Client using Reflection.
//   */
//  public static HttpClient getHttpClient(Client client) {
//    return getHelperFromClient(client).getHttpClient();
//  }

}
