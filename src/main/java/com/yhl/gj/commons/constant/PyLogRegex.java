package com.yhl.gj.commons.constant;

import java.util.regex.Pattern;

public interface PyLogRegex {
     String pattern="\\[(.+)    @ (\\d+-\\d+-\\d+ \\d+:\\d+:\\d+.\\d+)\\] (.+)";
    Pattern regex = Pattern.compile(pattern);
}
