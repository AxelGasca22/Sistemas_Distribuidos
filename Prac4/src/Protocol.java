import java.util.HashMap;
import java.util.Map;

public class Protocol {

    public static String build(String type, Map<String, String> data) {
        StringBuilder sb = new StringBuilder(type);
        for (Map.Entry<String, String> e : data.entrySet()) {
            sb.append("|").append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    public static ParsedMessage parse(String line) {
        String[] parts = line.split("\\|");
        String type = parts[0];
        Map<String, String> data = new HashMap<>();

        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) {
                data.put(kv[0], kv[1]);
            }
        }
        return new ParsedMessage(type, data);
    }

    public record ParsedMessage(String type, Map<String, String> data) {}
}