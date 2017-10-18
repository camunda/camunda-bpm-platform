'use strict';

module.exports = function(page, rootScope, $translate) {
  rootScope.showBreadcrumbs = true;

  page.breadcrumbsClear();

  page.breadcrumbsAdd({
    label: $translate.instant('BATCHES_BREAD_BATCHES')
  });

  page.titleSet($translate.instant('BATCHES_TITLE_BATCHES'));
};
