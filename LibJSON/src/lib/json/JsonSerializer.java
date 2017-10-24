package lib.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** JSON标准：IETF RFC7159
 * 数字：必须十进制，小数整数均可，-开头可以，+开始不行, 0开头不行，科学计数法有效e大写小写均可
 * 字符串：双引号括起来，控制字符必须转义\\uxxxx,Unicode字符集，必须4个16进制数表示，大写小写均可。
 * 字面值：true false null
 * 
 * 扩展如下：
 * 数字：支持2进制，8进制，十进制，16进制，+-号，显然：0开头即是8进制，0x，0X,x,X同义十16进制，0b,0B,b,B同义是2进制，a~f不分大小写,小数.开头,支持巨大浮点数/整数
 * 字符串：U,u同义
 * 字面值：不分大小写nil,none,undefined视为null,yes,no,按boolean处理，0,1根据需要可以按boolean处理*/
public final class JsonSerializer extends Serializer
{
	public String serialize(Object obj)
	{
		if (null == obj) {
			return null;
		}
		final Class<?> clazz = obj.getClass();
		if (Character.class == clazz || obj instanceof CharSequence) {
			return null;
		}
		if (Boolean.class == clazz || obj instanceof Number) {
			return null;
		}
		if (obj instanceof JsonSerializable) {
			return ((JsonSerializable)obj).toJsonValue();
		}
		if (clazz.isEnum()) {
            return "[\"" + ((Enum<?>)obj).name() + "\"]";
		}
		if (obj instanceof Class) {
			return serializeBean(obj, false);
		}
		return serialize(obj, true);
	}
	
