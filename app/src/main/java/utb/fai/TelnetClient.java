package utb.fai;

import java.io.*;
import java.net.*;

public class TelnetClient {

    private String serverIp;
    private int port;
    private volatile boolean running = true;

    public TelnetClient(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public void run() {
        try (Socket socket = new Socket(serverIp, port)) {

            Thread readerThread = new Thread(() -> readFromServer(socket));
            Thread writerThread = new Thread(() -> writeToServer(socket));

            readerThread.start();
            writerThread.start();
            
            readerThread.join();
            writerThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void readFromServer(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            while (running && !socket.isClosed()) {

                if (in.available() > 0) {
                    int data = in.read();
                    if (data == -1) {
                        break;
                    }
                    System.out.print((char) data);
                } else {
                    Thread.sleep(20);
                }
            }
        } catch (IOException | InterruptedException e) {

        }
    }

    private void writeToServer(Socket socket) {
        try {
            OutputStream out = socket.getOutputStream();
            InputStream sysIn = System.in;
            ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

            while (running && !socket.isClosed()) {
                if (sysIn.available() > 0) {
                    int data = sysIn.read();
                    if (data == -1) {
                        break;
                    }


                    if (data == '\n' || data == '\r') {
                        lineBuffer.write(data);
                        byte[] lineBytes = lineBuffer.toByteArray();
                        String lineStr = new String(lineBytes).trim();

                        out.write(lineBytes);
                        out.flush();


                        if ("/QUIT".equalsIgnoreCase(lineStr)) {
                            running = false;
                            socket.close();
                            break;
                        }
                        lineBuffer.reset();
                    } else {
                        lineBuffer.write(data);
                    }
                } else {
                    Thread.sleep(20);
                }
            }
        } catch (IOException | InterruptedException e) {

        }
    }
}
