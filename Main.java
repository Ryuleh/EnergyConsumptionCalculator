import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {

    // ── Wattage lookup table ───────────────────────────────────────────────
    private static final Map<String, double[]> WATTAGE_DB = new HashMap<>();
    static {
        // Format: keyword -> [watts, suggestedHours]
        // Kitchen
        WATTAGE_DB.put("refrigerator", new double[]{150, 24});
        WATTAGE_DB.put("fridge",       new double[]{150, 24});
        WATTAGE_DB.put("microwave",    new double[]{1200, 0.5});
        WATTAGE_DB.put("dishwasher",   new double[]{1800, 1});
        WATTAGE_DB.put("oven",         new double[]{2400, 1});
        WATTAGE_DB.put("stove",        new double[]{1500, 1});
        WATTAGE_DB.put("toaster",      new double[]{900, 0.2});
        WATTAGE_DB.put("coffee",       new double[]{800, 0.5});
        WATTAGE_DB.put("kettle",       new double[]{1500, 0.3});
        WATTAGE_DB.put("blender",      new double[]{300, 0.1});
        WATTAGE_DB.put("freezer",      new double[]{100, 24});
        // HVAC
        WATTAGE_DB.put("ac",           new double[]{3500, 8});
        WATTAGE_DB.put("air conditioner", new double[]{3500, 8});
        WATTAGE_DB.put("central air",  new double[]{3500, 8});
        WATTAGE_DB.put("window ac",    new double[]{1200, 8});
        WATTAGE_DB.put("heater",       new double[]{1500, 6});
        WATTAGE_DB.put("furnace",      new double[]{900, 8});
        WATTAGE_DB.put("heat pump",    new double[]{2000, 8});
        WATTAGE_DB.put("fan",          new double[]{75, 8});
        WATTAGE_DB.put("ceiling fan",  new double[]{75, 8});
        WATTAGE_DB.put("water heater", new double[]{4000, 2});
        WATTAGE_DB.put("dehumidifier", new double[]{280, 8});
        WATTAGE_DB.put("humidifier",   new double[]{50, 8});
        // Entertainment
        WATTAGE_DB.put("tv",           new double[]{80, 5});
        WATTAGE_DB.put("television",   new double[]{80, 5});
        WATTAGE_DB.put("monitor",      new double[]{30, 8});
        WATTAGE_DB.put("desktop",      new double[]{200, 6});
        WATTAGE_DB.put("desktop pc",   new double[]{200, 6});
        WATTAGE_DB.put("laptop",       new double[]{50, 8});
        WATTAGE_DB.put("gaming",       new double[]{300, 4});
        WATTAGE_DB.put("playstation",  new double[]{200, 4});
        WATTAGE_DB.put("xbox",         new double[]{180, 4});
        WATTAGE_DB.put("router",       new double[]{10, 24});
        WATTAGE_DB.put("modem",        new double[]{10, 24});
        // Laundry
        WATTAGE_DB.put("washer",       new double[]{500, 1});
        WATTAGE_DB.put("washing machine", new double[]{500, 1});
        WATTAGE_DB.put("dryer",        new double[]{5000, 1});
        WATTAGE_DB.put("iron",         new double[]{1000, 0.5});
        // Lighting
        WATTAGE_DB.put("light",        new double[]{10, 6});
        WATTAGE_DB.put("lights",       new double[]{100, 6});
        WATTAGE_DB.put("lamp",         new double[]{10, 6});
        WATTAGE_DB.put("led",          new double[]{10, 6});
        // Devices
        WATTAGE_DB.put("phone",        new double[]{5, 3});
        WATTAGE_DB.put("charger",      new double[]{5, 3});
        WATTAGE_DB.put("tablet",       new double[]{10, 4});
        WATTAGE_DB.put("printer",      new double[]{30, 0.5});
        WATTAGE_DB.put("vacuum",       new double[]{1400, 0.5});
        WATTAGE_DB.put("hair dryer",   new double[]{1800, 0.2});
        WATTAGE_DB.put("hairdryer",    new double[]{1800, 0.2});
        WATTAGE_DB.put("treadmill",    new double[]{600, 1});
        WATTAGE_DB.put("ev charger",   new double[]{7200, 8});
        WATTAGE_DB.put("electric car", new double[]{7200, 8});
    }

    public static void main(String[] args) {
        printBanner();
        Scanner scanner = new Scanner(System.in);

        String name  = prompt(scanner, "Your name");
        String city  = prompt(scanner, "City");
        String state = promptState(scanner);

        String location = city + ", " + EIAClient.getStateName(state);
        double rate = fetchRateWithFallback(scanner, state);

        User user = new User(name, location, rate);

        System.out.println();
        System.out.println("  You can add appliances now, or add them in the GUI.");
        int count = promptInt(scanner, "How many appliances to add here? (0 to skip)", 0, 100);

        for (int i = 0; i < count; i++) {
            System.out.println();
            System.out.println("  Appliance " + (i + 1) + " of " + count);
            String aName  = prompt(scanner, "  Name");
            double watts  = promptWatts(scanner, aName);
            double hours  = promptHours(scanner, aName);
            String cat    = promptCategory(scanner);
            user.addAppliance(new Appliance(aName, watts, hours, cat));
        }

        scanner.close();

        System.out.println();
        System.out.println("  Launching dashboard...");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            GUI gui = new GUI(user);
            gui.setVisible(true);
        });
    }

    // ── Smart wattage prompt ───────────────────────────────────────────────

    private static double promptWatts(Scanner sc, String applianceName) {
        double[] suggestion = lookupAppliance(applianceName);

        if (suggestion != null) {
            System.out.printf("  Suggested wattage: %.0fW — press Enter to accept, or type a value: ",
                suggestion[0]);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return suggestion[0];
            try {
                double val = Double.parseDouble(input);
                if (val > 0 && val <= 100000) return val;
            } catch (NumberFormatException ignored) {}
            System.out.println("  ⚠  Invalid — using suggested value of " + (int)suggestion[0] + "W.");
            return suggestion[0];
        }

        // Not recognized — ask manually with a helpful hint
        System.out.println("  ℹ  Tip: check the label on the appliance, or search \"[appliance name] wattage\".");
        return promptDouble(sc, "  Power (watts)", 0.1, 100000);
    }

    private static double promptHours(Scanner sc, String applianceName) {
        double[] suggestion = lookupAppliance(applianceName);

        if (suggestion != null) {
            System.out.printf("  Suggested hours/day: %.1fh — press Enter to accept, or type a value: ",
                suggestion[1]);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return suggestion[1];
            try {
                double val = Double.parseDouble(input);
                if (val > 0 && val <= 24) return val;
            } catch (NumberFormatException ignored) {}
            System.out.println("  ⚠  Invalid — using suggested value of " + suggestion[1] + "h.");
            return suggestion[1];
        }

        return promptDouble(sc, "  Hours used per day", 0.1, 24);
    }

    /**
     * Looks up the appliance name against the wattage DB.
     * Checks for an exact match first, then a partial/keyword match.
     */
    private static double[] lookupAppliance(String name) {
        String lower = name.toLowerCase().trim();

        // Exact match
        if (WATTAGE_DB.containsKey(lower)) return WATTAGE_DB.get(lower);

        // Partial match — check if any key is contained in the name or vice versa
        double[] best = null;
        int bestLen = 0;
        for (Map.Entry<String, double[]> entry : WATTAGE_DB.entrySet()) {
            String key = entry.getKey();
            if (lower.contains(key) || key.contains(lower)) {
                if (key.length() > bestLen) {
                    bestLen = key.length();
                    best = entry.getValue();
                }
            }
        }
        return best;
    }

    // ── EIA fetch + fallback ───────────────────────────────────────────────

    private static double fetchRateWithFallback(Scanner scanner, String state) {
        System.out.println();
        System.out.print("  Fetching electricity rate for " +
            EIAClient.getStateName(state) + " from EIA... ");

        double rate = EIAClient.fetchRate(state);

        if (rate > 0) {
            System.out.printf("✓ $%.4f/kWh%n", rate);
            System.out.println("  (Latest residential average from U.S. Energy Information Administration)");
            return rate;
        } else {
            System.out.println("✗ Could not reach EIA API.");
            System.out.println("  Enter your rate manually (check your electricity bill).");
            return promptDouble(scanner, "  Electricity rate ($/kWh) [e.g. 0.13]", 0.001, 5.0);
        }
    }

    // ── Prompt helpers ─────────────────────────────────────────────────────

    private static String prompt(Scanner sc, String label) {
        System.out.print("  " + label + ": ");
        String val = sc.nextLine().trim();
        while (val.isEmpty()) {
            System.out.print("  (required) " + label + ": ");
            val = sc.nextLine().trim();
        }
        return val;
    }

    private static String promptState(Scanner sc) {
        while (true) {
            System.out.print("  State (2-letter code, e.g. NY): ");
            String code = sc.nextLine().trim().toUpperCase();
            if (EIAClient.getStateName(code) != null) return code;
            System.out.println("  ⚠  Unrecognized state code. Try again.");
        }
    }

    private static double promptDouble(Scanner sc, String label, double min, double max) {
        while (true) {
            System.out.print("  " + label + ": ");
            try {
                double val = Double.parseDouble(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.printf("  ⚠  Enter a value between %.4f and %.0f%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid number. Try again.");
            }
        }
    }

    private static int promptInt(Scanner sc, String label, int min, int max) {
        while (true) {
            System.out.print("  " + label + ": ");
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.printf("  ⚠  Enter a value between %d and %d%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid number. Try again.");
            }
        }
    }

    private static String promptCategory(Scanner sc) {
        String[] cats = {"Kitchen","HVAC","Entertainment","Laundry","Lighting","Devices","Other"};
        System.out.println("  Category:");
        for (int i = 0; i < cats.length; i++)
            System.out.printf("    [%d] %s%n", i + 1, cats[i]);
        int idx = promptInt(sc, "  Choice (1-" + cats.length + ")", 1, cats.length);
        return cats[idx - 1];
    }

    // ── Banner ─────────────────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║           ⚡   Watt Wise   ⚡           ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println();
    }
}