package com.gwall.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class NetClient {

	String ip;
	private int udp_port;
	private DatagramSocket ds = null;

	public void connect(String ip, int port) {

		this.ip = ip;
		this.udp_port = port;
		try {
			ds = new DatagramSocket(udp_port);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		new Thread(new UDPThread()).start();
	}

	private class UDPThread implements Runnable {

		byte[] buf = new byte[1024];

		@Override
		public void run() {

			while (ds != null) {
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				try {
					ds.receive(dp);
					parse(dp);
					System.out.println("receive a package from server");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void parse(DatagramPacket dp) {
			ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, dp.getLength());
			DataInputStream dis = new DataInputStream(bais);
			String msg = null;
			try {
				msg = dis.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/** 
     * send data package to server 
     * @param ds 
     * @param ip 
     * @param udpPort 
     */  
    public void sendMsg(String str) {  
        if(ds==null)  
            return;  
        ByteArrayOutputStream baos=new ByteArrayOutputStream();  
        DataOutputStream dos=new DataOutputStream(baos);  
          
        try {  
            dos.write(str.getBytes("UTF-8"));  
            dos.flush();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        byte[] buf=baos.toByteArray();  
        try {  
            DatagramPacket dp=new DatagramPacket(buf, buf.length, new InetSocketAddress(ip, udp_port));  
            ds.send(dp);  
        } catch (Exception e) {  
        	e.printStackTrace();
        }
    }
}