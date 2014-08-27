define([
  'angular',
  'bpmn-js',
  'snap-svg',
  'text!./cam-tasklist-task-diagram.html'
], function(
  angular,
  Viewer,
  Snap,
  template
) {
  'use strict';


window.Snap = Snap;
// console.info('Snap', Snap);


  return [
    'camAPI',
  function(
    camAPI
  ) {
    var ProcessDefinition = camAPI.resource('process-definition');



    return {
      scope: {
        task: '='
      },

      link: function(scope, element) {
        ProcessDefinition.xml(scope.task._embedded.processDefinition[0], function(err, xml) {
          var viewer = new Viewer({
            container: element
          });

          viewer.importXML(xml.bpmn20Xml, function(err) {
            if (err) {
              console.log('error rendering', err);
            }
            else {
              viewer.get('canvas').zoom('fit-viewport');
              console.log('rendered');
            }
          });
        });
      },
      template: template
    };
  }];
});
