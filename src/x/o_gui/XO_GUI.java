/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.o_gui;

import com.guigarage.flatterfx.FlatterFX;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import x.o_gui.logic.Networks;
import x.o_gui.logic.TCPClient;
import x.o_gui.logic.TCPServer;
import x.o_gui.logic.UDPClient;
import x.o_gui.logic.UDPServer;

/**
 *
 * @author ahmadbarakat
 */
public class XO_GUI extends Application {

    static GridPane grid;

    static int currentTurn = 0, myTurn = -1;
    static final int player1 = 0, player2 = 1;
        
    static enum State{Blank, X, O};
    static int n = 3;
    static State[] board = new State[n * n];
    static State myState = State.X;
    
    static Button[] btns;    
    static NumericTextField portTextField;
    static TextField stateTextField;
    static TextField ipAddressTextField;
    static Label turnLabel;
    static RadioButton tcpRadioButton;
    static RadioButton udpRadioButton;
    static Button connectButton;
    static Button waitConnectionButton;
    static Button cancelButton;

    static final String welcome = "Welcome";
    static final String state = "State";
    static final String won = "You Won :D";
    static final String lose = "You Lose !!";
    static final String draw = "Draw Game !! :D";
    static final String wait = "Waiting";
    static final String connect = "Connecting";
    static final String connected = "Connected";
    static final String myTurnString = "Your Turn";
    static final String anotherPlayerString = "Waiting for the other Player";

    static TCPServer tcpServer;
    static UDPServer udpServer;
    static TCPClient tcpClient;
    static UDPClient udpClient;

    static int mode = 0;

    public static void main(String[] args) {
        launch(args);
    }
    
    private static boolean checkWon(int x, int y, State s) {        
    	//check col
    	for(int i = 0; i < n; i++){
            if(board[x * n + i] != s)
                break;
            if(i == n - 1){
                return true;
            }
    	}
    	//check row
    	for(int i = 0; i < n; i++){
            if(board[i * n + y] != s)
                break;
            if(i == n - 1){
                return true;
            }
    	}
    	//check diag
    	if(x == y){
            //we're on a diagonal
            for(int i = 0; i < n; i++){
                if(board[i * n + i] != s) 
                    break;
                if(i == n - 1){
                    return true;
                }
            }
    	}
        //check anti diag
    	for(int i = 0;i < n;i++){
            if(board[i * n + ((n - 1) - i)] != s)
                break;
            if(i == n - 1){
                return true;
            }
    	}
        return false;
    }

    static void cellPressed(int row, int column, boolean sudo) throws SocketException, UnknownHostException, InterruptedException {
        grid.requestFocus();
        Button btn = btns[row * 3 + column];
        if (myTurn != -1 && (currentTurn % 2 == myTurn || sudo)) {
            btn.setDisable(true);
            if (currentTurn % 2 == myTurn) {
                btn.setText("X");
                board[row * 3 + column] = myState;
                if (tcpClient != null && !sudo) {
//                    tcpClient = new TCPClient();
                    tcpClient.send(row, column);
//                    TimeUnit.MILLISECONDS.sleep(50);
//                    tcpClient.start();
                } else if (udpClient != null && !sudo) {
                    udpClient.send(row, column);
                } else if (tcpServer != null && !sudo) {
                    tcpServer.send(row, column);
                } else if (udpServer != null && !sudo) {
                    udpServer.send(row, column);
                }                
                if (checkWon(row, column, myState)) {
                    System.out.println("You Won");
                    turnLabel.setText(won);
                    endGame(false);
                }
                ++currentTurn;
            } else {
                btn.setText("O");
                board[row * 3 + column] = State.O;
                if (checkWon(row, column, State.O)) {
                    System.out.println("You Lose");
                    turnLabel.setText(lose);    
                    endGame(false);
                }                
                ++currentTurn;
            }
            updateButtons();
            if(currentTurn > (n * n - 1)) {
                System.out.println("Draw Game");
                turnLabel.setText(draw);                
                endGame(false);
            }
            if (myTurn > -1) {
                turnLabel.setText(currentTurn % 2 == myTurn ? myTurnString
                        : anotherPlayerString);
            }
        }
    }

