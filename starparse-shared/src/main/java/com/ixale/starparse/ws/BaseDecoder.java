package com.ixale.starparse.ws;

import java.nio.ByteBuffer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.ixale.starparse.serialization.KryoSerialization;

public class BaseDecoder implements Decoder.Binary<BaseMessage> {

	@Override
	public BaseMessage decode(final ByteBuffer bytes) throws DecodeException {

		ByteBufferInput bis = null;
		Object obj = null;
		try {
			bis = new ByteBufferInput(bytes);
			obj = KryoSerialization.getKryo().readClassAndObject(bis);

		} catch (Exception e) {
			throw new DecodeException(bytes, e.getMessage(), e);

		} finally {
			if (bis != null) {
				bis.close();
			}
		}

		if (!(obj instanceof BaseMessage)) {
			throw new DecodeException(bytes, "Invalid object - BaseMessage expected");
		}

		return (BaseMessage) obj;
	}

	@Override
	public boolean willDecode(ByteBuffer bytes) {
		return true;
	}

	@Override
	public void init(EndpointConfig config) {
		//
	}

	@Override
	public void destroy() {
		//
	}

}