interface org.etsi.ttcn.tci.TciCDProvided {
	public abstract org.etsi.ttcn.tci.Value decode(
			org.etsi.ttcn.tri.TriMessage message,
			org.etsi.ttcn.tci.Type decodingHypothesis);

	public abstract org.etsi.ttcn.tri.TriMessage encode(
			org.etsi.ttcn.tci.Value template);
}
