package com.jinoh.ruby.marshal;

import java.io.IOException;

public interface CustomMarshallable extends Marshallable {
	public void load (Unmarshaler inst, byte[] data) throws IOException;
	public byte[] dump (Marshaler inst) throws IOException;
}
