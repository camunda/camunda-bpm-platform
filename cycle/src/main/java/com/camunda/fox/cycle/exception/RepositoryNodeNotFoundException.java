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
package com.camunda.fox.cycle.exception;

import com.camunda.fox.cycle.api.connector.ConnectorNode;


/**
 * Exception to indicate a requested node was not found
 * 
 * @author ruecker
 */
public class RepositoryNodeNotFoundException extends RepositoryException {

  private static final long serialVersionUID = 1L;

  public static String createNodeNotFoundMessage(String repositoryName, Class<ConnectorNode> artifactType, String artifactId) {
    return artifactType.getSimpleName() + " with id '" + artifactId + "' not found in repository '" + repositoryName + "'";
  }
  public static String createChildrenNotFoundMessage(String repositoryName, Class<ConnectorNode> artifactType, String artifactId) {
    return "Children for " + artifactType.getSimpleName() + " with id '" + artifactId + "' couldn't be loaded in repository '" + repositoryName + "'";
  }

  public RepositoryNodeNotFoundException(String repositoryName, Class<ConnectorNode> artifactType, String artifactId) {
    super(createNodeNotFoundMessage(repositoryName, artifactType, artifactId));
  }

  public RepositoryNodeNotFoundException(String repositoryName, Class<ConnectorNode> artifactType, String artifactId, Throwable cause) {
    super(createNodeNotFoundMessage(repositoryName, artifactType, artifactId), cause);
  }

  public RepositoryNodeNotFoundException(String msg) {
    super(msg);
  }

  public RepositoryNodeNotFoundException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
