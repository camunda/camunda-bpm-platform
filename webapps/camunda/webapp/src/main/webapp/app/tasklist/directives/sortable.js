ngDefine('tasklist.directives', [
  'angular',
  'jquery'
], function(module, angular, $) {

  /**
   * The main controller for variables.
   *
   * @param $scope {Scope}
   */
  var SortableController = function SortableController($scope) {

    this.setDefaultOrder = function(defaultOrder) {
      this.defaultOrder = defaultOrder;
    };

    this.isActive = function(key, order) {
      if (!this.sort || this.sort.by != key) {
        return false;
      }

      return !order || this.sort.order == order;
    };

    this.toggleSort = function(variable) {

      if (this.sort.by == variable) {
        this.sort.order = (this.sort.order == "desc" ? "asc" : "desc");
      } else {
        this.sort.order = this.defaultOrder;
        this.sort.by = variable;
      }

      $scope.$emit("sortChanged", this.sort);
    };
  };

  SortableController.$inject = ["$scope"];

  var SortableDirective = function SortableDirective() {
    return {
      restrict: "EA",
      controller: "SortableController",
      link: function(scope, element, attributes, controller) {

        var defaultOrder = scope.$eval(attributes["defaultOrder"]);
        controller.setDefaultOrder(defaultOrder || "asc");

        scope.$watch(attributes["sortable"], function(newValue) {
          controller.sort = newValue;
        });
      }
    };
  };

  var SortDirective = function SortDirective() {
    return {
      scope: true,
      require: "^sortable",
      transclude: true,
      template: '<span class="sort-by" ng-click="toggleSort()" ng-class="sortCls()"><span ng-transclude></span><i class="order desc icon-arrow-up"></i><i class="order asc icon-arrow-down"></i></span>',
      link: function(scope, element, attributes, sortable) {

        var sort = attributes["sortBy"];
        if (!sort) {
          throw new Error("sortBy must be defined");
        }

        scope.sortCls = function() {
          return sortable.isActive(sort) ? sortable.sort.order : null;
        };

        scope.toggleSort = function() {
          sortable.toggleSort(sort);
        };
      }
    };
  };

  module
    .controller("SortableController", SortableController)
    .directive("sortable", SortableDirective)
    .directive("sortBy", SortDirective);
});