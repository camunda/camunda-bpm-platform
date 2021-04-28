<#assign sortByValues = [
  'finished'
]>

<#assign params = {
  "decisionDefinitionIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by decision definition ids. Must be a comma-separated list of decision definition ids."
  },
  
  "decisionDefinitionKeyIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by decision definition keys. Must be a comma-separated list of decision definition keys."
  },
  
  "tenantIdIn": {
    "type": "array",
    "itemType": "string",
    "desc": "Filter by a comma-separated list of tenant ids. A decision definition must have one of the given tenant 
             ids."
  },
  
  "withoutTenantId": {
    "type": "boolean",
    "desc": "Only include decision definitions which belong to no tenant. Value may only be `true`, as `false` 
             is the default behavior."
  },
  
  "compact": {
    "type": "boolean",
    "desc": "Only include decision instances which have more than zero finished instances. Value may only be `true`, 
             as `false` is the default behavior."
  }
}>