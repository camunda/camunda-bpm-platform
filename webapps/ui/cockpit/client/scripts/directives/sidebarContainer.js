'use strict';

var $ = require('jquery');
var angular = require('camunda-commons-ui/vendor/angular');

require('jquery-ui/draggable');

module.exports = ['localConf', '$rootScope', function(localConf, $rootScope) {
  return {
    restrict: 'CA',
    link: function(scope, element, attrs) {
      var container = $(element);
      var containerId = attrs.ctnCollapsableParent;

        // the element being collapsed
      var collapsableElement = $('[ctn-collapsable]', container).eq(0);

        // the direction into which to collapse
      var direction = collapsableElement.attr('ctn-collapsable') || 'left';
      var vertical = direction === 'left' || direction === 'right';

      var minWidth = 0;
      if (attrs.ctnMinWidth) {
        var minWidthEls = $('.ctn-' + containerId + ' ' + attrs.ctnMinWidth, container);
        minWidthEls.each(function(e, el) {
          minWidth += $(el).outerWidth(true);
        });

        // just to avoid half pixels glitches
        if(minWidth) {
          minWidth += 10;
        }
      }

      var previouslyCollapsed = localConf.get('ctnCollapsableParent:collapsed:'+ containerId, 'no');
      var previouslyMaximized = localConf.get('ctnCollapsableParent:maximized:'+ containerId, 'no');

        // the main element that compensates the collapsing
      var compensateElement = collapsableElement[direction === 'left' || direction === 'top' ? 'next' : 'prev']();

        // a resize handle
      var resizeHandle = $('<div class="resize-handle"></div>')
                              .appendTo(container);

        /////// init show / hide handles ////////

      var showHandle =
          compensateElement
            .children('.show-collapsable')
              .addClass('expand-collapse')
              .append('<i class="glyphicon glyphicon-menu-' + (vertical ? 'right' : 'down') + '"></i>');

      var hideHandle =
          collapsableElement
            .children('.hide-collapsable')
              .addClass('expand-collapse')
              .append('<i class="glyphicon glyphicon-menu-' + (vertical ? 'left' : 'up') + '"></i>');

      var maximizeHandle =
          collapsableElement
            .children('.maximize-collapsable')
              .addClass('expand-collapse')
              .append('<i class="glyphicon glyphicon-resize-full"></i>');

      var maximizeDirection = maximizeHandle.attr('maximize-parent-direction');

      var restoreHandle =
          collapsableElement
            .children('.restore-collapsable')
              .addClass('expand-collapse')
              .append('<i class="glyphicon glyphicon-resize-small"></i>');

        /**
         * Toggle show / hide handles
         */
      function setCollapsed(collapsed, maximized) {
        updateCollapsedClass(collapsed);

        if (collapsed) {
          hideHandle.hide();
          maximizeHandle.hide();
          restoreHandle.hide();
          showHandle.css('display', 'block');
        } else {
          showHandle.hide();
          hideHandle.css('display', 'block');
        }

        if (maximized) {
          maximizeHandle.hide();
          restoreHandle.css('display', 'block');
        } else if (!collapsed) {
          maximizeHandle.css('display', 'block');
          restoreHandle.hide();
        }

        localConf.set('ctnCollapsableParent:collapsed:'+ containerId, collapsed ? 'yes' : 'no');
        localConf.set('ctnCollapsableParent:maximized:'+ containerId, maximized ? 'yes' : 'no');
      }

      function initResize() {
        var changeAttr = vertical ? 'width' : 'height';
        var resizeHandleAttachAttr = vertical ? 'left' : 'top';
        var changeAxis = vertical ? 'x' : 'y';

          /**
           * Create a { direction: i } object
           */
        function createOffset(i) {
          var style = {};
          style[direction] = i;

          return style;
        }

          /**
           * Create a { changeAttr: i } object
           */
        function createSize(i) {
          var style = {};
          style[changeAttr] = i;

          return style;
        }

        resizeHandle
            .addClass(vertical ? 'vertical' : 'horizontal');

        var originalCollapsableSize = collapsableElement[changeAttr]();

        localConf.get('ctnCollapsableParent:size:'+ containerId, originalCollapsableSize);

        originalCollapsableSize = Math.max(minWidth, originalCollapsableSize);

        if (previouslyCollapsed === 'yes') {
          collapsableElement.css(changeAttr, 0);
          compensateElement.css(direction, 0);
        }
        else {
          collapsableElement.css(changeAttr, originalCollapsableSize);
          compensateElement.css(direction, originalCollapsableSize +'px');
        }

        if (previouslyMaximized === 'yes') {
          var maxSize = element[changeAttr]();

          setCollapsed(false, true);

          resizeHandle.css(createOffset(maxSize));
          collapsableElement.css(createSize(maxSize));
          compensateElement.css(createOffset(maxSize));
        }

        function updateResizeHandlePosition() {
          var collapsableSize = collapsableElement[changeAttr]();
          var collapsablePosition = collapsableElement.position();

          if (direction === 'left' || direction === 'top') {
            resizeHandle
                .css(resizeHandleAttachAttr, collapsableSize);
          } else {
            resizeHandle
                .css(resizeHandleAttachAttr, collapsablePosition[resizeHandleAttachAttr]);
          }
        }

        $(resizeHandle)
            .draggable({ axis: changeAxis, containment: 'parent'})
            .on('drag', function() {
              var pos = getPos();
              var collapsed = isCollapsed();

              // update collapsed state on drag
              setCollapsed(collapsed, isCurrentlyMaximized());

              collapsableElement.css(changeAttr, pos);
              compensateElement.css(direction, pos);

              localConf.set('ctnCollapsableParent:size:'+ containerId, pos);
            })
            .on('dragstop', function() {
              updateResizeHandlePosition();

              var collapsed = isCollapsed();

              updateCollapsedClass(collapsed);

              $rootScope.$broadcast('resize', {
                direction: direction,
                collapsed: collapsed
              });
            });

        hideHandle.click(function() {
          var targetSize = isCurrentlyMaximized() ? minWidth || originalCollapsableSize : 0;

          setCollapsed(targetSize === 0, false);

          resizeHandle.animate(createOffset(targetSize));
          collapsableElement
            .animate(
              createSize(targetSize),
              function() {
                $rootScope.$broadcast('resize', {
                  direction: direction,
                  collapsed: true
                });

                updateCollapsedClass(targetSize === 0);
              }
            );
          compensateElement.animate(createOffset(targetSize));
        });

        showHandle.click(function() {
          setCollapsed(false, false);

          resizeHandle.animate(createOffset(minWidth || originalCollapsableSize));
          collapsableElement
            .animate(
              createSize(minWidth || originalCollapsableSize),
              function() {
                $rootScope.$broadcast('resize', {
                  direction: direction,
                  collapsed: false
                });

                updateCollapsedClass(false);
              }
            );
          compensateElement.animate(createOffset(minWidth || originalCollapsableSize));
        });

        maximizeHandle.click(function() {
          $rootScope.$broadcast('maximize', {
            source: element,
            direction: maximizeDirection
          });

          maximize(
            $rootScope.$broadcast.bind($rootScope, 'resize', {
              direction: direction,
              collapsed: false
            })
          );
        });

        restoreHandle.click(function() {
          $rootScope.$broadcast('restore', {
            source: element
          });

          restore(
            $rootScope.$broadcast.bind($rootScope, 'resize', {
              direction: direction,
              collapsed: false
            })
          );
        });

        function maximize(callback) {
          callback = typeof callback === 'function' ? callback : angular.noop;
          var maxSize = element[changeAttr]();

          setCollapsed(false, true);

          resizeHandle.animate(createOffset(maxSize));
          collapsableElement
            .animate(
              createSize(maxSize),
              function() {
                callback();
                updateCollapsedClass(false);
              }
            );
          compensateElement.animate(createOffset(maxSize));
        }

        function minimize(callback) {
          callback = typeof callback === 'function' ? callback : angular.noop;
          var minSize = 0;

          setCollapsed(true, false);

          resizeHandle.animate(createOffset(minSize));
          collapsableElement
            .animate(
              createSize(minSize),
              function() {
                callback();
                updateCollapsedClass(true);
              }
            );
          compensateElement.animate(createOffset(minSize));
        }

        function restore(callback) {
          callback = typeof callback === 'function' ? callback : angular.noop;
          setCollapsed(false, false);

          resizeHandle.animate(createOffset(minWidth || originalCollapsableSize));
          collapsableElement
            .animate(
              createSize(minWidth || originalCollapsableSize),
              function() {
                callback();
                updateCollapsedClass(false);
              }
            );
          compensateElement.animate(createOffset(minWidth || originalCollapsableSize));
        }

        $(window).on('resize', updateResizeHandlePosition);

        scope.$on('$destroy', function() {
          $(window).off('resize', updateResizeHandlePosition);
        });

        $rootScope.$on('restore', function(event, data) {
          if (element !== data.source) {
            restore();
          }
        });

        $rootScope.$on('maximize', function(event, data) {
          if (element !== data.source) {
            if (data.direction === direction) {
              minimize();
            } else {
              maximize();
            }
          }
        });

        $rootScope.$on('resize', function(event, data) {
          if (data.direction === maximizeDirection) {
            setCollapsed(isCollapsed(), data.collapsed && isCurrentlyMaximized());
          }
        });

        function isCurrentlyMaximized() {
          var pos = getPos();
          var maxSize = element[changeAttr]();

          return maxSize - Math.ceil(pos) < 10;
        }

        function isCollapsed() {
          var pos = getPos();

          return pos < 10;
        }

        updateResizeHandlePosition();

        function getPos() {
          var position = resizeHandle.position();
          var pos = position[direction];

          if (direction === 'right') {
            pos = container.width() - position.left;
          }

          if (direction === 'bottom') {
            pos = container.height() - position.top;
          }

          if(pos < minWidth) {
            pos = 0;
          }

          return pos;
        }
      }

      function updateCollapsedClass(collapsed) {
        if (collapsed) {
          collapsableElement.addClass('collapsed');
        } else {
          collapsableElement.removeClass('collapsed');
        }
      }

      setCollapsed(previouslyCollapsed === 'yes', previouslyMaximized === 'yes');
      initResize();
    }
  };
}];
