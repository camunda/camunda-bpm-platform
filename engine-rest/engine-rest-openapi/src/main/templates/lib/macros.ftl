<#macro parameter name location type typeDefault=false typeDefaultValue=false required=false description="TODO">
  {
    "name": "${name}",
    "in": "${location}",
    "schema": {
      "type": "${type}"
      <#if typeDefault>
        ,"default": ${typeDefaultValue?c} <#-- ?c to print the value-->
      </#if>
    },
    <#if required>
      "required": true,
    </#if>
    "description": "${description}"
  }
</#macro>
<#-- TODO arrays -->

<#macro response responseCode refDto desc>
    "${responseCode}": {
       "description": "${desc}",
       "content": {
         "application/json": {
           "schema": {
             "$ref": "#/components/schemas/${refDto}"
           }
         }
       }
     }
</#macro>