    static void connect()
            throws SocketException, UnknownHostException {
        System.out.println("Client");
        grid.requestFocus();
        stateTextField.setText(connect);
        waitConnectionButton.setDisable(true);
        connectButton.setDisable(true);
        ipAddressTextField.setDisable(true);
        portTextField.setDisable(true);
        tcpRadioButton.setDisable(true);
        udpRadioButton.setDisable(true);
        int portNumber = portTextField.getValue();
        String ipAddress = ipAddressTextField.getText();
        Networks.portNumber = portNumber;
        Networks.IPAddress = ipAddress;
        if (tcpRadioButton.isSelected()) {
            TCPClient.working = true;
            tcpClient = new TCPClient();
            tcpClient.start();
            tcpClient.send(7, 7);
        }
        if (udpRadioButton.isSelected()) {
            UDPClient.working = true;
            udpClient = new UDPClient();
            udpClient.start();
            udpClient.send(7, 7);
        }
        mode = 1;
    }

    static void waitConnection() throws UnknownHostException {
        System.out.println("Server");
        grid.requestFocus();
        stateTextField.setText(wait);
        connectButton.setDisable(true);
        waitConnectionButton.setDisable(true);
        ipAddressTextField.setDisable(true);
        portTextField.setDisable(true);
        tcpRadioButton.setDisable(true);
        udpRadioButton.setDisable(true);
        int portNumber = portTextField.getValue();
        Networks.portNumber = portNumber;
        if (tcpRadioButton.isSelected()) {
            TCPServer.initialize();
            tcpServer = new TCPServer();
            tcpServer.start();
        }
        if (udpRadioButton.isSelected()) {
            UDPServer.initialize();
            udpServer = new UDPServer();
            udpServer.start();
        }
        mode = 0;
    }

    static void cancel() throws InterruptedException {
        grid.requestFocus();
        stateTextField.setText(state);
        cancelButton.setText("Cancel");
        connectButton.setDisable(false);
        waitConnectionButton.setDisable(false);
        ipAddressTextField.setDisable(false);
        portTextField.setDisable(false);
        tcpRadioButton.setDisable(false);
        udpRadioButton.setDisable(false);
        endGame(true);
        closeAll();
    }

    static void endGame(boolean full) throws InterruptedException {        
        myTurn = -1;
        currentTurn = -1;
        if (full) {
            turnLabel.setText(welcome);
            for (int i = 0; i < 9; ++i) {
                board[i] = State.Blank;
                btns[i].setText("");
                btns[i].setDisable(true);
            }            
        }
        else {            
            for (int i = 0; i < 9; ++i) {
                board[i] = State.Blank;             
                btns[i].setDisable(true);
            }            
        }
    }

    static void startGame(int myTurn2) {
        currentTurn = 0;
        myTurn = myTurn2;
        for (int i = 0; i < 9; ++i) {
            board[i] = State.Blank;
            btns[i].setText("");
            btns[i].setDisable(false);
        }
        updateButtons();
    }

    static void updateButtons() {
        for (int i = 0; i < 9; ++i) {
            if (board[i] == State.Blank && currentTurn % 2 != myTurn) {
                btns[i].setDisable(true);
            } else if (board[i] == State.Blank && currentTurn % 2 == myTurn) {
                btns[i].setDisable(false);
            }
        }
    }

    static void closeAll() throws InterruptedException {
        TCPServer.closeAll();
        UDPServer.closeAll();
        TCPClient.closeAll();
        UDPClient.closeAll();
        tcpServer = null;
        udpServer = null;
        tcpClient = null;
        udpClient = null;
    }

    public static void sent(int num1, int num2) throws SocketException, UnknownHostException, InterruptedException {
//        System.out.println("\n:D :: " + num1 + ", " + num2);
        if (num1 == 7 && num2 == 7) {
            stateTextField.setText(connected);
            if (mode == 1) {
                startGame(player1);
                turnLabel.setText(myTurnString);
                cancelButton.setText("End");
            } else if (mode == 0) {
                startGame(player2);
                turnLabel.setText(anotherPlayerString);
                cancelButton.setText("End");
            }
        } else {
            cellPressed(num1, num2, true);
        }
    }

