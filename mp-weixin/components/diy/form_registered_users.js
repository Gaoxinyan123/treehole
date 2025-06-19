(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["components/diy/form_registered_users"],{"3ade":function(e,t,o){"use strict";(function(e){t["a"]={model:{prop:"form",event:"change"},props:{form:{type:Object,default:function(){return{url_upload:"~/api/registered_users/upload?"}}}},data:function(){return{}},methods:{change_img:function(t){var o=this;o.upload_img_flag=!1,e.chooseImage({count:1,sizeType:["original","compressed"],sourceType:["album"],success:function(n){var r=n.tempFilePaths,s=e.uploadFile({url:o.$fullUrl("/api/registered_users/upload?"),filePath:r[0],name:"file",formData:{i_want_to_customize:"test"},header:{"x-auth-token":"null"},success:function(e){var n=JSON.parse(e.data).result.url;o.$delete(o.form,t),o.$set(o.form,t,n),o.handleBlur(n,t)}});s.onProgressUpdate((function(e){o.percent=e.progress,console.log("上传进度"+e.progress),console.log("已经上传的数据长度"+e.totalBytesSent),console.log("预期需要上传的数据总长度"+e.totalBytesExpectedToSend)}))},error:function(e){console.log(e)}})},handleBlur:function(e,t){this.$emit("change",{value:e,type:t})}},mounted:function(){}}}).call(this,o("543d")["default"])},"6cc7":function(e,t,o){"use strict";var n;o.r(t);var r,s=function(){var e=this,t=e.$createElement,o=(e._self._c,e.$check_register_field("add","mobile_phone_number","/registered_users/view"));e.$mp.data=Object.assign({},{$root:{m0:o}})},a=[],l=o("3ade"),u=l["a"],c=(o("e199"),o("f0c5")),i=Object(c["a"])(u,s,a,!1,null,null,null,!1,n,r);t["default"]=i.exports},e199:function(e,t,o){"use strict";var n=o("fea9"),r=o.n(n);r.a},fea9:function(e,t,o){}}]);
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'components/diy/form_registered_users-create-component',
    {
        'components/diy/form_registered_users-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('543d')['createComponent'](__webpack_require__("6cc7"))
        })
    },
    [['components/diy/form_registered_users-create-component']]
]);
