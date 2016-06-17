  'use strict';

  module.exports = [
    'ProcessDefinitionResource',
    'page',
    function(ProcessDefinitionResource, page) {
      function breadcrumbTrails(processInstance, fetchSuperInstance, trail, index, urlSuffix) {
        trail = trail || [];

        function handleSuperProcessInstance(err, superProcessInstance) {

          if (!superProcessInstance) {
            page.breadcrumbsInsertAt(index, trail);
            return;
          }

        // ... and fetch its process definition
          ProcessDefinitionResource
        .get({
          // TODO: CAM-2017 API definition cleanup
          id: superProcessInstance.processDefinitionId || superProcessInstance.definitionId
        })
        .$promise.then(function(response) {
          // var superProcessDefinition = response.data;
          var superProcessDefinition = response;

          // ... PREpend the breadcrumbs
          trail = [
            {
              href: '#/process-definition/'+ superProcessDefinition.id +(urlSuffix ? '/'+ urlSuffix : ''),
              label: superProcessDefinition.name || superProcessDefinition.key
            },
            {
              divider: ':',
              href: '#/process-instance/'+ superProcessInstance.id +(urlSuffix ? '/'+ urlSuffix : ''),
              label: superProcessInstance.id.slice(0, 8) +'…'
            }
          ].concat(trail);

          breadcrumbTrails(superProcessInstance, fetchSuperInstance, trail, index, urlSuffix);
        });
        }

        fetchSuperInstance(processInstance, handleSuperProcessInstance);
      }

      return breadcrumbTrails;
    }];
