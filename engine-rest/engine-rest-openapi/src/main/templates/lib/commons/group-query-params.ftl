<#assign sortByValues = [
  '"id"',
  '"name"',
  '"type"'
]>

<#if requestMethod == "GET">
  <#assign listTypeDescription = "comma seperated list of">
<#elseif requestMethod == "POST">
  <#assign listTypeDescription = "JSON string array of">
</#if>

<#assign params = {
  "id": {
    "type": "string",
    "desc": "Filter by the id of the group."
  },
  "idIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a ${listTypeDescription} group ids."
  },
  "name": {
    "type": "string",
    "desc": "Filter by the name of the group."
  },
  "nameLike": {
    "type": "string",
    "desc": "Filter by the name that the parameter is a substring of."
  },
  "type": {
    "type": "string",
    "desc": "Filter by the type of the group."
  },
  "member": {
    "type": "string",
    "desc": "Only retrieve groups where the given user id is a member of."
  },
  "memberOfTenant": {
    "type": "string",
    "desc": "Only retrieve groups which are members of the given tenant."
  }
}>
