from datetime import datetime, timedelta
from pathlib import Path


OUTPUT = Path(__file__).resolve().parents[1] / "sample-data" / "customer-support-group-10000.txt"
TOTAL_MESSAGES = 10_000

customers = ["Ananya", "Ravi", "Priya", "Karthik", "Meera", "Arjun", "Sneha", "Vikram"]
agents = ["Support - Neha", "Support - Rahul", "Support - Aisha", "Support - Daniel"]
topics = [
    ("order #{} has not arrived yet", "Your order #{} is being checked with the delivery team."),
    ("I need an update on refund request #{}", "Refund request #{} is under review and we will update you shortly."),
    ("the payment for order #{} shows as pending", "We have recorded the pending payment for order #{} and are checking it."),
    ("the item in order #{} arrived damaged", "We are sorry about order #{}. A replacement request has been created."),
    ("I need help changing the delivery address for order #{}", "Address changes for order #{} are possible before dispatch. We are checking its status."),
    ("the tracking page for order #{} has not changed", "The tracking information for order #{} may take a few hours to refresh."),
    ("I received the wrong item for order #{}", "We have opened a wrong-item report for order #{} and will arrange the next step."),
    ("can I cancel order #{}", "We are checking whether order #{} can still be cancelled."),
]

acknowledgements = [
    "Thank you for the update.",
    "Okay, please keep me informed.",
    "Thanks, I will wait for the next update.",
    "I understand. Please let me know once it is resolved.",
]


def line(timestamp: datetime, name: str, message: str) -> str:
    hour = timestamp.hour % 12 or 12
    suffix = "am" if timestamp.hour < 12 else "pm"
    return f"{timestamp:%d/%m/%y}, {hour}:{timestamp:%M} {suffix} - {name}: {message}"


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    current = datetime(2026, 8, 1, 9, 0)
    messages = []

    alert_messages = [
        ("Unknown Contact", "Urgent! Your bank account will be suspended. Verify at https://bit.ly/verify-now and send your OTP immediately."),
        ("Unknown Contact", "Urgent! Your bank account will be suspended. Verify at https://bit.ly/verify-now and send your OTP immediately."),
        ("Support - Neha", "Security notice: Use only the official help center for account assistance."),
        ("Prize Alerts", "Congratulations! You won a free prize. Send money via UPI to claim your reward."),
        ("Prize Alerts", "Congratulations! You won a free prize. Send money via UPI to claim your reward."),
        ("Support - Rahul", "Please ignore unverified requests in the group and contact official support."),
        ("Unknown Contact", "Limited time offer: click https://tinyurl.com/account-help to avoid account suspension."),
        ("Unknown Contact", "Limited time offer: click https://tinyurl.com/account-help to avoid account suspension."),
        ("Meera", "I received a suspicious link and did not open it."),
        ("Support - Aisha", "Thank you for reporting it. We have recorded the suspicious-message report."),
        ("Fraud Check", "Share your card number, CVV, and PIN now to receive an instant refund."),
        ("Support - Daniel", "Reminder: contact the official help center if you receive an unexpected message."),
    ]

    for index in range(TOTAL_MESSAGES - len(alert_messages)):
        customer = customers[(index // 3) % len(customers)]
        agent = agents[(index // 3) % len(agents)]
        topic_index = (index // 3) % len(topics)
        customer_text, agent_text = topics[topic_index]
        order_id = 10001 + ((index // 3) % 1800)
        phase = index % 3

        if phase == 0:
            sender = customer
            message = customer_text.format(order_id)
        elif phase == 1:
            sender = agent
            message = agent_text.format(order_id)
        else:
            sender = customer
            message = acknowledgements[(index // 3) % len(acknowledgements)]

        messages.append(line(current, sender, message))
        current += timedelta(minutes=2 + (index % 4))

    for sender, message in alert_messages:
        messages.append(line(current, sender, message))
        current += timedelta(minutes=3)

    OUTPUT.write_text("\n".join(messages) + "\n", encoding="utf-8")
    print(f"Created {OUTPUT} with {len(messages)} messages.")


if __name__ == "__main__":
    main()
