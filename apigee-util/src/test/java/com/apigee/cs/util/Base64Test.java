package com.apigee.cs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


import org.junit.Assert;
import org.junit.Test;

public class Base64Test {

	String encoded = "gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4"
			+ "ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx"
			+ "8vP09fb3+Pn6+/z9/v8AAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkq"
			+ "KywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJj"
			+ "ZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXp7fH1+f4CBgoOEhYaHiImKi4yNjo+QkZKTlJWWl5iZmpuc"
			+ "nZ6foKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr/AwcLDxMXGx8jJysvMzc7P0NHS09TV"
			+ "1tfY2drb3N3e3+Dh4uPk5ebn6Onq6+zt7u/w8fLz9PX29/j5+vv8/f7/AAECAwQFBgcICQoLDA0O"
			+ "DxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZH"
			+ "SElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnM=";

	byte[] createRawByteData() {
		byte[] b = new byte[500];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) ((i % 256) - 128);

		}
		return b;
	}

	@Test
	public void testDecode() throws IOException {

		Assert.assertEquals(encoded,
				Base64.encodeBytes(createRawByteData(), Base64.NO_OPTIONS));

	}

	@Test
	public void testEncodeDecode() throws IOException, NoSuchAlgorithmException {
		createRawByteData();
		for (int j = 0; j < 5; j++) {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int length = sr.nextInt(1000);
			for (int i = 0; i < length; i++) {

				baos.write(sr.nextInt(255));
			}
			baos.write(0xFF);
			byte[] b = baos.toByteArray();

			String b64 = Base64.encodeBytes(b, Base64.DO_BREAK_LINES);

			byte[] b2 = Base64.decode(b64);

			org.junit.Assert.assertArrayEquals(b, b2);
		}
	}

	@Test
	public void testEncodeDecodeNoLineBreaks() throws IOException,
			NoSuchAlgorithmException {

		for (int j = 0; j < 5; j++) {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int length = sr.nextInt(1000);
			for (int i = 0; i < length; i++) {

				baos.write(sr.nextInt(255));
			}
			baos.write(0xFF);
			byte[] b = baos.toByteArray();

			String b64 = Base64.encodeBytes(b, Base64.NO_OPTIONS);

			byte[] b2 = Base64.decode(b64);

			org.junit.Assert.assertArrayEquals(b, b2);
		}
	}

}
