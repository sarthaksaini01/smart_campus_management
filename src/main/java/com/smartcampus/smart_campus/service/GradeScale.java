package com.smartcampus.smart_campus.service;

import java.util.List;

public final class GradeScale {

    public static final int PASS_MARK = 40;
    public static final List<String> GRADES = List.of("O", "A+", "A", "B+", "B", "C", "F");

    private GradeScale() {
    }

    public static String grade(int total) {
        if (total >= 90) return "O";
        if (total >= 80) return "A+";
        if (total >= 70) return "A";
        if (total >= 60) return "B+";
        if (total >= 50) return "B";
        if (total >= 40) return "C";
        return "F";
    }

    public static int gradePoint(int total) {
        if (total >= 90) return 10;
        if (total >= 80) return 9;
        if (total >= 70) return 8;
        if (total >= 60) return 7;
        if (total >= 50) return 6;
        if (total >= 40) return 5;
        return 0;
    }
}