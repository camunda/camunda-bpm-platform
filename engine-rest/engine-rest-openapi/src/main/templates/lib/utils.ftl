<#macro parameter name location type desc
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

    "description": "${desc?replace('\n( ){2,}', '\n', 'r')}"
  }

  <#if !last> , </#if> <#-- if not a last parameter add a comma-->
</#macro>

<#macro property name type
        desc=""
        enumValues=[]
        defaultValue="" <#-- it will work for boolean, integer, string -->
        minimum=""
        required=false
        deprecated=false
        additionalProperties=false
        itemType="string"
        dto=""
        format="none"
        addProperty=""
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

        <#if addProperty?has_content>
          ${addProperty},
        </#if>

        "description": "${desc?replace('\n( ){2,}', '\n', 'r')}"
      </#if>
    }

    <#if !last> , </#if> <#-- if not a last property add a comma-->
</#macro>

<#macro requestBody mediaType dto
        requestDesc=""
        examples=[] >
  "requestBody" : {

    <#if requestDesc?has_content >
      "description": "${requestDesc?replace('\n( ){2,}', '\n', 'r')}",
    </#if>

    "content" : {
      "${mediaType}" : {

        "schema" : {
          "$ref" : "#/components/schemas/${dto}"
        }

        <#if examples?size != 0>,
          
          "examples": {
            ${examples?join(", ")}
          }
        </#if>
      }
    }
  },
</#macro>

<#macro response code desc
        dto="ExceptionDto"
        array=false
        additionalProperties=false
        examples=[]
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
             <#if examples?size != 0>,
               "examples": {
                 ${examples?join(", ")}
               }
             </#if>
           }
         },
       </#if>

       "description": "${desc?replace('\n( ){2,}', '\n', 'r')}"
     }

    <#if !last> , </#if> <#-- if not a last response, add a comma-->
</#macro>

<#macro server
        url
        variables
        desc
        last = false >
    {
      "url": "${url}",
      "description":  "${desc?replace('\n( ){2,}', '\n', 'r')}",
      "variables": {
        <#list variables as name, default>
          ${name}: {
            "default": "${default}"
          }<#sep>,
        </#list>
      }
    }
    <#if !last> , </#if> <#-- if not the last entry, add a comma -->
</#macro>

<#macro endpointInfo
        id
        tag
        desc >
    "operationId": "${id}",
    "tags": [
      "${tag}"
    ],
    "description": "${desc?replace('\n( ){2,}', '\n', 'r')}",
</#macro>