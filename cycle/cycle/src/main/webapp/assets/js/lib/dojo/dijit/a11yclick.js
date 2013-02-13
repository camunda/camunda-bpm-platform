//>>built
define("dijit/a11yclick",["dojo/on","dojo/_base/array","dojo/keys","dojo/_base/declare","dojo/has","dojo/_base/unload","dojo/_base/window"],function(on,_1,_2,_3,_4,_5,_6){
var _7=null;
if(_4("dom-addeventlistener")){
_6.doc.addEventListener("keydown",function(_8){
_7=_8.target;
},true);
}else{
(function(){
var _9=function(_a){
_7=_a.srcElement;
};
_6.doc.attachEvent("onkeydown",_9);
_5.addOnWindowUnload(function(){
_6.doc.detachEvent("onkeydown",_9);
});
})();
}
function _b(e){
return (e.keyCode===_2.ENTER||e.keyCode===_2.SPACE)&&!e.ctrlKey&&!e.shiftKey&&!e.altKey&&!e.metaKey;
};
return function(_c,_d){
if(/input|button/i.test(_c.nodeName)){
return on(_c,"click",_d);
}else{
var _e=[on(_c,"keydown",function(e){
if(_b(e)){
_7=e.target;
e.preventDefault();
}
}),on(_c,"keyup",function(e){
if(_b(e)&&e.target==_7){
_7=null;
on.emit(e.target,"click",{cancelable:true,bubbles:true});
}
}),on(_c,"click",function(e){
_d.call(this,e);
})];
if(_4("touch")){
var _f;
_e.push(on(_c,"touchend",function(e){
var _10=e.target;
_f=setTimeout(function(){
_f=null;
on.emit(_10,"click",{cancelable:true,bubbles:true});
},600);
}),on(_c,"click",function(e){
if(_f){
clearTimeout(_f);
}
}));
}
return {remove:function(){
_1.forEach(_e,function(h){
h.remove();
});
if(_f){
clearTimeout(_f);
_f=null;
}
}};
}
};
return ret;
});
