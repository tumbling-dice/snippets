package android.widget;

/*
 * Copyright (C) 2009 - 2012 Sosuke Masui(esmasui@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

/**
 * システムサービスのローカルインターフェイスを取得するロケータークラス.
 *
 * @author esmasui@gmail.com
 *
 */
final class ServiceLocator {

	/**
	 * サービスマネージャーのクラス名.
	 */
	private static final String SERVICE_MANAGER = "android.os.ServiceManager";

	/**
	 * サービス取得メソッド名.
	 */
	private static final String GET_SERVICE_METHOD = "getService";

	/**
	 * ローカルインターフェイス取得メソッド名.
	 */
	private static final String AS_INTERFACE = "asInterface";

	/**
	 * サービスのローカルインターフェイスを取得する.
	 *
	 * @param serviceName
	 *            サービス名
	 * @param binderType
	 *            ローカルインターフェイス・スタブのクラス名
	 * @return サービスのローカルインターフェイス
	 */
	public static final synchronized Object getServiceStub(String serviceName,
			String binderType) {

		try {

			return getServiceStubInternal(serviceName, binderType);
		} catch (Exception e) {

			Log.e("ServiceLocator", "", e);
			return null;
		}
	}

	private static final ClassLoader getClassLoader() {

		return ServiceLocator.class.getClassLoader();
	}

	private static final Method getDeclaredMethod(Class<?> owner,
			String methodName, Class<?>... parameterTypes)
			throws SecurityException, NoSuchMethodException {

		Method m = owner.getDeclaredMethod(methodName, parameterTypes);

		if (!m.isAccessible()) {

			m.setAccessible(true);
		}

		return m;
	}

	private static final Method getGetServiceMethod(Class<?> serviceManager)
			throws SecurityException, NoSuchMethodException {

		return getDeclaredMethod(serviceManager, GET_SERVICE_METHOD,
				String.class);
	}

	private static final Object getServiceStub(Object binder, Class<?> stub)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			NoSuchMethodException, ClassNotFoundException {

		Method method = getAsInterfaceMethod(stub);

		return method.invoke(null, binder);
	}

	private static final Class<?> getServiceStubClass(String binderType)
			throws ClassNotFoundException {

		return getClassLoader().loadClass(binderType);
	}

	private static final Method getAsInterfaceMethod(
			Class<?> stub) throws SecurityException, NoSuchMethodException {

		return getDeclaredMethod(stub, AS_INTERFACE, IBinder.class);
	}

	private static final Class<?> getServiceManager()
			throws ClassNotFoundException {

		return getClassLoader().loadClass(SERVICE_MANAGER);
	}

	private static final Object getServiceStubInternal(String serviceName,
			String binderType) throws Exception {

		Class<?> serviceManager = getServiceManager();
		Method method = getGetServiceMethod(serviceManager);
		Object binder = method.invoke(null, serviceName);
		Class<?> stub = getServiceStubClass(binderType);
		Object service = getServiceStub(binder, stub);

		return service;
	}
}