	private String serialize(Object obj, boolean asValue)
	{
		if (null == obj) {
			return null;
		}
		final Class<?> clazz = obj.getClass();
		if (clazz == Character.class || obj instanceof CharSequence)
		{
			String ret = obj.toString();
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
		if (obj instanceof JsonSerializable) {
			JsonSerializable jsonAble = (JsonSerializable) obj;
			return asValue ? jsonAble.toJsonValue() : jsonAble.toJsonKey();
		}
		if (obj instanceof Iterable<?>) { // 线性集合
			return serializeList((Iterable<?>)obj);
		}
		if (obj instanceof Map<?, ?>) { // 键值对
			return serializeMap((Map<?, ?>)obj);
		}
		if (clazz.isEnum()) { // 枚举对象
			return "\"" + ((Enum<?>)obj).name() + "\"";
		}
		if (obj instanceof Class) // 类，静态，枚举类
		{
			Class<?> type = (Class<?>)obj;
			if (type.isEnum()) {
				return serializeEnum(type);
			}
			return "\"" + (type).toGenericString() + "\"";
		}
		if (clazz.isArray()) { // 数组
			return serializeArray(obj);
		}
		if (clazz.isAnnotation()) { // 注解
			return serializeAnnotation((Annotation) obj);
		}
		return serializeBean(obj, true);
	}
	
	private String serializeList(Iterable<?> obj)
	{
		StringBuilder sb = new StringBuilder("[");
		for (Object object : obj)
		{
			String value = serialize(object, true);
			if (excludeLetterNull) {
				if (null == value) {
					continue;
				}
			} else {
				if (null == value) {
					value = "null";
				}
			}
			sb.append(value).append(",");
		}
		int len = sb.length();
		if (1 != len) {
			sb.deleteCharAt(len - 1);
			return sb.append("]").toString();
		}
		return excludeEffectNull ? null : "[]";
	}
	
	private String serializeMap(Map<?, ?> obj)
	{
		StringBuilder sb = new StringBuilder("{");
		Set<?> keys = obj.keySet();
		if (null == keys || 0 == keys.size()) {
			return excludeEffectNull ? null : "{}";
		}
		for (Object key : keys)
		{
			String stringKey = serialize(key, false);
			Object value = obj.get(key);
			String stringValue = serialize(value, true);
			if (excludeLetterNull) {
				if (null == stringValue) {
					continue;
				}
			} else {
				if (null == stringValue) {
					stringValue = "null";
				}
			}
			if (null == stringKey) {
				stringKey = "\"null\"";
			}
			sb.append(stringKey).append(":").append(stringValue).append(",");
		}
		int len = sb.length();
		if (1 != len) {
			sb.deleteCharAt(len - 1);
			return sb.append("}").toString();
		}
		return excludeEffectNull ? null : "{}";
	}
	
	private String serializeArray(Object obj)
	{
		StringBuilder sb = new StringBuilder("[");
		int len = Array.getLength(obj);
		if (0 == len) {
			return excludeEffectNull ? null : "[]";
		}
		for (int i = 0; i < len; i++)
		{
			Object element = Array.get(obj, i);
			String value = serialize(element, true);
			if (excludeLetterNull) {
				if (null == value) {
					continue;
				}
			} else {
				if (null == value) {
					value = "null";
				}
			}
			sb.append(value).append(",");
		}
		len = sb.length();
		if (1 != len) {
			sb.deleteCharAt(len - 1);
			return sb.append("]").toString();
		}
		return excludeEffectNull ? null : "[]";
	}
	
	private String serializeAnnotation(Annotation obj)
	{
		StringBuilder sb = new StringBuilder("{");
		Class<?> annotation = obj.getClass().getInterfaces()[0];
		Method[] methods = annotation.getDeclaredMethods();
		if (null == methods || 0 == methods.length) {
			return excludeEffectNull ? null : "{}";
		}
		for (Method method : methods) // 取所有方法的返回值
		{
			Object value = null;
			try {
				value = method.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				continue;
			}
			RequireGet require = method.getDeclaredAnnotation(RequireGet.class);
			value = this.serialize(value, true);
			String key = null;
			if (null == require)
			{
				if (excludeLetterNull) {
					if (null == value) {
						continue;
					}
				} else {
					if (null == value) {
						value = "null";
					}
				}
				key = this.getFieldName(method);
			}
			else if (require.value() == RequireType.FALSE)
			{
				continue;
			}
			else
			{
				if (require.value() == RequireType.NOT_NULL)
				{
					if (null == value) {
						continue;
					}
				}
				else // RequireType.TRUE
				{
					if (null == value) {
						value = "null";
					}
				}
				if (require.name().length() == 0) {
					key = this.getFieldName(method);
				} else {
					key = this.serialize(require.name(), false);
				}
			}
			sb.append("\"").append(key).append("\":").append(value).append(",");
		}
		int len = sb.length();
		if (1 != len) {
			sb.deleteCharAt(len - 1);
			return sb.append("}").toString();
		}
		return excludeEffectNull ? null : "{}";
	}
	
	@SuppressWarnings("null")
	private String serializeEnum(Class<?> clazz)
	{
		StringBuilder sb = new StringBuilder("[");
		Field field = null;
		Enum<?>[] enums = null;
		try {
			field = clazz.getDeclaredField("ENUM$VALUES");
			if (null == field) {
				return excludeEffectNull ? null : "[]";
			}
			field.setAccessible(true);
			enums = (Enum<?>[]) field.get(clazz);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		if (null == enums && 0 == enums.length) {
			return excludeEffectNull ? null : "[]";
		}
		for (Enum<?> enumObj : enums) {
			sb.append("\"").append(enumObj.name()).append("\",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.append("]").toString();
	}
	
	private String serializeBean(Object obj, boolean asInstance)
	{
		Class<?> beanClass = asInstance ? obj.getClass() : (obj instanceof Class ? (Class<?>)obj : obj.getClass());
		StringBuilder sb = null;
		if (!asInstance) // 作为静态
		{
			if (beanClass.isPrimitive()) {
				return "[\"" + beanClass.getSimpleName() + "\"]";
			}
			if (beanClass.isEnum()) {
				return serializeEnum(beanClass);
			}
		}
		List<Method> allValidMethods = new LinkedList<>();
		List<Method> allIgnoreMethods = new LinkedList<>();
		List<Field> allValidFields = new LinkedList<>();
		List<Field> allIgnoreFields = new LinkedList<>();
		final Class<?> finalClass = excludeRootClass ? Object.class : null;
		while (beanClass != finalClass) {
			getClassProperties(beanClass, asInstance, allValidMethods, allIgnoreMethods, allValidFields, allIgnoreFields);
			List<Class<?>> types = Arrays.asList(beanClass.getInterfaces());
			List<Class<?>> classes = new LinkedList<>();
			while (true)
			{
				for (int i = 0, icnt = types.size(); i < icnt; i++)
				{
					Class<?> type = types.get(i);
					getClassProperties(type, asInstance, allValidMethods, allIgnoreMethods, allValidFields, allIgnoreFields);
					Class<?>[] tmpTypes = type.getInterfaces();
					if (null != tmpTypes && 0 != tmpTypes.length) {
						classes.addAll(Arrays.asList(tmpTypes));
					}
				}
				if (classes.size() == 0) {
					break;
				}
				types = classes;
				classes = new LinkedList<>();
			}
			beanClass = beanClass.getSuperclass();
		} // while end
		sb = new StringBuilder("{");
		for (Method method : allValidMethods) // 取所有方法的返回值
		{
			method.setAccessible(true);
			Object value = null;
			try {
				value = method.invoke(asInstance ? obj : null);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				continue;
			}
			RequireGet require = method.getDeclaredAnnotation(RequireGet.class);
			value = this.serialize(value, true);
			String key = null;
			if (null == require)
			{
				if (excludeLetterNull) {
					if (null == value) {
						continue;
					}
				} else {
					if (null == value) {
						value = "null";
					}
				}
				key = this.getFieldName(method);
			}
			else
			{
				if (require.value() == RequireType.NOT_NULL) {
					if (null == value) {
						continue;
					}
				} else { // RequireType.TRUE
					if (null == value) {
						value = "null";
					}
				}
				if (require.name().length() == 0) {
					key = this.getFieldName(method);
				} else {
					key = this.serialize(require.name(), false);
				}
			}
			sb.append("\"").append(key).append("\":").append(value).append(",");
		}
		for (Field field : allValidFields) // 取所有字段的值
		{
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(asInstance ? obj : null);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				continue;
			}
			RequireGet require = field.getDeclaredAnnotation(RequireGet.class);
			String key = null;
			value = this.serialize(value, true);
			if (null == require)
			{
				if (excludeLetterNull) {
					if (null == value) {
						continue;
					}
				} else {
					if (null == value) {
						value = "null";
					}
				}
				key = field.getName();
			}
			else
			{
				if (require.value() == RequireType.NOT_NULL) {
					if (null == value) {
						continue;
					}
				} else { // RequireType.TRUE
					if (null == value) {
						value = "null";
					}
				}
				if (require.name().length() == 0) {
					key = field.getName();
				} else {
					key = this.serialize(require.name(), false);
				}
			}
			sb.append("\"").append(key).append("\":").append(value).append(",");
		}
		int len = sb.length();
		if (1 != len) {
			sb.deleteCharAt(len - 1);
			return sb.append("}").toString();
		}
		return excludeEffectNull ? null : "{}";
	}
	
	private void getClassProperties(Class<?> beanClass, boolean asInstance, List<Method> allValidMethods, List<Method> allIgnoreMethods, List<Field> allValidFields, List<Field> allIgnoreFields)
	{
		Method[] beanMethods = beanClass.getDeclaredMethods();
		List<Method> validMethods = new LinkedList<>();
		List<Method> ignoreMethods = new LinkedList<>();
		loopMethod:
		for (int i = 0, icnt = beanMethods.length; i <icnt; i++) // 取有效方法和无效方法
		{
			Method method = beanMethods[i];
			int methodModifier = method.getModifiers();
			if (Modifier.isStatic(methodModifier) == asInstance) { // 静态与实例必须匹配
				continue;
			}
			if (Modifier.isAbstract(methodModifier)) {
				continue;
			}
			if (method.getParameterCount() != 0) { // 有参数
				continue;
			}
			Class<?> methodReturnType = method.getReturnType();
			if (void.class == methodReturnType || Void.class == methodReturnType) { // 无返回值
				continue;
			}
			RequireGet require = method.getDeclaredAnnotation(RequireGet.class);//注解优先
			if (null != require) { // 存在忽略注解
				@SuppressWarnings("unused")
				boolean b = require.value() == RequireType.FALSE ? ignoreMethods.add(method) : validMethods.add(method);
				continue;
			}
			final String mn = method.getName();
			if (standardAccessor) {
				if (!mn.startsWith("get") && !mn.startsWith("is")) {
					continue loopMethod;
				}
			} else {
				if (mn.equals("clone")) {
					continue loopMethod;
				}
				if (JsonSerializable.class.isAssignableFrom(beanClass)) {
					Method[] tmpMethods = JsonSerializable.class.getDeclaredMethods();
					for (Method tmpMethod : tmpMethods) {
						if (tmpMethod.getName().equals(mn)) {
							continue loopMethod;
						}
					}
				}
			}
			boolean isPrivate = Modifier.isPrivate(methodModifier);
			boolean isProtected = Modifier.isProtected(methodModifier);
			boolean isPublic = Modifier.isPublic(methodModifier);
			boolean isPackage = !(isPrivate || isProtected || isPublic);
			switch (accesserModifier)
			{
			case PUBLIC:
				if (!isPublic) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PROTECTED:
				if (!isProtected) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PACKAGE:
				if (!isPackage) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PRIVATE:
				if (!isPrivate) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PROTECTED:
				if (isPackage || isPrivate) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PACKAGE:
				if (isProtected || isPrivate) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PRIVATE:
				if (isPackage || isProtected) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PROTECTED | PACKAGE:
				if (isPublic || isPrivate) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PROTECTED | PRIVATE:
				if (isPackage || isPublic) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PACKAGE | PRIVATE:
				if (isPublic || isProtected) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PROTECTED | PACKAGE:
				if (isPrivate) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PROTECTED | PRIVATE:
				if (isPackage) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PUBLIC | PACKAGE | PRIVATE:
				if (isProtected) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
				break;
				
			case PROTECTED | PACKAGE | PRIVATE:
				if (isPublic) {
					ignoreMethods.add(method);
					continue loopMethod;
				}
			}
			validMethods.add(method);
		}
		Field[] beanFields = beanClass.getDeclaredFields();
		List<Field> validFields = new LinkedList<>();
		List<Field> ignoreFields = new LinkedList<>();
		loopField:
		for (int i = 0, icnt = beanFields.length; i <icnt; i++) // 取有效字段和无效字段
		{
			Field field = beanFields[i];
			int fieldModifier = field.getModifiers();
			if (Modifier.isStatic(fieldModifier) == asInstance) { // 静态与实例必须匹配
				continue;
			}
			RequireGet require = field.getDeclaredAnnotation(RequireGet.class);//注解优先
			if (null != require) { // 存在忽略注解
				@SuppressWarnings("unused")
				boolean b = require.value() == RequireType.FALSE ? ignoreFields.add(field) : validFields.add(field);
				continue;
			}
			boolean isPrivate = Modifier.isPrivate(fieldModifier);
			boolean isProtected = Modifier.isProtected(fieldModifier);
			boolean isPublic = Modifier.isPublic(fieldModifier);
			boolean isPackage = !(isPrivate || isProtected || isPublic);
			switch (accesserModifier)
			{
			case PUBLIC:
				if (!isPublic) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PROTECTED:
				if (!isProtected) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PACKAGE:
				if (!isPackage) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PRIVATE:
				if (!isPrivate) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PROTECTED:
				if (isPackage || isPrivate) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PACKAGE:
				if (isProtected || isPrivate) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PRIVATE:
				if (isPackage || isProtected) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PROTECTED | PACKAGE:
				if (isPublic || isPrivate) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PROTECTED | PRIVATE:
				if (isPackage || isPublic) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PACKAGE | PRIVATE:
				if (isPublic || isProtected) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PROTECTED | PACKAGE:
				if (isPrivate) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PROTECTED | PRIVATE:
				if (isPackage) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PUBLIC | PACKAGE | PRIVATE:
				if (isProtected) {
					ignoreFields.add(field);
					continue loopField;
				}
				break;
				
			case PROTECTED | PACKAGE | PRIVATE:
				if (isPublic) {
					ignoreFields.add(field);
					continue loopField;
				}
			}
			validFields.add(field);
		}
		if (validFields.size() != 0) // 去除字段与方法的重复
		{
			final int validMethodCount = validMethods.size();
			String[] computedFieldNames = new String[validMethodCount];
			for (int i = 0; i < validMethodCount; i++) {
				computedFieldNames[i] = getFieldName(validMethods.get(i));
			}
			for (int i = 0; i < validMethodCount; i++)//字段名与有效方法字段名一样就删除
			{
				String fieldName = computedFieldNames[i];
				for (int j = 0; j < validFields.size(); j++)
				{
					if (validFields.get(j).getName().equals(fieldName)) {
						validFields.remove(j--);
					}
				}
			}
			final int ignoreMethodCount = ignoreMethods.size();
			computedFieldNames = new String[ignoreMethodCount];
			for (int i = 0; i < ignoreMethodCount; i++) {
				computedFieldNames[i] = getFieldName(ignoreMethods.get(i));
			}
			for (int i = 0; i < ignoreMethodCount; i++)//字段名与忽略方法字段名一样要删除
			{
				String fieldName = computedFieldNames[i];
				for (int j = 0; j < validFields.size(); j++)
				{
					if (validFields.get(j).getName().equals(fieldName)) {
						validFields.remove(j--);
					}
				}
			}
		}
		for (Method methodObj : allIgnoreMethods) { // 如果方法已经被忽略，应该移除
			String methodName = methodObj.getName();
			for (int i = 0; i < validMethods.size(); i++) {
				if (validMethods.get(i).getName().equals(methodName)) {
					validMethods.remove(i--);
				}
			}
			for (int i = 0; i < ignoreMethods.size(); i++) {
				if (ignoreMethods.get(i).getName().equals(methodName)) {
					ignoreMethods.remove(i--);
				}
			}
		}
		for (Method methodObj : allValidMethods) { // 如果方法已经存在，应该移除
			String methodName = methodObj.getName();
			for (int i = 0; i < validMethods.size(); i++) {
				if (validMethods.get(i).getName().equals(methodName)) {
					validMethods.remove(i--);
				}
			}
			for (int i = 0; i < ignoreMethods.size(); i++) {
				if (ignoreMethods.get(i).getName().equals(methodName)) {
					ignoreMethods.remove(i--);
				}
			}
		}
		allValidMethods.addAll(validMethods); // 当前有效方法全部加入目标方法列表
		allIgnoreMethods.addAll(ignoreMethods); // 当前忽略方法全部加入忽略方法列表
		for (Field fieldObj : allIgnoreFields) // 如果字段已经被忽略，应该移除
		{
			String fieldName = fieldObj.getName();
			for (int i = 0; i < validFields.size(); i++) {
				if (validFields.get(i).getName().equals(fieldName)) {
					validFields.remove(i--);
				}
			}
			for (int i = 0; i < ignoreFields.size(); i++) {
				if (ignoreFields.get(i).getName().equals(fieldName)) {
					ignoreFields.remove(i--);
				}
			}
		}
		for (Field fieldObj : allValidFields) // 如果字段已经存在，应该移除
		{
			String fieldName = fieldObj.getName();
			for (int i = 0; i < validFields.size(); i++) {
				if (validFields.get(i).getName().equals(fieldName)) {
					validFields.remove(i--);
				}
			}
			for (int i = 0; i < ignoreFields.size(); i++) {
				if (ignoreFields.get(i).getName().equals(fieldName)) {
					ignoreFields.remove(i--);
				}
			}
		}
		allValidFields.addAll(validFields); // 当前有效字段全部加入目标字段列表
		allIgnoreFields.addAll(ignoreFields); // 当前忽略字段全部加入忽略字段列表
	}
	
	private String getFieldName(Method method)
	{
		String methodName = method.getName();
		char[] nameChar = methodName.toCharArray();
		int prefixLen = 0;
		for (char c : nameChar)
		{
			if (96 < c) // 小写字母
			{
				prefixLen++;
			}
			else if (65 > c) // 数字 || $
			{
				prefixLen++;
			}
			else if (95 == c) // _
			{
				prefixLen++;
			}
			else
			{
				break; // 大写字母
			}
		}
		int nameLen = nameChar.length;
		if (nameLen == prefixLen) { // 全部小写字母
			return methodName;
		}
		if (0 == prefixLen) { // 大写字母开头
			return methodName;
		}
		if (prefixLen + 1 == nameLen) { // 一个大写字母结尾
			return (char)(nameChar[prefixLen] + 32) + "";
		}
		int prefixEnd = prefixLen + 1;
		if (64 < nameChar[prefixEnd] && 91 > nameChar[prefixEnd]) { // 大写字母
			return methodName.substring(prefixLen);
		}
		return (char)(methodName.charAt(prefixLen) + 32) + methodName.substring(prefixEnd);
	}
	
	public Object deserialize(String src, Object obj, String expr)
	{
		JsonReader reader = new JsonReader();
		reader.accesserModifier = accesserModifier;
		reader.excludeEffectNull = excludeEffectNull;
		reader.excludeLetterNull = excludeLetterNull;
		reader.excludeRootClass = excludeRootClass;
		reader.standardAccessor = standardAccessor;
		Object ret = reader.parseJson(src, expr);
		if (null == ret) {
			return null;
		}
		if (null == obj) {
			return ret;
		}
		return deserialize(src, obj, true);
	}
	
	private Object deserialize(Object src, Object des, boolean asInstance)
	{
		if (des instanceof Class)//静态成员，不应该对类做什么写操作,应该操作静态成员
		{
			return null;
		}
		final Class<?> clazz = des.getClass();
		if (Character.class == clazz || des instanceof CharSequence) {
			return null;
		}
		if (Boolean.class == clazz || des instanceof Number) {
			return null;
		}
		return null;
	}
	
	public <T> Collection<T> deserializeList(Collection<T> retList, List<Object> srcList)
	{
		return null;
	}
	
	public <KT, VT> Map<KT, VT> deserializeMap(Map<KT, VT> retMap, Map<String, Object> srcMap)
	{
		return null;
	}
	
	public Object deserializeArray(Object retArray, List<Object> srcList)
	{
		return null;
	}
	
	public Annotation deserializeAnnotation(Annotation obj, Map<String, Object> srcMap)
	{
		return null;
	}
	
	public Enum<?> deserializeEnum(Class<?> clazz, String string)
	{
		return null;
	}
	
	public String deserializeBean(Object obj, Map<String, Object> srcMap)
	{
		return null;
	}
}