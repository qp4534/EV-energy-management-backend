package com.ev_energy_management.backend.util;

public class MaskingUtils {

    public static String maskApiKey(String apikey) {
        if (apikey == null || apikey.length() < 8) return "****";
        return apikey.substring(0, 4)
                + "*".repeat(Math.max(apikey.length() - 8, 4))
                + apikey.substring(apikey.length() - 4);
    }

    public static String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 4) return local.charAt(0) + "***@" + parts[1];
        return local.substring(0, 2) + "*".repeat(local.length() - 4) + local.substring(local.length() - 2) + "@" + parts[1];
    }

    public static String maskPhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 8) return phone;
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
    }

    public static String maskIp(String ip) {
        if (ip == null || !ip.contains(".")) return ip;
        int lastDot = ip.lastIndexOf(".");
        return ip.substring(0, lastDot) + ".***";
    }
}