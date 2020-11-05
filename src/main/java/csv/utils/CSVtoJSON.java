package csv.utils;

import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CSVtoJSON {

    private final static String SEPARATOR = ",(?![^()]*\\))"; // Split on comma, except if the comma is between parentheses.
    private static List<String> fields;

    public static void main(String... args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Give the CSV file path as first parameter");
        } else if (args.length < 2) {
            throw new IllegalArgumentException("Give the JSON file path as second parameter");
        } else {
            String csvFileName = args[0];
            File csvFile = new File(csvFileName);
            if (!csvFile.exists()) {
                throw new FileNotFoundException(String.format("%s not found in %s", csvFileName, System.getProperty("user.dir")));
            }
            String jsonFileName = args[1];
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFileName));
            int lineNo = 0;
            String line;
            boolean keepReading = true;
            while (keepReading) {
                line = br.readLine();
                if (line == null) {
                    keepReading = false;
                } else {
                    String[] splitted = line.split(SEPARATOR);
                    if (lineNo == 0) {
                        fields = Arrays.asList(splitted);
                    } else {
                        JSONObject json = new JSONObject();
                        for (int i=0; i<splitted.length; i++) {
                            json.put(fields.get(i), splitted[i].replace("\"", "'"));
                        }
//                        System.out.println(json.toString());
                        bw.write(json.toString() + "\n");
                    }
                }
                lineNo++;
            }
            br.close();
            bw.close();
            System.out.println(String.format("%s ready!", jsonFileName));
        }
    }
}
