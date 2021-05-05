<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-query/index.html -->

<#assign sortByValues = [
  '"id"',
  '"key"',
  '"name"',
  '"version"',
  '"deploymentId"',
  '"category"',
  '"tenantId"'
]>

<#assign params = {
  "decisionRequirementsDefinitionId": {
    "type": "string",
    "desc": "Filter by decision requirements definition id."
  },
  "decisionRequirementsDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by decision requirements definition ids."
  },
  "name": {
    "type": "string",
    "desc": "Filter by decision requirements definition name."
  },
  "nameLike": {
    "type": "string",
    "desc": "Filter by decision requirements definition names that the parameter is a substring of."
  },
  "deploymentId": {
    "type": "string",
    "desc": "Filter by the id of the deployment a decision requirement definition belongs to."
  },
  "key": {
    "type": "string",
    "desc": "Filter by decision requirements definition key, i.e., the id in the DMN 1.3 XML. Exact
             match."
  },
  "keyLike": {
    "type": "string",
    "desc": "Filter by decision requirements definition keys that the parameter is a substring of."
  },
  "category": {
    "type": "string",
    "desc": "Filter by decision requirements definition category. Exact match."
  },
  "categoryLike": {
    "type": "string",
    "desc": "Filter by decision requirements definition categories that the parameter is a substring
             of."
  },
  "version": {
    "type": "integer",
    "format": "int32",
    "desc": "Filter by decision requirements definition version."
  },
  "latestVersion": {
    "type": "boolean",
    "desc": "Only include those decision requirements definitions that are latest versions. Value may
             only be `true`, as `false` is the default behavior."
  },
  "resourceName": {
    "type": "string",
    "desc": "Filter by the name of the decision requirements definition resource. Exact match."
  },
  "resourceNameLike": {
    "type": "string",
    "desc": "Filter by names of those decision requirements definition resources that the parameter is
             a substring of."
  },
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids. A decision requirements definition must
             have one of the given tenant ids."
  },
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include decision requirements definitions which belong to no tenant. Value may only
             be `true`, as `false` is the default behavior."
  },
  "includeDecisionRequirementsDefinitionsWithoutTenantId": {
    "type": "boolean",
    "desc": "Include decision requirements definitions which belong to no tenant. Can be used in
             combination with `tenantIdIn`. Value may only be `true`, as `false` is the
             default behavior."
  }
}>
