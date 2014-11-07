define([
  'text!./cam-widget-inline-field.html',
  'angular'
], function(
  template,
  angular
) {
  'use strict';

  var $ = angular.element;

  return [
    '$timeout',
    '$filter',
    '$document',
  function(
    $timeout,
    $filter,
    $document
  ) {

    return {
      scope: {
        varValue:       '=value',
        varType:        '@type',
        validator:      '&validate',
        change:         '&',
        onStart:        '&onStartEditing',
        onCancel:       '&onCancelEditing',

        placeholder:    '@',
        inputFormat:    '@',
        displayFormat:  '@',
        icon:           '@',
        suffixed:       '@',
        options:        '=?',
        flexible:       '@'
      },

      template: template,

      link: function(scope, element) {
        var $bdyEl = angular.element('body');
        var $btnsEl;
        var $ctrlsEl;

        var dateFilter = $filter('date'),
            dateFormat = 'yyyy-MM-dd\'T\'HH:mm:ss';

        var dateRegex = /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/;

        scope.$on('$locationChangeSuccess', function() {
          scope.cancelChange();
        });

        function convertDateStringToObject(date) {
          var YEAR, MONTH, DAY, HOURS, MINUTES, SECONDS, MILLISECONDS;

          var match = dateRegex.exec(date);

          if (match) {

            YEAR = parseInt(match[1] || 0, 10);
            MONTH = parseInt(match[2] || 0, 10) - 1;
            DAY = parseInt(match[3] || 0, 10);
            HOURS = parseInt(match[4] || 0, 10);
            MINUTES = parseInt(match[5] || 0, 10);
            SECONDS = parseInt(match[6] || 0, 10);
            MILLISECONDS = parseInt(match[7] || 0, 10);

            return new Date(YEAR, MONTH, DAY, HOURS, MINUTES, SECONDS, MILLISECONDS);
          }
        }

        function isDate() {
          return ['datetime', 'date', 'time'].indexOf(scope.varType) > -1;
        }

        function isSimpleField() {
          return ['color', 'email', 'month', 'number', 'range', 'tel', 'text', 'time', 'url', 'week'].indexOf(scope.varType) > -1;
        }

        function reset() {
          scope.editing =       false;
          scope.invalid =       false;
          scope.editValue =     scope.varValue;

          scope.validator =     scope.validator ||     function() {};
          scope.onStart =       scope.onStart ||       function() {};
          scope.onCancel =      scope.onCancel ||      function() {};
          scope.change =        scope.change ||        function() {};
          scope.inputFormat =   scope.inputFormat ||   'X';
          scope.displayFormat = scope.displayFormat || 'LLL';
          scope.icon =          scope.icon ||          false;
          scope.suffixed =      scope.suffixed ||      false;
          scope.options =       scope.options ||       [];
          scope.flexible =      scope.flexible ||      false;

          scope.varType =       !!scope.varType ? scope.varType : 'text';

          scope.simpleField = isSimpleField();

          if (isDate()) {
            var dateStr = scope.varValue,
                dateObj = null;

            if (dateStr) {
              dateObj = convertDateStringToObject(dateStr);
            } else {
              dateObj = Date.now();
            }

            scope.dateValue = dateObj;
          }
        }

        function bodyDirectChild($el) {
          if (!$el || !$el.length) {
            return false;
          }

          var $parent = $el.parent();
          if (!$parent || !$parent.length) {
            return false;
          }

          return $parent[0].tagName.toLowerCase() === 'body';
        }

        function positionElements() {
          var $fieldEl = element;//.find('.edit');
          var offset = $fieldEl.offset();

          $btnsEl
            .show()
            .css({
              left: offset.left + ($fieldEl.outerWidth() - $btnsEl.outerWidth()),
              top: offset.top - $btnsEl.outerHeight()
            });

          $ctrlsEl
            .show()
            .css({
              left: offset.left,
              top: offset.top + $fieldEl.outerHeight()
            });
          }

        function appendToBody() {
          $btnsEl = (($btnsEl && $btnsEl.length) ? $btnsEl : element.find('.btn-group'))
                    .hide();
          if (!bodyDirectChild($btnsEl)) {
            $bdyEl
              .append($btnsEl);
          }

          $ctrlsEl = (($ctrlsEl && $ctrlsEl.length) ? $ctrlsEl : element.find('.field-control'))
                    .hide();
          if (!bodyDirectChild($ctrlsEl)) {
            $bdyEl
              .append($ctrlsEl);
          }

          $timeout(positionElements, 10);
        }

        function removeFromBody() {
          if ($btnsEl && $btnsEl.remove) {
            $btnsEl.remove();
          }
          $btnsEl = null;

          if ($ctrlsEl && $ctrlsEl.remove) {
            $ctrlsEl.remove();
          }
          $ctrlsEl = null;
        }



        function bodyClicked(evt) {
          return element[0].contains(evt.target) ||
            ($btnsEl && $btnsEl.length && $btnsEl[0].contains(evt.target)) ||
            ($ctrlsEl && $ctrlsEl.length && $ctrlsEl[0].contains(evt.target));
        }



        scope.$watch('editing', function () {
          if (scope.editing) {
            appendToBody();
          }
          else {
            removeFromBody();
          }
        });

        function stopEditing(evt) {
          if(!scope.editing) {
            return;
          }

          if(bodyClicked(evt)) {
            return;
          }

          var targetElement = $(evt.target),
              expectedClasses = 'ng-binding text-muted';

          if (targetElement.hasClass(expectedClasses)) {
            return;
          }

          var children = targetElement.children();
          if (children.hasClass(expectedClasses)) {
            return;
          }

          scope.$apply(scope.cancelChange);
        }

        scope.changeType = function() {
          if(scope.varType === 'datetime') {
            scope.varType = 'text';
          } else {
            scope.varType = 'datetime';
          }
          reset();
          scope.editing = true;
          element[0].attributes.type.value = scope.varType;
          scope.simpleField = isSimpleField();
        };

        scope.startEditing = function() {
          if(!scope.editing) {
            reset();

            scope.editing = true;
            scope.onStart(scope);

            $timeout(function(){
              $('[ng-model="editValue"]').focus();
              $document.bind('click', stopEditing);
            }, 50);
          }
        };

        scope.applyChange = function(selection) {
          scope.invalid = scope.validator(scope);

          if (!scope.invalid) {

            if(scope.simpleField) {
              scope.editValue = $('[ng-model="editValue"]').val();
              scope.varValue = scope.editValue;
            }
            else if (scope.varType === 'option') {
              scope.editValue = selection;
              scope.varValue = scope.editValue;
            }
            else if (isDate()) {
              scope.varValue = dateFilter(scope.dateValue, dateFormat);
            }

            scope.change(scope);

            scope.editing = false;

            $document.unbind('click', stopEditing);
          }
        };

        scope.cancelChange = function() {
          scope.editing = false;
          scope.onCancel(scope);

          $document.unbind('click', stopEditing);
        };

        scope.changeDate = function(pickerScope) {
          scope.editValue = scope.dateValue = pickerScope.dateValue;
        };

      },

      transclude: true
    };
  }];
});
