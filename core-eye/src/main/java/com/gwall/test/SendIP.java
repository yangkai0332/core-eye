package com.gwall.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendIP {

	public static void main(String args[]) {
		new SendIP().lanchApp();
	}

	private void lanchApp() {
		SendThread th = new SendThread();
		th.start();
	}

	private class SendThread extends Thread {
		@Override
		public void run() {
			int n = 0;
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					BroadcastIP(n++);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void BroadcastIP(int n) throws Exception {
			DatagramSocket dgSocket = new DatagramSocket();
			byte b[] = ("你好，这是我发给你的消息:"+n).getBytes();
			DatagramPacket dgPacket = new DatagramPacket(b, b.length, InetAddress.getByName("255.255.255.255"), 8989);
			dgSocket.send(dgPacket);
			dgSocket.close();
			System.out.println("send message is ok.");
		}
	}

}