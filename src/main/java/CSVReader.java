import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.net.Socket;


public class CSVReader {
    public static void main(String[] args) {
        String pathToCsv = "service-names-port-numbers.csv"; // replace with your CSV file path
        String line;
        Map<Integer, String> portDescriptions = new TreeMap<>(); //Sorted

        try (BufferedReader br = new BufferedReader(new FileReader(pathToCsv))) {

            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] columns = line.split(",");
                // Print the content on the console
                // for (String column : columns) {
                try {
                    if (Character.isDigit(columns[1].charAt(0))){
                        int portIntoInt = Integer.parseInt(columns[1]);
                        portDescriptions.put(portIntoInt, columns[3]);
                    }

                }catch (Exception e){
                    // don't care
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("letters cannot be parsed into integers");
        }

        portDescriptions.forEach((key, value) -> System.out.println(key+ " " + value));

        //Scan local host *******************************************************************
        List<Integer> localPorts = new ArrayList<>(); // holds local ports

        String targetHost = "localhost"; // Change this to the target host you want to scan
        int minPort = 1;
        int maxPort = 65535;

        System.out.println("Scanning ports on " + targetHost + "...");

        for (int port = minPort; port <= maxPort; port++) {
            try {
                Socket socket = new Socket(targetHost, port);
                System.out.println("Port " + port + " is open");
                localPorts.add(port);
                socket.close();
            } catch (IOException e) {
                // Port is likely closed or unreachable
            }
        }

        System.out.println("Port scanning finished.");
        System.out.println(localPorts);

    }
}
