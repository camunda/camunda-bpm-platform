//>>built
define("dijit/a11y",["dojo/_base/array","dojo/_base/config","dojo/_base/declare","dojo/dom","dojo/dom-attr","dojo/dom-style","dojo/sniff","./main"],function(_1,_2,_3,_4,_5,_6,_7,_8){
var _9=(_8._isElementShown=function(_a){
var s=_6.get(_a);
return (s.visibility!="hidden")&&(s.visibility!="collapsed")&&(s.display!="none")&&(_5.get(_a,"type")!="hidden");
});
_8.hasDefaultTabStop=function(_b){
switch(_b.nodeName.toLowerCase()){
case "a":
return _5.has(_b,"href");
case "area":
case "button":
case "input":
case "object":
case "select":
case "textarea":
return true;
case "iframe":
var _c;
try{
var _d=_b.contentDocument;
if("designMode" in _d&&_d.designMode=="on"){
return true;
}
_c=_d.body;
}
catch(e1){
try{
_c=_b.contentWindow.document.body;
}
catch(e2){
return false;
}
}
return _c&&(_c.contentEditable=="true"||(_c.firstChild&&_c.firstChild.contentEditable=="true"));
default:
return _b.contentEditable=="true";
}
};
var _e=(_8.isTabNavigable=function(_f){
if(_5.get(_f,"disabled")){
return false;
}else{
if(_5.has(_f,"tabIndex")){
return _5.get(_f,"tabIndex")>=0;
}else{
return _8.hasDefaultTabStop(_f);
}
}
});
_8._getTabNavigable=function(_10){
var _11,_12,_13,_14,_15,_16,_17={};
function _18(_19){
return _19&&_19.tagName.toLowerCase()=="input"&&_19.type&&_19.type.toLowerCase()=="radio"&&_19.name&&_19.name.toLowerCase();
};
var _1a=function(_1b){
for(var _1c=_1b.firstChild;_1c;_1c=_1c.nextSibling){
if(_1c.nodeType!=1||(_7("ie")&&_1c.scopeName!=="HTML")||!_9(_1c)){
continue;
}
if(_e(_1c)){
var _1d=+_5.get(_1c,"tabIndex");
if(!_5.has(_1c,"tabIndex")||_1d==0){
if(!_11){
_11=_1c;
}
_12=_1c;
}else{
if(_1d>0){
if(!_13||_1d<_14){
_14=_1d;
_13=_1c;
}
if(!_15||_1d>=_16){
_16=_1d;
_15=_1c;
}
}
}
var rn=_18(_1c);
if(_5.get(_1c,"checked")&&rn){
_17[rn]=_1c;
}
}
if(_1c.nodeName.toUpperCase()!="SELECT"){
_1a(_1c);
}
}
};
if(_9(_10)){
_1a(_10);
}
function rs(_1e){
return _17[_18(_1e)]||_1e;
};
return {first:rs(_11),last:rs(_12),lowest:rs(_13),highest:rs(_15)};
};
_8.getFirstInTabbingOrder=function(_1f,doc){
var _20=_8._getTabNavigable(_4.byId(_1f,doc));
return _20.lowest?_20.lowest:_20.first;
};
_8.getLastInTabbingOrder=function(_21,doc){
var _22=_8._getTabNavigable(_4.byId(_21,doc));
return _22.last?_22.last:_22.highest;
};
return {hasDefaultTabStop:_8.hasDefaultTabStop,isTabNavigable:_8.isTabNavigable,_getTabNavigable:_8._getTabNavigable,getFirstInTabbingOrder:_8.getFirstInTabbingOrder,getLastInTabbingOrder:_8.getLastInTabbingOrder};
});
