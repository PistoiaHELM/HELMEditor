/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.helm.editor.utility;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class NumericalUtils {

	private final static int BYTES_PER_LONG = Long.SIZE / Byte.SIZE;
	private static final String DEFAULT_RANDOM_ALOGRITHM = "SHA1PRNG";

	public static byte[] longToBytes(long inLong) {
		byte[] bArray = new byte[BYTES_PER_LONG];
		ByteBuffer bBuffer = ByteBuffer.wrap(bArray);
		LongBuffer lBuffer = bBuffer.asLongBuffer();
		lBuffer.put(inLong);
		return bArray;
	}

	public static String getUniqueCode() throws NoSuchAlgorithmException {
		long currentTime = System.currentTimeMillis();

		SecureRandom rand = SecureRandom.getInstance(DEFAULT_RANDOM_ALOGRITHM);
		rand.setSeed(longToBytes(currentTime));

		return String.valueOf(rand.nextLong());
	}

}
