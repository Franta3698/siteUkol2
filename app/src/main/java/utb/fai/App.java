package utb.fai;

public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Použití: java -jar app.jar <serverIp> <port>");
            return;
        }

        String serverIp = args[0];
        int port;

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Chyba: Port musí být celé číslo.");
            return;
        }

        TelnetClient telnetClient = new TelnetClient(serverIp, port);
        telnetClient.run();
    }
}