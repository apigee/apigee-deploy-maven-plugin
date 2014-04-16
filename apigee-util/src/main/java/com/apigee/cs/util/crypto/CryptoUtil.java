package com.apigee.cs.util.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apigee.cs.util.*;

/**
 * Simple enhancement of PropertyOverrideConfigurer that will decrypt property
 * values starting with {AES} that have been encrypted with the same class.
 * 
 * @author rob
 * 
 */
public class CryptoUtil {
	// private String secretKey;
	Logger log = LoggerFactory.getLogger(CryptoUtil.class);
	public static final String AES_ALGORITHM = "AES";
	public static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
	public static final String SHA1PRNG = "SHA1PRNG";
	public static final String DEFAULT_PBE_ALG = "PBEWithSHA1AndDESede";
	public static final int DEFAULT_PBE_ITERATION_COUNT = 20;
	public static int PBE_SALT_LEN = 8;
	public static int CBC_IV_LEN = 16;
	private int secretKeyLength = 128;
	private KeyStore keystore;

	public static final String DEFAULT_KEYSTORE_SEARCH_PATH = "conf";
	public static final String DEFAULT_KEYSTORE_NAME = "ast.jceks";

	public byte[] generateIV() {
		try {
			byte[] iv = new byte[CBC_IV_LEN];
			SecureRandom sr = SecureRandom.getInstance(SHA1PRNG);
			sr.nextBytes(iv);

			return iv;
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
	}

	protected byte[] extractIv(byte[] data) {
		byte[] b = new byte[CBC_IV_LEN];
		if (data.length < CBC_IV_LEN) {
			throw new IllegalArgumentException(
					"input data must be greater than IV length");
		}
		System.arraycopy(data, 0, b, 0, b.length);
		return b;
	}

	protected byte[] extractCipherData(byte[] data) {
		byte[] cipherData = new byte[data.length - CBC_IV_LEN];
		System.arraycopy(data, CBC_IV_LEN, cipherData, 0, data.length
				- CBC_IV_LEN);

		return cipherData;
	}

	/**
	 * Encrypts a String value into a Base64 encoded string with an {AES} prefix
	 * 
	 * @param The
	 *            cleartext string to be encrypted
	 * @return
	 */
	public String encrypt(String s, SecretKeySpec sks) {
		try {

			// Generate a random input vector
			byte[] iv = generateIV();
			IvParameterSpec ips = new IvParameterSpec(iv);

			// Initialize the Cipher
			Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);

			// Initialize the cipher with our secret key and the input vector
			// parameter spec
			cipher.init(Cipher.ENCRYPT_MODE, sks, ips);

			// do the encryption
			byte[] enc = cipher.doFinal(s.getBytes());

			// now we want to include the input vector in the encoded output,
			// so pack the iv with the cipherdata
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(iv);
			baos.write(enc);
			byte[] ivAndCipherData = baos.toByteArray();

			String encodedCipherData = Base64.encodeBytes(ivAndCipherData);

			// now prepend with {AES}
			String v = "{" + AES_ALGORITHM + "}" + encodedCipherData;

			// Decrypt what we just encrypted to be sure it works properly
			String x = decryptString(v, sks);
			if (!x.equals(s)) {
				throw new CryptoException("identity decryption failed");
			}
			return v;

		} catch (NoSuchPaddingException e) {
			throw new CryptoException(e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptoException(e);
		} catch (BadPaddingException e) {
			throw new CryptoException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(e);
		} catch (IOException e) {
			throw new CryptoException(e);
		}
	}

	public String decryptString(String s, SecretKeySpec sks) {
		try {

			s = s.substring("{AES}".length());
			Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);

			byte[] ivAndCipherData = Base64.decode(s);
			IvParameterSpec ips = new IvParameterSpec(
					extractIv(ivAndCipherData));

			ips = new IvParameterSpec(extractIv(ivAndCipherData));
			Cipher decryptCipher = Cipher.getInstance(AES_CBC_PKCS5);

			decryptCipher.init(Cipher.DECRYPT_MODE, sks, ips);

			byte[] deciphertext = decryptCipher
					.doFinal(extractCipherData(ivAndCipherData));
			return new String(deciphertext);

		} catch (NoSuchPaddingException e) {
			throw new CryptoException(e);
		} catch (InvalidKeyException e) {
			throw new CryptoException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptoException(e);
		} catch (BadPaddingException e) {
			throw new CryptoException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(e);
		} catch (IOException e) {
			throw new CryptoException(e);
		}
	}

	public int getSecretKeyLength() {
		return secretKeyLength;
	}

	public SecretKeySpec decodeSecretKeySpec(String base64Encoded) {
		try {

			byte[] b = Base64.decode(base64Encoded);
			return new SecretKeySpec(b, AES_ALGORITHM);
		} catch (IOException e) {
			throw new CryptoException(e);
		}
	}

	public String encryptString(String plain) {
		return encryptString(plain,getKeyAlias());
	}
	
	public String decryptString(String cipher) {
		return decryptString(cipher,getSecretKeySpec(getKeyAlias()));
	}
	public String decryptString(String cipher, String alias) {
		return decryptString(cipher,getSecretKeySpec(alias));
	}
	public String encryptString(String plain, String alias) {
		try {
			SecretKeySpec sks = (SecretKeySpec) keystore.getKey(alias,
					getAliasPassword(alias));
			return encrypt(plain, sks);

		} catch (KeyStoreException e) {
			throw new CryptoException(e);
		} catch (UnrecoverableKeyException e) {
			throw new CryptoException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
	}

	public void loadKeyStore() {
		try {
			KeyStore ks = KeyStore.getInstance("JCEKS");
			File f = findKeyStore(new File("."));
			if (f == null) {
				throw new CryptoException("Could not locate AST Keystore");
			}
			ks.load(new FileInputStream(f), getKeyStorePassword());
			this.keystore = ks;
		} catch (CryptoException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new CryptoException(e);
		}
	}

	File findKeyStore(File f) throws IOException {

		if (f == null) {
			return null;
		}

		File ks = new File(new File(f, getKeyStoreSearchPath()),
				getKeyStoreName());
		log.debug("Looking for: {}", ks.getAbsolutePath());
		if (ks.exists() && ks.isFile()) {
			return ks;
		}

		return findKeyStore(f.getCanonicalFile().getParentFile());

	}

	public String getKeyStoreSearchPath() {
		return DEFAULT_KEYSTORE_SEARCH_PATH;
	}

	public String getKeyStoreName() {
		return DEFAULT_KEYSTORE_NAME;
	}

	public char[] getKeyStorePassword() {
		return "changeit".toCharArray();
	}

	public char[] getAliasPassword(String name) {
		return getKeyStorePassword();
	}

	public String getKeyAlias() {
		return "ast";
	}
	private KeyStore getKeyStore() {
		if (keystore==null) {
			throw new IllegalStateException("KeyStore not loaded");
		}
		return keystore;
	}
	public SecretKeySpec getSecretKeySpec(String alias) {
		try {
			SecretKeySpec sks = (SecretKeySpec) getKeyStore().getKey(alias,
					getAliasPassword(alias));
			return sks;
		} catch (KeyStoreException e) {
			throw new CryptoException(e);
		} catch (UnrecoverableKeyException e) {
			throw new CryptoException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
	}

	public SecretKeySpec getSecretKeySpec() {
		return getSecretKeySpec(getKeyAlias());
	}
}
