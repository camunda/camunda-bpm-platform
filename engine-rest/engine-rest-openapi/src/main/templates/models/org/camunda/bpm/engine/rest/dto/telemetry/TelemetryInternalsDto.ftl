<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "database"
        type = "object"
        additionalProperties = true
        dto = "AbstractVendorVersionInformationDto"
        desc = "Vendor and version of the connected database."/>

    <@lib.property
        name = "application-server"
        type = "object"
        additionalProperties = true
        dto = "AbstractVendorVersionInformationDto"
        desc = "Vendor and version of the application server."/>

    <@lib.property
        name = "license-key"
        type = "object"
        additionalProperties = true
        dto = "TelemetryLicenseKeyDto"
        desc = "Information about the Camunda license key."/>

    <@lib.property
        name = "camunda-integration"
        type = "array"
        itemType = "string"
        desc = "List of Camunda integrations used (e.g., Camunda Spring Boot Starter, Camunda Run, WildFly/JBoss subsystem, Camunda EJB)."/>

    <@lib.property
        name = "commands"
        type = "object"
        additionalProperties = true
        dto = "TelemetryCountDto"
        desc = "The count of executed commands after the last retrieved data."/>

    <@lib.property
        name = "metrics"
        type = "object"
        additionalProperties = true
        dto = "TelemetryCountDto"
        desc = "The collected metrics are the number of root process instance executions started, the number of activity instances started or also known as flow node instances, and the number of executed decision instances and elements."/>

    <@lib.property
        name = "webapps"
        type = "array"
        itemType = "string"
        desc = "The webapps enabled in this installation of Camunda."/>

    <@lib.property
        name = "jdk"
        type = "object"
        last = true
        additionalProperties = true
        dto = "AbstractVendorVersionInformationDto"
        desc = "Vendor and version of the installed JDK."/>

</@lib.dto>

</#macro>