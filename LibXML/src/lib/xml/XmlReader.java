package lib.xml;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public final class XmlReader extends Object implements Serializable, Comparable<XmlReader>
{
	private transient Object xmlValue;
	private volatile String xmlText;
	
	protected XmlReader(String xmlText)
	{
		if (null == xmlText) {
			return;
		}
		xmlText = xmlText.trim();
		if (xmlText.length() < 2) {
			return;
		}
		this.xmlText = xmlText;
		char prefix = xmlText.charAt(0);
		switch (prefix) {
		case '[':
			xmlValue = getListValue();
			break;
				
		case '{':
			xmlValue = getMapValue();
			break;

		default:
			throw new RuntimeException("--- json format error ---");
		}
		if (xmlValue == null) {
			throw new RuntimeException("--- json format error ---");
		}
	}
	
	public String getStringValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		return null;
	}
	
	public Integer getIntValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value, 10);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (value instanceof Boolean) {
			return (Boolean) value ? 1 : 0;
		}
		return null;
	}
	
	public Long getLongValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value, 10);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (value instanceof Boolean) {
			return (Boolean) value ? 1L : 0L;
		}
		return null;
	}
	
	public BigInteger getBigIntValue(String jnl)
	{
		//Object value = getJnlParsedValue(jnl);
		return null;
	}
	
	public Double getRealValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		if (value instanceof String) {
			try {
				return Double.parseDouble((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
	
	public BigDecimal getBigRealValue(String jnl)
	{
		//Object value = getJnlParsedValue(jnl);
		return null;
	}
	
	public Boolean getBooleanValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value instanceof String) {
			if (((String) value).equalsIgnoreCase("true")) {
				return true;
			}
			if (((String) value).equalsIgnoreCase("false")) {
				return false;
			}
			return null;
		}
		if (value instanceof Long || value instanceof BigInteger) {
			int lv = ((Number) value).intValue();
			if (1 == lv) {
				return true;
			}
			if (0 == lv) {
				return false;
			}
			return null;
		}
		return null;
	}
	
	public List<?> getListValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof List<?>) {
			return (List<?>) value;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMapValue(String jnl)
	{
		Object value = getValue(jnl, false);
		if (value instanceof Map<?, ?>) {
			return (Map<String, ?>) value;
		}
		return null;
	}
	
	public Object getRootValue()
	{
		if (xmlValue instanceof Map) {
			return ((HashMap<?, ?>) xmlValue).clone();
		}
		return ((LinkedList<?>) xmlValue).clone();
	}

	public Object getValue(String jnl, boolean delete)
	{
		List<String> keys = getJnlParsedKeys(jnl);
		if (null == keys) {
			return null;
		}
		Iterator<String> it = keys.iterator();
		Object obj = xmlValue;
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
		if (delete) {
			if (parent instanceof List<?>) {
				((List<?>) parent).remove(idx);
			} else {
				((Map<?, ?>) parent).remove(keys.get(keys.size() - 1));
			}
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public Object setValue(String jnl, Object jsonParam)
	{
		if (jsonParam instanceof Integer) {
			jsonParam = ((Number) jsonParam).longValue();
		}
		List<String> keys = getJnlParsedKeys(jnl);
		if (null == keys || null == jsonParam) {
			return null;
		}
		Iterator<String> it = keys.iterator();
		Object obj = xmlValue;
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
		if (parent instanceof List<?>) {
			((List<?>) parent).remove(idx);
			((List<Object>) parent).add(idx, jsonParam);
		} else {
			idx = keys.size() - 1;
			((Map<String, Object>) parent).put(keys.get(idx), jsonParam);
		}
		return jsonParam;
	}
	
	private List<String> getJnlParsedKeys(String jnl)
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
	
	private String getStringValue()
	{
		StringBuilder retBuilder = new StringBuilder();
		String jsonText = this.xmlText;
		int len = jsonText.length();
		loop:
		for (int i = 1; i < len; i++)
		{
			final char idxChar = jsonText.charAt(i);
			if (idxChar == '\"')
			{
				this.xmlText = jsonText.substring(i + 1).trim();
				return retBuilder.toString();
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
				return null;
			}
			retBuilder.append(escChar);
			i = --hexEndIdx;
		}
		return null;
	}
	
	private Number getNumberValue()
	{
		StringBuilder retBuilder = new StringBuilder();
		String jsonText = this.xmlText;
		this.xmlText = "";
		int len = jsonText.length();
		loop:
		for (int i = 0; i < len; i++)
		{
			final char idxChar = jsonText.substring(i, i + 1).toUpperCase().charAt(0);
			if (47 < idxChar && idxChar < 58 || 64 < idxChar && idxChar < 71)
			{
				retBuilder.append(idxChar);
				continue loop;
			}
			switch (idxChar)
			{
			case '+':
			case '-':
			case 'E':
			case 'X':
				retBuilder.append(idxChar);
				continue loop;

			default:
				this.xmlText = jsonText.substring(i).trim();
				break loop;
			}
		}
		jsonText = retBuilder.toString();
		if (jsonText.length() == 0) {
			return null;
		}
		char idxChar = jsonText.charAt(0);
		boolean negative = idxChar == '-';
		if (negative) {
			jsonText = jsonText.substring(1);
			if (jsonText.length() == 0) {
				return null;
			}
			idxChar = jsonText.charAt(0);
		}
		boolean integer = jsonText.indexOf('.') == -1;
		if (!integer) {//小数点开头的10进制小数
			if (idxChar == '.') {
				idxChar = '0';
				jsonText = idxChar + jsonText;
			}
		}
		int cardinal;
		switch (idxChar)
		{
		case 'B'://2进制
			cardinal = 2;
			break;
			
		case 'X'://16进制
			cardinal = 16;
			break;
			
		case '0'://2，8，16进制,10进制小数
			switch (jsonText.charAt(1))
			{
			case 'B'://2进制
				cardinal = 2;
				break;
				
			case 'X'://16进制
				cardinal = 16;
				break;
				
			case '.'://10进制小数
				cardinal = 10;
				break;

			default://8进制
				cardinal = 8;
			}
			break;

		default://10进制
			cardinal = 10;
		}
		if (integer) // 整数
		{
			long ret;
			try {
				ret = Long.parseUnsignedLong(jsonText, cardinal);
			} catch (NumberFormatException e) {
				return null;
			}
			if (negative) {
				return -ret;
			}
			return ret;
		}
		else // 小数
		{
			double ret;
			try {
				ret = Double.parseDouble(jsonText);
			} catch (NumberFormatException e) {
				return null;
			}
			if (negative) {
				return -ret;
			}
			return ret;
		}
	}
	
	private Boolean getBooleanValue()
	{
		String jsonText = this.xmlText.substring(0, 1);
		if ("0".equals(jsonText)) {
			this.xmlText = this.xmlText.substring(1).trim();
			return false;
		}
		if ("1".equals(jsonText)) {
			this.xmlText = this.xmlText.substring(1).trim();
			return true;
		}
		if (this.xmlText.substring(0, 2).equalsIgnoreCase("no")) {
			this.xmlText = this.xmlText.substring(2).trim();
			return false;
		}
		if (this.xmlText.substring(0, 3).equalsIgnoreCase("yes")) {
			this.xmlText = this.xmlText.substring(3).trim();
			return true;
		}
		if (this.xmlText.substring(0, 4).equalsIgnoreCase("true")) {
			this.xmlText = this.xmlText.substring(4).trim();
			return true;
		}
		if (this.xmlText.substring(0, 5).equalsIgnoreCase("false")) {
			this.xmlText = this.xmlText.substring(5).trim();
			return false;
		}
		return null;
	}
	
	private Object getNULLValue()
	{
		String jsonText = this.xmlText.substring(0, 3);
		if (jsonText.equalsIgnoreCase("nil")) {
			this.xmlText = this.xmlText.substring(3).trim();
			return null;
		}
		jsonText = this.xmlText.substring(0, 4).toLowerCase();
		if (jsonText.equals("null") || jsonText.equals("none")) {
			this.xmlText = this.xmlText.substring(4).trim();
			return null;
		}
		if (this.xmlText.substring(0, 9).equalsIgnoreCase("undefined")) {
			this.xmlText = this.xmlText.substring(9).trim();
			return null;
		}
		return 0;
	}
	
	private List<Object> getListValue()
	{
		List<Object> jsonList = new LinkedList<>();
		Object retValue;
		xmlText = xmlText.substring(1).trim();
		loop:
		while (true)
		{
			char idxChar = xmlText.substring(0, 1).toLowerCase().charAt(0);
			switch (idxChar)
			{
			case ','://分割
				xmlText = xmlText.substring(1).trim();
				continue loop;
				
			case '\"'://字符串
				if ((retValue = getStringValue()) == null) {
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
				
			case 't'://true
			case 'f'://false
			case 'y'://yes
				if ((retValue = getBooleanValue()) == null) {
					return null;
				}
				jsonList.add(retValue);
				continue loop;
				
			case 'u'://undefined
				if (getNULLValue() != null) {
					return null;
				}
				continue loop;
				
			case 'n'://null nil none no
				idxChar = xmlText.charAt(1);
				if ('o' == idxChar || 'O' == idxChar) //no none
				{
					if (xmlText.charAt(2) == 'n' || xmlText.charAt(2) == 'N') //none
					{
						if (getNULLValue() != null) {
							return null;
						}
					}
					else //no
					{
						if ((retValue = getBooleanValue()) == null) {
							return null;
						}
						jsonList.add(retValue);
					}
				}
				else //null nil
				{
					if (getNULLValue() != null) {
						return null;
					}
				}
				continue loop;
				
			case ']'://解析list完成
				xmlText = xmlText.substring(1).trim();
				return jsonList;

			default://number
				if ((retValue = getNumberValue()) == null) {
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
		this.xmlText = this.xmlText.substring(1).trim();
		loop:
		while (true)
		{
			char idxChar = xmlText.substring(0, 1).toLowerCase().charAt(0);
			switch (idxChar)
			{
			case ','://分割
			case ':':
				xmlText = xmlText.substring(1).trim();
				continue loop;
				
			case '\"'://字符串
				if (retKey == null) {
					if ((retKey = getStringValue()) == null) {
						return null;
					}
				} else {
					if ((retValue = getStringValue()) == null) {
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
			case 'f'://false
			case 'y'://yes
				if (retKey == null || (retValue = getBooleanValue()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
				
			case 'u'://undefined
				if (retKey == null || getNULLValue() != null) {
					return null;
				}
				retKey = null;
				continue loop;
				
			case 'n'://null nil none no
				if (retKey == null) {
					return null;
				}
				idxChar = xmlText.charAt(1);
				if ('o' == idxChar || idxChar == 'O') //no none
				{
					if (xmlText.charAt(2) == 'n' || xmlText.charAt(2) == 'N') //none
					{
						if (getNULLValue() != null) {
							return null;
						}
					}
					else //no
					{
						if ((retValue = getBooleanValue()) == null) {
							return null;
						}
						jsonMap.put(retKey, retValue);
					}
				}
				else //null nil
				{
					if (getNULLValue() != null) {
						return null;
					}
				}
				retKey = null;
				continue loop;
				
			case '}'://解析map完成
				xmlText = xmlText.substring(1).trim();
				return jsonMap;

			default://number
				if (retKey == null || (retValue = getNumberValue()) == null) {
					return null;
				}
				jsonMap.put(retKey, retValue);
				retKey = null;
				continue loop;
			}
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return serialize(xmlValue);
	}
	
	@Override
	public int compareTo(XmlReader o) {
		return toString().compareTo(o.toString());
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
}