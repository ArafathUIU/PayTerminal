using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class Transaction : Entity
{
    public string TransactionNumber { get; private set; } = null!;
    public string IdempotencyKey { get; private set; } = null!;
    public Guid MerchantId { get; private set; }
    public Merchant Merchant { get; private set; } = null!;
    public Guid TerminalId { get; private set; }
    public Terminal Terminal { get; private set; } = null!;
    public Guid UserId { get; private set; }
    public User User { get; private set; } = null!;
    public decimal Amount { get; private set; }
    public string Currency { get; private set; } = null!;
    public PaymentMethod PaymentMethod { get; private set; }
    public TransactionStatus Status { get; private set; } = TransactionStatus.Initiated;
    public DateTimeOffset? ProcessedAt { get; private set; }
    public DateTimeOffset? RefundedAt { get; private set; }
    public decimal? RefundedAmount { get; private set; }

    public ICollection<PaymentAttempt> Attempts { get; private set; } = new List<PaymentAttempt>();
    public ICollection<TransactionEvent> Events { get; private set; } = new List<TransactionEvent>();
    public ICollection<Refund> Refunds { get; private set; } = new List<Refund>();

    private Transaction() { }

    public Transaction(
        string transactionNumber,
        string idempotencyKey,
        Guid merchantId,
        Guid terminalId,
        Guid userId,
        decimal amount,
        string currency,
        PaymentMethod paymentMethod)
    {
        TransactionNumber = transactionNumber;
        IdempotencyKey = idempotencyKey;
        MerchantId = merchantId;
        TerminalId = terminalId;
        UserId = userId;
        Amount = amount;
        Currency = currency;
        PaymentMethod = paymentMethod;
    }

    internal void ApplyStatus(TransactionStatus status) => Status = status;

    internal void ApplyProcessedAt(DateTimeOffset processedAt) => ProcessedAt = processedAt;

    public void AddEvent(string eventType, TransactionStatus? from, TransactionStatus? to, string? payload = null)
    {
        Events.Add(new TransactionEvent(Id, eventType, from, to, payload));
    }

    public void MarkRefunded(decimal amount)
    {
        RefundedAt = DateTimeOffset.UtcNow;
        RefundedAmount = amount;
        Touch();
    }
}
