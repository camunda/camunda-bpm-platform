/**
 * @file better-placeholder-polyfill
 * @version 1.0.5 2013-07-08T10:54:36
 * @overview [placeholder] polyfill for better-dom
 * @copyright Maksim Chemerisuk 2013
 * @license MIT
 * @see https://github.com/chemerisuk/better-placeholder-polyfill
 */
(function(DOM) {
    "use strict";

    if (DOM.supports("placeholder", "input")) return;

    DOM.extend("[placeholder]", [
        "input[tabindex=-1 style='box-sizing: border-box; position: absolute; color: graytext; background: none no-repeat 0 0; border-color: transparent']"
    ], {
        constructor: function(holder) {
            var offset = this.offset();

            this
                .on("focus", holder, "hide")
                .on("blur", this, "_showPlaceholder", [holder]);

            holder
                .set(this.get("placeholder"))
                .setStyle("width", offset.right - offset.left)
                .on("click", this, "fire", ["focus"]);

            if (this.get() || this.isFocused()) holder.hide();

            this.before(holder);
        },
        _showPlaceholder: function(holder) {
            if (!this.get()) holder.show();
        }
    });
}(window.DOM));
