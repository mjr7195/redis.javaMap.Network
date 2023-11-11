import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.net.Socket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class CSVReader {
    public static void main(String[] args) {
        String pathToCsv = "service-names-port-numbers.csv"; // replace with your CSV file path
        String line;
        Map<Integer, String> portDescriptions = new TreeMap<>(); //Sorted, holds ports with descriptions from CSV file

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
        Map<Integer, Integer> localPorts = new TreeMap<>(); // holds local ports

        String targetHost = "localhost"; // Change this to the target host you want to scan
        int minPort = 1;
        int maxPort = 65535;

        System.out.println("Scanning ports on " + targetHost + "...");

        for (int port = minPort; port <= maxPort; port++) {
            try {
                Socket socket = new Socket(targetHost, port);
                System.out.println("Port " + port + " is open");
                localPorts.put(port, port);
                socket.close();
            } catch (IOException e) {
                // Port is likely closed or unreachable
            }
        }

        System.out.println("Port scanning finished.");

        //match local ports with port descriptions **************************************************
        Map<Integer, String> localPortDescriptions = new TreeMap<>();//Sorted, holds my local ports with descriptions
        for (Map.Entry<Integer, String> entry : portDescriptions.entrySet()){
            if (localPorts.containsKey(entry.getKey())) {
                localPortDescriptions.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println();
        System.out.println("My local ports descriptions are:");
        for(Map.Entry<Integer, String> entry : localPortDescriptions.entrySet()){
            int localHostKey = entry.getKey();
            String localHostDescription = entry.getValue();
            System.out.println("Port: " +localHostKey+ ", Description: "+ localHostDescription);
        }

        //upload to redis and display from redis ********************************************************
        Jedis jedis = null;
        try{
            jedis = new Jedis("localhost");
            for (Map.Entry<Integer, String> entry : localPortDescriptions.entrySet()) {
                int localKey = entry.getKey();
                String localDescription = entry.getValue();
                String stringLocalKey = String.valueOf(localKey);
                //Create (Set a key-value pair)
                jedis.set(stringLocalKey, localDescription);
                String value = jedis.get(stringLocalKey);
                System.out.println("Port:" +stringLocalKey+ ", Description: "+ value );
            }
        }catch (JedisConnectionException e){
            System.out.println("Could not connect to Redis:" + e.getMessage());
        }finally {
            jedis.close();
        }


      }
    }

