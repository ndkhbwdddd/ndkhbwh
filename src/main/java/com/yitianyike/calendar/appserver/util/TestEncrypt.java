package com.yitianyike.calendar.appserver.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class TestEncrypt {
	public static void main22() throws Exception {
		String ctos = ctos();
		String[] split = ctos.split("你");
		stoc(split[0], split[1]);
	}

	public static void main(String[] args) {
		try {
			main22();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void stoc(String ctos, String data) throws Exception {
		byte[] decodeBase64 = Base64.decodeBase64(ctos);
		// rsa解密key
		String rsaDecode = RSADecode(decodeBase64);
		System.out.println("RSA解密后数据:" + rsaDecode);

		// aes
		String request = data;
		String targ = request.substring(0, 32);
		String trueData = request.substring(32);
		String localSign = StringEx.MD5(trueData + rsaDecode);
		if (!targ.equals(localSign)) {
			System.out.println("md5标识位不同,解密失败,重复请求");
		} else {
			String decrypt = AESUtil.Decrypt(trueData, rsaDecode);
			System.out.println("解密后的数据:" + decrypt);
		}
	}

	private static String ctos() throws Exception {
		String trueData = "token=t0051007148651918438756800000000";

		UUID randomUUID = UUID.randomUUID();
		String substring = randomUUID.toString().replace("-", "").substring(0, 16);

		String destKey = substring;
		// String md5 = StringEx.MD5(key + sPassword);
		// System.out.println(md5);
		// 客户端
		// byte[] strToBytes = Base64.decodeBase64(destKey);

		byte[] rsaEncode = RSAEncode(destKey.getBytes());// 首先把key用rsa公钥加密

		// System.out.println("比较一下 Base64.decodeBase64和key.getBytes()的编码不同");
		String encodeBase64String = Base64.encodeBase64String(rsaEncode);
		System.out.println(encodeBase64String);
		// aes加密

		String destData = AESUtil.Encrypt(trueData, destKey);
		// String string = new String(rsaEncode);

		String localSign = StringEx.MD5(destData + destKey);
		// System.out.println(trueData + "正文前面的md5串用来验证的");

		// 发给服务端的值为
		String responseString = localSign + destData;

		System.out.println("加密传输的key:" + encodeBase64String);
		System.out.println("加密传输的data:" + responseString);
		return encodeBase64String + "你" + responseString;
	}

	private static final String KEY_ALGORITHM = "RSA";
	/** 貌似默认是RSA/NONE/PKCS1Padding，未验证 */
	private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

	/**
	 * * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	public static PublicKey restorePublicKey(byte[] keyBytes) {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
			PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
			return publicKey;
		} catch (Exception e) {// NoSuchAlgorithmException |
								// InvalidKeySpecException e) {
			// TODO Auto-generated catch block 94
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * * 还原私钥，PKCS8EncodedKeySpec 用于构建私钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	public static PrivateKey restorePrivateKey(byte[] keyBytes) {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
			PrivateKey privateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
			return privateKey;
		} catch (Exception e) {// NoSuchAlgorithmException |
								// InvalidKeySpecException e) {
			// TODO Auto-generated catch block 115
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 加密，三步走。
	 * 
	 * @param plainText
	 * @return
	 */
	public static byte[] RSAEncode(byte[] plainText) {
		String sss = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbfg0XNkS1YSUjCp9P0NNZ3uyXxCeZYuJeoFKBkhtBoDSNKCn1hS5JIFk6BMVpxBNvuhVJpUANXGbQFzbJmk9YBmFxrdT7rDU7pXEckRFaUuvcIzhqrtrKRG49QDDAHFrRyNu1RjSVrMPbDNlNjCsaDpa2YubsulAf3Co8bSsh9QIDAQAB";

		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			PublicKey restorePublicKey = restorePublicKey(Base64.decodeBase64(sss));
			byte[] encoded = restorePublicKey.getEncoded();
			String algorithm = restorePublicKey.getAlgorithm();
			cipher.init(Cipher.ENCRYPT_MODE, restorePublicKey(Base64.decodeBase64(sss)));
			return cipher.doFinal(plainText);
		} catch (Exception e) {// NoSuchAlgorithmException |
								// NoSuchPaddingException| InvalidKeyException |
								// IllegalBlockSizeException|
								// BadPaddingException e) {
			// TODO Auto-generated catch block 137
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 解密，三步走。
	 * 
	 * @param encodedText
	 * @return
	 */
	public static String RSADecode(byte[] encodedText) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, restorePrivateKey(Base64.decodeBase64(
					"MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJt+DRc2RLVhJSMKn0/Q01ne7JfEJ5li4l6gUoGSG0GgNI0oKfWFLkkgWToExWnEE2+6FUmlQA1cZtAXNsmaT1gGYXGt1PusNTulcRyREVpS69wjOGqu2spEbj1AMMAcWtHI27VGNJWsw9sM2U2MKxoOlrZi5uy6UB/cKjxtKyH1AgMBAAECgYBPhabSQTngfV9NzYfufQEqTD053OLXql/VPy8S/xMbfACEVI8o9sTWN0oKxxfjYJkEIenzMSVR7/jQ4Z5uepGqxndHQmClEDI+6k8DtMdS4IAqa+TvzuKngj3zkUOdmi1gMeGpYLV1lckoHO13hIe2C7IpGt/DhJcuE52S14OQvQJBAPAyefTxEhMffih4gPoFGpTzI2ZL9DbzDGQvatnemXrDYpzu+Gkxb1CEjHVH8BfNDjy8hshwCrceWuLNk39PIg8CQQCluO703x7CU8otmyhjkW0iszeacYKHroAKSeIORvpHiHYLMofx7RjRApQmYIw4tqgoOGLI52bRXfEpNWHZm6+7AkBnswMgEqvhAamvw0a7qlRtlgLkeUo4JvpkjmwtH4NXkt8SLcGleKg8NN2HDMXFIMxSwHnYMzcNE9fdLy/MuNdVAkBjACKIWI44ivO54Pn02Vi4JRYvhmXzBlTpUI/h9ZboiuXx9ILwDLMJkZ/NeVnrO3sjY+Pnnw12P8ek1YYaCH6nAkATc+Qz+mamOciPN639uVGR5u0hNFVQBLx/2n8otUJISda/8fJlLe4ggiCp9XWF8g++oGOtZP2RUgWSCrPc0nCy")));
			return new String(cipher.doFinal(encodedText));
		} catch (Exception e) {// NoSuchAlgorithmException |
								// NoSuchPaddingException| InvalidKeyException |
								// IllegalBlockSizeException|
								// BadPaddingException e) {
			// TODO Auto-generated catch block 160
			e.printStackTrace();
		}
		return null;
	}

	public static void main5(String key) {
		// String key =
		// "l+2Xh3oIwycG2zHZwiM79gj6s76Zu6VKdHBbmWw0hk0/IeIkWkpUs4uz6+iKNZsCdSCpJQAr87g+tkNbvVhCy/M20SueA0x3IBTIrB4mauI5FvoX4QJ35/pfWsa+AA9Vzlx8Ou8hJBPO4xqYPIYqeVzZ+KguRgowVnHe+BT1IvM=";
		byte[] decodeBase64 = Base64.decodeBase64(key);
		String rsaDecode = RSADecode(decodeBase64);
		System.out.println(rsaDecode);
	}

	public static void main1111(String[] args) throws Exception {
		String key = "82jtkXZJuDbSvari";
		String data = "1cd4e9fd59ecf5644b938b12c449beebs9+Wgp7s/4fNcBpLLMKzxRWgjCTy82UoPdBpOgC9vSqBl1Xj4guZmPRdZqowhH9/CX16z7PLK69Pt5aR2yuDYUlys/VuaFQjlDk6YUpaEdfCB8s4NfyZyyPsqmbBFyaZ5csS1x78FkENSTjZ080GQrqbO+qRzvjkX5je8MKLds74cTkG8sT5WdchqbC0yhNr";
		String targ = data.substring(0, 32);
		String trueData = data.substring(32);
		String encrypt = AESUtil.Encrypt(trueData, key);
		System.out.println(encrypt);
		String decrypt = AESUtil.Decrypt(encrypt, key);
		System.out.println(decrypt.length());
		String md5 = StringEx.MD5(trueData + key);

		System.out.println(md5);
	}
}
