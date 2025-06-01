package com.example.project_generator.util;

public class MavenVersionResolver {
    public static String resolve(String javaVersion) {
        int version = Integer.parseInt(javaVersion.replace("1.", ""));
        return switch (version) {
            case 8 -> "3.6.3";
            case 11 -> "3.8.1";
            case 17 -> "3.8.6";
            case 21 -> "3.9.6";
            default -> "3.9.9";
        };
    }
}

