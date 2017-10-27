/**
 * 
 */

function ajaxReqUploadVideo(aliyunVodInfo,serverUrl=null)
{
	if(serverUrl==null)
	{
		serverUrl = getRootPath() + "/vod/"  + "/requestUploadVideo";
	}
    let data = aliyunVodInfo;
    console.log(JSON.stringify(data));
	ajaxPost(serverUrl,data,function successLogin(data,textStatus){
		console.log(data);
		if(data.retCode==0)
		{
			let retAliyunVodInfo = data.responseInfo;
			return [data.retCode,retAliyunVodInfo];
		}
		//successCallback(loginUserSession);
	},
	function errorLogin(xhr,testStatus){
		//errorCallback(xhr,testStatus);
		return [-1,null];
	});
}
/**
 * 请求文件上传列表
 * @param aliyunVodInfoList
 * @param serverUrl
 * @returns
 */
function ajaxReqUploadVideoList(aliyunVodInfoList,successCallback,serverUrl=null)
{
	if(serverUrl==null)
	{
		serverUrl = getRootPath() + "/vod/"  + "/requestUpVideoList";
	}
    let data ={
    		"transId":"000000",
    		"reqParms":JSON.stringify(aliyunVodInfoList)
    };
    console.log(JSON.stringify(data));
	ajaxPost(serverUrl,data,function successLogin(data,textStatus){
		console.log(data);
		if(data.retCode==0)
		{
			let retAliyunVodInfo = data.responseInfo;
			//return [data.retCode,retAliyunVodInfo];
			successCallback(data.retCode,retAliyunVodInfo);
			return;
		}
		successCallback(data.retCode,null);
	},
	function errorLogin(xhr,testStatus){
		//errorCallback(xhr,testStatus);
		//return [-1,null];
		successCallback(-1,null);
	});
}

/**
 * 视频播放请求授权
 * @param vodPlayInfo
 * @param successCallback
 * @param serverUrl
 * @returns
*/ 
function ajaxReqPlayVideo(vodPlayInfo,successCallback,serverUrl=null)
{
	if(serverUrl==null)
	{
		serverUrl = getRootPath() + "/vod/"  + "/requestPlayAuth";
	}
    let data =vodPlayInfo;
    console.log(JSON.stringify(data));
	ajaxPost(serverUrl,data,function successLogin(data,textStatus){
		console.log(data);
		if(data.retCode==0)
		{
			let playAuth = data.responseInfo;
			//return [data.retCode,retAliyunVodInfo];
			successCallback(data.retCode,playAuth);
			return;
		}
		successCallback(data.retCode,null);
	},
	function errorLogin(xhr,testStatus){
		//errorCallback(xhr,testStatus);
		//return [-1,null];
		successCallback(-1,null);
	});
}
