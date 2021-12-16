package com.company;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.Socket;

public class Main {
    static JFrame frame;
    static JTextField textFieldAddress;
    static JTextField textFieldFilename;
    static JButton buttonConnect;
    static JButton buttonDownload;

    static Socket socket = null;

    public static void connect() {
        try {
            String[] uri = textFieldAddress.getText().split(":");
            socket = new Socket(uri[0], Integer.parseInt(uri[1]));
            buttonConnect.setText("disconnect");
            textFieldAddress.setEditable(false);
            buttonDownload.setVisible(true);
        } catch (Exception e) {
            System.out.println("ошибка: " + e);
        }
    }

    public static void disconnect() {
        try {
            socket.close();
            buttonConnect.setText("connect");
            textFieldAddress.setEditable(true);
            buttonDownload.setVisible(false);
        } catch (Exception e) {
            System.out.println("ошибка: " + e);
        }
    }

    public static void downloadFile() {
        try {
            //send filename for loading
            BufferedReader inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //read message about loading-status
            BufferedWriter outputMessage = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //send
            outputMessage.write(textFieldFilename.getText() + "\n");
            outputMessage.flush();

            //read
            String returnMessage = inputMessage.readLine();
            System.out.println(returnMessage);

            String filename = "Downloads/" + textFieldFilename.getText();
            if (!returnMessage.equals("NOT_EXIST")) {
                //block for if directory is not exist
                String[] paths = filename.split("/");
                String path = "";
                for(int i = 0; i < paths.length-1; i++){
                    path += paths[i] + "/";
                }
                File file = new File(path);
                if(!file.exists()){
                    file.mkdirs();
                }

                //loading file
                FileOutputStream fileStream = new FileOutputStream("Downloads/"+textFieldFilename.getText());
                DataInputStream inputFile = new DataInputStream(socket.getInputStream());
                long length = inputFile.readLong();
                byte[] bytes = new byte[(int) length];
                int count, total = 0;
                while ((count = inputFile.read(bytes)) > -1) {
                    total += count;
                    fileStream.write(bytes, 0, count);
                    if (total == length) break;
                }
                fileStream.close();
            }
            connect();
        } catch (Exception e) {
            System.out.println("ошибка: " + e);
        }
    }

    public static void runAndListen() {
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonConnect.getText().equals("connect")) {
                    connect();
                } else {
                    disconnect();
                }
            }
        });
        buttonDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    downloadFile();

            }
        });
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (buttonConnect.getText().equals("disconnect")) {
                    disconnect();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {            }

            @Override
            public void windowIconified(WindowEvent e) {            }

            @Override
            public void windowDeiconified(WindowEvent e) {            }

            @Override
            public void windowActivated(WindowEvent e) {            }

            @Override
            public void windowDeactivated(WindowEvent e) {            }
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("Client");
        frame.setSize(200, 200);
        frame.setResizable(false);

        textFieldAddress = new JTextField("127.0.0.1:0");
        textFieldAddress.setBounds(20, 20, 150, 20);
        textFieldAddress.setVisible(true);

        buttonConnect = new JButton("connect");
        buttonConnect.setBounds(textFieldAddress.getX(), textFieldAddress.getY() + textFieldAddress.getHeight() + 10, 150, 30);
        buttonConnect.setVisible(true);

        textFieldFilename = new JTextField("file");
        textFieldFilename.setBounds(textFieldAddress.getX(), buttonConnect.getY() + buttonConnect.getHeight() + 10, 150, 20);
        textFieldFilename.setVisible(true);

        buttonDownload = new JButton("download file");
        buttonDownload.setBounds(textFieldAddress.getX(), textFieldFilename.getY() + textFieldFilename.getHeight() + 10, 150, 30);
        buttonDownload.setVisible(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.add(textFieldAddress);
        panel.add(buttonConnect);
        panel.add(textFieldFilename);
        panel.add(buttonDownload);

        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                runAndListen();
            }
        });
    }
}