/* Attempt to start a method call in a new Thread. The returned value
 * indicates the success or failure of this attempt. The separate thread
 * calls the method and enqueues a reply or exception.
 */
private TriStatus launchMethodCallThread(String className,
		String mangledSigName, final TriPortId tsiPortId,
		final TriAddress sutAddress, final TriComponentId componentId,
		final TriSignatureId signatureId,
		final TriParameterList parameterList) {
	try {
		Class<?> cls = ClassMapper.loadClass(className, getRegistryClassLoader(tsiPortId));
		String[] methodAndParamNames = ClassMapper.getMethodAndParamNames(mangledSigName);
		String methodName = methodAndParamNames[0];
		Class<?> paramTypes[] = loadParamTypes(ClassMapper
				.discardFirstElement(methodAndParamNames), tsiPortId);
		final Method m = cls.getMethod(methodName, paramTypes);
		final Object[] params = new Object[paramTypes.length];
		boolean objHandlePresent = !Modifier.isStatic(m.getModifiers())
				|| ClassMapper.outputObjHandleForStatics;
		for (int i = 0; i < params.length; i++) {
			if (objHandlePresent)
				params[i] = decodeParameter(parameterList.get(i + 1), paramTypes[i]);
			else
				params[i] = decodeParameter(parameterList.get(i), paramTypes[i]);

			if (!paramTypes[i].isPrimitive() && params[i] != null
					&& !paramTypes[i].isInstance(params[i]))
				throw new WrongParameterException("Wrong parameter types supplied");
		}
		final Object objInstance;
		if (!Modifier.isStatic(m.getModifiers())) {
			objInstance = decodeParameter(parameterList.get(0), Object.class);
			if (!cls.isInstance(objInstance))
				throw new WrongParameterException("Wrong object handle supplied");
		} else { objInstance = null; }

		new Thread(new Runnable() {
			public void run() {
				try {
					Object retVal = m.invoke(objInstance, params);
					TriParameter returnParam = encodeParameter(retVal);
					synchronized (RB) {
						RB.getTriCommunicationTE().triEnqueueReply(
								tsiPortId, sutAddress, componentId,
								signatureId, parameterList, returnParam);
					}
				} catch (IllegalAccessException e) { e.printStackTrace(); }
				catch (InvocationTargetException e) {
					// we need to report this exception
					Throwable cause = e.getCause();
					byte[] encodedExc = encodeParameter(cause).getEncodedParameter();
					synchronized (RB) {
						RB.getTriCommunicationTE().triEnqueueException(
							tsiPortId, sutAddress, componentId,
							signatureId, new TriExceptionImpl(encodedExc));
					}
				}
			}
		}).start();
		return new TriStatusImpl();
	} catch (WrongParameterException e) { throw new TestCaseError(e); }
	catch (/* ClassNotFoundException, NoSuchMethodException etc. */) { return new TriStatusImpl(e); }
}

@Override
public TriStatus triCall(final TriComponentId componentId,
		final TriPortId tsiPortId, final TriAddress sutAddress,
		final TriSignatureId signatureId,
		final TriParameterList parameterList) {
	final String sigName = afterLastDot(signatureId.getSignatureName());
	String moduleName = ((TriSignatureIdImpl) signatureId).getModuleName();
	String fullName = (moduleName != null ? moduleName + "." + sigName : sigName);
	Type type = RB.getTciCDRequired().getTypeForName(fullName);
	String classNameVariant = type.getTypeEncodingVariant();

	if (classNameVariant.equals(JAVAAUX_MODULE_VARIANT)) {
		if (sigName.equals("makeString")) {
			try {
				String className = String.class.getName();
				Constructor<?> cons = String.class
						.getConstructor(String.class);
				String constructorSignature = ClassMapper
						.mangleConstructorName(cons);

				return launchConstructorThread(className,
						constructorSignature, tsiPortId, sutAddress,
						componentId, signatureId, parameterList);
			} catch (NoSuchMethodException e) { return new TriStatusImpl(e); }
		} else if (sigName.equals("eq")) {
			Object obj1 = decodeParameter(parameterList.get(0), Object.class);
			Object obj2 = decodeParameter(parameterList.get(1), Object.class);
			boolean result = (obj1 == obj2);

			TriParameter returnParam = encodeParameter(result);
			synchronized (RB) {
				RB.getTriCommunicationTE().triEnqueueReply(tsiPortId,
						sutAddress, componentId, signatureId,
						parameterList, returnParam);
			}
			return new TriStatusImpl();
		} else if (sigName.equals("deleteObject")) {
			IStorageCodec codec = getJavaCodec();
			Object objToDelete = decodeParameter(parameterList.get(0), Object.class);

			// The argument passed to `deleteObject' is a valid object handle.
			// Otherwise the JavaCodec throws an error when requested to encode
			// and the call never gets here (in the port plugin).
			String key = codec.putRegistryValue(objToDelete);
			codec.deleteRegistryEntry(key);

			// dummy return value
			Object retVal = new Object();
			TriParameter returnParam = encodeParameter(retVal);
			synchronized (RB) {
				RB.getTriCommunicationTE().triEnqueueReply(tsiPortId,
						sutAddress, componentId, signatureId,
						parameterList, returnParam);
			}
			return new TriStatusImpl();
		}
		// the other meta-signatures are also implemented here ..
	} else if (ClassMapper.isConstructor(sigName)) {
		String className = classNameVariant;
		return launchConstructorThread(className, sigName, tsiPortId,
				sutAddress, componentId, signatureId, parameterList);
	} else if (ClassMapper.isGetter(sigName) || ClassMapper.isSetter(sigName)) {
		String className = classNameVariant;
		return launchAccessorCallThread(className, sigName, tsiPortId,
				sutAddress, componentId, signatureId, parameterList);
	} else {
		// Method call
		String className = classNameVariant;
		return launchMethodCallThread(className, sigName, tsiPortId,
				sutAddress, componentId, signatureId, parameterList);
	}
}
