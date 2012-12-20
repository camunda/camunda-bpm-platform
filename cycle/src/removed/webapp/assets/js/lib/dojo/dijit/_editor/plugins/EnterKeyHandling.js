//>>built
define("dijit/_editor/plugins/EnterKeyHandling",["dojo/_base/declare","dojo/dom-construct","dojo/_base/event","dojo/keys","dojo/_base/lang","dojo/sniff","dojo/_base/window","dojo/window","../_Plugin","../RichText","../range","../../_base/focus"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c){
return _1("dijit._editor.plugins.EnterKeyHandling",_9,{blockNodeForEnter:"BR",constructor:function(_d){
if(_d){
if("blockNodeForEnter" in _d){
_d.blockNodeForEnter=_d.blockNodeForEnter.toUpperCase();
}
_5.mixin(this,_d);
}
},setEditor:function(_e){
if(this.editor===_e){
return;
}
this.editor=_e;
if(this.blockNodeForEnter=="BR"){
this.editor.customUndo=true;
_e.onLoadDeferred.then(_5.hitch(this,function(d){
this.connect(_e.document,"onkeypress",function(e){
if(e.charOrCode==_4.ENTER){
var ne=_5.mixin({},e);
ne.shiftKey=true;
if(!this.handleEnterKey(ne)){
_3.stop(e);
}
}
});
if(_6("ie")==9){
this.connect(_e.document,"onpaste",function(e){
setTimeout(dojo.hitch(this,function(){
var r=this.editor.document.selection.createRange();
r.move("character",-1);
r.select();
r.move("character",1);
r.select();
}),0);
});
}
return d;
}));
}else{
if(this.blockNodeForEnter){
var h=_5.hitch(this,this.handleEnterKey);
_e.addKeyHandler(13,0,0,h);
_e.addKeyHandler(13,0,1,h);
this.connect(this.editor,"onKeyPressed","onKeyPressed");
}
}
},onKeyPressed:function(){
if(this._checkListLater){
if(_7.withGlobal(this.editor.window,"isCollapsed",_c)){
var _f=this.editor._sCall("getAncestorElement",["LI"]);
if(!_f){
_a.prototype.execCommand.call(this.editor,"formatblock",this.blockNodeForEnter);
var _10=this.editor._sCall("getAncestorElement",[this.blockNodeForEnter]);
if(_10){
_10.innerHTML=this.bogusHtmlContent;
if(_6("ie")){
var r=this.editor.document.selection.createRange();
r.move("character",-1);
r.select();
}
}else{
console.error("onKeyPressed: Cannot find the new block node");
}
}else{
if(_6("mozilla")){
if(_f.parentNode.parentNode.nodeName=="LI"){
_f=_f.parentNode.parentNode;
}
}
var fc=_f.firstChild;
if(fc&&fc.nodeType==1&&(fc.nodeName=="UL"||fc.nodeName=="OL")){
_f.insertBefore(fc.ownerDocument.createTextNode(" "),fc);
var _11=_b.create(this.editor.window);
_11.setStart(_f.firstChild,0);
var _12=_b.getSelection(this.editor.window,true);
_12.removeAllRanges();
_12.addRange(_11);
}
}
}
this._checkListLater=false;
}
if(this._pressedEnterInBlock){
if(this._pressedEnterInBlock.previousSibling){
this.removeTrailingBr(this._pressedEnterInBlock.previousSibling);
}
delete this._pressedEnterInBlock;
}
},bogusHtmlContent:"&#160;",blockNodes:/^(?:P|H1|H2|H3|H4|H5|H6|LI)$/,handleEnterKey:function(e){
var _13,_14,_15,_16,_17,_18,doc=this.editor.document,br,rs,txt;
if(e.shiftKey){
var _19=this.editor._sCall("getParentElement",[]);
var _1a=_b.getAncestor(_19,this.blockNodes);
if(_1a){
if(_1a.tagName=="LI"){
return true;
}
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
if(!_14.collapsed){
_14.deleteContents();
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
}
if(_b.atBeginningOfContainer(_1a,_14.startContainer,_14.startOffset)){
br=doc.createElement("br");
_15=_b.create(this.editor.window);
_1a.insertBefore(br,_1a.firstChild);
_15.setStartAfter(br);
_13.removeAllRanges();
_13.addRange(_15);
}else{
if(_b.atEndOfContainer(_1a,_14.startContainer,_14.startOffset)){
_15=_b.create(this.editor.window);
br=doc.createElement("br");
_1a.appendChild(br);
_1a.appendChild(doc.createTextNode(" "));
_15.setStart(_1a.lastChild,0);
_13.removeAllRanges();
_13.addRange(_15);
}else{
rs=_14.startContainer;
if(rs&&rs.nodeType==3){
txt=rs.nodeValue;
_16=doc.createTextNode(txt.substring(0,_14.startOffset));
_17=doc.createTextNode(txt.substring(_14.startOffset));
_18=doc.createElement("br");
if(_17.nodeValue==""&&_6("webkit")){
_17=doc.createTextNode(" ");
}
_2.place(_16,rs,"after");
_2.place(_18,_16,"after");
_2.place(_17,_18,"after");
_2.destroy(rs);
_15=_b.create(this.editor.window);
_15.setStart(_17,0);
_13.removeAllRanges();
_13.addRange(_15);
return false;
}
return true;
}
}
}else{
_13=_b.getSelection(this.editor.window);
if(_13.rangeCount){
_14=_13.getRangeAt(0);
if(_14&&_14.startContainer){
if(!_14.collapsed){
_14.deleteContents();
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
}
rs=_14.startContainer;
if(rs&&rs.nodeType==3){
var _1b=false;
var _1c=_14.startOffset;
if(rs.length<_1c){
ret=this._adjustNodeAndOffset(rs,_1c);
rs=ret.node;
_1c=ret.offset;
}
txt=rs.nodeValue;
_16=doc.createTextNode(txt.substring(0,_1c));
_17=doc.createTextNode(txt.substring(_1c));
_18=doc.createElement("br");
if(!_17.length){
_17=doc.createTextNode(" ");
_1b=true;
}
if(_16.length){
_2.place(_16,rs,"after");
}else{
_16=rs;
}
_2.place(_18,_16,"after");
_2.place(_17,_18,"after");
_2.destroy(rs);
_15=_b.create(this.editor.window);
_15.setStart(_17,0);
_15.setEnd(_17,_17.length);
_13.removeAllRanges();
_13.addRange(_15);
if(_1b&&!_6("webkit")){
this.editor._sCall("remove",[]);
}else{
this.editor._sCall("collapse",[true]);
}
}else{
var _1d;
if(_14.startOffset>=0){
_1d=rs.childNodes[_14.startOffset];
}
var _18=doc.createElement("br");
var _17=doc.createTextNode(" ");
if(!_1d){
rs.appendChild(_18);
rs.appendChild(_17);
}else{
_2.place(_18,_1d,"before");
_2.place(_17,_18,"after");
}
_15=_b.create(this.editor.window);
_15.setStart(_17,0);
_15.setEnd(_17,_17.length);
_13.removeAllRanges();
_13.addRange(_15);
this.editor._sCall("collapse",[true]);
}
}
}else{
_a.prototype.execCommand.call(this.editor,"inserthtml","<br>");
}
}
return false;
}
var _1e=true;
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
if(!_14.collapsed){
_14.deleteContents();
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
}
var _1f=_b.getBlockAncestor(_14.endContainer,null,this.editor.editNode);
var _20=_1f.blockNode;
if((this._checkListLater=(_20&&(_20.nodeName=="LI"||_20.parentNode.nodeName=="LI")))){
if(_6("mozilla")){
this._pressedEnterInBlock=_20;
}
if(/^(\s|&nbsp;|&#160;|\xA0|<span\b[^>]*\bclass=['"]Apple-style-span['"][^>]*>(\s|&nbsp;|&#160;|\xA0)<\/span>)?(<br>)?$/.test(_20.innerHTML)){
_20.innerHTML="";
if(_6("webkit")){
_15=_b.create(this.editor.window);
_15.setStart(_20,0);
_13.removeAllRanges();
_13.addRange(_15);
}
this._checkListLater=false;
}
return true;
}
if(!_1f.blockNode||_1f.blockNode===this.editor.editNode){
try{
_a.prototype.execCommand.call(this.editor,"formatblock",this.blockNodeForEnter);
}
catch(e2){
}
_1f={blockNode:this.editor._sCall("getAncestorElement",[this.blockNodeForEnter]),blockContainer:this.editor.editNode};
if(_1f.blockNode){
if(_1f.blockNode!=this.editor.editNode&&(!(_1f.blockNode.textContent||_1f.blockNode.innerHTML).replace(/^\s+|\s+$/g,"").length)){
this.removeTrailingBr(_1f.blockNode);
return false;
}
}else{
_1f.blockNode=this.editor.editNode;
}
_13=_b.getSelection(this.editor.window);
_14=_13.getRangeAt(0);
}
var _21=doc.createElement(this.blockNodeForEnter);
_21.innerHTML=this.bogusHtmlContent;
this.removeTrailingBr(_1f.blockNode);
var _22=_14.endOffset;
var _23=_14.endContainer;
if(_23.length<_22){
var ret=this._adjustNodeAndOffset(_23,_22);
_23=ret.node;
_22=ret.offset;
}
if(_b.atEndOfContainer(_1f.blockNode,_23,_22)){
if(_1f.blockNode===_1f.blockContainer){
_1f.blockNode.appendChild(_21);
}else{
_2.place(_21,_1f.blockNode,"after");
}
_1e=false;
_15=_b.create(this.editor.window);
_15.setStart(_21,0);
_13.removeAllRanges();
_13.addRange(_15);
if(this.editor.height){
_8.scrollIntoView(_21);
}
}else{
if(_b.atBeginningOfContainer(_1f.blockNode,_14.startContainer,_14.startOffset)){
_2.place(_21,_1f.blockNode,_1f.blockNode===_1f.blockContainer?"first":"before");
if(_21.nextSibling&&this.editor.height){
_15=_b.create(this.editor.window);
_15.setStart(_21.nextSibling,0);
_13.removeAllRanges();
_13.addRange(_15);
_8.scrollIntoView(_21.nextSibling);
}
_1e=false;
}else{
if(_1f.blockNode===_1f.blockContainer){
_1f.blockNode.appendChild(_21);
}else{
_2.place(_21,_1f.blockNode,"after");
}
_1e=false;
if(_1f.blockNode.style){
if(_21.style){
if(_1f.blockNode.style.cssText){
_21.style.cssText=_1f.blockNode.style.cssText;
}
}
}
rs=_14.startContainer;
var _24;
if(rs&&rs.nodeType==3){
var _25,_26;
_22=_14.endOffset;
if(rs.length<_22){
ret=this._adjustNodeAndOffset(rs,_22);
rs=ret.node;
_22=ret.offset;
}
txt=rs.nodeValue;
_16=doc.createTextNode(txt.substring(0,_22));
_17=doc.createTextNode(txt.substring(_22,txt.length));
_2.place(_16,rs,"before");
_2.place(_17,rs,"after");
_2.destroy(rs);
var _27=_16.parentNode;
while(_27!==_1f.blockNode){
var tg=_27.tagName;
var _28=doc.createElement(tg);
if(_27.style){
if(_28.style){
if(_27.style.cssText){
_28.style.cssText=_27.style.cssText;
}
}
}
if(_27.tagName==="FONT"){
if(_27.color){
_28.color=_27.color;
}
if(_27.face){
_28.face=_27.face;
}
if(_27.size){
_28.size=_27.size;
}
}
_25=_17;
while(_25){
_26=_25.nextSibling;
_28.appendChild(_25);
_25=_26;
}
_2.place(_28,_27,"after");
_16=_27;
_17=_28;
_27=_27.parentNode;
}
_25=_17;
if(_25.nodeType==1||(_25.nodeType==3&&_25.nodeValue)){
_21.innerHTML="";
}
_24=_25;
while(_25){
_26=_25.nextSibling;
_21.appendChild(_25);
_25=_26;
}
}
_15=_b.create(this.editor.window);
var _29;
var _2a=_24;
if(this.blockNodeForEnter!=="BR"){
while(_2a){
_29=_2a;
_26=_2a.firstChild;
_2a=_26;
}
if(_29&&_29.parentNode){
_21=_29.parentNode;
_15.setStart(_21,0);
_13.removeAllRanges();
_13.addRange(_15);
if(this.editor.height){
_8.scrollIntoView(_21);
}
if(_6("mozilla")){
this._pressedEnterInBlock=_1f.blockNode;
}
}else{
_1e=true;
}
}else{
_15.setStart(_21,0);
_13.removeAllRanges();
_13.addRange(_15);
if(this.editor.height){
_8.scrollIntoView(_21);
}
if(_6("mozilla")){
this._pressedEnterInBlock=_1f.blockNode;
}
}
}
}
return _1e;
},_adjustNodeAndOffset:function(_2b,_2c){
while(_2b.length<_2c&&_2b.nextSibling&&_2b.nextSibling.nodeType==3){
_2c=_2c-_2b.length;
_2b=_2b.nextSibling;
}
return {"node":_2b,"offset":_2c};
},removeTrailingBr:function(_2d){
var _2e=/P|DIV|LI/i.test(_2d.tagName)?_2d:this.editor._sCall("getParentOfType",[_2d,["P","DIV","LI"]]);
if(!_2e){
return;
}
if(_2e.lastChild){
if((_2e.childNodes.length>1&&_2e.lastChild.nodeType==3&&/^[\s\xAD]*$/.test(_2e.lastChild.nodeValue))||_2e.lastChild.tagName=="BR"){
_2.destroy(_2e.lastChild);
}
}
if(!_2e.childNodes.length){
_2e.innerHTML=this.bogusHtmlContent;
}
}});
});
