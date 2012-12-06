//>>built
require({cache:{"url:dijit/form/templates/TextBox.html":"<div class=\"dijit dijitReset dijitInline dijitLeft\" id=\"widget_${id}\" role=\"presentation\"\n\t><div class=\"dijitReset dijitInputField dijitInputContainer\"\n\t\t><input class=\"dijitReset dijitInputInner\" data-dojo-attach-point='textbox,focusNode' autocomplete=\"off\"\n\t\t\t${!nameAttrSetting} type='${type}'\n\t/></div\n></div>\n"}});
define("dijit/form/TextBox",["dojo/_base/declare","dojo/dom-construct","dojo/dom-style","dojo/_base/kernel","dojo/_base/lang","dojo/sniff","./_FormValueWidget","./_TextBoxMixin","dojo/text!./templates/TextBox.html","../main"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a){
var _b=_1("dijit.form.TextBox",[_7,_8],{templateString:_9,_singleNodeTemplate:"<input class=\"dijit dijitReset dijitLeft dijitInputField\" data-dojo-attach-point=\"textbox,focusNode\" autocomplete=\"off\" type=\"${type}\" ${!nameAttrSetting} />",_buttonInputDisabled:_6("ie")?"disabled":"",baseClass:"dijitTextBox",postMixInProperties:function(){
var _c=this.type.toLowerCase();
if(this.templateString&&this.templateString.toLowerCase()=="input"||((_c=="hidden"||_c=="file")&&this.templateString==this.constructor.prototype.templateString)){
this.templateString=this._singleNodeTemplate;
}
this.inherited(arguments);
},postCreate:function(){
this.inherited(arguments);
if(_6("ie")<9){
this.defer(function(){
try{
var s=_3.getComputedStyle(this.domNode);
if(s){
var ff=s.fontFamily;
if(ff){
var _d=this.domNode.getElementsByTagName("INPUT");
if(_d){
for(var i=0;i<_d.length;i++){
_d[i].style.fontFamily=ff;
}
}
}
}
}
catch(e){
}
});
}
},_onInput:function(e){
this.inherited(arguments);
if(this.intermediateChanges){
this.defer(function(){
this._handleOnChange(this.get("value"),false);
});
}
},_setPlaceHolderAttr:function(v){
this._set("placeHolder",v);
if(!this._phspan){
this._attachPoints.push("_phspan");
this._phspan=_2.create("span",{className:"dijitPlaceHolder dijitInputField"},this.textbox,"after");
}
this._phspan.innerHTML="";
this._phspan.appendChild(this._phspan.ownerDocument.createTextNode(v));
this._updatePlaceHolder();
},_updatePlaceHolder:function(){
if(this._phspan){
this._phspan.style.display=(this.placeHolder&&!this.focused&&!this.textbox.value)?"":"none";
}
},_setValueAttr:function(_e,_f,_10){
this.inherited(arguments);
this._updatePlaceHolder();
},getDisplayedValue:function(){
_4.deprecated(this.declaredClass+"::getDisplayedValue() is deprecated. Use get('displayedValue') instead.","","2.0");
return this.get("displayedValue");
},setDisplayedValue:function(_11){
_4.deprecated(this.declaredClass+"::setDisplayedValue() is deprecated. Use set('displayedValue', ...) instead.","","2.0");
this.set("displayedValue",_11);
},_onBlur:function(e){
if(this.disabled){
return;
}
this.inherited(arguments);
this._updatePlaceHolder();
if(_6("mozilla")){
if(this.selectOnClick){
this.textbox.selectionStart=this.textbox.selectionEnd=undefined;
}
}
},_onFocus:function(by){
if(this.disabled||this.readOnly){
return;
}
this.inherited(arguments);
this._updatePlaceHolder();
}});
if(_6("ie")){
_b.prototype._isTextSelected=function(){
var _12=this.ownerDocument.selection.createRange();
var _13=_12.parentElement();
return _13==this.textbox&&_12.text.length>0;
};
_a._setSelectionRange=_8._setSelectionRange=function(_14,_15,_16){
if(_14.createTextRange){
var r=_14.createTextRange();
r.collapse(true);
r.moveStart("character",-99999);
r.moveStart("character",_15);
r.moveEnd("character",_16-_15);
r.select();
}
};
}
return _b;
});
