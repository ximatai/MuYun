package net.ximatai.muyun.model.code;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateCodePart implements ICodePart {
    private final String formatString;

    public DateCodePart() {
        this.formatString = "yyyyMMdd";
    }

    public DateCodePart(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public String varchar() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(formatString));
    }
}
