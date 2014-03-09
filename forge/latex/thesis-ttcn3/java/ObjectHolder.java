private class ObjectHolder {
	private Object ref;

	ObjectHolder(Object obj) { ref = obj; }

	Object get() { return ref; }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectHolder)
			return ref == ((ObjectHolder) obj).ref;
		else
			return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if (ref == null)
			return 0;
		return ref.hashCode();
	}
}

