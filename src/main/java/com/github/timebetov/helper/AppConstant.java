package com.github.timebetov.helper;

import java.time.format.DateTimeFormatter;

public class AppConstant {

    private AppConstant() {}

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
