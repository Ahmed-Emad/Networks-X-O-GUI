/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.o_gui.logic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import x.o_gui.XO_GUI;

/**
 *
 * @author ahmadbarakat
 */
public class TCPClient extends Networks {

    private Socket ClientSocket;

    private DataInputStream dIn;
    private DataOutputStream dOut;

    public static void closeAll() throws InterruptedException {
        TCPClient.working = false;
    }

    public TCPClient() throws SocketException,
            UnknownHostException {
        super();        
    }

    public void send(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;        
    }
    
    @Override
    public void run() {
        while (TCPClient.working) {
//            System.out.println("In While");
            if (num1 != -1 && num2 != -1) {                
//                System.out.println("IN If");
                try {
                    ClientSocket = new Socket(Networks.IPAddress,
                            Networks.portNumber);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }                
                sendData = appendToByteArray(sendData, num1, 1);
                sendData = appendToByteArray(sendData, num2, 2);                

                try {
                    dOut = new DataOutputStream(ClientSocket.getOutputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    writeMessage(dOut, sendData, 8);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                try {
                    dIn = new DataInputStream(ClientSocket.getInputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    receiveData = readMessage(dIn);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                int result1, result2;
                result1 = fromByteArray(receiveData, 1);
                result2 = fromByteArray(receiveData, 2);

                Platform.runLater(() -> {
                    try {
                        XO_GUI.sent(result1, result2);
                    } catch (SocketException ex) {
                        Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                num1 = num2 = -1;
            }
            else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            ClientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
    }

}
