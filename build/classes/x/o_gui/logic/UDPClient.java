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
public class UDPClient extends Networks {

    public static void closeAll() throws InterruptedException {
        UDPClient.working = false;
    }

    private final DatagramSocket clientSocket;
    private final InetAddress IPAddressInetAddress;

    public UDPClient() throws SocketException, UnknownHostException {
        super();
        clientSocket = new DatagramSocket();
        IPAddressInetAddress = InetAddress.getByName(Networks.IPAddress);
    }

    public void send(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
    }

    @Override
    public void run() {
        while (UDPClient.working) {
            if (num1 != -1 && num2 != -1) {
                sendData = appendToByteArray(sendData, num1, 1);
                sendData = appendToByteArray(sendData, num2, 2);
                DatagramPacket sendPacket;
                sendPacket = new DatagramPacket(sendData,
                        sendData.length, IPAddressInetAddress, portNumber);
                try {
                    clientSocket.send(sendPacket);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                num1 = num2 = -1;
                
                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                int result1, result2;
                result1 = fromByteArray(receivePacket.getData(), 1);
                result2 = fromByteArray(receivePacket.getData(), 2);
                Platform.runLater(() -> {
                    try {
                        XO_GUI.sent(result1, result2);
                    } catch (SocketException ex) {
                        Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });                
            } 
            else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        clientSocket.close();
    }


}
