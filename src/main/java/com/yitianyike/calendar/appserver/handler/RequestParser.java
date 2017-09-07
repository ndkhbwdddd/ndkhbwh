package com.yitianyike.calendar.appserver.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSONObject;
import com.yitianyike.calendar.appserver.util.AESUtil;
import com.yitianyike.calendar.appserver.util.RSAUtil;
import com.yitianyike.calendar.appserver.util.StringEx;

/**
 * HTTP请求参数解析器, 支持GET, POST Created by whf on 12/23/15.
 */
public class RequestParser {

	private FullHttpRequest fullReq;

	private static List<String> paramList = new ArrayList<String>();
	static {
		paramList.add("num");
		paramList.add("uid");
		paramList.add("uuid");
		paramList.add("thirdid");
		paramList.add("telephone");
		paramList.add("token");
		paramList.add("devicetoken");
		paramList.add("aids");
		paramList.add("aid");
		paramList.add("datetime");
		paramList.add("channelno");
		paramList.add("version");
		paramList.add("type");
		paramList.add("secretkey");
		paramList.add("status");
		paramList.add("mintime");
		paramList.add("maxtime");
		paramList.add("key");// 公钥加密钥加密后的字符串
		paramList.add("data");// 签名和加密数据
		paramList.add("aeskey");// 用私钥解密出为的密钥字符串
		paramList.add("olduid");// 用私钥解密出为的密钥字符串
	}

	/**
	 * 构造一个解析器
	 * 
	 * @param req
	 */
	public RequestParser(FullHttpRequest req) {
		this.fullReq = req;
	}

	/**
	 * 解析请求参数
	 * 
	 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
	 *
	 * @throws BaseCheckedException
	 * @throws IOException
	 */
	public Map<String, String> parse() throws IOException {
		HttpMethod method = fullReq.getMethod();

		Map<String, String> parmMap = new HashMap<String, String>();

		HttpHeaders hm = fullReq.headers();

		if (hm != null) {
			List<Map.Entry<String, String>> entryList = hm.entries();
			for (Map.Entry<String, String> entry : entryList) {
				String key = entry.getKey();
				if (paramList.contains(key.toLowerCase())) {
					parmMap.put(key.toLowerCase(), entry.getValue());
				} else if (key.equals("Host") || key.equals("User-Agent") || key.equals("X-Source-Id")) {
					parmMap.put(key, entry.getValue());
				}
			}
		}

		if (HttpMethod.GET == method) {
			// 是GET请求
			QueryStringDecoder decoder = new QueryStringDecoder(fullReq.getUri());

			Map<String, List<String>> parame = decoder.parameters();
			for (Entry<String, List<String>> entry : parame.entrySet()) {
				// System.out.println(entry.getKey() + " : " +entry.getValue());
				if (paramList.contains(entry.getKey().toLowerCase())) {
					parmMap.put(entry.getKey().toLowerCase(), entry.getValue().get(0));
				}
				// parmMap.put(entry.getKey(), entry.getValue().get(0));

			}

		} else if (HttpMethod.POST == method) {
			// 是POST请求
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
			decoder.offer(fullReq);

			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

			for (InterfaceHttpData parm : parmList) {

				Attribute data = (Attribute) parm;
				parmMap.put(data.getName(), data.getValue());
			}

		} else {
			// 不支持其它方法
			// throw new MethodNotSupportedException(""); // 这是个自定义的异常, 可删掉这一行
			System.out.println("MethodNotSupportedException");
		}

		if (parmMap.get("key") != null && parmMap.get("data") != null) {
			try {
				byte[] strToBytes = Base64.decodeBase64(parmMap.get("key"));
				String destKey = RSAUtil.RSADecode(strToBytes);
				String srcData = parmMap.get("data");
				String sign = "";
				String trueData = "";
				String destData = "";
				if (srcData.length() < 32) {
				} else {
					sign = srcData.substring(0, 32);
					trueData = srcData.substring(32);
				}
				String localSign = StringEx.MD5(trueData + destKey);
				if (localSign.equalsIgnoreCase(sign)) {
					destData = AESUtil.Decrypt(trueData, destKey);

					String[] paramArray = destData.split("&");
					for (int i = 0; i < paramArray.length; i++) {
						String[] keyValue = paramArray[i].split("=", 2);
						if (keyValue != null && keyValue.length == 2) {
							if (paramList.contains(keyValue[0].toLowerCase())) {
								parmMap.put(keyValue[0].toLowerCase(), keyValue[1]);
							}
						}
					}

					parmMap.put("aeskey", destKey);
				} else {
					parmMap.clear();
				}

			} catch (Exception e) {
				e.printStackTrace();
				parmMap.clear();
			}
		}

		return parmMap;
	}

}