    @Override
    public void start(Stage primaryStage) {

        FlatterFX.style();

        final String buttonFont = "Action_Man_Bold.ttf";
        final int buttonFontSize = 70;
        final String labelsFont = "GoodDog.otf";
        final int labelsFontSize = 60;
        final String textFieldsFont = "KomikaTitle-Paint.ttf";
        final int textFieldsFontSize = 35;
        final String radioButtonsFont = "Action_Man_Bold.ttf";
        final int radioButtonsFontSize = 25;

        final int cellWidth = 180, cellHeight = 120;

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(1, 1, 1, 1));
        grid.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            grid.requestFocus();
        });

        turnLabel = new Label(welcome);
        GridPane.setHalignment(turnLabel, HPos.CENTER);
        GridPane.setValignment(turnLabel, VPos.CENTER);
        turnLabel.setAlignment(Pos.CENTER);
        turnLabel.setFont(Font.loadFont(
                "file:resources/fonts/" + buttonFont, buttonFontSize - 35));
        turnLabel.setPadding(new Insets(0, 0, 20, 0));
        turnLabel.setPrefWidth(3 * cellWidth);
        grid.add(turnLabel, 0, 0, 3, 1);

        btns = new Button[9];
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.WHITE);
        shadow.setRadius(20);
        for (int i = 0; i < 9; i++) {
            btns[i] = new Button(" ");
            GridPane.setHalignment(btns[i], HPos.CENTER);
            GridPane.setValignment(btns[i], VPos.CENTER);
            btns[i].setAlignment(Pos.CENTER);
            btns[i].setDefaultButton(true);
            btns[i].setCursor(Cursor.HAND);
            final int index = i;
            btns[index].addEventHandler(MouseEvent.MOUSE_ENTERED,
                    (MouseEvent e) -> {
                        btns[index].setEffect(shadow);
                    });
            btns[i].addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> {
                btns[index].setEffect(null);
            });
            btns[i].setFont(Font.loadFont(
                    "file:resources/fonts/" + buttonFont, buttonFontSize));
            btns[i].setOnAction(event -> {
                try {
                    cellPressed(index / 3, index % 3, false);
                } catch (SocketException ex) {
                    Logger.getLogger(XO_GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(XO_GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(XO_GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            btns[i].setPrefSize(cellWidth, cellHeight);
            btns[i].setDisable(true);
            grid.add(btns[i], i / 3, (i % 3) + 1);
        }

        grid.add(new Label(" "), 0, 4, 3, 1);

        Label portLabel = new Label("Port: ");
        GridPane.setHalignment(portLabel, HPos.CENTER);
        GridPane.setValignment(portLabel, VPos.CENTER);
        portLabel.setFont(Font.loadFont(
                "file:resources/fonts/" + labelsFont, labelsFontSize - 10));

        portTextField = new NumericTextField(7070, 10, 1024, 50000);
        GridPane.setHalignment(portTextField, HPos.CENTER);
        GridPane.setValignment(portTextField, VPos.CENTER);
        portTextField.setAlignment(Pos.CENTER);
        portTextField.setFont(Font.loadFont(
                "file:resources/fonts/" + textFieldsFont,
                textFieldsFontSize - 10));
        portTextField.setPrefWidth(cellWidth);
        portTextField.setPadding(Insets.EMPTY);

        ToggleGroup group = new ToggleGroup();
        tcpRadioButton = new RadioButton("TCP");
        tcpRadioButton.setToggleGroup(group);
        tcpRadioButton.setSelected(true);
        tcpRadioButton.setFont(Font.loadFont(
                "file:resources/fonts/" + radioButtonsFont,
                radioButtonsFontSize));
        udpRadioButton = new RadioButton("UDP");
        udpRadioButton.setToggleGroup(group);
        udpRadioButton.setFont(Font.loadFont(
                "file:resources/fonts/" + radioButtonsFont,
                radioButtonsFontSize));
        HBox rbContainer = new HBox(tcpRadioButton, udpRadioButton);
        rbContainer.setSpacing(10);
        rbContainer.setAlignment(Pos.CENTER);

        HBox rowBox = new HBox(portLabel, portTextField, rbContainer);
        rowBox.setSpacing(20);
        rowBox.setAlignment(Pos.CENTER);
        rowBox.setMaxWidth(3 * cellWidth);
        grid.add(rowBox, 0, 5, 3, 1);

        waitConnectionButton = new Button("Wait for Connection on this Port");
        GridPane.setHalignment(waitConnectionButton, HPos.CENTER);
        GridPane.setValignment(waitConnectionButton, VPos.CENTER);
        waitConnectionButton.setAlignment(Pos.CENTER);
        waitConnectionButton.setCursor(Cursor.HAND);
        waitConnectionButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                (MouseEvent e) -> {
                    waitConnectionButton.setEffect(shadow);
                });
        waitConnectionButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                (MouseEvent e) -> {
                    waitConnectionButton.setEffect(null);
                });
        waitConnectionButton.setFont(Font.loadFont(
                "file:resources/fonts/" + "KOMIKAX_.ttf", 15));
        waitConnectionButton.setOnAction(event -> {
            try {
                waitConnection();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        });
        waitConnectionButton.setPrefWidth(2 * cellWidth);
        waitConnectionButton.setPadding(new Insets(5));
        grid.add(waitConnectionButton, 0, 6, 2, 1);

        cancelButton = new Button("Cancel");
        GridPane.setHalignment(cancelButton, HPos.CENTER);
        GridPane.setValignment(cancelButton, VPos.CENTER);
        cancelButton.setAlignment(Pos.CENTER);
        cancelButton.setCursor(Cursor.HAND);
        cancelButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                (MouseEvent e) -> {
                    cancelButton.setEffect(shadow);
                });
        cancelButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                (MouseEvent e) -> {
                    cancelButton.setEffect(null);
                });
        cancelButton.setFont(Font.loadFont(
                "file:resources/fonts/" + "KOMIKAX_.ttf", 20));
        cancelButton.setOnAction(event -> {
            try {
                cancel();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        cancelButton.setPrefWidth(cellWidth);
        cancelButton.setPadding(Insets.EMPTY);
        grid.add(cancelButton, 2, 6);

        ipAddressTextField = new TextField("");
        ipAddressTextField.setPromptText("Enter an IP Address");
        GridPane.setHalignment(ipAddressTextField, HPos.CENTER);
        GridPane.setValignment(ipAddressTextField, VPos.CENTER);
        ipAddressTextField.setAlignment(Pos.CENTER);
        ipAddressTextField.setFont(Font.loadFont(
                "file:resources/fonts/" + labelsFont, textFieldsFontSize + 5));
        ipAddressTextField.setPadding(new Insets(0, 0, 0, 0));
        ipAddressTextField.setPrefWidth(2 * cellWidth);
        grid.add(ipAddressTextField, 0, 7, 2, 1);

        connectButton = new Button("Connect");
        GridPane.setHalignment(connectButton, HPos.CENTER);
        GridPane.setValignment(connectButton, VPos.CENTER);
        connectButton.setAlignment(Pos.CENTER);
        connectButton.setDefaultButton(true);
        connectButton.setCursor(Cursor.HAND);
        connectButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                (MouseEvent e) -> {
                    connectButton.setEffect(shadow);
                });
        connectButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                (MouseEvent e) -> {
                    connectButton.setEffect(null);
                });
        connectButton.setFont(Font.loadFont(
                "file:resources/fonts/" + "KOMIKAX_.ttf", 25));
        connectButton.setOnAction(event -> {
            try {
                connect();
            } catch (SocketException ex) {
                Logger.getLogger(XO_GUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(XO_GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        connectButton.setPrefWidth(cellWidth);
        connectButton.setPadding(Insets.EMPTY);
        grid.add(connectButton, 2, 7);

        grid.add(new Label(" "), 0, 8, 3, 1);

        Label myIpAddress = new Label("MY IP Address:  "
                + Networks.getCurrentEnvironmentNetworkIp());
        GridPane.setHalignment(myIpAddress, HPos.CENTER);
        GridPane.setValignment(myIpAddress, VPos.CENTER);
        myIpAddress.setAlignment(Pos.CENTER);
        myIpAddress.setFont(Font.loadFont(
                "file:resources/fonts/" + labelsFont, textFieldsFontSize));
        myIpAddress.setPadding(new Insets(0));
        myIpAddress.setPrefWidth(2 * cellWidth);
        grid.add(myIpAddress, 0, 9, 2, 1);

        stateTextField = new TextField(state);
        GridPane.setHalignment(stateTextField, HPos.CENTER);
        GridPane.setValignment(stateTextField, VPos.CENTER);
        stateTextField.setAlignment(Pos.CENTER);
        stateTextField.setFont(Font.loadFont(
                "file:resources/fonts/" + buttonFont, buttonFontSize - 37));
        stateTextField.setPrefWidth(cellWidth);
        stateTextField.setPadding(new Insets(0, 0, 0, 0));
        stateTextField.setDisable(true);
        grid.add(stateTextField, 2, 9);

        Scene scene = new Scene(grid, 1200, 1000);
        primaryStage.setScene(scene);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setMaximized(true);
        primaryStage.setTitle("X - O");
        primaryStage.show();

        grid.requestFocus();

        initialize();

    }
// **/*.java,**/*.form

    void initialize() {
        for (int i = 0; i < 9; ++i) {
            board[i] = State.Blank;
        }
    }

}
