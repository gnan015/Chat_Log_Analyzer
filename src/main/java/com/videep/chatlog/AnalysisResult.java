package com.videep.chatlog;

/** Immutable structured result for one input line. */
public record AnalysisResult(
        String user,
        String message,
        int messageCount,
        int wordCount,
        boolean spam,
        String mostActive,
        String fraudRisk,
        String fraudReasons,
        String error) {

    public static AnalysisResult invalid(String error) {
        return new AnalysisResult(null, null, 0, 0, false, null, "NONE", "", error);
    }

    public boolean isValid() {
        return error == null;
    }

    public String toJson() {
        if (!isValid()) {
            return "{\"error\":\"" + escape(error) + "\"}";
        }
        return "{\"user\":\"" + escape(user) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"message_count\":" + messageCount + ","
                + "\"word_count\":" + wordCount + ","
                + "\"is_spam\":" + spam + ","
                + "\"most_active\":\"" + escape(mostActive) + "\","
                + "\"fraud_risk\":\"" + escape(fraudRisk) + "\","
                + "\"fraud_reasons\":\"" + escape(fraudReasons) + "\"}";
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) escaped.append(String.format("\\u%04x", (int) character));
                    else escaped.append(character);
                }
            }
        }
        return escaped.toString();
    }
}
