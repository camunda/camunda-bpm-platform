'use strict';

module.exports = function(page, rootScope) {
  rootScope.showBreadcrumbs = true;

  page.breadcrumbsClear();

  page.breadcrumbsAdd({
    label: 'Batches'
  });

  page.titleSet('Batches');
};
