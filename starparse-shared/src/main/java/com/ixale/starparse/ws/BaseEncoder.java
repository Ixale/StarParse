package com.ixale.starparse.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.esotericsoftware.kryo.io.Output;
import com.ixale.starparse.serialization.KryoSerialization;

public class BaseEncoder implements Encoder.Binary<BaseMessage> {

	@Override
	public ByteBuffer encode(BaseMessage message) throws EncodeException {

		ByteArrayOutputStream bos = null;
		Output output = null;
		try {
			bos = new ByteArrayOutputStream();
			output = new Output(bos);

			KryoSerialization.getKryo().writeClassAndObject(output, message);
			output.close();

			return ByteBuffer.wrap(bos.toByteArray());

		} catch (Exception e) {
			throw new EncodeException(message, e.getMessage(), e);

		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {}
			}
		}

	}

	@Override
	public void init(EndpointConfig ec) {
		//
	}

	@Override
	public void destroy() {
		// 
	}
}