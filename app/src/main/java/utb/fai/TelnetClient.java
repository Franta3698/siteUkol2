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
            // Vytvoření a spuštění nezávislých vláken pro čtení a zápis
            Thread readerThread = new Thread(() -> readFromServer(socket));
            Thread writerThread = new Thread(() -> writeToServer(socket));

            readerThread.start();
            writerThread.start();

            // Čekání na ukončení vláken
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
                // Zamezení blokování pomocí available() a snížení zátěže CPU pomocí sleep(20)
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
            // Spojení bylo ukončeno
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

                    // Odeslání proběhne po stisknutí ENTER (\n nebo \r)
                    if (data == '\n' || data == '\r') {
                        lineBuffer.write(data);
                        byte[] lineBytes = lineBuffer.toByteArray();
                        String lineStr = new String(lineBytes).trim();

                        out.write(lineBytes);
                        out.flush();

                        // Pokud uživatel napíše "/QUIT", ukončíme spojení a aplikaci
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
            // Spojení bylo ukončeno
        }
    }
}

/*package utb.fai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TelnetClient {
    private String hostname;
    private int port;

    public TelnetClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void run(){
        try (Socket socket = new Socket(hostname, port)){
            Thread readerThread = new Thread(() -> readFromServer(socket));
            Thread writerThread = new Thread(() -> writeToServer(socket));

            readerThread.start();
            writerThread.start();

            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Writer thread finished. Closing socket.");

            
        }

        catch (Exception e){
            e.printStackTrace();
        }
             
    }

    private void readFromServer(Socket socket){
        

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeToServer(Socket socket){
        

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}*/
