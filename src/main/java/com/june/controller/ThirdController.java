package com.june.controller;
/**
 * 微信第三方平台
 * @author wuchaoqun
 *
 */

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.june.config.WechatConfig;
import com.june.constant.RedisKey;
import com.june.service.ThirdService;
import com.june.util.HttpClientUtil;
import com.june.util.RedisUtil;
import com.june.util.XmlUtil;
import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import redis.clients.jedis.Jedis;

@Controller
@RequestMapping("/wx")
public class ThirdController {
	
	@Autowired
	private ThirdService thirdService;
	
	private Logger logger = LoggerFactory.getLogger(ThirdController.class);
	
	/**
	 * 授权事件接收url，返回ticket
	 * @param request
	 * @param timestamp
	 * @param nonce
	 * @param msgSignature
	 * @param postData
	 * @return
	 */
	@RequestMapping("/third_auth")
	@ResponseBody
	public String third_auth(HttpServletRequest request,@RequestParam("timestamp")String timestamp, @RequestParam("nonce")String nonce,
            @RequestParam("msg_signature")String msgSignature, @RequestBody String postData) {
		String appId=WechatConfig.THIRDAPPID;
		String encodingAesKey=WechatConfig.ENCODINGAESKEY;
		String token=WechatConfig.THIRDTOKEN;
		try {
			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
			String result = pc.decryptMsg(msgSignature, timestamp, nonce, postData);
			Map<String,String> map = XmlUtil.xmlToMap(result);
			String ticket = map.get("ComponentVerifyTicket");
			RedisUtil.set(RedisKey.WX_TICKET,ticket);
			logger.info(result);
			logger.info(map.toString());
		} catch (AesException e) {
			logger.info(e.getMessage());
		} catch (Exception e) {
			logger.info(e.getMessage());
		}	
		return "success";
	}
	/**
	 * 获取ticket
	 * @return
	 */
	@RequestMapping("/ticket")
	@ResponseBody
	public String getTicket() {
		return RedisUtil.get(RedisKey.WX_TICKET);
	}
	
	/**
	 * 获取accessToken
	 * @return
	 */
	@RequestMapping("/access_token")
	@ResponseBody
	public String getAccessToken() {
		return RedisUtil.get(RedisKey.WX_ACCESS_TOKEN);
	}
	
	/**
	 * 刷新token
	 * @return
	 */
	@RequestMapping("/refresh_access_token")
	@ResponseBody
	public String refreshAccessToken() {
		return thirdService.refreshToken();
	}	
	
	/**
	 * 成功授权后返回的授权码
	 * @param auth_code
	 * @param expires_in
	 * @return
	 */
	@RequestMapping("/success_auth")
	@ResponseBody
	public String successAuth(String auth_code,int expires_in) {
		RedisUtil.set(RedisKey.WX_AUTH_CODE, auth_code);
		return auth_code;
	}
	
	/**
	 * 授权页面
	 */
	@RequestMapping("/auth_page")
	public void  authPage(HttpServletResponse response) {
		StringBuilder sb = new StringBuilder();
		String url = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?";
		String appid = WechatConfig.THIRDAPPID;
		String preAuthCode=thirdService.preAuthCode();
		String redirectUri="http://wx.xxxx.com/wx/success_auth";
		sb.append(url);
		sb.append("component_appid=").append(appid);
		sb.append("&pre_auth_code=").append(preAuthCode);
		sb.append("&redirect_uri=").append(redirectUri);
		try {
			response.getWriter().write("<a href='"+sb.toString()+"'>"+"auth !"+"</a>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 使用授权码换取公众号或小程序的接口调用凭据和授权信息
	 */
	@RequestMapping("/query_auth")
	public void queryAuth() {
		Jedis jedis = RedisUtil.getJedis();
		StringBuilder sb = new StringBuilder();
		sb.append("https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=");
		sb.append(jedis.get(RedisKey.WX_ACCESS_TOKEN));
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("component_appid", WechatConfig.THIRDAPPID);
		jsonObject.put("authorization_code", jedis.get(RedisKey.WX_AUTH_CODE));
		String result = HttpClientUtil.doPostJson(sb.toString(), jsonObject.toString());
		JSONObject results = new JSONObject(result);
		JSONObject authorizationInfo = results.getJSONObject("authorization_info");
		logger.info(result);
		jedis.set(RedisKey.WX_AUTHORIZER_ACCESS_TOKEN, authorizationInfo.getString("authorizer_access_token"));
		jedis.set(RedisKey.WX_AUTHORIZER_REFRESH_TOKEN, authorizationInfo.getString("authorizer_refresh_token"));
		jedis.close();
	}
	
	/**
	*  通过code换取access_token
	 */
	@RequestMapping("/recieve_code")
	public void recieveCode(HttpServletRequest req,HttpServletResponse rep,String code,String state,String appid) throws IOException {
		StringBuilder sb=new StringBuilder();
		String component_appid = WechatConfig.THIRDAPPID;
		String url="https://api.weixin.qq.com/sns/oauth2/component/access_token?";
		sb.append(url);
		sb.append("appid=").append(appid);
		sb.append("&code=").append(code);
		sb.append("&grant_type=").append("authorization_code");
		sb.append("&component_appid=").append(component_appid);
		sb.append("&component_access_token=").append(RedisUtil.get(RedisKey.WX_ACCESS_TOKEN));
		logger.info(sb.toString());
		String result = HttpClientUtil.doGet(sb.toString(), null);
         logger.info(result);
		JSONObject json = new JSONObject(result);
		String access_token = json.getString("access_token");
		String openid = json.getString("openid");
		state=state.replaceAll("\\[=a\\]", "&"); 
		
		StringBuilder redirect_url=new StringBuilder();
		redirect_url.append(state);
		redirect_url.append("?access_token=").append(access_token);
		redirect_url.append("&openid=").append(openid);
		

		try {
			rep.sendRedirect(redirect_url.toString());
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rep.sendRedirect(null);
			return;
		}
	}
	
}
