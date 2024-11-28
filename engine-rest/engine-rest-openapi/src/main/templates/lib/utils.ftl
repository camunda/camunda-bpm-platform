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

      <#if type == "array">
        "type": "string"
      <#else>
        "type": "${type}"
      </#if>

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

<#macro parameters
        object
        skip = []
        last = false>
  <#local result>
    <#list object as key, value>
      <#if !skip?seq_contains(key)>
        <@lib.parameter
            name = key
            location = "query"
            type = value["type"]
            format = value["format"]
            defaultValue = value["defaultValue"]
            enumValues = value["enumValues"]
            last = true
            desc = value["desc"] />,
      </#if>
    </#list>
  </#local>
  <#if last>
    ${result?keep_before_last(",")}
  <#else>
    ${result}
  </#if>
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
        </#if>

        <#if type == "boolean" | type == "string" | type == "array" | format?has_content | dto?has_content >
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

<#macro properties
        object
        skip = []
        last = false>
  <#local result>
    <#list object as key, value>
      <#if !skip?seq_contains(key)>
        <@lib.property name = key
            type = value["type"]
            enumValues = value["enumValues"]
            format = value["format"]
            dto = value["dto"]
            itemType = value["itemType"]
            defaultValue = value["defaultValue"]
            last = true
            desc = value["desc"]/>,
      </#if>
    </#list>
  </#local>
  <#if last>
    ${result?keep_before_last(",")}
  <#else>
    ${result}
  </#if>
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
<#macro requestBody mediaType 
        dto=""
        flatType=""
        requestDesc=""
        examples=[] >
  "requestBody" : {

    <#if requestDesc?has_content >
      "description": "${removeIndentation(requestDesc)}",
    </#if>

    "content" : {
      "${mediaType}" : {

        "schema" : {
          <#if dto?has_content >
            "$ref" : "#/components/schemas/${dto}"
          <#elseif flatType?has_content >
            "type": "${flatType}"
          </#if>
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
        contentDesc=""
        array=false
        additionalProperties=false
        mediaType="application/json"
        binary = false
        examples=[]
        last=false >
    "${code}": {
        <#if code != "204">
          "content": {
            <@lib.responseContentMediaType
                flatType = flatType
                dto = dto
                array = array
                additionalProperties = additionalProperties
                mediaType = mediaType
                contentDesc = contentDesc
                binary = binary
                examples = examples />
          },
        </#if>
          "description": "${removeIndentation(desc)}"
    }

    <#if !last> , </#if> <#-- if not a last response, add a comma-->
</#macro>

<#-- Generates an HTTP multi type Response JSON object -->
<#macro multiTypeResponse 
        code 
        desc
        types=[]
        last=false >
    "${code}": {
        <#if code != "204">
          "content": {
            <#list types as type >
              <@lib.responseContentMediaType
                  flatType = type["flatType"]
                  dto = type["dto"]
                  array = type["array"]
                  additionalProperties = type["additionalProperties"]
                  mediaType = type["mediaType"]
                  contentDesc = type["contentDesc"]
                  binary = type["binary"]
                  examples = type["examples"] /><#sep>,
            </#list>
          },
        </#if>
        "description": "${removeIndentation(desc)}"
    }

    <#if !last> , </#if> <#-- if not a last response, add a comma-->
</#macro>

<#-- Generates a content media type JSON object for HTTP response -->
<#macro responseContentMediaType
        flatType=""
        dto=""
        array=false
        additionalProperties=false
        mediaType="application/json"
        contentDesc=""
        binary = false
        examples=[] >
           <#if mediaType == "application/xhtml+xml" | (mediaType == "application/json" & !array & flatType == "string") | binary>
             "${mediaType}": {
               "schema": {
                 "type": "string",
                 "format": "binary",
                 "description": "For `${mediaType}` Responses, a byte stream is returned."
               }
           <#else>
             "${mediaType}": {
               "schema": {

               <#if mediaType == "text/plain" && contentDesc?has_content>
                   "type" : "string",
                   "description" : "${contentDesc}"
               </#if>

                 <#if array>
                   "type" : "array",
                   "items" : {
                 </#if>

                 <#if additionalProperties>
                   <#if !array>
                     "type" : "object",
                   </#if>
                   "additionalProperties": {
                 </#if>

                 <#if dto?has_content>
                   "$ref": "#/components/schemas/${dto}"
                 </#if>

                 <#if flatType?has_content>
                  "type": "${flatType}"
                 </#if>

                 <#if additionalProperties>
                   }
                 </#if>

                 <#if array>
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
</#macro>

<#-- Generates a Server JSON object -->
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
        deprecated = false
        summary = "" >

    <#if deprecated>
      "deprecated": true,
    </#if>

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
