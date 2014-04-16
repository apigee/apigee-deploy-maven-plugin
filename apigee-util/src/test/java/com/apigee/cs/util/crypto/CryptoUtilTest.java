package com.apigee.cs.util.crypto;

import java.io.IOException;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import static org.junit.Assert.*;

import org.apache.commons.io.HexDump;
import org.junit.Assert;
import org.junit.Test;

import com.apigee.cs.util.*;

public class CryptoUtilTest {

	public static final String TEST_KEY = "VqvCvwh9XsQndfaBP7+8ZA==";

	public static final String TEST_PLAINTEXT = "Hello, world!";
	public static final String TEST_CIPHERTEXT = "{AES}XYIkd9k+nig0zdsiEqoLDcnYoCY+DbrMuX8xvWMgdwY=";



	@Test
	public void testCreateInputVector() {
		CryptoUtil ct = new CryptoUtil();
		byte[] iv = ct.generateIV();
		assertEquals(CryptoUtil.CBC_IV_LEN, iv.length);
	}

	@Test
	public void testEncryptWithStaticKey() throws IOException {
		CryptoUtil ct = new CryptoUtil();
		SecretKeySpec sks = new SecretKeySpec(Base64.decode(TEST_KEY), "AES");

		// Encrypting the same value twice should generate two different
		// ciphertext strings
		assertFalse(ct.encrypt("test", sks).equals(ct.encrypt("test", sks)));

		for (int i = 0; i < 20; i++) {
			String input = UUID.randomUUID().toString();
			assertEquals(input, ct.decryptString(ct.encrypt(input, sks), sks));
		}
	}

	
	@Test
	public void testEncryptWithKeyStore() throws IOException {
		CryptoUtil ct = new CryptoUtil();
		ct.loadKeyStore();
		SecretKeySpec sks = ct.getSecretKeySpec("ast");
		
		// Encrypting the same value twice should generate two different
		// ciphertext strings
		assertFalse(ct.encrypt("test", sks).equals(ct.encrypt("test", sks)));

		for (int i = 0; i < 20; i++) {
			String input = UUID.randomUUID().toString();
			assertEquals(input, ct.decryptString(ct.encrypt(input, sks), sks));
		}
	}
	@Test
	public void testDecrypt() throws Exception {
		CryptoUtil ct = new CryptoUtil();
		SecretKeySpec sks = new SecretKeySpec(Base64.decode(TEST_KEY), "AES");
		assertEquals(TEST_PLAINTEXT, ct.decryptString(TEST_CIPHERTEXT, sks));
	}

	@Test(expected = CryptoException.class)
	public void testEncryptWithInvalidKeyLength() {
		byte b[] = new byte[15];
		SecretKeySpec sks = new SecretKeySpec(b, "AES");
		new CryptoUtil().encrypt("test", sks);
	}
	
	@Test
	public void testDefaults() {
		CryptoUtil cu = new CryptoUtil();
		Assert.assertEquals("ast", cu.getKeyAlias());
		Assert.assertArrayEquals("changeit".toCharArray(), cu.getKeyStorePassword());
		Assert.assertArrayEquals("changeit".toCharArray(), cu.getAliasPassword("ast"));
		Assert.assertEquals("ast.jceks",cu.getKeyStoreName());
		Assert.assertEquals("conf",cu.getKeyStoreSearchPath());
	}
	@Test
	public void testLoadKeystore() {
		
		// This should load the keystore out of apigee-util/conf/ast.jceks
		CryptoUtil cu = new CryptoUtil();
		cu.loadKeyStore();
		
		String PLAIN_TEXT="Hello, World!";
		String cipherString = cu.encryptString(PLAIN_TEXT);
		

		String x = cu.decryptString(cipherString);

		Assert.assertEquals(PLAIN_TEXT, x);
	}
	
	
	
}
