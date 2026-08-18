using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class TransactionEvent : Entity
{
    public Guid TransactionId { get; private set; }
    public Transaction Transaction { get; private set; } = null!;
    public string EventType { get; private set; } = null!;
    public TransactionStatus? FromStatus { get; private set; }
    public TransactionStatus? ToStatus { get; private set; }
    public string? Payload { get; private set; }

    private TransactionEvent() { }

    public TransactionEvent(
        Guid transactionId,
        string eventType,
        TransactionStatus? fromStatus,
        TransactionStatus? toStatus,
        string? payload)
    {
        TransactionId = transactionId;
        EventType = eventType;
        FromStatus = fromStatus;
        ToStatus = toStatus;
        Payload = payload;
    }
}
