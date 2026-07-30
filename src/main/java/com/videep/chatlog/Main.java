package com.videep.chatlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Reads chat lines from standard input and emits a JSON array. */
public final class Main {
    private Main() { }

    public static void main(String[] args) throws IOException {
        ChatLogAnalyzer analyzer = new ChatLogAnalyzer();
        List<String> results = new ArrayList<>();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = input.readLine()) != null) {
                if (!line.isBlank()) {
                    results.add(analyzer.process(line).toJson());
                }
            }
        }
        System.out.println("[" + String.join(",", results) + "]");
    }
}
