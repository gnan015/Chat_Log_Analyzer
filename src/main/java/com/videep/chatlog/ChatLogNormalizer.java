package com.videep.chatlog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts supported WhatsApp export entries into the application's standard format. */
public final class ChatLogNormalizer {
    private static final Pattern BRACKETED_WHATSAPP = Pattern.compile(
            "^\\s*\\[\\d{1,4}[/.-]\\d{1,2}[/.-]\\d{1,4},\\h*(\\d{1,2}):(\\d{2})(?:\\h*([AaPp][Mm]))?]\\h+([^:]+?):\\h*(.*)$");
    private static final Pattern DASHED_WHATSAPP = Pattern.compile(
            "^\\s*\\d{1,4}[/.-]\\d{1,2}[/.-]\\d{1,4},\\h*(\\d{1,2}):(\\d{2})(?:\\h*([AaPp][Mm]))?\\h+-\\h+([^:]+?):\\h*(.*)$");
    private static final Pattern WHATSAPP_SYSTEM_NOTICE = Pattern.compile(
            "^\\s*\\d{1,4}[/.-]\\d{1,2}[/.-]\\d{1,4},\\h*\\d{1,2}:\\d{2}(?:\\h*[AaPp][Mm])?\\h+-\\h+.*$");

    private ChatLogNormalizer() { }

    public static List<String> normalize(String text) {
        List<String> entries = new ArrayList<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;
            Matcher match = BRACKETED_WHATSAPP.matcher(line);
            if (!match.matches()) match = DASHED_WHATSAPP.matcher(line);
            if (match.matches()) {
                entries.add(format(match));
            } else if (WHATSAPP_SYSTEM_NOTICE.matcher(line).matches()) {
                // Encryption and group event notices have no sender/message structure to analyze.
                continue;
            } else if (!entries.isEmpty() && !line.startsWith("[")) {
                int last = entries.size() - 1;
                entries.set(last, entries.get(last) + " " + line.trim());
            } else {
                entries.add(line);
            }
        }
        return entries;
    }

    private static String format(Matcher match) {
        int hour = Integer.parseInt(match.group(1));
        String meridiem = match.group(3);
        if (meridiem != null) {
            if (meridiem.equalsIgnoreCase("PM") && hour < 12) hour += 12;
            if (meridiem.equalsIgnoreCase("AM") && hour == 12) hour = 0;
        }
        return String.format("[%02d:%s] %s: %s", hour, match.group(2), match.group(4).trim(), match.group(5));
    }
}
