'use strict';

module.exports = ['readFiles', function(readFiles) {
  return {
    restrict: 'A',
    scope: {
      onChange: '&camFile'
    },
    link: function($scope, $element) {
      $element[0].addEventListener('change', function($event) {
        $scope.$apply(function() {
          readFiles($element[0].files).then(function(files) {
            $scope.onChange({
              $event: $event,
              files: files
            });
          });
        });
      });
    }
  };
}];
