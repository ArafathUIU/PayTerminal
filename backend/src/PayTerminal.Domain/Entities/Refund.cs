using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class Refund : Entity
{
    public string RefundNumber { get; private set; } = null!;
    public Guid TransactionId { get; private set; }
    public Transaction Transaction { get; private set; } = null!;
    public decimal Amount { get; private set; }
    public string? Reason { get; private set; }
    public RefundStatus Status { get; private set; } = RefundStatus.Requested;
    public DateTimeOffset? ProcessedAt { get; private set; }

    private Refund() { }

    public Refund(string refundNumber, Guid transactionId, decimal amount, string? reason)
    {
        RefundNumber = refundNumber;
        TransactionId = transactionId;
        Amount = amount;
        Reason = reason;
    }

    public void StartProcessing()
    {
        Status = RefundStatus.Processing;
        Touch();
    }

    public void Complete()
    {
        Status = RefundStatus.Completed;
        ProcessedAt = DateTimeOffset.UtcNow;
        Touch();
    }

    public void Reject()
    {
        Status = RefundStatus.Rejected;
        ProcessedAt = DateTimeOffset.UtcNow;
        Touch();
    }
}
