package com.videep.chatlog;

/** Dependency-free checks runnable with `java ...ChatLogAnalyzerTest`. */
public final class ChatLogAnalyzerTest {
    public static void main(String[] args) {
        ChatLogAnalyzer analyzer = new ChatLogAnalyzer();

        AnalysisResult first = analyzer.process("[09:00] Alice: Hello team");
        check(first.isValid(), "first entry should be valid");
        check(first.messageCount() == 1 && first.wordCount() == 2, "counts should be calculated");
        check(!first.spam() && first.mostActive().equals("Alice"), "first entry analytics should match");

        AnalysisResult second = analyzer.process("[09:01] Bob: Welcome all");
        check(second.mostActive().equals("Alice"), "tie should select alphabetical user");

        AnalysisResult duplicate = analyzer.process("[09:02] Alice: Hello team");
        check(duplicate.spam() && duplicate.messageCount() == 2, "duplicate should be flagged and counted");
        check(duplicate.mostActive().equals("Alice"), "most active user should update");

        check(!analyzer.process("not a log").isValid(), "malformed lines should be rejected");
        check(!analyzer.process("[25:00] Alice: Invalid time").isValid(), "invalid times should be rejected");

        AnalysisResult fraud = analyzer.process("[10:00] Eve: Urgent! Verify your account at bit.ly/claim and send your OTP");
        check(fraud.fraudRisk().equals("HIGH"), "credential and link fraud indicators should be high risk");
        String whatsapp = ChatLogNormalizer.normalize("[18/06/2026, 9:15 PM] Alice: Hello from WhatsApp").get(0);
        check(whatsapp.equals("[21:15] Alice: Hello from WhatsApp"), "WhatsApp time should be normalized");
        check(new ChatLogAnalyzer().process(whatsapp).isValid(), "normalized WhatsApp entry should be analyzed");
        String unicodeSpace = ChatLogNormalizer.normalize("13/08/25, 1:48\u202Fpm - Santhosh: Rey").get(0);
        check(unicodeSpace.equals("[13:48] Santhosh: Rey"), "WhatsApp narrow-space timestamps should be normalized");
        check(ChatLogNormalizer.normalize("13/08/25, 1:48\u202Fpm - Messages are end-to-end encrypted.").isEmpty(), "system notices should be ignored");
        var codeMessage = ChatLogNormalizer.normalize("27/08/25, 3:00\u202Fpm - Gnaneswar Cse: #include <stdio.h>\nint main() { return 0; }");
        check(codeMessage.size() == 1 && new ChatLogAnalyzer().process(codeMessage.get(0)).isValid(), "multi-line code messages should remain one valid message");
        check(new ChatLogAnalyzer().process("[10:01] Dev: first\tsecond").toJson().contains("\\t"), "control characters should be JSON escaped");
        System.out.println("All checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
