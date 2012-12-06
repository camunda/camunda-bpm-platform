//>>built
require({cache:{"url:dijit/templates/InlineEditBox.html":"<span data-dojo-attach-point=\"editNode\" role=\"presentation\" class=\"dijitReset dijitInline dijitOffScreen\"\n\tdata-dojo-attach-event=\"onkeypress: _onKeyPress\"\n\t><span data-dojo-attach-point=\"editorPlaceholder\"></span\n\t><span data-dojo-attach-point=\"buttonContainer\"\n\t\t><button data-dojo-type=\"dijit/form/Button\" data-dojo-props=\"label: '${buttonSave}', 'class': 'saveButton'\"\n\t\t\tdata-dojo-attach-point=\"saveButton\" data-dojo-attach-event=\"onClick:save\"></button\n\t\t><button data-dojo-type=\"dijit/form/Button\"  data-dojo-props=\"label: '${buttonCancel}', 'class': 'cancelButton'\"\n\t\t\tdata-dojo-attach-point=\"cancelButton\" data-dojo-attach-event=\"onClick:cancel\"></button\n\t></span\n></span>\n"}});
define("dijit/InlineEditBox",["dojo/_base/array","dojo/_base/declare","dojo/dom-attr","dojo/dom-class","dojo/dom-construct","dojo/dom-style","dojo/_base/event","dojo/i18n","dojo/_base/kernel","dojo/keys","dojo/_base/lang","dojo/sniff","dojo/when","./focus","./_Widget","./_TemplatedMixin","./_WidgetsInTemplateMixin","./_Container","./form/Button","./form/_TextBoxMixin","./form/TextBox","dojo/text!./templates/InlineEditBox.html","dojo/i18n!./nls/common"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,fm,_e,_f,_10,_11,_12,_13,_14,_15){
var _16=_2("dijit._InlineEditor",[_e,_f,_10],{templateString:_15,postMixInProperties:function(){
this.inherited(arguments);
this.messages=_8.getLocalization("dijit","common",this.lang);
_1.forEach(["buttonSave","buttonCancel"],function(_17){
if(!this[_17]){
this[_17]=this.messages[_17];
}
},this);
},buildRendering:function(){
this.inherited(arguments);
var Cls=typeof this.editor=="string"?(_b.getObject(this.editor)||require(this.editor)):this.editor;
var _18=this.sourceStyle,_19="line-height:"+_18.lineHeight+";",_1a=_6.getComputedStyle(this.domNode);
_1.forEach(["Weight","Family","Size","Style"],function(_1b){
var _1c=_18["font"+_1b],_1d=_1a["font"+_1b];
if(_1d!=_1c){
_19+="font-"+_1b+":"+_18["font"+_1b]+";";
}
},this);
_1.forEach(["marginTop","marginBottom","marginLeft","marginRight","position","left","top","right","bottom","float","clear","display"],function(_1e){
this.domNode.style[_1e]=_18[_1e];
},this);
var _1f=this.inlineEditBox.width;
if(_1f=="100%"){
_19+="width:100%;";
this.domNode.style.display="block";
}else{
_19+="width:"+(_1f+(Number(_1f)==_1f?"px":""))+";";
}
var _20=_b.delegate(this.inlineEditBox.editorParams,{style:_19,dir:this.dir,lang:this.lang,textDir:this.textDir});
_20["displayedValue" in Cls.prototype?"displayedValue":"value"]=this.value;
this.editWidget=new Cls(_20,this.editorPlaceholder);
if(this.inlineEditBox.autoSave){
_5.destroy(this.buttonContainer);
}
},postCreate:function(){
this.inherited(arguments);
var ew=this.editWidget;
if(this.inlineEditBox.autoSave){
this.connect(ew,"onChange","_onChange");
this.connect(ew,"onKeyPress","_onKeyPress");
}else{
if("intermediateChanges" in ew){
ew.set("intermediateChanges",true);
this.connect(ew,"onChange","_onIntermediateChange");
this.saveButton.set("disabled",true);
}
}
},startup:function(){
this.editWidget.startup();
this.inherited(arguments);
},_onIntermediateChange:function(){
this.saveButton.set("disabled",(this.getValue()==this._resetValue)||!this.enableSave());
},destroy:function(){
this.editWidget.destroy(true);
this.inherited(arguments);
},getValue:function(){
var ew=this.editWidget;
return String(ew.get("displayedValue" in ew?"displayedValue":"value"));
},_onKeyPress:function(e){
if(this.inlineEditBox.autoSave&&this.inlineEditBox.editing){
if(e.altKey||e.ctrlKey){
return;
}
if(e.charOrCode==_a.ESCAPE){
_7.stop(e);
this.cancel(true);
}else{
if(e.charOrCode==_a.ENTER&&e.target.tagName=="INPUT"){
_7.stop(e);
this._onChange();
}
}
}
},_onBlur:function(){
this.inherited(arguments);
if(this.inlineEditBox.autoSave&&this.inlineEditBox.editing){
if(this.getValue()==this._resetValue){
this.cancel(false);
}else{
if(this.enableSave()){
this.save(false);
}
}
}
},_onChange:function(){
if(this.inlineEditBox.autoSave&&this.inlineEditBox.editing&&this.enableSave()){
fm.focus(this.inlineEditBox.displayNode);
}
},enableSave:function(){
return this.editWidget.isValid?this.editWidget.isValid():true;
},focus:function(){
this.editWidget.focus();
if(this.editWidget.focusNode){
fm._onFocusNode(this.editWidget.focusNode);
if(this.editWidget.focusNode.tagName=="INPUT"){
this.defer(function(){
_13.selectInputText(this.editWidget.focusNode);
});
}
}
}});
var _21=_2("dijit.InlineEditBox",_e,{editing:false,autoSave:true,buttonSave:"",buttonCancel:"",renderAsHtml:false,editor:_14,editorWrapper:_16,editorParams:{},disabled:false,onChange:function(){
},onCancel:function(){
},width:"100%",value:"",noValueIndicator:_c("ie")<=6?"<span style='font-family: wingdings; text-decoration: underline;'>&#160;&#160;&#160;&#160;&#x270d;&#160;&#160;&#160;&#160;</span>":"<span style='text-decoration: underline;'>&#160;&#160;&#160;&#160;&#x270d;&#160;&#160;&#160;&#160;</span>",constructor:function(){
this.editorParams={};
},postMixInProperties:function(){
this.inherited(arguments);
this.displayNode=this.srcNodeRef;
var _22={ondijitclick:"_onClick",onmouseover:"_onMouseOver",onmouseout:"_onMouseOut",onfocus:"_onMouseOver",onblur:"_onMouseOut"};
for(var _23 in _22){
this.connect(this.displayNode,_23,_22[_23]);
}
this.displayNode.setAttribute("role","button");
if(!this.displayNode.getAttribute("tabIndex")){
this.displayNode.setAttribute("tabIndex",0);
}
if(!this.value&&!("value" in this.params)){
this.value=_b.trim(this.renderAsHtml?this.displayNode.innerHTML:(this.displayNode.innerText||this.displayNode.textContent||""));
}
if(!this.value){
this.displayNode.innerHTML=this.noValueIndicator;
}
_4.add(this.displayNode,"dijitInlineEditBoxDisplayMode");
},setDisabled:function(_24){
_9.deprecated("dijit.InlineEditBox.setDisabled() is deprecated.  Use set('disabled', bool) instead.","","2.0");
this.set("disabled",_24);
},_setDisabledAttr:function(_25){
this.domNode.setAttribute("aria-disabled",_25?"true":"false");
if(_25){
this.displayNode.removeAttribute("tabIndex");
}else{
this.displayNode.setAttribute("tabIndex",0);
}
_4.toggle(this.displayNode,"dijitInlineEditBoxDisplayModeDisabled",_25);
this._set("disabled",_25);
},_onMouseOver:function(){
if(!this.disabled){
_4.add(this.displayNode,"dijitInlineEditBoxDisplayModeHover");
}
},_onMouseOut:function(){
_4.remove(this.displayNode,"dijitInlineEditBoxDisplayModeHover");
},_onClick:function(e){
if(this.disabled){
return;
}
if(e){
_7.stop(e);
}
this._onMouseOut();
this.defer("edit");
},edit:function(){
if(this.disabled||this.editing){
return;
}
this._set("editing",true);
this._savedTabIndex=_3.get(this.displayNode,"tabIndex")||"0";
if(this.wrapperWidget){
var ew=this.wrapperWidget.editWidget;
ew.set("displayedValue" in ew?"displayedValue":"value",this.value);
}else{
var _26=_5.create("span",null,this.domNode,"before");
var Ewc=typeof this.editorWrapper=="string"?_b.getObject(this.editorWrapper):this.editorWrapper;
this.wrapperWidget=new Ewc({value:this.value,buttonSave:this.buttonSave,buttonCancel:this.buttonCancel,dir:this.dir,lang:this.lang,tabIndex:this._savedTabIndex,editor:this.editor,inlineEditBox:this,sourceStyle:_6.getComputedStyle(this.displayNode),save:_b.hitch(this,"save"),cancel:_b.hitch(this,"cancel"),textDir:this.textDir},_26);
if(!this.wrapperWidget._started){
this.wrapperWidget.startup();
}
if(!this._started){
this.startup();
}
}
var ww=this.wrapperWidget;
_4.add(this.displayNode,"dijitOffScreen");
_4.remove(ww.domNode,"dijitOffScreen");
_6.set(ww.domNode,{visibility:"visible"});
_3.set(this.displayNode,"tabIndex","-1");
_d(ww.editWidget.onLoadDeferred,_b.hitch(ww,function(){
this.defer(function(){
this.focus();
this._resetValue=this.getValue();
});
}));
},_onBlur:function(){
this.inherited(arguments);
if(!this.editing){
}
},destroy:function(){
if(this.wrapperWidget&&!this.wrapperWidget._destroyed){
this.wrapperWidget.destroy();
delete this.wrapperWidget;
}
this.inherited(arguments);
},_showText:function(_27){
var ww=this.wrapperWidget;
_6.set(ww.domNode,{visibility:"hidden"});
_4.add(ww.domNode,"dijitOffScreen");
_4.remove(this.displayNode,"dijitOffScreen");
_3.set(this.displayNode,"tabIndex",this._savedTabIndex);
if(_27){
fm.focus(this.displayNode);
}
},save:function(_28){
if(this.disabled||!this.editing){
return;
}
this._set("editing",false);
var ww=this.wrapperWidget;
var _29=ww.getValue();
this.set("value",_29);
this._showText(_28);
},setValue:function(val){
_9.deprecated("dijit.InlineEditBox.setValue() is deprecated.  Use set('value', ...) instead.","","2.0");
return this.set("value",val);
},_setValueAttr:function(val){
val=_b.trim(val);
var _2a=this.renderAsHtml?val:val.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;").replace(/\n/g,"<br>");
this.displayNode.innerHTML=_2a||this.noValueIndicator;
this._set("value",val);
if(this._started){
this.defer(function(){
this.onChange(val);
});
}
if(this.textDir=="auto"){
this.applyTextDir(this.displayNode,this.displayNode.innerText);
}
},getValue:function(){
_9.deprecated("dijit.InlineEditBox.getValue() is deprecated.  Use get('value') instead.","","2.0");
return this.get("value");
},cancel:function(_2b){
if(this.disabled||!this.editing){
return;
}
this._set("editing",false);
this.defer("onCancel");
this._showText(_2b);
},_setTextDirAttr:function(_2c){
if(!this._created||this.textDir!=_2c){
this._set("textDir",_2c);
this.applyTextDir(this.displayNode,this.displayNode.innerText);
this.displayNode.align=this.dir=="rtl"?"right":"left";
}
}});
_21._InlineEditor=_16;
return _21;
});
