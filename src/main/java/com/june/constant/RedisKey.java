package com.june.constant;

/**
 * redis 存放的 key
 * @author wuchaoqun
 *
 */
public class RedisKey {
	/**
	 * 微信每隔10分钟会推送一次ticket
	 */
	public static final String WX_TICKET = "WX_TICKET";
	/**
	 * 第三方平台的访问令牌
	 */
	public static final String WX_ACCESS_TOKEN = "WX_ACCESS_TOKEN";
	/**
	 * 授权码
	 */
	public static final String WX_AUTH_CODE = "WX_AUTH_CODE";
	/**
	 * 授权方接口调用令牌
	 */
	public static final String WX_AUTHORIZER_ACCESS_TOKEN = "WX_AUTHORIZER_ACCESS_TOKEN";
	/**
	 * 授权方接口调用刷新令牌
	 */
	public static final String WX_AUTHORIZER_REFRESH_TOKEN = "WX_AUTHORIZER_REFRESH_TOKEN";
}
