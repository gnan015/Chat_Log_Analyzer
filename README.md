# Chat Log Analyzer

A Java 17 chat-log analyzer with a Bootstrap-powered browser interface and a built-in Java HTTP backend.

Supported input format:

```text
[HH:MM] Name: Message
```

For every valid message, the analyzer reports the sender, cumulative message count, word count, duplicate-message spam status, and the current most active user. Ties for most active user are resolved alphabetically.

## Run the website

```powershell
mvn package
java -jar target/chat-log-analyzer-1.0.4.jar
```

Then open `http://localhost:8080` in a browser. If port 8080 is already occupied, choose another port, such as `java -jar target/chat-log-analyzer-1.0.4.jar 8081`, then open `http://localhost:8081`. Paste chat messages, or use the included sample, and select **Analyze chat**. The frontend uses HTML, CSS, JavaScript, and Bootstrap; the Java backend serves the page and processes `POST /api/analyze` requests.

## Import a WhatsApp chat export

In WhatsApp, export a chat **without media** and save the `.txt` file. On the website, choose the file using **Chat export file**, then select **Analyze chat**. Common bracketed and dashed WhatsApp export formats are converted automatically; multi-line messages are combined with their previous message.

## Run the command-line version

```powershell
@'
[09:00] Alice: Hello team
[09:01] Bob: Hi Alice
[09:02] Alice: Hello team
'@ | java -cp target/classes com.videep.chatlog.Main
```

Blank lines are ignored. Invalid lines are returned as JSON objects with an `error` field; processing continues.

## Project layout

- `src/main/java/com/videep/chatlog/Main.java` - console entry point
- `src/main/java/com/videep/chatlog/WebServer.java` - HTTP server and API
- `src/main/java/com/videep/chatlog/ChatLogAnalyzer.java` - parsing and analytics logic
- `src/main/java/com/videep/chatlog/AnalysisResult.java` - immutable result model and JSON serialization
- `src/main/resources/static/` - HTML, CSS, JavaScript, and Bootstrap interface
- `src/test/java/com/videep/chatlog/ChatLogAnalyzerTest.java` - lightweight automated checks
