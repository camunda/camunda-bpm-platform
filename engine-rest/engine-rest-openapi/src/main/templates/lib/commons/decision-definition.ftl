<#assign sortByValues = [
  '"category"',
  '"decisionRequirementsDefinitionKey"',
  '"key"',
  '"id"',
  '"name"',
  '"version"',
  '"deploymentId"',
  '"deployTime"',
  '"versionTag"',
  '"tenantId"'
]>

<#if requestMethod == "GET">
  <#assign listTypeDescription = "Filter by a comma-separated list of `Strings`." />
<#elseif requestMethod == "POST">
  <#assign listTypeDescription = "Must be a JSON array of `Strings`">
</#if>

<#assign params = {
  "decisionDefinitionId": {
    "type": "string",
    "desc": "Filter by decision definition id."
  },

  "decisionDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by decision definition ids."
  },

  "name": {
    "type": "string",
    "desc": "Filter by decision definition name."
  },

  "nameLike": {
    "type": "string",
    "desc": "Filter by decision definition names that the parameter is a substring of."
  },

  "deploymentId": {
    "type": "string",
    "desc": "Filter by the deployment the id belongs to."
  },

  "deployedAfter": {
    "type": "string",
    "format": "date-time",
    "desc": "Filter by the deploy time of the deployment the decision definition belongs to.
             Only selects decision definitions that have been deployed after (exclusive) a specific time."
  },

  "deployedAt": {
    "type": "string",
    "format": "date-time",
    "desc": "Filter by the deploy time of the deployment the decision definition belongs to.
             Only selects decision definitions that have been deployed at a specific time (exact match)."
  },

  "key": {
    "type": "string",
    "desc": "Filter by decision definition key, i.e., the id in the DMN 1.0 XML. Exact match."
  },

  "keyLike": {
    "type": "string",
    "desc": "Filter by decision definition keys that the parameter is a substring of."
  },

  "category": {
    "type": "string",
    "desc": "Filter by decision definition category. Exact match."
  },

  "categoryLike": {
    "type": "string",
    "desc": "Filter by decision definition categories that the parameter is a substring of."
  },

  "version": {
    "type": "integer",
    "format": "int32",
    "desc": "Filter by decision definition version."
  },

  "latestVersion": {
    "type": "boolean",
    "desc": "Only include those decision definitions that are latest versions.
             Value may only be `true`, as `false` is the default behavior."
  },

  "resourceName": {
    "type": "string",
    "desc": "Filter by the name of the decision definition resource. Exact match."
  },

  "resourceNameLike": {
    "type": "string",
    "desc": "Filter by names of those decision definition resources that the parameter is a substring of."
  },

  "decisionRequirementsDefinitionId": {
    "type": "string",
    "desc": "Filter by the id of the decision requirements definition this decision definition belongs to."
  },

  "decisionRequirementsDefinitionKey": {
    "type": "string",
    "desc": "Filter by the key of the decision requirements definition this decision definition belongs to."
  },

  "withoutDecisionRequirementsDefinition": {
    "type": "boolean",
    "desc": "Only include decision definitions which does not belongs to any decision requirements definition.
             Value may only be `true`, as `false` is the default behavior."
  },

  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "${listTypeDescription} A decision definition must have one of the given tenant ids."
  },

  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include decision definitions which belong to no tenant.
             Value can effectively only be `true`, as `false` is the default behavior."
  },

  "includeDecisionDefinitionsWithoutTenantId": {
    "type": "boolean",
    "desc": "Include decision definitions which belong to no tenant.
             Can be used in combination with `tenantIdIn`.
             Value may only be `true`, as `false` is the default behavior."
  },

  "versionTag": {
    "type": "string",
    "desc": "Filter by the version tag."
  },

  "versionTagLike": {
    "type": "string",
    "desc": "Filter by the version tags of those decision definition resources that the parameter is a substring of."
  }
}>