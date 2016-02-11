/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.o_gui.logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import x.o_gui.XO_GUI;

/**
 *
 * @author ahmadbarakat
 */
public class UDPServer extends Networks {

    public static DatagramSocket serverSocket;

    public static void initialize() {
        UDPServer.working = true;
        try {
            serverSocket = new DatagramSocket(portNumber);
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeAll() throws InterruptedException {
        UDPServer.working = false;
        closeThis();
    }

    protected static void closeThis() {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private InetAddress IPAddressInetAddress;
    private int port;

    public UDPServer() throws UnknownHostException {
        super();
        IPAddressInetAddress = InetAddress.getByName(Networks.IPAddress);
    }

    public void send(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
        respond();
    }

    @Override
    public void run() {
        while (UDPServer.working) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            final int result1, result2;
            result1 = fromByteArray(receivePacket.getData(), 1);
            result2 = fromByteArray(receivePacket.getData(), 2);

            Platform.runLater(() -> {
                try {
                    XO_GUI.sent(result1, result2);
                } catch (SocketException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            IPAddressInetAddress = receivePacket.getAddress();
            port = receivePacket.getPort();            

            if (result1 == 7 && result2 == 7) {
                send(7, 7);
            }
        }
    }

    private void respond() {
//        System.out.println("Send " + num1 + ", " + num2);
        sendData = appendToByteArray(sendData, num1, 1);
        sendData = appendToByteArray(sendData, num2, 2);
        DatagramPacket sendPacket;
        sendPacket = new DatagramPacket(sendData,
                sendData.length, IPAddressInetAddress, port);
        try {
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        num1 = num2 = -1;
    }

}
