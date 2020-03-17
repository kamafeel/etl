package com.linkage.ftpdrudgery.tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.linkage.intf.tools.StringUtils;

/**
 * 利用反射状态纵表数据到Bean
 * 
 * @author run[zhangqi@lianchuang.com] 2:43:25 PM Nov 10, 2009
 */

public class BeanReflect {

	// get方法前缀
	private static String getMethodPrefix = "get";
	// set方法前缀
	private static String setMethodPrefix = "set";
	// EL表达式分割符
	private static String spliter = "\\.";

	/**
	 * 得到方法名称
	 * 
	 * @param property
	 * @param prefix
	 * @return
	 */
	private static String getMethodName(String property, String prefix) {
		return new StringBuilder(prefix).append(
				StringUtils.upperCaseInitial(property)).toString();
	}

	/**
	 * 反射set元素属性
	 * @param bean
	 * @param property
	 * @param obj
	 * @param cla
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static void setProperty(Object bean, String property,
			Object obj, Class<?>... cla) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class<?> beanClass = bean.getClass();
		Method setMethod = beanClass.getDeclaredMethod(getMethodName(property,
				setMethodPrefix), cla);
		setMethod.invoke(bean, obj);
	}
	
	/**
	 * 调用WebService
	 * @param portType
	 * @param methodName
	 * @param obj
	 * @param cla
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Object callWebService(Object portType, String methodName,
			Object obj, Class<?>... cla) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {

		Class<?> portTypeClass = portType.getClass();
		Method method = portTypeClass.getDeclaredMethod(methodName, cla);
		return method.invoke(portType, obj);
	}

	/**
	 * 反射get元素
	 * 
	 * @param bean
	 * @param property
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Object getProperty(Object bean, String property)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		Class<?> beanClass = bean.getClass();
		Method getMethod = beanClass.getDeclaredMethod(getMethodName(property,
				getMethodPrefix));
		return getMethod.invoke(bean);
	}

	/**
	 * Field赋值
	 * 
	 * @param bean
	 * @param property
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public static void setField(Object bean, String property, Object value)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		Class<?> beanClass = bean.getClass();
		Field field = beanClass.getDeclaredField(property);
		field.setAccessible(true);
		field.set(bean, value);
	}
	
	/**
	 * 获取元素值,根据EL
	 * @param bean
	 * @param expr
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static Object doParseByEL(Object bean, String expr)
			throws IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		String keys[] = expr.split(spliter);

		if (keys.length == 0) {
			throw new IllegalArgumentException("expr不符合规定");
		}
		Object obj = null;
		for (int i = 1; i < keys.length; i++) {
			obj = getProperty(bean, keys[i]);
			bean = obj;
		}
		return obj;
	}
}
