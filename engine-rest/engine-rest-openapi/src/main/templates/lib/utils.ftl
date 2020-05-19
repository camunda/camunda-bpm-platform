<#-- Generates a Query Parameter JSON object -->
<#macro parameter name location type desc
        enumValues=[]
        defaultValue="" <#-- it will work for boolean, integer, string -->
        format=""
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

      <#if format?has_content>,
        "format": "${format}"
      </#if>
    },

    <#if required>
      "required": true,
    </#if>

    "description": "${removeIndentation(desc)}"
  }

  <#if !last> , </#if> <#-- if not a last parameter add a comma-->
</#macro>

<#-- Generates a DTO Property JSON object -->
<#macro property name type
        desc=""
        enumValues=[]
        defaultValue="" <#-- it will work for boolean, integer, string -->
        nullable=true
        minimum=""
        deprecated=false
        additionalProperties=false
        itemType="string"
        dto=""
        format=""
        addProperty=""
        last=false >
    "${name}": {

      <#if type == "ref">
        "$ref": "#/components/schemas/${dto}"
      <#else>
        "type": "${type}",

        <#if format?has_content>
          "format": "${format}",
          <#if nullable>
            "nullable": true,
          </#if>
        </#if>

        <#if type == "boolean">
          <#if nullable>
            "nullable": true,
          </#if>
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

        "description": "${removeIndentation(desc)}"
      </#if>
    }

    <#if !last> , </#if> <#-- if not a last property add a comma-->
</#macro>

<#-- Generates a DTO JSON object -->
<#macro dto
        type="object"
        title=""
        desc=""
        required=[]
        extends="" >
  {

    <#if extends?has_content>
      "allOf": [
        {
    </#if>

    <#if title?has_content>
      "title": "${title}",
    </#if>

    <#if type?has_content>
      "type": "${type}",
    </#if>

    <#if required?size != 0>
      "required": [
        ${required?join(", ")}
      ],
    </#if>

    <#if desc?has_content>
      "description": "${removeIndentation(desc)}",
    </#if>

    "properties": {
      <#nested>
    }

    <#if extends?has_content>
      },
      {
      "$ref": "#/components/schemas/${extends}"
      }
    ]
    </#if>

  }
</#macro>

<#-- Generates a Request Body JSON object -->
<#macro requestBody mediaType dto
        requestDesc=""
        examples=[] >
  "requestBody" : {

    <#if requestDesc?has_content >
      "description": "${removeIndentation(requestDesc)}",
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

<#-- Generates an HTTP Response JSON object
     * `dto` needs to be defined if `mediaType` is, the default, "application/json" -->
<#macro response code desc
        flatType=""
        dto=""
        array=false
        additionalProperties=false
        mediaType="application/json"
        examples=[]
        last=false >
    "${code}": {

       <#if code != "204">
         "content": {
           <#if mediaType == "application/xhtml+xml">
             "${mediaType}": {
               "schema": {
                 "type": "string",
                 "format": "binary",
                 "description": "For `application/xhtml+xml` Responses, a byte stream is returned."
               }
           <#else>
             "${mediaType}": {
               "schema": {

                 <#if array>
                   "type" : "array",
                   "items" : {
                 </#if>

                 <#if additionalProperties>
                   "type" : "object",
                   "additionalProperties": {
                 </#if>

                 <#if dto?has_content>
                   "$ref": "#/components/schemas/${dto}"
                 </#if>

                 <#if flatType?has_content>
                  "type": "${flatType}"
                 </#if>

                 <#if array || additionalProperties >
                   }
                 </#if>
               }
           </#if>

           <#if examples?size != 0>,
             "examples": {
               ${examples?join(", ")}
             }
           </#if>

           }
         },
       </#if>

       "description": "${removeIndentation(desc)}"
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
      "description":  "${removeIndentation(desc)}",
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

<#-- Generates an Operation Information JSON object -->
<#macro endpointInfo
        id
        tag
        desc
        summary = "" >
    "operationId": "${id}",
    "tags": [
      "${tag}"
    ],

    <#if summary?has_content>
      "summary": "${summary}",
    </#if>

    "description": "${removeIndentation(desc)}",
</#macro>

<#-- Removes source formatting indentations from descriptions -->
<#function removeIndentation text>
  <#return text?replace('\n( ){2,}', '\n', 'r') >
</#function>
