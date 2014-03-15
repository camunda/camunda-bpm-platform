ngDefine(
  'cockpit.directives', [
  'jquery', 'angular'
], function(module, $, angular) {

  module.directive('ctnCollapsableParent', function() {

    return {
      restrict: 'CA',
      link: function(scope, element, attrs) {
        var container = $(element);
        var containerId = attrs.ctnCollapsableParent;

        // the element being collapsed
        // var collapsableElement = container.children('.ctn-collapsable');
        var collapsableElement = $('[ctn-collapsable]', container).eq(0);

        // the direction into which to collapse
        // var direction = collapsableElement.attr('collapse') || 'left';
        var direction = collapsableElement.attr('ctn-collapsable') || 'left';
        var vertical = direction === 'left' || direction === 'right';

        var originalCollapsabled = localStorage ? localStorage.getItem('ctnCollapsableParent:collapsed:'+ containerId) : 'no';

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
              .append('<i class="icon-chevron-right"></i>')
              .attr('title', 'Show sidebar');

        var hideHandle =
          collapsableElement
            .children('.hide-collapsable')
              .addClass('expand-collapse')
              .append('<i class="icon-chevron-left"></i>')
              .attr('title', 'Hide sidebar');

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

          if (localStorage) {
            // we need to store something else than a boolean because if it was never "initialized"
            localStorage.setItem('ctnCollapsableParent:collapsed:'+ containerId, collapsed ? 'yes' : 'no');
          }
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

          if (localStorage) {
            var storedPos = localStorage.getItem('ctnCollapsableParent:size:'+ containerId);
            originalCollapsableSize = (storedPos !== null ? storedPos : originalCollapsableSize);
          }

          if (originalCollapsabled === 'yes') {
            collapsableElement.css(changeAttr, 0);
            compensateElement.css(direction, 6 +'px');
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
                .css(resizeHandleAttachAttr, collapsableSize)
            } else {
              resizeHandle
                .css(resizeHandleAttachAttr, collapsablePosition[resizeHandleAttachAttr]);
            }
          }

          $(resizeHandle)
            .draggable({ axis: changeAxis, containment: 'parent'})
            .on('drag', function(e, ui) {
              var position = resizeHandle.position();
              var pos = position[direction];

              if (direction === 'right') {
                pos = container.width() - position.left;
              }

              if (direction === 'bottom') {
                pos = container.height() - position.top;
              }

              // update collapsed state on drag
              setCollapsed(pos < 10);

              collapsableElement.css(changeAttr, pos);
              compensateElement.css(direction, (pos + 6) +'px');

              if (localStorage) {
                localStorage.setItem('ctnCollapsableParent:size:'+ containerId, pos);
              }
            })
            .on('dragstop', function(event) {
              scope.$broadcast('resize', [ event ]);
            });

          hideHandle.click(function() {
            setCollapsed(true);

            resizeHandle.animate(createOffset(0));
            collapsableElement.animate(createSize(0));
            compensateElement.animate(createOffset(0));
          });

          showHandle.click(function() {
            setCollapsed(false);

            resizeHandle.animate(createOffset(originalCollapsableSize));
            collapsableElement.animate(createSize(originalCollapsableSize));
            compensateElement.animate(createOffset(originalCollapsableSize));
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
  });
});
