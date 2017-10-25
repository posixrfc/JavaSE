package lib.ncrypt;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public final class EncryptionBase64 extends EncryptionComm
{
	public static byte[] encode(byte[] bytes)
	{
		if (null == bytes || 0 == bytes.length) {
			return null;
		}
		Encoder encoder = Base64.getEncoder();		
		bytes = encoder.encode(bytes);
		return bytes;
	}
	public static String encode(String src)
	{
		if (null == src || 0 == src.length()) {
			return null;
		}
		byte[] bytes = null;
		try {
			bytes = src.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
			return null;
		}		
		Encoder encoder = Base64.getEncoder();
		bytes = encoder.encode(bytes);
		String ret = null;
		try {
			ret = new String(bytes, "ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return ret;
	}
	
	public static byte[] decode(byte[] bytes)
	{
		if (null == bytes || 0 == bytes.length) {
			return null;
		}
		Decoder decoder = Base64.getDecoder();
		bytes = decoder.decode(bytes);
		return bytes;
	}
	public static String decode(String src)
	{
		if (null == src || 0 == src.length()) {
			return null;
		}
		byte[] bytes = null;
		try {
			bytes = src.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
			return null;
		}
		Decoder decoder = Base64.getDecoder();
		bytes = decoder.decode(bytes);
		String ret = null;
		try {
			ret = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
			return null;
		}
		return ret;
	}
	
	public static String standardized(String src)
	{
		if (null == src) {
			return null;
		}
		src = src.replace('+', '`');
		src = src.replace('/', '^');
		src = src.replace('=', '.');
		return src;
	}
	public static String unstandardized(String src)
	{
		if (null == src) {
			return null;
		}
		src = src.replace('`', '+');
		src = src.replace('^', '/');
		src = src.replace('.', '=');
		return src;
	}
	
	private EncryptionBase64(){}
}