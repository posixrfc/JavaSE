package lib.ncrypt;

import java.util.Random;

public abstract class EncryptionComm
{
	public static String randomCharSecquence(String src)
	{
		StringBuilder sb = new StringBuilder(src);
		final int sl = sb.length();
		char[] cs = new char[sl];
		Random rm = new Random();
		for (int i = 0; i < sl; i++)
		{
			int idx = rm.nextInt(sl - i);
			cs[i] = sb.charAt(idx);
			sb.deleteCharAt(idx);
		}
		return new String(cs);
	}
	public static String getRandomString(int len)
	{
		if (1 > len) {
			return null;
		}
		final String rdmstr = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		final int strlen = rdmstr.length();
		StringBuilder sb = new StringBuilder(len);
		Random rm = new Random();
		for (int i = 0; i < len; i++) {
			sb.append(rdmstr.charAt(rm.nextInt(strlen)));
		}
		return sb.toString();
	}
	public static String getRandomNumber(int len)
	{
		if (1 > len) {
			return null;
		}
		final String rdmstr = "0123456789";
		final int strlen = rdmstr.length();
		StringBuilder sb = new StringBuilder(len);
		Random rm = new Random();
		for (int i = 0; i < len; i++) {
			sb.append(rdmstr.charAt(rm.nextInt(strlen)));
		}
		return sb.toString();
	}
}
