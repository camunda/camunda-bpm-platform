ngDefine(
  'cockpit.directives', [ 
  'jquery', 'angular' 
], function(module, $, angular) {

  module.directive('ctnSitebarContainer', function() {

    return {
      restrict: 'CA',
      link: function(scope, element, attrs) {
        var container = $(element);

        var showSitebar = $('.show-sitebar', container);
        var hideSitebar = $('.hide-sitebar', container);

        showSitebar
          .addClass('expand-collapse')
          .append('<i class="icon-chevron-right"></i>')
          .attr('title', 'Show sitebar');
        
        hideSitebar
          .addClass('expand-collapse')
          .append('<i class="icon-chevron-left"></i>')
          .attr('title', 'Hide sitebar');

        var sitebar = $('.ctn-sitebar', container);
        var content = $('.ctn-content', container);

        function setShown(shown) {
          if (shown) {
            showSitebar.hide();
            hideSitebar.css('display', 'block');
          } else {
            showSitebar.css('display', 'block');
            hideSitebar.hide();
          }
        }

        var sitebarWidth = sitebar.css("width");

        showSitebar.click(function(e) {
          setShown(true);

          content.animate({ left: sitebarWidth });
          sitebar.animate({ left: 0 });
        });

        hideSitebar.click(function(e) {
          setShown(false);

          content.animate({ left: '0' });
          sitebar.animate({ left: '-' + sitebarWidth });
        });

        setShown(true);
      }
    };
  });
});