import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Represents a user with their appliance inventory and electricity rate.
 */
public class User {
    private String name;
    private String location;
    private double electricityRate; // $/kWh
    private List<Appliance> appliances;

    public User(String name, String location, double electricityRate) {
        this.name = name;
        this.location = location;
        this.electricityRate = electricityRate;
        this.appliances = new ArrayList<>();
    }

    public void addAppliance(Appliance a) {
        appliances.add(a);
    }

    public void removeAppliance(int index) {
        if (index >= 0 && index < appliances.size()) {
            appliances.remove(index);
        }
    }

    public List<Appliance> getAppliances() {
        return appliances;
    }

    // ── Totals ────────────────────────────────────────────────────────────

    public double getTotalDailyKwh() {
        return appliances.stream().mapToDouble(Appliance::getDailyKwh).sum();
    }

    public double getTotalMonthlyKwh() {
        return appliances.stream().mapToDouble(Appliance::getMonthlyKwh).sum();
    }

    public double getTotalAnnualKwh() {
        return appliances.stream().mapToDouble(Appliance::getAnnualKwh).sum();
    }

    public double getDailyCost()   { return getTotalDailyKwh()   * electricityRate; }
    public double getMonthlyCost() { return getTotalMonthlyKwh() * electricityRate; }
    public double getAnnualCost()  { return getTotalAnnualKwh()  * electricityRate; }

    /**
     * Returns kWh per category for chart/breakdown use.
     */
    public Map<String, Double> getMonthlyKwhByCategory() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Appliance a : appliances) {
            map.merge(a.getCategory(), a.getMonthlyKwh(), Double::sum);
        }
        return map;
    }

    // ── Getters / Setters ────────────────────────────────────────────────

    public String getName()             { return name; }
    public String getLocation()         { return location; }
    public double getElectricityRate()  { return electricityRate; }

    public void setName(String name)                      { this.name = name; }
    public void setLocation(String location)              { this.location = location; }
    public void setElectricityRate(double rate)           { this.electricityRate = rate; }
}