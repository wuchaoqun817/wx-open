package com.june.service;
/**
 * 微信第三方平台服务
 * @author wuchaoqun
 *
 */
public interface ThirdService {
	/**
	 * 刷新第三方的accessToken
	 * @return
	 */
	String refreshToken();
	/**
	 * 获取预授权码
	 * @return
	 */
	String preAuthCode();
	/**
	 * 刷新授权方的accessToken
	 * @return
	 */
	void refreshAuthorizerToken();
}
