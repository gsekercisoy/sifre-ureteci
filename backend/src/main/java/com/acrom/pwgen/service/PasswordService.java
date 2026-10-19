package com.acrom.pwgen.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PasswordService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?/~";
    private static final String AMBIGUOUS = "0Oo1lI|B8";

    private final SecureRandom random = new SecureRandom();

    public Map<String, Object> generate(Map<String, Object> req) {
        int length = optInt(req, "length", 16);
        boolean upper = optBool(req, "upper", true);
        boolean lower = optBool(req, "lower", true);
        boolean digits = optBool(req, "digits", true);
        boolean symbols = optBool(req, "symbols", true);
        boolean excludeAmbiguous = optBool(req, "excludeAmbiguous", false);

        if (length < 4 || length > 64) {
            throw new IllegalArgumentException("length 4-64 araliginda olmali");
        }

        StringBuilder pool = new StringBuilder();
        if (upper) pool.append(UPPER);
        if (lower) pool.append(LOWER);
        if (digits) pool.append(DIGITS);
        if (symbols) pool.append(SYMBOLS);

        String p = pool.toString();
        if (excludeAmbiguous) {
            StringBuilder filtered = new StringBuilder();
            for (char c : p.toCharArray()) {
                if (AMBIGUOUS.indexOf(c) < 0) filtered.append(c);
            }
            p = filtered.toString();
        }
        if (p.isEmpty()) {
            throw new IllegalArgumentException("En az bir karakter turu secilmeli");
        }

        StringBuilder pw = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pw.append(p.charAt(random.nextInt(p.length())));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("password", pw.toString());
        out.put("length", length);
        out.put("poolSize", p.length());
        out.put("entropyBits", round1(length * (Math.log(p.length()) / Math.log(2))));
        return out;
    }

    public Map<String, Object> strength(Map<String, Object> req) {
        String pw = req.get("password") == null ? "" : String.valueOf(req.get("password"));
        Map<String, Object> out = new LinkedHashMap<>();

        String upper = "A-Z", lower = "a-z", digit = "0-9", sym = "simge";
        boolean hasU = false, hasL = false, hasD = false, hasS = false;
        int classes = 0;
        for (char c : pw.toCharArray()) {
            if (Character.isUpperCase(c) && !hasU) { hasU = true; classes++; }
            if (Character.isLowerCase(c) && !hasL) { hasL = true; classes++; }
            if (Character.isDigit(c) && !hasD) { hasD = true; classes++; }
            if (!upper.contains(String.valueOf(c)) && !lower.contains(String.valueOf(c))
                    && !digit.contains(String.valueOf(c)) && c != ' ' && !hasS) { hasS = true; classes++; }
        }
        double bits = pw.isEmpty() ? 0 : round1(pw.length() * (Math.log(Math.max(classes * 20, classes * 20 + 10)) / Math.log(2)));
        out.put("charsetClasses", classes);
        out.put("length", pw.length());
        out.put("approxBits", bits);
        out.put("verdict", bits < 40 ? "zayif" : bits < 70 ? "orta" : "guclu");
        return out;
    }

    private int optInt(Map<String, Object> req, String key, int def) {
        if (req == null || req.get(key) == null) return def;
        try {
            return ((Number) req.get(key)).intValue();
        } catch (Exception e) {
            return Integer.parseInt(String.valueOf(req.get(key)));
        }
    }

    private boolean optBool(Map<String, Object> req, String key, boolean def) {
        if (req == null || req.get(key) == null) return def;
        if (req.get(key) instanceof Boolean) {
            return (Boolean) req.get(key);
        }
        return Boolean.parseBoolean(String.valueOf(req.get(key)));
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
