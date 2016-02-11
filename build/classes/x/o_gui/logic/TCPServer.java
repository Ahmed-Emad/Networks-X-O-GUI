/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.o_gui.logic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class TCPServer extends Networks {

    public static ServerSocket WelcomeSocket;

    public static void initialize() {
        TCPServer.working = true;
        try {
            WelcomeSocket = new ServerSocket(Networks.portNumber);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeAll() throws InterruptedException {
        TCPServer.working = false;
        closeThis();
    }

    protected static void closeThis() throws InterruptedException {
        if (WelcomeSocket != null) {
            try {
                WelcomeSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Socket connectionSocket;

    private DataInputStream dIn;
    private DataOutputStream dOut;

    public TCPServer() {
        super();
    }
    
    public void send(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
        respond();
    }    

    @Override
    public void run() {        
        while (TCPServer.working) {            
            try {
                connectionSocket = WelcomeSocket.accept();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            try {
                dIn = new DataInputStream(connectionSocket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                dOut = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                receiveData = readMessage(dIn);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            final int result1 = fromByteArray(receiveData, 1);
            final int  result2 = fromByteArray(receiveData, 2);                        
            Platform.runLater(() -> {
                try {
                    XO_GUI.sent(result1, result2);
                } catch (SocketException ex) {
                    ex.printStackTrace();
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            });            
            if (result1 == 7 && result2 == 7) {
                send(7, 7);
            }            
        }
    }
    
    private void respond() {        
        sendData = appendToByteArray(sendData, num1, 1);
        sendData = appendToByteArray(sendData, num2, 2);
        try {
            writeMessage(dOut, sendData, 8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        num1 = num2 = -1;        
    }

}
