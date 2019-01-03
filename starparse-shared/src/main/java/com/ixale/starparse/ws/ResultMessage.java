package com.ixale.starparse.ws;

public class ResultMessage extends BaseMessage {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ERROR, SUCCESS
	}

	private Type type;
	private String message;

	public ResultMessage() {
		
	}

	public ResultMessage(String message, final Exception e) {
		this.type = Type.ERROR;
		this.message = message+(e != null ? " ("+e.getMessage()+")" : "");
	}

	public ResultMessage(String message, final Type type) {
		this.type = type;
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return type+": "+message;
	}
}
