'use strict';

var $ = require('jquery');

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

      var originalCollapsabled = localConf.get('ctnCollapsableParent:collapsed:'+ containerId, 'no');

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

        /**
         * Toggle show / hide handles
         */
      function setCollapsed(collapsed) {
        if (collapsed) {
          hideHandle.hide();
          showHandle.css('display', 'block');
        } else {
          showHandle.hide();
          hideHandle.css('display', 'block');
        }

        localConf.set('ctnCollapsableParent:collapsed:'+ containerId, collapsed ? 'yes' : 'no');
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

        if (originalCollapsabled === 'yes') {
          collapsableElement.css(changeAttr, 0);
          compensateElement.css(direction, 0);
        }
        else {
          collapsableElement.css(changeAttr, originalCollapsableSize);
          compensateElement.css(direction, originalCollapsableSize +'px');
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

              // update collapsed state on drag
              setCollapsed(pos < 10);

              collapsableElement.css(changeAttr, pos);
              compensateElement.css(direction, pos);

              localConf.set('ctnCollapsableParent:size:'+ containerId, pos);
            })
            .on('dragstop', function(event) {
              updateResizeHandlePosition();

              $rootScope.$broadcast('resize', [ event ]);
            });

        hideHandle.click(function() {
          setCollapsed(true);

          resizeHandle.animate(createOffset(0));
          collapsableElement
            .animate(
              createSize(0),
              $rootScope.$broadcast.bind($rootScope, 'resize', {
                direction: direction,
                collapsed: true
              })
            );
          compensateElement.animate(createOffset(0));
        });

        showHandle.click(function() {
          setCollapsed(false);

          resizeHandle.animate(createOffset(minWidth || originalCollapsableSize));
          collapsableElement
            .animate(
              createSize(minWidth || originalCollapsableSize),
              $rootScope.$broadcast.bind($rootScope, 'resize', {
                direction: direction,
                collapsed: false
              })
            );
          compensateElement.animate(createOffset(minWidth || originalCollapsableSize));
        });

        $(window).on('resize', updateResizeHandlePosition);

        scope.$on('$destroy', function() {
          $(window).off('resize', updateResizeHandlePosition);
        });

        updateResizeHandlePosition();
      }

      setCollapsed(originalCollapsabled === 'yes');
      initResize();
    }
  };
}];
