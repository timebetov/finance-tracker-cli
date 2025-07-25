package com.github.timebetov.helper;

import java.time.format.DateTimeFormatter;

public class AppConstant {

    private AppConstant() {}

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String DISPLAY_FORMAT = "| %-36s | %-7s | %10s | %-13s | %-20s | %20s |";
}
