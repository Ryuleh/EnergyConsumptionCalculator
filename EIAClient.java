import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

public class EIAClient {

    private static final String API_KEY = loadApiKey();

    private static String loadApiKey() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            return props.getProperty("EIA_API_KEY");
        } catch (Exception e) {
            System.err.println("  [EIA] config.properties not found. API fetch disabled.");
            return null;
        }
    }

    public static double fetchRate(String stateCode) {
        if (API_KEY == null) return -1.0;

        try {
            String urlStr =
                "https://api.eia.gov/v2/electricity/retail-sales/data/" +
                "?api_key=" + API_KEY +
                "&frequency=monthly" +
                "&data%5B0%5D=price" +
                "&facets%5Bsectorid%5D%5B%5D=RES" +
                "&facets%5Bstateid%5D%5B%5D=" + stateCode.toUpperCase() +
                "&sort%5B0%5D%5Bcolumn%5D=period" +
                "&sort%5B0%5D%5Bdirection%5D=desc" +
                "&length=1";

            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("  [EIA] HTTP " + status);
                return -1.0;
            }

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            return parsePrice(response.toString());

        } catch (Exception e) {
            System.err.println("  [EIA] " + e.getMessage());
            return -1.0;
        }
    }

    private static double parsePrice(String json) {
        String marker = "\"price\":\"";
        int idx = json.indexOf(marker);
        if (idx != -1) {
            int start = idx + marker.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                double centsPerKwh = Double.parseDouble(json.substring(start, end).trim());
                return centsPerKwh / 100.0;
            }
        }
        marker = "\"price\":";
        idx = json.indexOf(marker);
        if (idx != -1) {
            int start = idx + marker.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end != -1) {
                double centsPerKwh = Double.parseDouble(json.substring(start, end).trim());
                return centsPerKwh / 100.0;
            }
        }
        return -1.0;
    }

    public static String getStateName(String code) {
        switch (code.toUpperCase()) {
            case "AL": return "Alabama";       case "AK": return "Alaska";
            case "AZ": return "Arizona";       case "AR": return "Arkansas";
            case "CA": return "California";    case "CO": return "Colorado";
            case "CT": return "Connecticut";   case "DE": return "Delaware";
            case "FL": return "Florida";       case "GA": return "Georgia";
            case "HI": return "Hawaii";        case "ID": return "Idaho";
            case "IL": return "Illinois";      case "IN": return "Indiana";
            case "IA": return "Iowa";          case "KS": return "Kansas";
            case "KY": return "Kentucky";      case "LA": return "Louisiana";
            case "ME": return "Maine";         case "MD": return "Maryland";
            case "MA": return "Massachusetts"; case "MI": return "Michigan";
            case "MN": return "Minnesota";     case "MS": return "Mississippi";
            case "MO": return "Missouri";      case "MT": return "Montana";
            case "NE": return "Nebraska";      case "NV": return "Nevada";
            case "NH": return "New Hampshire"; case "NJ": return "New Jersey";
            case "NM": return "New Mexico";    case "NY": return "New York";
            case "NC": return "North Carolina";case "ND": return "North Dakota";
            case "OH": return "Ohio";          case "OK": return "Oklahoma";
            case "OR": return "Oregon";        case "PA": return "Pennsylvania";
            case "RI": return "Rhode Island";  case "SC": return "South Carolina";
            case "SD": return "South Dakota";  case "TN": return "Tennessee";
            case "TX": return "Texas";         case "UT": return "Utah";
            case "VT": return "Vermont";       case "VA": return "Virginia";
            case "WA": return "Washington";    case "WV": return "West Virginia";
            case "WI": return "Wisconsin";     case "WY": return "Wyoming";
            case "DC": return "Washington D.C.";
            default: return null;
        }
    }
}