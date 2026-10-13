package com.smartcampus.smart_campus.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeScaleTest {

    @ParameterizedTest
    @CsvSource({
            "100,O,10", "90,O,10",
            "89,A+,9", "80,A+,9",
            "79,A,8", "70,A,8",
            "69,B+,7", "60,B+,7",
            "59,B,6", "50,B,6",
            "49,C,5", "40,C,5",
            "39,F,0", "0,F,0"
    })
    void mapsTotalMarksToGradeAndPoint(int total, String grade, int point) {
        assertEquals(grade, GradeScale.grade(total));
        assertEquals(point, GradeScale.gradePoint(total));
    }
}