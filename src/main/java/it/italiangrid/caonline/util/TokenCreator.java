package it.italiangrid.caonline.util;

import java.lang.reflect.UndeclaredThrowableException;

import java.math.BigInteger;

import java.security.GeneralSecurityException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

/**
Copyright (c) 2011 IETF Trust and the persons identified as
authors of the code. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, is permitted pursuant to, and subject to the license
terms contained in, the Simplified BSD License set forth in Section
4.c of the IETF Trust's Legal Provisions Relating to IETF Documents
(http://trustee.ietf.org/license-info).
*/
public final class TokenCreator {
	
	/**
	 * Constructor.
	 */
	private TokenCreator() {
		
	}
	
	/**
	 * Logger.
	 */
	private static final Logger log = Logger.getLogger(TokenCreator.class);

	/**
	 * Digest Power.
	 */
	private static final int[] DIGITS_POWER
	// 0 1 2 3 4 5 6 7 8
	= {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };
	
	/**
	 * MILLISEC.
	 */
	private static final int MILLISEC = 1000;

	/**
	 * This method uses the JCE to provide the crypto algorithm. HMAC computes a
	 * Hashed Message Authentication Code with the crypto hash algorithm as a
	 * parameter.
	 * 
	 * @param crypto
	 *            : the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
	 * @param keyBytes
	 *            : the bytes to use for the HMAC key
	 * @param text
	 *            : the message or text to be authenticated
	 * @return byte array.
	 */
	private static byte[] hmac_sha(final String crypto, final byte[] keyBytes,
			final byte[] text) {
		try {
			Mac hmac;
			hmac = Mac.getInstance(crypto);

			SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
			hmac.init(macKey);

			return hmac.doFinal(text);
		} catch (GeneralSecurityException gse) {
			throw new UndeclaredThrowableException(gse);
		}
	}

	/**
	 * This method converts a HEX string to Byte[].
	 * 
	 * @param hex
	 *            : the HEX string
	 * 
	 * @return a byte array.
	 */
	private static byte[] hexStr2Bytes(final String hex) {
		// Adding one byte to get the right conversion
		// Values starting with "0" can be converted
		byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

		// Copy all the REAL bytes, not the "first"
		byte[] ret = new byte[bArray.length - 1];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = bArray[i + 1];
		}
		
		return ret;
	}

	/**
	 * This method generates a TOTP value for the given set of parameters.
	 * 
	 * @param key
	 *            : the shared secret, HEX encoded
	 * @param time
	 *            : a value that reflects a time
	 * @param returnDigits
	 *            : number of digits to return
	 * 
	 * @return a numeric String in base 10 that includes
	 *          {@link truncationDigits} digits
	 */
	public static String generateTOTP(final String key, final String time,
			final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA1");
	}

	/**
	 * This method generates a TOTP value for the given set of parameters.
	 * 
	 * @param key
	 *            : the shared secret, HEX encoded
	 * @param time
	 *            : a value that reflects a time
	 * @param returnDigits
	 *            : number of digits to return
	 * 
	 * @return a numeric String in base 10 that includes
	 *          {@link truncationDigits} digits
	 */
	public static String generateTOTP256(final String key, final String time,
			final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA256");
	}

	/**
	 * This method generates a TOTP value for the given set of parameters.
	 * 
	 * @param key
	 *            : the shared secret, HEX encoded
	 * @param time
	 *            : a value that reflects a time
	 * @param returnDigits
	 *            : number of digits to return
	 * 
	 * @return a numeric String in base 10 that includes
	 *          {@link truncationDigits} digits
	 */
	public static String generateTOTP512(final String key, final String time,
			final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA512");
	}

	/**
	 * This method generates a TOTP value for the given set of parameters.
	 * 
	 * @param key
	 *            : the shared secret, HEX encoded
	 * @param time
	 *            : a value that reflects a time
	 * @param returnDigits
	 *            : number of digits to return
	 * @param crypto
	 *            : the crypto function to use
	 * 
	 * @return a numeric String in base 10 that includes
	 *          {@link truncationDigits} digits
	 */
	public static String generateTOTP(final String key, String time,
			final String returnDigits, final String crypto) {
		int codeDigits = Integer.decode(returnDigits).intValue();
		String result = null;

		// Using the counter
		// First 8 bytes are for the movingFactor
		// Compliant with base RFC 4226 (HOTP)
		while (time.length() < 16) {
			time = "0" + time;
		}
		// Get the HEX in a Byte[]
		byte[] msg = hexStr2Bytes(time);
		byte[] k = hexStr2Bytes(key);
		byte[] hash = hmac_sha(crypto, k, msg);

		// put selected bytes into result int
		int offset = hash[hash.length - 1] & 0xf;

		int binary = ((hash[offset] & 0x7f) << 24)
				| ((hash[offset + 1] & 0xff) << 16)
				| ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

		int otp = binary % DIGITS_POWER[codeDigits];

		result = Integer.toString(otp);

		while (result.length() < codeDigits) {
			result = "0" + result;
		}

		return result;
	}

	/**
	 * 
	 * @param arg 
	 * @return srting
	 */
	public static String toHex(final String arg) {
		return String.format("%x",
				new BigInteger(arg.getBytes(/* YOUR_CHARSET? */)));
	}

	/**
	 * 
	 * @param userSecret 
	 * @return string
	 */
	public static String getToken(final String userSecret) {

		log.debug(System.currentTimeMillis() / MILLISEC);

		// Seed for HMAC-SHA512 - 64 bytes
		String baseSeed64 = "3132333435363738393031323334353637383930"
				+ "3132333435363738393031323334353637383930"
				+ "3132333435363738393031323334353637383930" + "31323334";

		String seed64 = toHex(userSecret)
				+ baseSeed64.substring(toHex(userSecret).length());

		log.debug(baseSeed64);
		log.debug(seed64);

		long T0 = 0;
		long X = 60;

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		try {

			long timeNow = System.currentTimeMillis() / MILLISEC;

			long T = ((timeNow) - T0) / X;
			String steps = Long.toHexString(T).toUpperCase();

			while (steps.length() < 16) {
				steps = "0" + steps;
			}
			String token = generateTOTP(seed64, steps, "8", "HmacSHA512");

			log.debug("TokenCreator = " + token);

			return token;

		} catch (final Exception e) {
			System.out.println("Error : " + e);
		}
		return null;
	}

}
