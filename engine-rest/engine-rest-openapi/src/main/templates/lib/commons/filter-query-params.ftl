<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/get-query/index.html -->

<#assign sortByValues = [
  '"filterId"',
  '"resourceType"',
  '"name"',
  '"owner"'
]>

 
<#assign params = {
  "filterId": {
    "type": "string",
    "desc": "Filter by the id of the filter."
  },
  "resourceType": {
    "type": "string",
    "desc": "Filter by the resource type of the filter, e.g., `Task`."
  },
  "name": {
    "type": "string",
    "desc": "Filter by the name of the filter."
  },
  "nameLike": {
    "type": "string",
    "desc": "Filter by the name that the parameter is a substring of."
  },
  "owner": {
    "type": "string",
    "desc": "Filter by the user id of the owner of the filter."
  }
}>
