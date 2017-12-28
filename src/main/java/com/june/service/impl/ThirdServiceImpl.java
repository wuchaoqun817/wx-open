package com.june.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.june.config.WechatConfig;
import com.june.constant.RedisKey;
import com.june.service.ThirdService;
import com.june.util.HttpClientUtil;
import com.june.util.RedisUtil;

import redis.clients.jedis.Jedis;

@Service
public class ThirdServiceImpl implements ThirdService{

	private Logger logger = LoggerFactory.getLogger(ThirdServiceImpl.class);
	
	@Override
	public String refreshToken() {
		Jedis jedis = RedisUtil.getJedis();
		String url = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("component_appid", WechatConfig.THIRDAPPID);
		jsonObject.put("component_appsecret", WechatConfig.THIRDSECRET);
		jsonObject.put("component_verify_ticket", jedis.get(RedisKey.WX_TICKET));
		String result = HttpClientUtil.doPostJson(url, jsonObject.toString());
		JSONObject jsobj = new JSONObject(result);
		String token = (String) jsobj.get("component_access_token");
		logger.info(token);
		jedis.set(RedisKey.WX_ACCESS_TOKEN, token);
		jedis.close();
		return token;
	}

	
	@Override
	public String preAuthCode() {
		String token = RedisUtil.get(RedisKey.WX_ACCESS_TOKEN);
		String url = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token=";
		if(StringUtils.isBlank(token)) {
			token = refreshToken();
		}
		url+=token;
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("component_appid", WechatConfig.THIRDAPPID);
		String result = HttpClientUtil.doPostJson(url, jsonObject.toString());
		logger.info(result);
		JSONObject jsobj = new JSONObject(result);
		String code = jsobj.getString("pre_auth_code");		
		return code;
	}

	@Override
	public void refreshAuthorizerToken() {
		Jedis jedis = RedisUtil.getJedis();
		String url = "https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?component_access_token=";
		url+=jedis.get(RedisKey.WX_ACCESS_TOKEN);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("component_appid", WechatConfig.THIRDAPPID);
		jsonObject.put("authorizer_appid", WechatConfig.AUTHORIZERAPPID);
		jsonObject.put("authorizer_refresh_token",jedis.get(RedisKey.WX_AUTHORIZER_REFRESH_TOKEN));
		try {
			String result = HttpClientUtil.doPostJson(url, jsonObject.toString());
			JSONObject results = new JSONObject(result);
			jedis.set(RedisKey.WX_AUTHORIZER_ACCESS_TOKEN, results.getString("authorizer_access_token"));
			jedis.set(RedisKey.WX_AUTHORIZER_REFRESH_TOKEN, results.getString("authorizer_refresh_token"));
			logger.info(result);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}finally {
			jedis.close();
		}
	}

}
