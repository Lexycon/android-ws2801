package de.lexycon.ledcontrol;


import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

class UDPClientThread extends Thread {
    private String dstAddress;
    private int dstPort;

    private byte[] byteBuffer;
    private MainActivity.UdpClientHandler handler;

    private DatagramSocket socket;
    private InetAddress address;

    public UDPClientThread(String addr, int port, MainActivity.UdpClientHandler handler) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
    }


    private void sendValues(String values){
        handler.sendMessage(Message.obtain(handler, MainActivity.UdpClientHandler.UPDATE_VALUES, values));
    }

    public void setBytes(String str) {
        this.byteBuffer = str.getBytes();
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            address = InetAddress.getByName(dstAddress);

            // send request

            DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length, address, dstPort);
            socket.send(packet);

//            // get response
            if (byteBuffer[0] == 'A') {
                byte[] retBuf = new byte[255];
                packet = new DatagramPacket(retBuf, retBuf.length);

                socket.receive(packet);
                String ret = new String(packet.getData(), 0, packet.getLength());

                handler.sendMessage(Message.obtain(handler, MainActivity.UdpClientHandler.UPDATE_VALUES, ret));
            }
        } catch (SocketTimeoutException e) {
            socket.close();
            if (byteBuffer[0] == 'A') handler.sendMessage(Message.obtain(handler, MainActivity.UdpClientHandler.UPDATE_VALUES, null));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
            }
        }

    }
}