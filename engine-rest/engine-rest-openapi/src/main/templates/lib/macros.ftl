<#macro parameter name location type
        enum=false enumValues='""'
        hasDefault=false defaultValue=false
        required=false description="TODO" last=false >
  {
    "name": "${name}",
    "in": "${location}",
    "schema": {

      <#if enum>
        "enum": [
          ${enumValues?join(", ")}
        ],
      </#if>

      <#if hasDefault>
        "default": ${defaultValue?c}, <#-- ?c to print the value-->
      </#if>

      "type": "${type}"
    },

    <#if required>
      "required": true,
    </#if>

    "description": "${description}"
  }

  <#if !last> , </#if> <#-- if not a last parameter add a comma-->
</#macro>

<#macro property name type description="TODO"
        enum=false enumValues='""'
        hasDefault=false defaultValue=false
        hasMinimum=false minimum=0
        required=false
        deprecated=false
        additionalProperties=false
        itemType="string" dto=""
        format="none"
        last=false >
    "${name}": {

      <#if type="ref">
        "$ref": "#/components/schemas/${dto}"
      <#else>
        "type": "${type}",

        <#if format!="none">
          "format": ${format},
        </#if>

        <#if enum>
          "enum": [
            ${enumValues?join(", ")}
          ],
        </#if>

        <#if hasDefault>
          "default": ${defaultValue?c}, <#-- ?c to print the value-->
        </#if>

        <#if hasMinimum>
          "minimum": ${minimum},
        </#if>

        <#if required>
          "required": true,
        </#if>

        <#if deprecated>
          "deprecated": true,
        </#if>

        <#if type="array">
          "items": {
            <#if dto="">
              "type": "${itemType}"
            <#else>
              "$ref": "#/components/schemas/${dto}"
            </#if>
          },
        </#if>

        <#if additionalProperties>
          "additionalProperties": {
            "$ref": "#/components/schemas/${dto}"
          },
        </#if>

        "description": "${description}"
      </#if>
    }

    <#if !last> , </#if> <#-- if not a last property add a comma-->
</#macro>

<#macro requestBody dto >
  "requestBody" : {
    "content" : {
      "application/json" : {
        "schema" : {
          "$ref" : "#/components/schemas/${dto}"
        }
      }
    }
  },
</#macro>

<#macro response code desc 
        dto="ExceptionDto"
        array=false
        additionalProperties=false 
        last=false >
    "${code}": {

       <#if code!="204">
         "content": {
           "application/json": {
             "schema": {

               <#if array>
                 "type" : "array",
                 "items" : {
               </#if>

               <#if additionalProperties>
                 "type" : "object",
                 "additionalProperties": {
               </#if>

                 "$ref": "#/components/schemas/${dto}"

               <#if array || additionalProperties >
                 }
               </#if>

             }
           }
         },
       </#if>

       "description": "${desc}"
     }

    <#if !last> , </#if> <#-- if not a last response add a comma-->
</#macro>