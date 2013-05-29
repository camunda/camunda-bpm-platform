"use strict";

define([ "angular", "jquery" ], function(angular, $) {

  var paginatorTmpl =
    '<div class="pagination pagination-centered">' +
    '  <ul>' +
    '    <li ng-repeat="page in pages" ng-class="{active: page.isActive, disabled: page.isDisabled, pointer: (!page.isActive && page.number != -1)}">' +
    '      <a ng-click="selectPage(page.number)">{{ page.text }}</a>' +
    '    </li>' +
    '  </ul>' +
    '</div>';

  var module = angular.module("cockpit.directives");

  var Directive = function ($window) {
    return {
      restrict: 'A',
      template: paginatorTmpl,
      replace: true,
      scope: {
        numRows: '=',
        currentPage: '='
      },
      link: function(scope, element, attrs) {

        /* constants */
        var previousText = '&laquo;';
        var nextText = '&raquo;';
        var dots = '&hellip;';

        /* default offset if no one is specified */
        var defaultOffset = 20;

        /* check whether an offset is set, if not the default offset is chosen */
        var offset = angular.isDefined(attrs.offset) ? attrs.offset : defaultOffset;

        /**
         * Returns a configured page.
         */
        function createPage (number, text, isActive, isDisabled) {
          return {
            number: number,
            text: text,
            isActive: isActive,
            isDisabled: isDisabled
          };
        }

        scope.$watch('numRows', function () {
          if (!!scope.numRows) {
            scope.createPaginator();
          }
        });

        scope.$watch('currentPage', function () {
          if (!!scope.currentPage) {
            scope.createPaginator();
          }
        });

        scope.createPaginator = function () {

          scope.pages = [];

          var numPages = scope.numPages = Math.ceil(scope.numRows/offset);

          if (numPages <= 10) {
            // If there are less or equal 10 pages that has to be shown, then
            // all of them will be displayed in the pagination.
            for (var number = 1; number <= numPages; number++) {
              var page = createPage(number, number, scope.isActive(number), false);
              scope.pages.push(page);
            }
          } else if (numPages > 10) {

            // If there are more than 10 pages that has to be shown, then
            // only the first and last one will be shown. Furthermore, the current
            // selected page and the previous and next page will be shown, too.

            var placeHolder = createPage(-1, dots, false, true);

            if (scope.currentPage <= 3) {
              for (var number = 2; number <= 4; number++) {
                scope.pages.push(createPage(number, number, scope.isActive(number), false));
              };
              scope.pages.push(placeHolder);
            }

            if (scope.currentPage >= (numPages-2)) {
              scope.pages.push(placeHolder);
              for (var number = numPages-3; number <= numPages-1; number++) {
                scope.pages.push(createPage(number, number, scope.isActive(number), false));
              };
            }

            if (scope.currentPage > 3 && scope.currentPage < (numPages-2)) {
              scope.pages.push(placeHolder);
              for (var number = scope.currentPage-1; number <= scope.currentPage+1; number++) {
                scope.pages.push(createPage(number, number, scope.isActive(number), false));
              };
              scope.pages.push(placeHolder);
            }

            var firstPage = createPage(1, 1, scope.isActive(1), false);
            scope.pages.unshift(firstPage);

            var lastPage = createPage(numPages, numPages, scope.isActive(numPages), false);
            scope.pages.push(lastPage);
          }

          var previousPage = createPage(scope.currentPage - 1, previousText, false, scope.noPrevious());
          scope.pages.unshift(previousPage);

          var nextPage = createPage(scope.currentPage + 1, nextText, false, scope.noNext());
          scope.pages.push(nextPage);

        };

        scope.isActive = function (page) {
          return scope.currentPage == page;
        };

        scope.noPrevious = function() {
          return scope.currentPage == 1;
        };

        scope.noNext = function() {
          return scope.currentPage == scope.numPages;
        };

        scope.selectPage = function(page) {
          if (!scope.isActive(page) && page > 0 && page <= scope.numPages) {
            scope.currentPage = page;
          }
        };

      }
    };
  };

  module
    .directive('paginator', Directive);

});