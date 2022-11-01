package com.oodles.security;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CryptionService {

	private static final Logger log = LoggerFactory.getLogger(CryptionService.class);

	private SecretKey generateKey(String salt, String passphrase) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), 1000, 128);
			return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public String decrypt(String password, String passphrase) {
		try {
			String decodedPassword = new String(java.util.Base64.getDecoder().decode(password));
			String iv = decodedPassword.split("::")[0];
			String salt = decodedPassword.split("::")[1];
			String ciphertext = decodedPassword.split("::")[2];
			SecretKey key = generateKey(salt, passphrase);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv)));
			byte[] decryptedByte = cipher.doFinal(Base64.decodeBase64(ciphertext));
			return new String(decryptedByte, "UTF-8");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

}
