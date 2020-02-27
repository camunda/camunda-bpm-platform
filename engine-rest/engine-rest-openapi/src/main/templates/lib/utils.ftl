<#macro parameter name location type description
        enumValues=[]
        defaultValue="" <#-- it will work for boolean, integer, string -->
        required=false
        last=false >
  {
    "name": "${name}",
    "in": "${location}",
    "schema": {

      <#if enumValues?size != 0>
        "enum": [
          ${enumValues?join(", ")}
        ],
      </#if>

      <#if defaultValue?has_content>
        "default": ${defaultValue},
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

<#macro property name type description
        enumValues=[]
        defaultValue="" <#-- it will work for boolean, integer, string -->
        minimum=""
        required=false
        deprecated=false
        additionalProperties=false
        itemType="string"
        dto=""
        format="none"
        last=false >
    "${name}": {

      <#if type == "ref">
        "$ref": "#/components/schemas/${dto}"
      <#else>
        "type": "${type}",

        <#if format != "none">
          "format": "${format}",
        </#if>

        <#if enumValues?size != 0>
          "enum": [
            ${enumValues?join(", ")}
          ],
        </#if>

        <#if defaultValue?has_content>
          "default": ${defaultValue},
        </#if>

        <#if minimum?has_content>
          "minimum": ${minimum},
        </#if>

        <#if required>
          "required": true,
        </#if>

        <#if deprecated>
          "deprecated": true,
        </#if>

        <#if type == "array">
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

<#macro requestBody mediaType dto
        requestDescription="" >
  "requestBody" : {

    <#if requestDescription?has_content >
      "description": "${requestDescription}",
    </#if>

    "content" : {
      "${mediaType}" : {
        "schema" : {
          "$ref" : "#/components/schemas/${dto}"
        }
      }
    }
  },
</#macro>

<#macro response code description
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

       "description": "${description}"
     }

    <#if !last> , </#if> <#-- if not a last response add a comma-->
</#macro>

<#macro server
        url
        variables
        description
        last = false >
    {
      "url": "${url}",
      "description":  "${description}",
      "variables": {
        <#list variables as name, default>
          ${name}: {
            "default": "${default}"
          }<#sep>,
        </#list>
      }
    }
    <#if !last> , </#if> <#-- if not a last response add a comma-->
</#macro>
