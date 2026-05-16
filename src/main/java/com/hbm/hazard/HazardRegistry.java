package com.hbm.hazard;

public class HazardRegistry {
    public static final HazardType RADIATION = new HazardType("radiation");
    public static final HazardType FIRE = new HazardType("fire");
    public static final HazardType CRYOGENIC = new HazardType("cryogenic");
    public static final HazardType ASBESTOS = new HazardType("asbestos");

    public static class HazardType {
        public final String name;
        public HazardType(String name) { this.name = name; }
    }
}
