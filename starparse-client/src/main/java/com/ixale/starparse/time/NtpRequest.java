package com.ixale.starparse.time;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NtpRequest {

	private static double localClockOffset;

	public static void requestTime(String serverName, int timeout) throws IOException {

		// Send request
		final DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(timeout);

		final InetAddress address = InetAddress.getByName(serverName);
		final byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

		// Set the transmit timestamp *just* before sending the packet
		NtpMessage.encodeTimestamp(packet.getData(), 40,
				(System.currentTimeMillis() / 1000.0) + 2208988800.0);

		socket.send(packet);

		// Get response
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);

		// Immediately record the incoming timestamp
		final double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

		// Process response
		final NtpMessage msg = new NtpMessage(packet.getData());

		// Corrected, according to RFC2030 errata
		/*final double roundTripDelay = (destinationTimestamp - msg.originateTimestamp)
				- (msg.transmitTimestamp - msg.receiveTimestamp);*/

		localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;

		//System.out.println("Dest. timestamp: "+ NtpMessage.timestampToString(destinationTimestamp));
		//System.out.println("Round-trip delay: "+ new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");
		//System.out.println("Local clock offset: "+ new DecimalFormat("0.00").format(localClockOffset * 1000) + " ms");

		socket.close();
	}

	public static double getLocalClockOffset() {
		return localClockOffset;
	}
}