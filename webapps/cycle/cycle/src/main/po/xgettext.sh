#!/bin/sh

# extract gettext strings from Java sources with options provided by Maven
xgettext --copyright-holder='camunda services GmbH' --package-name='camunda cycle Webapp' --package-version='1.0-SNAPSHOT' --msgid-bugs-address='info@camunda.com' $*

# replace some placeholders
sed -i '1 s/SOME DESCRIPTIVE TITLE/This is the LANGUAGE translation of camunda cycle Webapp/' ../po/keys.pot
sed -i '2 s/YEAR/2012/' ../po/keys.pot
sed -i '3 s/PACKAGE/camunda cycle Webapp/' ../po/keys.pot

# fix Java source file locations
#sed -i 's/^#: /#: ..\/java\//' ../po/keys.pot

# add a newline
echo >> ../po/keys.pot

# extract gettext strings from JSF templates
grep "#{\([^{]*\)}" --recursive --exclude-dir=.svn --include='*.html' --only-matching --with-filename --line-number ../ | sed "s/\(.*\)\:#{\([^{]*\)}/#: \1\nmsgid \"\2\"\nmsgstr \"\"\n/" >> ../po/keys.pot

# unify duplicate translations in message catalog
msguniq --sort-by-file --output-file=../po/keys.pot ../po/keys.pot
