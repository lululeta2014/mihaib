@Override
protected Value internalDecode(Object nativeValue, Type type) {
	switch (type.getTypeClass()) {
	case TciTypeClass.RECORD:
		try {
			String recordClassName = type.getTypeEncodingVariant();
			RecordValue rV = (RecordValue) type.newInstance();

			Value handleVal = rV.getField("handle");
			CharstringValue csV1 = (CharstringValue) handleVal.getType().newInstance();
			// find out the handle for the object
			String objHandle = reversedRegistry.get(new ObjectHolder(nativeValue));
			csV1.setString(objHandle);
			rV.setField("handle", csV1);

			Value typeNameVal = rV.getField("typeName");
			CharstringValue csV2 = (CharstringValue) typeNameVal.getType().newInstance();
			// return the handle for the object
			csV2.setString(recordClassName);
			rV.setField("typeName", csV2);

			// check that nativeValue is of the correct type
			if (nativeValue != null) {
				Class<?> cls = ClassMapper.loadClass(recordClassName,
						nativeValue.getClass().getClassLoader());
				if (!cls.isInstance(nativeValue))
					return null;
			}

			return rV;
		} catch (ClassNotFoundException e) {
			return null;
		}
	// other `case' labels
	}
}

@Override
protected Object internalEncode(Value ttcn3Value) {
	switch (ttcn3Value.getType().getTypeClass()) {
	case TciTypeClass.RECORD: {
		String str = ((CharstringValue) ((RecordValue) ttcn3Value)
				.getField("handle")).getString();
		Object obj = super.retrieveRegistryValue(str);
		if (obj != null)
			return ((ObjectHolder) obj).get();

		throw new TestCaseError("Invalid handle supplied: `" + str
				+ "'. No object with this handle in storage codec.");
	}
	// other `case' labels
	}
}
