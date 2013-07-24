ngDefine('camunda.common.directives', [ 'angular', 'jquery' ], function(module, angular, $) {

  var paginatorTmpl =
    '<div class="pagination pagination-centered">' +
    '  <ul ng-if="totalPages">' +
    '    <li ng-repeat="page in pages" ng-class="{ active: page.current, disabled: page.disabled }">' +
    '      <a ng-if="page.disabled" href ng-bind-html-unsafe="page.text"></a>' +
    '      <a ng-if="!page.disabled" ng-click="selectPage(page.number)" ng-bind-html-unsafe="page.text"></a>' +
    '    </li>' +
    '  </ul>' +
    '</div>';

  var PaginatorDirective = function () {
    return {
      restrict: 'EAC',
      template: paginatorTmpl,
      replace: true,
      scope: {
        totalPages: '=',
        currentPage: '='
      },
      link: function(scope, element, attrs) {

        /* constants */

        var LABEL = {
          PREVIOUS: '&laquo;',
          NEXT: '&raquo;',
          DOTS: '&hellip;'
        };

        scope.$watch('totalPages', function() {
          update();
        });

        scope.$watch('currentPage', function() {
          update();
        });

        function update() {
          var totalPages = scope.totalPages;
          var currentPage = scope.currentPage;

          var pages = scope.pages = [];

          if ((!totalPages && totalPages !== 0) ||
              (!currentPage && currentPage !== 0)) {

            return;
          }

          if (totalPages > 10) {

            // more than 10 pages, show
            //   1 ... 4 [5] 6 ... n
            for (var i = currentPage - 1; i < currentPage + 2; i++) {
              if (i > 0 && i <= totalPages) {
                pages.push({ number: i, text: i, current: (currentPage === i) });
              }
            }

            if (currentPage - 1 > 2) {
              pages.unshift({ disabled: true, text: LABEL.DOTS });
            }

            if (currentPage - 1 > 1) {
              pages.unshift({ number: 1, text: 1 });
            }

            if (currentPage + 2 < totalPages) {
              pages.push({ disabled: true, text: LABEL.DOTS });
            }

            if (currentPage + 1 < totalPages) {
              pages.push({ number: totalPages, text: totalPages });
            }
          } else {

            // less/eq 10 pages, show
            //   1 2 3 4 5 6
            for (var i = 1; i <= totalPages; i++) {
              pages.push({ number: i, text: i, current: (currentPage === i) });
            }
          }

          // add << and >> handles

          pages.push({ number: (currentPage + 1), text: LABEL.NEXT, disabled: (currentPage + 1 > totalPages) });
          pages.unshift({ number: (currentPage - 1), text: LABEL.PREVIOUS, disabled: (currentPage - 1 < 1)});
        }

        scope.selectPage = function(page) {
          scope.currentPage = page;
        };
      }
    };
  };

  module.directive('paginator', PaginatorDirective);
});