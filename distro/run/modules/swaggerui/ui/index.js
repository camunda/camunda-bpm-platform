import SwaggerUI from 'swagger-ui'


SwaggerUI({
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
        SwaggerUI.presets.apis,
    ],
    url: './openapi.json',
    layout: 'BaseLayout',
    docExpansion: 'none',
    tryItOutEnabled: true
    //filter: true, would be cool but only allows filter by tag, which is confusing.
})