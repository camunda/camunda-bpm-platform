
<#assign sortByValues = [
  '"resourceType"',
  '"resourceId"'
]>

<#assign params = {
  "id": {
    "type": "string",
    "desc": "Filter by the id of the authorization."
  },
  "type": {
    "type": "integer",
    "format": "int32",
    "desc": "Filter by authorization type. (0=global, 1=grant, 2=revoke).
             See the [User Guide](${docsUrl}/user-guide/process-engine/authorization-service/#authorization-type)
             for more information about authorization types."
  },
  "userIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of userIds."
  },
  "groupIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of groupIds."
  },
  "resourceType": {
    "type": "integer",
    "format": "int32",
    "desc": "Filter by an integer representation of the resource type. See the
             [User Guide](${docsUrl}/user-guide/process-engine/authorization-service/#resources)
             for a list of integer representations of resource types."
  },
  "resourceId": {
    "type": "string",
    "desc": "Filter by resource id."
  }
}>
