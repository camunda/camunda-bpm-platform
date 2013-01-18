package org.camunda.bpm.engine.rest.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

public class ProcessDefinitionQueryDto extends SortableParameterizedQueryDto {

  private static final String SORT_BY_CATEGORY_VALUE = "category";
  private static final String SORT_BY_KEY_VALUE = "key";
  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_VERSION_VALUE = "version";
  private static final String SORT_BY_DEPLOYMENT_ID_VALUE = "deploymentId";
  
  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_CATEGORY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VERSION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOYMENT_ID_VALUE);
  }
  
  private String category;
  private String categoryLike;
  private String name;
  private String nameLike;
  private String deploymentId;
  private String key;
  private String keyLike;
  private Integer version;
  private Boolean latestVersion;
  private String resourceName;
  private String resourceNameLike;
  private String startableBy;
  private Boolean active;
  private Boolean suspended;
  
  @CamundaQueryParam("category")
  public void setCategory(String category) {
    this.category = category;
  }
  
  @CamundaQueryParam("categoryLike")
  public void setCategoryLike(String categoryLike) {
    this.categoryLike = categoryLike;
  }
  
  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }
  
  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }
  
  @CamundaQueryParam("deploymentId")
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  @CamundaQueryParam("key")
  public void setKey(String key) {
    this.key = key;
  }
  
  @CamundaQueryParam("keyLike")
  public void setKeyLike(String keyLike) {
    this.keyLike = keyLike;
  }
  
  @CamundaQueryParam("ver")
  public void setVersion(Integer version) {
    this.version = version;
  }
  
  @CamundaQueryParam("latest")
  public void setLatestVersion(Boolean latestVersion) {
    this.latestVersion = latestVersion;
  }
  
  @CamundaQueryParam("resourceName")
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  
  @CamundaQueryParam("resourceNameLike")
  public void setResourceNameLike(String resourceNameLike) {
    this.resourceNameLike = resourceNameLike;
  }
  
  @CamundaQueryParam("startableBy")
  public void setStartableBy(String startableBy) {
    this.startableBy = startableBy;
  }
  
  @CamundaQueryParam("active")
  public void setActive(Boolean active) {
    this.active = active;
  }
  
  @CamundaQueryParam("suspended")
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }
  
  /**
   * Creates a {@link ProcessDefinitionQuery} against the given {@link RepositoryService}.
   * @param repositoryService
   * @return
   */
  public ProcessDefinitionQuery toQuery(RepositoryService repositoryService) {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    if (category != null) {
      query.processDefinitionCategory(category);
    }
    if (categoryLike != null) {
      query.processDefinitionCategoryLike(categoryLike);
    }
    if (name != null) {
      query.processDefinitionName(name);
    }
    if (nameLike != null) {
      query.processDefinitionNameLike(nameLike);
    }
    if (deploymentId != null) {
      query.deploymentId(deploymentId);
    }
    if (key != null) {
      query.processDefinitionKey(key);
    }
    if (keyLike != null) {
      query.processDefinitionKeyLike(keyLike);
    }
    if (version != null) {
      query.processDefinitionVersion(version);
    }
    if (latestVersion != null && latestVersion == true) {
      query.latestVersion();
    }
    if (resourceName != null) {
      query.processDefinitionResourceName(resourceName);
    }
    if (resourceNameLike != null) {
      query.processDefinitionResourceNameLike(resourceNameLike);
    }
    if (startableBy != null) {
      query.startableByUser(startableBy);
    }
    if (active != null && active == true) {
      query.active();
    }
    if (suspended != null && suspended == true) {
      query.suspended();
    }
    
    if (!sortOptionsValid()) {
      throw new InvalidRequestException("You may not specify a single sorting parameter.");
    }
    
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_CATEGORY_VALUE)) {
        query.orderByProcessDefinitionCategory();
      } else if (sortBy.equals(SORT_BY_KEY_VALUE)) {
        query.orderByProcessDefinitionKey();
      } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
        query.orderByProcessDefinitionId();
      } else if (sortBy.equals(SORT_BY_VERSION_VALUE)) {
        query.orderByProcessDefinitionVersion();
      } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
        query.orderByProcessDefinitionName();
      } else if (sortBy.equals(SORT_BY_DEPLOYMENT_ID_VALUE)) {
        query.orderByDeploymentId();
      }
    }
    
    if (sortOrder != null) {
      if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
        query.asc();
      } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
        query.desc();
      }
    }
    
    return query;
  }
  
  @Override
  public void setPropertyFromParameterPair(String key, String value) {
    try {
      if (key.equals("active") || key.equals("suspended") || key.equals("latest")) {
        Boolean booleanValue = new Boolean(value);
        setValueBasedOnAnnotation(key, booleanValue);
      } else if (key.equals("ver")) {
        Integer intValue = new Integer(value);
        setValueBasedOnAnnotation(key, intValue);
      } else {
        setValueBasedOnAnnotation(key, value);
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidRequestException("Cannot set parameter.");
    } catch (IllegalAccessException e) {
      throw new RestException("Cannot set parameter.");
    } catch (InvocationTargetException e) {
      throw new InvalidRequestException(e.getTargetException().getMessage());
    }
  }

}
