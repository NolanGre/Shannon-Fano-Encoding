import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static Map<Character, Integer> valueMap = new LinkedHashMap<>();
    static Map<Character, String> codeMap = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {

        fileReader("InitData.txt");
        System.out.println("Value map: " + valueMap.toString());

        fillCodeMap(valueMap, "");
        System.out.println("\nCode map: " + codeMap.toString());

        makeBinFile("InitData.txt", "Encoded.txt");
        makeKeyFile("Key.txt");

        makeDecodedFile("Result.txt", "Key.txt", "Encoded.txt");

        System.out.println("\nПочатковий файл (Байт): " + Files.size(Paths.get("InitData.txt")));
        System.out.println("Закодований файл (Байт): " + Files.size(Paths.get("Encoded.txt")));
    }

    static void fileReader(String file) throws IOException {

        FileReader reader = new FileReader(file);

        int temp;
        while ((temp = reader.read()) != -1) {

            char ch = (char) temp;

            // We have \r and \n when read \n in file
            if (ch != '\r') {

                // If we haven't character in map, put 0 + 1
                valueMap.put(ch, valueMap.getOrDefault(ch, 0) + 1);
            }
        }

        // Be sure to close stream
        reader.close();

        // Use \0 as own EOF char
        valueMap.put('\0', 1);

        // And cooking our data by sort
        List<Map.Entry<Character, Integer>> list = new ArrayList<>(valueMap.entrySet());

        // Sort in descending order
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        valueMap.clear();  // Clear all elements from map

        // Refill in the proper order
        for (Map.Entry<Character, Integer> entry : list) {

            valueMap.put(entry.getKey(), entry.getValue());
        }
    }

    static void fillCodeMap(Map<Character, Integer> main, String code) {

        // Recursion exit
        if (main.size() == 1) {

            codeMap.put(main.keySet().iterator().next(), code);
            return;
        }

        Map<Character, Integer> leftMap = new LinkedHashMap<>();
        Map<Character, Integer> rightMap = new LinkedHashMap<>();

        int sum = 0;
        for (int temp : main.values()) sum += temp;

        int halfSum = sum / 2;

        int leftSum = 0;

        // From higher to lower values (Major upgrade)
        for (char temp : main.keySet()) {

            if (leftSum + main.get(temp) <= halfSum) {

                leftMap.put(temp, main.get(temp));
                leftSum += main.get(temp);
            } else {

                rightMap.put(temp, main.get(temp));
            }
        }

        fillCodeMap(leftMap, code + "0");
        fillCodeMap(rightMap, code + "1");

    }

    static void makeBinFile(String initFile, String encodedFile) throws IOException {

        // Open streams for reading/writing
        FileReader reader = new FileReader(initFile);
        FileOutputStream output = new FileOutputStream(encodedFile, false);

        StringBuilder binCode = new StringBuilder();

        int temp;
        while ((temp = reader.read()) != -1) {

            String value = codeMap.get((char) temp);

            if (value != null) binCode.append(value);
        }

        reader.close();

        // After all, we put our EOF char
        binCode.append(codeMap.get('\0'));

        // Check for mod 8. If we have 5 bit, need to put 3 else
        // Common mod (len % 8) give 5
        int customMod = 8 - (binCode.length() % 8);
        binCode.append("0".repeat(customMod));

        for (int i = 0; i < binCode.length(); i += 8){

            // Convert string(of bits) to int(byte)
            output.write(Integer.parseInt(binCode.substring(i, i + 8), 2));
        }

        output.close();
    }

    static void makeKeyFile(String keyFile) throws IOException {

        // Reminder: false mode for erase data, or create nonexistent file
        FileWriter writer = new FileWriter(keyFile, false);

        for (Map.Entry<Character, String> entry : codeMap.entrySet()){

            writer.write(entry.getKey() + entry.getValue() + '\n');
        }

        writer.close();
    }

    static void makeDecodedFile(String decodeFile, String keyFile, String encodedFile) throws IOException {

        // Use additional map for future expansion
        Map<Character, String> tempCodeMap = new LinkedHashMap<>();

        FileReader reader = new FileReader(keyFile);
        Scanner scanner = new Scanner(reader);

        while (scanner.hasNextLine()) {

            String str = scanner.nextLine();

            if (str.isEmpty()) str += '\n' + scanner.nextLine();

            tempCodeMap.put(str.charAt(0), str.substring(1));
        }

        reader.close();
        System.out.println("\nKey file: "+ tempCodeMap.toString());

        // Read and write decode
        FileInputStream input = new FileInputStream(encodedFile);
        FileWriter writer = new FileWriter(decodeFile, false);

        // Half total code (almost always hold to <2 byte)
        StringBuilder totalCode = new StringBuilder();

        int tempBin;
        while ((tempBin = input.read()) != -1) {

            // Just convert int to string
            StringBuilder tempBinStr = new StringBuilder(Integer.toBinaryString(tempBin));

            // Fill missing 0 at start ("1011" -> "00001011")
            while (tempBinStr.length() < 8) {
                tempBinStr.insert(0, "0");
            }

            // Add aligned string to working string
            totalCode.append(tempBinStr);

            for (int i = 0; i <= totalCode.length(); i++) {

                char buff;
                if (tempCodeMap.containsKey(buff = getKeyFromValue(totalCode.substring(0, i), tempCodeMap))) {

                    if (buff == '\0') {   // EOF char

                        reader.close();
                        writer.close();
                        return;
                    }

                    writer.write(buff);

                    // Erase code(value) of buff
                    totalCode = new StringBuilder(totalCode.substring(tempCodeMap.get(buff).length(), totalCode.length()));

                    i = -1;  // In next cycle i = 0
                }
            }
        }
        reader.close();
        writer.close();
    }

    public static char getKeyFromValue(String value, Map<Character, String> tempCodeMap) {

        // Search Map for coincidence of Key and Value
        for (Map.Entry<Character, String> entry : tempCodeMap.entrySet()) {

            if (entry.getValue().equals(value)) return entry.getKey();
        }
        // In any case we haven't \r in map
        return '\r';
    }

}
