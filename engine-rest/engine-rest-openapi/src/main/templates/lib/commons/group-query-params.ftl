<!-- is this the right name? -->
<#assign sortByValues = [
  '"id"',
  '"name"',
  '"type"',
]>

<#assign params = {
  "id": {
    "type": "TODO",
    "desc": "Filter by the id of the group."
  },
  "idIn": {
    "type": "TODO",
    "desc": "Filter by a comma-separated list of group ids."
  },
  "name": {
    "type": "TODO",
    "desc": "Filter by the name of the group."
  },
  "nameLike": {
    "type": "TODO",
    "desc": "Filter by the name that the parameter is a substring of."
  },
  "type": {
    "type": "TODO",
    "desc": "Filter by the type of the group."
  },
  "member": {
    "type": "TODO",
    "desc": "Only retrieve groups which the given user id is a member of."
  },
  "memberOfTenant": {
    "type": "TODO",
    "desc": "Only retrieve groups which are members of the given tenant."
  }
}>