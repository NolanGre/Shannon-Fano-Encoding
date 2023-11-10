import java.util.LinkedHashMap;
import java.util.Map;

public class ForTest {
    static Map<Character, Integer> valueMap = new LinkedHashMap<>();
    static Map<Character, String> coedMap = new LinkedHashMap<>();

    public static void main(String[] args) {

        valueMap.put('A', 40);
        valueMap.put('B', 30);
        valueMap.put('C', 11);
        valueMap.put('D', 10);
        valueMap.put('E', 8);
        valueMap.put('F', 5);
        valueMap.put('G', 2);
        valueMap.put('H', 2);
        valueMap.put('I', 1);
        valueMap.put('J', 1);

        int sum = 0;
        for (int temp : valueMap.values()) sum += temp;
        System.out.println("Init Sum: " + sum);

        System.out.println("Init data: " + valueMap.toString());
        fillCoedMap();

    }

    static void fillCoedMap() {

        Map<Character, Integer> leftMap = new LinkedHashMap<>();
        Map<Character, Integer> rightMap = new LinkedHashMap<>();

        int sum = 0;
        for (int temp : valueMap.values()) sum += temp;

        int halfSum = sum / 2;

        int leftSum = 0;

        // From higher to lower value (Major upgrade)
        for (char temp : valueMap.keySet()) {

            if (leftSum + valueMap.get(temp) <= halfSum) {

                leftMap.put(temp, valueMap.get(temp));
                leftSum += valueMap.get(temp);
            } else {

                rightMap.put(temp, valueMap.get(temp));
            }
        }

        System.out.println("\nLeft: " + leftMap.toString());
        int sumL = 0;
        for (int temp : leftMap.values()) sumL += temp;
        System.out.println("Left Sum: " + sumL);

        System.out.println("Right: " + rightMap.toString());
        int sumR = 0;
        for (int temp : rightMap.values()) sumR += temp;
        System.out.println("Right Sum: " + sumR);


    }
}
