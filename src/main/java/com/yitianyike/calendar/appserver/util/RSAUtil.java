package com.yitianyike.calendar.appserver.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RSAUtil {

	private static final String KEY_ALGORITHM = "RSA";
    /** 貌似默认是RSA/NONE/PKCS1Padding，未验证 */  
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    
    private static PublicKey pubKey = null;
    private static PrivateKey priKey = null;
    
	static{
		try{
			//pubKey = restorePrivateKey(Base64.decodeBase64(PropertiesUtil.publicKey));
	    	priKey = restorePrivateKey(Base64.decodeBase64(PropertiesUtil.privateKey));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    /**        
     * * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
     * @param keyBytes
     * @return
    */  
	public static PublicKey restorePublicKey(byte[] keyBytes) {
    	X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
    	try {
	    	KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
	    	PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
	    	return publicKey;
    	} catch (Exception e ){//NoSuchAlgorithmException | InvalidKeySpecException e) {
    	// TODO Auto-generated catch block  94             
    		e.printStackTrace();
    	}
    	return null;
    }

	/**
	 * * 还原私钥，PKCS8EncodedKeySpec 用于构建私钥的规范
	 * @param keyBytes
	 * @return
	 */
	public static PrivateKey restorePrivateKey(byte[] keyBytes) {
	   	PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec( keyBytes);
	   	try {
	   	KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
	   	PrivateKey privateKey = factory
	   	                     .generatePrivate(pkcs8EncodedKeySpec);
	   	 return privateKey;
	   	} catch (Exception e ){//NoSuchAlgorithmException | InvalidKeySpecException e) {
	   	// TODO Auto-generated catch block 115             
	   		e.printStackTrace();
	   	}
	   	return null;
   }
	
	/*** 加密，三步走。
     * @param plainText
    * @return
    * */ 
	public static byte[] RSAEncode(byte[] plainText) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return cipher.doFinal(plainText);
		} catch (Exception e ){//NoSuchAlgorithmException | NoSuchPaddingException| InvalidKeyException | IllegalBlockSizeException| BadPaddingException e) {
		// TODO Auto-generated catch block 137             
			e.printStackTrace();
		}
		return null;
	}
	/*** 解密，三步走。
      * @param encodedText
     * @return  */ 
	public static String RSADecode(byte[] encodedText) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, priKey);
			return new String(cipher.doFinal(encodedText));
		} catch (Exception e ){//NoSuchAlgorithmException | NoSuchPaddingException| InvalidKeyException | IllegalBlockSizeException| BadPaddingException e) {
			// TODO Auto-generated catch block 160            
			e.printStackTrace();
		 }
		 return null;
	}

}
