/**
 * Represents a single electrical appliance with its power usage profile.
 */
public class Appliance {
    private String name;
    private double powerConsumption; // in watts
    private double hoursPerDay;      // daily usage in hours
    private String category;         // e.g., Kitchen, HVAC, Entertainment

    public Appliance(String name, double powerConsumption, double hoursPerDay, String category) {
        this.name = name;
        this.powerConsumption = powerConsumption;
        this.hoursPerDay = hoursPerDay;
        this.category = category;
    }

    /** kWh consumed per day */
    public double getDailyKwh() {
        return (powerConsumption * hoursPerDay) / 1000.0;
    }

    /** kWh consumed per month (30 days) */
    public double getMonthlyKwh() {
        return getDailyKwh() * 30;
    }

    /** kWh consumed per year */
    public double getAnnualKwh() {
        return getDailyKwh() * 365;
    }

    public String getName()             { return name; }
    public double getPowerConsumption() { return powerConsumption; }
    public double getHoursPerDay()      { return hoursPerDay; }
    public String getCategory()         { return category; }

    public void setName(String name)                       { this.name = name; }
    public void setPowerConsumption(double watts)          { this.powerConsumption = watts; }
    public void setHoursPerDay(double hours)               { this.hoursPerDay = hours; }
    public void setCategory(String category)               { this.category = category; }

    @Override
    public String toString() {
        return String.format("%s (%s) — %.0fW × %.1fh/day", name, category, powerConsumption, hoursPerDay);
    }
}