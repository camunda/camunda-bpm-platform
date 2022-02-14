<#assign sortByValues = [
  '"id"',
  '"name"'
]>

<#assign params = {
  "id": {
    "type": "string",
    "desc": "Filter by the id of the tenant."
  },
  "name": {
    "type": "string",
    "desc": "Filter by the name of the tenant."
  },
  "nameLike": {
    "type": "string",
    "desc": "Filter by the name that the parameter is a substring of."
  },
  "userMember": {
    "type": "string",
    "desc": "Select only tenants where the given user is a member of."
  },
  "groupMember": {
    "type": "string",
    "desc": "Select only tenants where the given group is a member of."
  },
  "includingGroupsOfUser": {
    "type": "boolean",
    "desc": "Select only tenants where the user or one of his groups is a member of.
             Can only be used in combination with the `userMember` parameter. Value may only be `true`,
             as `false` is the default behavior."
  }
}>