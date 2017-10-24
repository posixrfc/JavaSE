package lib.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public interface StringUtil
{
	public static Integer getIntegerValue(String numberString)
	{
		if (numberString == null || numberString.length() == 0) {
			return null;
		}
		NumberDescripter descripter = getNumberInfo(numberString);
		if (descripter == null) {
			return null;
		}
		int beginIndex = Math.abs(descripter.signSymbol) + descripter.prefix;
		if (descripter.dotLocate == -1) {//整数
			numberString = numberString.substring(beginIndex);
		} else {
			numberString = numberString.substring(beginIndex, descripter.dotLocate);
		}
		int ret;
		switch (descripter.cardinal)
		{
		case 10://只有10进制支持科学计数法
		case 16:
		case 8:
		case 2:
			try {
				ret = Integer.parseUnsignedInt(numberString, descripter.cardinal);
				if (descripter.signSymbol == -1) {
					return -ret;
				}
				return ret;
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
			
		default:
			return null;//其他进制不常用，不做支持
		}
	}
	
	public static Long getLongValue(String numberString)
	{
		if (numberString == null || numberString.length() == 0) {
			return null;
		}
		NumberDescripter descripter = getNumberInfo(numberString);
		if (descripter == null) {
			return null;
		}
		int beginIndex = Math.abs(descripter.signSymbol) + descripter.prefix;
		if (descripter.dotLocate == -1) {//整数
			numberString = numberString.substring(beginIndex);
		} else {
			numberString = numberString.substring(beginIndex, descripter.dotLocate);
		}
		long ret;
		switch (descripter.cardinal)
		{
		case 10://只有10进制支持科学计数法
		case 16:
		case 8:
		case 2:
			try {
				ret = Long.parseUnsignedLong(numberString, descripter.cardinal);
				if (descripter.signSymbol == -1) {
					return -ret;
				}
				return ret;
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
			
		default:
			return null;//其他进制不常用，不做支持
		}
	}
	
	public static BigInteger getBigIntegerValue(String numberString)
	{
		if (numberString == null || numberString.length() == 0) {
			return null;
		}
		NumberDescripter descripter = getNumberInfo(numberString);
		if (descripter == null) {
			return null;
		}
		int beginIndex = Math.abs(descripter.signSymbol) + descripter.prefix;
		if (descripter.dotLocate == -1) {//整数
			numberString = numberString.substring(beginIndex);
		} else {
			numberString = numberString.substring(beginIndex, descripter.dotLocate);
		}
		switch (descripter.cardinal)
		{
		case 10://只有10进制支持科学计数法
		case 16:
		case 8:
		case 2:
			try {
				if (descripter.signSymbol == -1) {
					numberString += '-';
				}
				return new BigInteger(numberString, descripter.cardinal);
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
			
		default:
			return null;//其他进制不常用，不做支持
		}
	}
	
	public static Double getDoublelValue(String numberString)
	{
		if (null == numberString || numberString.length() == 0) {
			return null;
		}
		NumberDescripter descripter = getNumberInfo(numberString);
		if (descripter == null) {
			return null;
		}
		int beginIndex = Math.abs(descripter.signSymbol) + descripter.prefix;
		numberString = numberString.substring(beginIndex);
		double ret;
		switch (descripter.cardinal)
		{
		case 10://只有10进制支持科学计数法
			try {
				ret = Double.parseDouble(numberString);
				if (descripter.signSymbol == -1) {
					return -ret;
				}
				return ret;
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
		case 16:
		case 8:
		case 2:
		default:
			return null;//其他进制不常用，不做支持
		}
	}
	
	public static BigDecimal getBigDecimalValue(String numberString)
	{
		if (null == numberString || numberString.length() == 0) {
			return null;
		}
		NumberDescripter descripter = getNumberInfo(numberString);
		if (descripter == null) {
			return null;
		}
		int beginIndex = Math.abs(descripter.signSymbol) + descripter.prefix;
		numberString = numberString.substring(beginIndex);
		switch (descripter.cardinal)
		{
		case 10://只有10进制支持科学计数法
			try {
				if (descripter.signSymbol == -1) {
					numberString += '-';
				}
				return new BigDecimal(numberString, MathContext.DECIMAL128);
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
		case 16:
		case 8:
		case 2:
		default:
			return null;//其他进制不常用，不做支持
		}
	}
	
	public static NumberDescripter getNumberInfo(String numberString)
	{
		NumberDescripter descripter = new NumberDescripter();
		descripter.dotLocate = (byte) numberString.indexOf('.');
		char idxChar = numberString.charAt(0);
		if (idxChar == '-') {
			descripter.signSymbol = -1;
		} else if (idxChar == '+') {
			descripter.signSymbol = 1;
		} else {
			descripter.signSymbol = 0;
		}
		if (descripter.signSymbol != 0) {
			numberString = numberString.substring(1);
			if (numberString.length() == 0) {
				return null;
			}
			idxChar = numberString.charAt(0);
		}
		switch (idxChar)
		{
		case 'b':
		case 'B'://2进制
			descripter.cardinal = 2;
			descripter.prefix = 1;
			break;
			
		case 'x':
		case 'X'://16进制
			descripter.cardinal = 16;
			descripter.prefix = 1;
			break;
			
		case '0'://2，8，16进制,10进制小数
			if (numberString.length() < 2) {
				return null;
			}
			switch (numberString.charAt(1))
			{
			case 'b':
			case 'B'://2进制
				descripter.cardinal = 2;
				descripter.prefix = 2;
				break;
				
			case 'x':
			case 'X'://16进制
				descripter.cardinal = 16;
				descripter.prefix = 2;
				break;
				
			case '.'://10进制小数
				descripter.cardinal = 10;
				descripter.prefix = 0;
				break;

			default://8进制
				descripter.cardinal = 8;
				descripter.prefix = 2;
			}
			break;

		default://10进制 | 错误
			if (49 > idxChar || idxChar > 57) {
				return null;
			}
			descripter.cardinal = 10;
			descripter.prefix = 0;
		}
		return descripter;
	}
	
	public static Boolean getBooleanValue(String booleanString)
	{
		if (null == booleanString || booleanString.length() == 0) {
			return null;
		}
		if (booleanString.equalsIgnoreCase("0")) {
			return false;
		}
		if (booleanString.equalsIgnoreCase("1")) {
			return true;
		}
		if (booleanString.equalsIgnoreCase("no")) {
			return false;
		}
		if (booleanString.equalsIgnoreCase("yes")) {
			return true;
		}
		if (booleanString.equalsIgnoreCase("true")) {
			return true;
		}
		if (booleanString.equalsIgnoreCase("false")) {
			return false;
		}
		return null;
	}
	
	public static Object getNULLValue(String nullString)
	{
		if (null == nullString || nullString.length() == 0) {
			return 0;
		}
		if (nullString.equalsIgnoreCase("nil")) {
			return null;
		}
		if (nullString.equalsIgnoreCase("null")) {
			return null;
		}
		if (nullString.equalsIgnoreCase("none")) {
			return null;
		}
		if (nullString.equalsIgnoreCase("undefined")) {
			return null;
		}
		return 0;
	}
}