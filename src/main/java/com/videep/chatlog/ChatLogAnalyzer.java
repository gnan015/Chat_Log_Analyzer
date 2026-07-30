package com.videep.chatlog;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses chat entries and maintains analytics state for one chat stream. */
public final class ChatLogAnalyzer {
    private static final Pattern ENTRY = Pattern.compile(
            "^\\s*\\[(\\d{2}):(\\d{2})]\\s+([^:]+?)\\s*:\\s*(.*)\\s*$");

    private final Map<String, Integer> messageCounts = new HashMap<>();
    private final Map<String, String> previousMessages = new HashMap<>();

    public AnalysisResult process(String line) {
        if (line == null || line.isBlank()) {
            return AnalysisResult.invalid("Empty log line");
        }

        Matcher match = ENTRY.matcher(line);
        if (!match.matches()) {
            return AnalysisResult.invalid("Invalid format; expected [HH:MM] Name: Message");
        }

        int hour = Integer.parseInt(match.group(1));
        int minute = Integer.parseInt(match.group(2));
        if (hour > 23 || minute > 59) {
            return AnalysisResult.invalid("Invalid timestamp");
        }

        String user = match.group(3).trim();
        String message = match.group(4).trim();
        if (user.isEmpty()) {
            return AnalysisResult.invalid("User name cannot be empty");
        }

        boolean spam = message.equals(previousMessages.get(user));
        previousMessages.put(user, message);
        int count = messageCounts.merge(user, 1, Integer::sum);
        FraudCheck fraud = checkFraud(message);
        return new AnalysisResult(user, message, count, countWords(message), spam, findMostActive(), fraud.risk(), fraud.reasons(), null);
    }

    private int countWords(String message) {
        return message.isBlank() ? 0 : message.trim().split("\\s+").length;
    }

    private String findMostActive() {
        return messageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .findFirst()
                .orElseThrow()
                .getKey();
    }

    /** Heuristic screening: a warning aid, not a definitive fraud verdict. */
    private FraudCheck checkFraud(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        int score = 0;
        List<String> reasons = new ArrayList<>();
        if (text.matches(".*(https?://|www\\.|bit\\.ly|tinyurl\\.com|t\\.me/).*")) {
            score += 1;
            reasons.add("link");
        }
        if (text.matches(".*(otp|one.time password|password|pin|cvv|card number|bank details).*")) {
            score += 3;
            reasons.add("sensitive information request");
        }
        if (text.matches(".*(urgent|immediately|act now|account.*(block|suspend|verify)|limited time).*")) {
            score += 2;
            reasons.add("urgency or account threat");
        }
        if (text.matches(".*(winner|lottery|prize|claim.*reward|free gift).*")) {
            score += 2;
            reasons.add("prize or reward claim");
        }
        if (text.matches(".*(crypto|investment.*return|send money|upi).*")) {
            score += 1;
            reasons.add("payment or investment request");
        }
        String risk = score >= 4 ? "HIGH" : score >= 2 ? "MEDIUM" : score == 1 ? "LOW" : "NONE";
        return new FraudCheck(risk, String.join(", ", reasons));
    }

    private record FraudCheck(String risk, String reasons) { }
}
