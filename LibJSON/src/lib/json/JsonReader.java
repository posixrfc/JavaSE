package lib.json;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.util.StringUtil;

public final class JsonReader extends Serializer
{
	public boolean isOccurredError() {
		return hasError;
	}
	
	public Object parseJson(String jsonText, String expr)
	{
		hasError = true;
		jsonValue = null;
		if (null == jsonText) {
			return null;
		}
		jsonText = jsonText.trim();
		if (jsonText.length() < 2) {
			return null;
		}
		this.jsonText = jsonText;
		char prefix = jsonText.charAt(0);
		switch (prefix) {
		case '[':
			jsonValue = getListValue();
			break;
				
		case '{':
			jsonValue = getMapValue();
		}
		hasError = jsonValue == null;
		if (hasError) {
			return null;
		}
		if (expr != null) {
			jsonValue = getValue(expr, false);
		}
		return jsonValue;
	}
	
	public String getStringValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (value instanceof String) {
			return (String) value;
		}
		return null;
	}
	
	public Integer getIntegerValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getIntegerValue((String) value);
	}
	
	public Long getLongValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getLongValue((String) value);
	}
	
	public BigInteger getBigIntegerValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getBigIntegerValue((String) value);
	}
	
	public Double getDoublelValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getDoublelValue((String) value);
	}
	
	public BigDecimal getBigDecimalValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getBigDecimalValue((String) value);
	}
	
	public Boolean getBooleanValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return null;
		}
		return StringUtil.getBooleanValue((String) value);
	}
	
	public Object getNULLValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (!String.class.isInstance(value)) {
			return 0;
		}
		return StringUtil.getNULLValue((String) value);
	}
	
	public List<?> getListValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (value instanceof List<?>) {
			return (List<?>) value;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMapValue(String ognl)
	{
		Object value = getValue(ognl, false);
		if (value instanceof Map<?, ?>) {
			return (Map<String, ?>) value;
		}
		return null;
	}

	public Object getValue(String ognl, boolean delete)
	{
		List<String> keys = getOgnlParsedKeys(ognl);
		if (null == keys) {
			return null;
		}
		Iterator<String> it = keys.iterator();
		Object obj = jsonValue;
		Object parent = null;
		int idx = 0;
		while (it.hasNext())
		{
			String key = it.next();
			parent = obj;
			if (obj instanceof List<?>)
			{
				try {
					idx = Integer.parseInt(key, 10);
					obj = ((List<?>) obj).get(idx);
				} catch (Exception e) {
					e.printStackTrace(System.err);
					return null;
				}
				continue;
			}
			if (obj instanceof Map<?, ?>)
			{
				obj = ((Map<?, ?>) obj).get(key);
				continue;
			}
			return null;
		}
		if (obj == null) {
			return null;
		}
		if (delete) {
			if (parent instanceof List<?>) {
				((List<?>) parent).remove(idx);
			} else {
				((Map<?, ?>) parent).remove(keys.get(keys.size() - 1));
			}
		}
		return obj;
	}
		
	private List<String> getOgnlParsedKeys(String jnl)
	{
		if (jnl == null) {
			return null;
		}
		final int len = jnl.length();
		if (len == 0) {
			return null;
		}
		List<String> keys = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++)
		{
			char chr = jnl.charAt(i);
			if (chr == '.')
			{
				if (i + 1 == len) {
					return null;
				}
				int len1 = sb.length();
				if (len1 == 0) {
					return null;
				}
				keys.add(sb.toString());
				sb.delete(0, sb.length());
				continue;
			}
			if (chr == '\\')
			{
				if (i + 1 == len)
				{
					sb.append(chr);
					break;
				}
				if (jnl.charAt(i + 1) == '.')
				{
					sb.append('.');
					i++;
					continue;
				}
			}
			sb.append(chr);
		}
		if (sb.length() == 0) {
			return null;
		}
		keys.add(sb.toString());
		return keys;
	}
	
	private String getPureString()
	{
		StringBuilder retBuilder = new StringBuilder();
		String jsonText = this.jsonText;
		int len = jsonText.length();
		loop:
		for (int i = 1; i < len; i++)
		{
			final char idxChar = jsonText.charAt(i);
			if (idxChar == '\"')
			{
				this.jsonText = jsonText.substring(i + 1).trim();
				return retBuilder.length() == 0 ? null : retBuilder.toString();
			}
			if ('\\' != idxChar)
			{
				retBuilder.append(idxChar);
				continue;
			}
			i++;
			if (i > len - 1) {
				return null;
			}
			char escChar = jsonText.charAt(i);
			switch (escChar)
			{
			case '\"':
				retBuilder.append('"');
				continue loop;
				
			case '\\':
				retBuilder.append('\\');
				continue loop;
				
			case '/':
				retBuilder.append('/');
				continue loop;
				
			case 'b':
				retBuilder.append('\b');
				continue loop;
				
			case 'f':
				retBuilder.append('\f');
				continue loop;
				
			case 'n':
				retBuilder.append('\n');
				continue loop;
				
			case 'r':
				retBuilder.append('\r');
				continue loop;
				
			case 't':
				retBuilder.append('\t');
				continue loop;
			}
			if (escChar != 'u' && escChar != 'U') {
				return null;
			}
			int hexEndIdx = ++i + 4;
			if (hexEndIdx > len) {
				return null;
			}
			String tmp = jsonText.substring(i, hexEndIdx);
			try {
				escChar = (char) Integer.parseUnsignedInt(tmp, 16);
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				return null;
			}
			retBuilder.append(escChar);
			i = --hexEndIdx;
		}
		return null;
	}
	
	private String getNumberString()
	{
		StringBuilder retBuilder = new StringBuilder();
		String jsonText = this.jsonText;
		this.jsonText = "";
		int len = jsonText.length();
		loop:
		for (int i = 0; i < len; i++)
		{
			final char idxChar = jsonText.charAt(i);
			if (47 < idxChar && idxChar < 58)//0123456789
			{
				retBuilder.append(idxChar);
				continue loop;
			}
			if (64 < idxChar && idxChar < 71 || 96 < idxChar && idxChar < 103)//ABCDEFabcdefg
			{
				retBuilder.append(idxChar);
				continue loop;
			}
			switch (idxChar)
			{
			case '+':
			case '-':
			case '.':
			case 'E':
			case 'e':
			case 'X':
			case 'x':
				retBuilder.append(idxChar);
				continue loop;

			default:
				this.jsonText = jsonText.substring(i).trim();
				break loop;
			}
		}
		return retBuilder.length() == 0 ? null : retBuilder.toString();
	}
	
	private String getBooleanString()
	{
		String jsonText = this.jsonText.substring(0, 2);
		if (jsonText.equalsIgnoreCase("no")) {
			this.jsonText = this.jsonText.substring(2).trim();
			return jsonText;
		}
		jsonText = this.jsonText.substring(0, 3);
		if (jsonText.equalsIgnoreCase("yes")) {
			this.jsonText = this.jsonText.substring(3).trim();
			return jsonText;
		}
		jsonText = this.jsonText.substring(0, 4);
		if (jsonText.equalsIgnoreCase("true")) {
			this.jsonText = this.jsonText.substring(4).trim();
			return jsonText;
		}
		jsonText = this.jsonText.substring(0, 5);
		if (jsonText.equalsIgnoreCase("false")) {
			this.jsonText = this.jsonText.substring(5).trim();
			return jsonText;
		}
		return null;
	}
	
	private String getNULLString()
	{
		String jsonText = this.jsonText.substring(0, 3);
		if (jsonText.equalsIgnoreCase("nil")) {
			this.jsonText = this.jsonText.substring(3).trim();
			return jsonText;
		}
		jsonText = this.jsonText.substring(0, 4);
		if (jsonText.equalsIgnoreCase("null") || jsonText.equalsIgnoreCase("none")) {
			this.jsonText = this.jsonText.substring(4).trim();
			return jsonText;
		}
		jsonText = this.jsonText.substring(0, 9);
		if (jsonText.equalsIgnoreCase("undefined")) {
			this.jsonText = this.jsonText.substring(9).trim();
			return jsonText;
		}
		return null;
	}
	
	private List<Object> getListValue()
	{
		List<Object> jsonList = new LinkedList<>();
		Object retValue;
		jsonText = jsonText.substring(1).trim();
		loop:
		while (true)
		{
			char idxChar = jsonText.charAt(0);
			switch (idxChar)
			{
			case ','://分割
				jsonText = jsonText.substring(1).trim();
				continue loop;
				
			case '\"'://字符串
				if ((retValue = getPureString()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
				
			case '['://数组
				if ((retValue = getListValue()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
				
			case '{'://对象
				if ((retValue = getMapValue()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
				
			case 't'://true etc.
			case 'T'://True etc.
			case 'f'://false etc.
			case 'F'://False etc.
			case 'y'://yes etc.
			case 'Y'://YES etc.
				if ((retValue = getBooleanString()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
				
			case 'u'://undefined etc.
			case 'U'://Undefined etc.
				if ((retValue = getNULLString()) == null) {
					return null;
				}
				//jsonList.add(retValue);
				continue loop;
				
			case 'n'://null nil none no etc.
			case 'N'://NULL Nil None NO etc.
				idxChar = jsonText.charAt(1);
				if ('o' == idxChar || 'O' == idxChar) //no none
				{
					if (jsonText.charAt(2) == 'n' || jsonText.charAt(2) == 'N') //none
					{
						if ((retValue = getNULLString()) == null) {
							return null;
						}
					}
					else //no
					{
						if ((retValue = getBooleanString()) == null) {
							return null;
						}
						jsonList.add(retValue);
						continue loop;
					}
				}
				else //null nil
				{
					if ((retValue = getNULLString()) == null) {
						return null;
					}
				}
				//jsonList.add(retValue);
				continue loop;
				
			case ']'://解析list完成
				jsonText = jsonText.substring(1).trim();
				return jsonList.size() == 0 ? null : jsonList;

			default://number
				if ((retValue = getNumberString()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
			}
		}
	}
	
	private Map<String, ?> getMapValue()
	{
		Map<String, Object> jsonMap = new HashMap<>();
		String retKey = null;
		Object retValue = null;
		this.jsonText = this.jsonText.substring(1).trim();
		loop:
		while (true)
		{
			char idxChar = jsonText.charAt(0);
			switch (idxChar)
			{
			case ','://分割
			case ':':
				jsonText = jsonText.substring(1).trim();
				continue loop;
				
			case '\"'://字符串
				if (retKey == null) {
					if ((retKey = getPureString()) == null) {
						return null;
					}
				} else {
					if ((retValue = getPureString()) == null) {
						return null;
					}
					jsonMap.put(retKey, retValue);
					retKey = null;
				}
				continue loop;
				
			case '['://数组
				if (retKey == null || (retValue = getListValue()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
				
			case '{'://对象
				if (retKey == null || (retValue = getMapValue()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
				
			case 't'://true
			case 'T'://True
			case 'f'://false
			case 'F'://False
			case 'y'://yes
			case 'Y'://YES
				if (retKey == null || (retValue = getBooleanString()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
				
			case 'u'://undefined
			case 'U'://undefined
				if (retKey == null || getNULLString() == null) {
					return null;
				}
				retKey = null;
				continue loop;
				
			case 'n'://null nil none no
			case 'N'://null nil none no
				if (retKey == null) {
					return null;
				}
				idxChar = jsonText.charAt(1);
				if ('o' == idxChar || idxChar == 'O') //no none
				{
					if (jsonText.charAt(2) == 'n' || jsonText.charAt(2) == 'N') //none
					{
						if ((retValue = getNULLString()) == null) {
							return null;
						}
					}
					else //no
					{
						if ((retValue = getBooleanString()) == null) {
							return null;
						}
						jsonMap.put(retKey, retValue);
					}
				}
				else //null nil
				{
					if ((retValue = getNULLString()) == null) {
						return null;
					}
				}
				retKey = null;
				continue loop;
				
			case '}'://解析map完成
				jsonText = jsonText.substring(1).trim();
				return jsonMap.size() == 0 ? null : jsonMap;

			default://number
				if (retKey == null || (retValue = getNumberString()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
			}
		}
	}
	
	@Override
	public String toString() {
		return serialize(jsonValue);
	}
	
	private String serialize(Object obj)
	{
		if (null == obj) {
			return null;
		}
		final Class<?> clazz = obj.getClass();
		if (clazz == String.class)
		{
			String ret = (String) obj;
			if (ret.matches("^\\w*$")) {
				return "\"" + ret + "\"";
			}
			final int len = ret.length();
			StringBuilder sb = new StringBuilder(len + len);
			for (int i = 0; i < len; i++)
			{
				char chr = ret.charAt(i);
				if (47 < chr && chr < 58)
				{
					sb.append(chr);
					continue;
				}
				if (64 < chr && chr < 91)
				{
					sb.append(chr);
					continue;
				}
				if (96 < chr && chr < 123)
				{
					sb.append(chr);
					continue;
				}
				String tmp = Integer.toHexString(chr);
				while (tmp.length() < 4) {
					tmp = "0" + tmp;
				}
				tmp = "\\u" + tmp;
				sb.append(tmp);
			}
			return "\"" + sb.toString() + "\"";
		}
		if (Boolean.class == clazz || obj instanceof Number) {
			return obj.toString();
		}
		if (obj instanceof List<?>) { // 线性集合
			return serializeList((List<?>)obj);
		}
		if (obj instanceof Map<?, ?>) { // 键值对
			return serializeMap((Map<?, ?>)obj);
		}
		return null;
	}
	
	private String serializeList(List<?> obj)
	{
		StringBuilder sb = new StringBuilder("[");
		for (Object object : obj)
		{
			String value = serialize(object);
			if (null != value) {
				sb.append(value).append(",");
			}
		}
		int len = sb.length();
		if (1 < len) {
			sb.deleteCharAt(len - 1);
		}
		return sb.append("]").toString();
	}
	
	private String serializeMap(Map<?, ?> obj)
	{
		StringBuilder sb = new StringBuilder("{");
		Set<?> keys = obj.keySet();
		if (null == keys || 0 == keys.size()) {
			return sb.append("}").toString();
		}
		for (Object key : keys)
		{
			String stringKey = serialize(key);
			Object value = obj.get(key);
			String stringValue = "null";
			if (value != null) {
				stringValue = serialize(value);
			}
			if (null != stringValue)
			{
				if (null == stringKey) {
					stringKey = "\"null\"";
				}
				sb.append(stringKey).append(":").append(stringValue).append(",");
			}
		}
		int len = sb.length();
		if (1 < len) {
			sb.deleteCharAt(len - 1);
		}
		return sb.append("}").toString();
	}
	
	private Object jsonValue;
	private String jsonText;
	private boolean hasError;
}