import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.FileOutputStream;
import java.io.IOException;

class Code {

    public static void main(String[] args) throws IOException {

        Map<Character, Integer> map = new LinkedHashMap<>();  //Linked для правильного порядка
        Map<Character, String> mapCodes = new LinkedHashMap<>();

        readFile("ToEncode.txt", map);
        map.put('└', 1);   // add EOF char

        System.out.println("\nInitial Map:");
        printMap(map);
        sortMap(map);

        System.out.println("\nSorted Map:");
        printMap(map);

        divisionFano(map, mapCodes, "", '-');

        System.out.println("\nCode Map:");
        printCodeMap(mapCodes);

        createEncodedFile(mapCodes, "ToEncode.txt");

        System.out.println("\nDecoded:");
        createDecodedFile(decodingFano("EncodedFile.bin"));

        System.out.println("\nSize of initial file: " + Files.size(Paths.get("ToEncode.txt")));
        System.out.println("Size of encoded file: " + Files.size(Paths.get("EncodedFile.bin")));

    }

    static void createDecodedFile(StringBuilder text) throws IOException {

        System.out.println(text);
        BufferedWriter writer = new BufferedWriter(new FileWriter("Decoded.txt", false));
        writer.write(String.valueOf(text));
        writer.close();
    }

    static void divisionFano(Map<Character, Integer> map, Map<Character, String> mapCodes, String code, char addToCode) {

        // recursion exit
        if (map.size() == 1) {

            code += addToCode;

            if (addToCode == '-') code = "0";

            // map have only 1 element
            mapCodes.put(map.keySet().iterator().next(), code);
            return;
        }

        if (addToCode != '-') code += addToCode;

        int sum = 0;
        for (char element : map.keySet()) {

            sum += map.get(element);
        }
        //System.out.println("sum: " + sum);      additional info

        int halfSum = sum / 2;
        sum = 0;           //reuse sum

        Map<Character, Integer> leftMap = new LinkedHashMap<>();
        Map<Character, Integer> rightMap = new LinkedHashMap<>();

        for (char element : map.keySet()) {

            if (map.get(element) + sum <= halfSum) {

                sum += map.get(element);
                leftMap.put(element, map.get(element));
            } else {

                rightMap.put(element, map.get(element));
            }
        }

        divisionFano(leftMap, mapCodes, code, '1');  // invert 0 to 1, cause left part is min
        divisionFano(rightMap, mapCodes, code, '0');
    }

    public static void createEncodedFile(Map<Character, String> codeMap, String fileName) throws IOException {

        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        FileOutputStream fileOutputStream = new FileOutputStream("EncodedFile.bin", false);
        BitOutputStream output = new BitOutputStream(fileOutputStream);

        StringBuilder value = new StringBuilder();
        int c;
        while ((c = bufferedReader.read()) != -1) {
            char character = (char) c;

            String code = codeMap.get(character);

            if (code != null) {
                value.append(code);
            }
        }

        value.append(codeMap.get('└'));  // add to end EOF

        for (int i = 0; i < value.length(); i++) {
            char bit = value.charAt(i);
            output.writeBit(bit == '1');
        }

        bufferedReader.close();
        output.close();

        createKeyFile(codeMap);
    }

    public static void createKeyFile(Map<Character, String> codeMap) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("Key.txt", false));

        for (char ch : codeMap.keySet()) {

            writer.write(ch + " " + codeMap.get(ch));
            writer.newLine();
        }
        writer.close();
    }

    public static StringBuilder decodingFano(String encodedFile) throws IOException {

        BufferedReader readerKey = new BufferedReader(new FileReader("Key.txt"));

        String line;
        Map<Character, String> codeMap = new HashMap<>();

        while ((line = readerKey.readLine()) != null) {

            if (line.isEmpty()) line += '\n' + readerKey.readLine();

            codeMap.put(line.charAt(0), line.substring(2));
        }

        FileInputStream fileInputStream = new FileInputStream("EncodedFile.bin");

        StringBuilder result = new StringBuilder();
        StringBuilder currentCode = new StringBuilder();

        int ch;
        while ((ch = fileInputStream.read()) != -1) {

            StringBuilder bitString = new StringBuilder(Integer.toBinaryString(ch));

            while (bitString.length() < 8) {
                bitString.insert(0, "0");
            }

            currentCode.append(bitString);

            for (int i = 0; i <= currentCode.length(); i++) {

                char key;
                if (codeMap.containsKey(key = getKeyByValue(codeMap, currentCode.substring(0, i)))) {

                    if (key == '└') {

                        fileInputStream.close();
                        return result;
                    }

                    result.append(key);
                    currentCode = new StringBuilder(currentCode.substring(codeMap.get(key).length(), currentCode.length()));
                    i = -1;
                }
            }
        }

        fileInputStream.close();
        return result;
    }

    public static char getKeyByValue(Map<Character, String> codeMap, String value) {

        for (Map.Entry<Character, String> entry : codeMap.entrySet()) {

            if (entry.getValue().equals(value)) return entry.getKey();
        }
        return '~';
    }

    static void sortMap(Map<Character, Integer> unsortedMap) {

        List<Map.Entry<Character, Integer>> list = new ArrayList<>(unsortedMap.entrySet());

        list.sort(Map.Entry.comparingByValue());
        unsortedMap.clear();

        for (Map.Entry<Character, Integer> entry : list) {
            unsortedMap.put(entry.getKey(), entry.getValue());
        }
    }

    static void printMap(Map<Character, Integer> map) {

        for (char a : map.keySet()) {

            if (a == '\n') System.out.println("key:\\n;\tvalue:" + map.get(a));
            else System.out.println("key: " + a + ";\tvalue:" + map.get(a));
        }
    }

    static void printCodeMap(Map<Character, String> map) {

        for (char a : map.keySet()) {

            if (a == '\n') System.out.println("key:\\n;\tvalue:" + map.get(a));
            else System.out.println("key: " + a + ";\tvalue:" + map.get(a));
        }
    }

    static void readFile(String fileName, Map<Character, Integer> characterCountMap) throws IOException {

        FileReader fileReader = new FileReader(fileName);

        BufferedReader bufferedReader = new BufferedReader(fileReader);

        int c;
        while ((c = bufferedReader.read()) != -1) {

            char character = (char) c;

            if (character == '\r') continue;

            if (characterCountMap.containsKey(character)) {

                characterCountMap.put(character, characterCountMap.get(character) + 1);
            } else {

                characterCountMap.put(character, 1);
            }
        }

        bufferedReader.close();
    }
}