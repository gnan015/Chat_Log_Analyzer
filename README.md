# Chat Log Analyzer

A Java 17 web application that analyzes chat conversations for user activity, repeated-message spam, and common fraud indicators. It accepts pasted chat logs or exported WhatsApp `.txt` files and presents the results in a simple browser dashboard.

## Features

- Import WhatsApp chat exports or paste chat messages directly.
- Convert supported WhatsApp timestamp formats automatically.
- Combine multi-line messages, including messages that contain source code.
- Count messages per user and calculate words in each message.
- Detect consecutive identical messages from the same sender as spam.
- Flag common fraud indicators, including suspicious links, OTP/PIN/password requests, urgency, account threats, prize claims, UPI, and payment requests.
- Show the message, fraud-risk level, and detection reason in the results table.
- Filter the table to show only spam or fraud alerts.

> Fraud detection is rule-based screening. It highlights suspicious messages for review; it does not guarantee that a message is fraudulent.

## Technology Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17 and the built-in Java HTTP server |
| Frontend | HTML, CSS, JavaScript, Bootstrap 5 |
| Build tool | Maven |
| Input | Pasted chat logs and WhatsApp `.txt` exports |

## Requirements

- Java Development Kit (JDK) 17 or newer
- Apache Maven 3.8 or newer

Check your installation:

```cmd
java -version
mvn -version
```

## Run the Website

From the project folder:

```cmd
mvn package
java -jar target\chat-log-analyzer-1.0.5.jar 8081
```

Open [http://localhost:8081](http://localhost:8081) in a browser.

If port `8081` is unavailable, choose another unused port:

```cmd
java -jar target\chat-log-analyzer-1.0.5.jar 8090
```

Then open [http://localhost:8090](http://localhost:8090).

To stop the server, return to the terminal and press `Ctrl + C`.

## Use a WhatsApp Export

1. Open the required chat in WhatsApp.
2. Select **More** → **Export chat**.
3. Choose **Without media**.
4. Save the exported `.txt` file.
5. Open the Chat Log Analyzer website.
6. Select the file under **Chat export file**.
7. Click **Analyze chat**.

The application supports common WhatsApp formats such as:

```text
01/09/25, 10:00 am - Alice: Good morning team
[01/09/2025, 10:00 AM] Alice: Good morning team
```

It also accepts the simple format below when messages are pasted manually:

```text
[10:00] Alice: Good morning team
```

WhatsApp system notices, such as end-to-end encryption messages, are ignored.

## Example Test Data

Copy this into a `.txt` file or paste it into the application:

```text
01/09/25, 10:00 am - Alice: Good morning team
01/09/25, 10:01 am - Bob: Meeting starts at 4 PM
01/09/25, 10:02 am - Bob: Meeting starts at 4 PM
01/09/25, 10:03 am - Alice: Please complete your assigned task
01/09/25, 10:04 am - Eve: Urgent! Your bank account will be suspended. Verify at https://bit.ly/verify-now and send your OTP immediately.
01/09/25, 10:05 am - Eve: Congratulations! You won a free prize. Send money via UPI to claim your reward.
```

Expected results:

- Bob's second “Meeting starts at 4 PM” message is marked as **Spam Detected**.
- Eve's OTP message is marked **High** fraud risk.
- Eve's prize and UPI message is marked **Medium** fraud risk.

## Detection Rules

| Detection | Rule |
| --- | --- |
| Spam | The same user sends the exact same message consecutively. |
| Low fraud risk | One weak indicator, such as a suspicious link. |
| Medium fraud risk | Multiple suspicious indicators, such as prize claims and payment requests. |
| High fraud risk | Strong combined indicators, such as a link plus an OTP/password request and urgency. |

## Run the Command-Line Version

The command-line mode accepts the simple `[HH:MM] Name: Message` format:

```cmd
echo [09:00] Alice: Hello team | java -cp target\classes com.videep.chatlog.Main
```

## Project Structure

```text
Chat_Log_Analyzer/
├── src/
│   ├── main/
│   │   ├── java/com/videep/chatlog/
│   │   │   ├── WebServer.java            # Website server and API
│   │   │   ├── ChatLogNormalizer.java    # WhatsApp export conversion
│   │   │   ├── ChatLogAnalyzer.java      # Spam and fraud analysis
│   │   │   ├── AnalysisResult.java       # JSON response model
│   │   │   └── Main.java                 # Command-line entry point
│   │   └── resources/static/             # Browser interface files
│   └── test/java/com/videep/chatlog/     # Automated checks
├── docs/diagrams/                        # Project diagrams
├── pom.xml
└── README.md
```

## Documentation Diagrams

- [System Architecture](docs/diagrams/system-architecture.svg)
- [Project Workflow](docs/diagrams/project-workflow.svg)
- [Spam and Fraud Detection Logic](docs/diagrams/detection-logic.svg)

## Verify the Project

```cmd
mvn package
java -cp "target\test-classes;target\classes" com.videep.chatlog.ChatLogAnalyzerTest
```

Successful verification prints:

```text
All checks passed.
```
