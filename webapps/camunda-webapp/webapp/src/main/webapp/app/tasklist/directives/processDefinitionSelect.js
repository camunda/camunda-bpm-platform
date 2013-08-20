ngDefine('tasklist.directives', [ 
  'require'
], function(module, require) {

  // CAM-1098: austin, we have got a problem:
  // - for some reason, angular does not allow us to use ngIf -> directive(replace=true, templaceUrl='some-url') reliably
  // - fix: put template directly in nagivation.html
  // 
  // left the old code here (commented out). We may check future angularJs versions for a fix.
  // 
  
  // var ProcessDefinitionsController = [ '$scope', 'EngineApi', function($scope, EngineApi) {
  //   var queryObject = { latest : true };

  //   $scope.processDefinitions = EngineApi.getProcessDefinitions().query(queryObject);
  // }];

  var ProcessDefinitionSelect = [ function() {
    return {
      // replace: true,
      // controller: ProcessDefinitionsController, 
      // templateUrl: require.toUrl('./process-definition-select.html'),
      link: function(scope, element, attrs) {

        var divider;

        scope.$watch(attrs['ngShow'], function(newValue) {
          if (newValue && !divider) {
            divider = $('<li class="divider-vertical"></li>').insertAfter(element);
          }

          if (!newValue && divider) {
            divider.remove();
            divider = null;
          }
        });

        scope.$on('$destroy', function() {
          if (divider) {
            divider.remove();
          }
        });
      }
    }
  }];

  module.directive('processDefinitionSelect', ProcessDefinitionSelect);
})