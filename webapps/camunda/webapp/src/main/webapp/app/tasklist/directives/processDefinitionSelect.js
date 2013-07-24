ngDefine('tasklist.directives', [ 
  'require'
], function(module, require) {

  var ProcessDefinitionsController = [ '$scope', 'EngineApi', function($scope, EngineApi) {
    var queryObject = { latest : true };

    $scope.processDefinitions = EngineApi.getProcessDefinitions().query(queryObject);
  }];

  var ProcessDefinitionSelect = [ function() {
    return {
      replace: true,
      controller: ProcessDefinitionsController, 
      templateUrl: require.toUrl('./process-definition-select.html'),
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
      }
    }
  }];

  module.directive('processDefinitionSelect', ProcessDefinitionSelect);
})