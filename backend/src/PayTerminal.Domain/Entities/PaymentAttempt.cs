using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class PaymentAttempt : Entity
{
    public Guid TransactionId { get; private set; }
    public Transaction Transaction { get; private set; } = null!;
    public int AttemptNumber { get; private set; }
    public PaymentMethod Method { get; private set; }
    public string? MaskedReference { get; private set; }
    public string? ProcessorReference { get; private set; }
    public AttemptStatus Status { get; private set; } = AttemptStatus.Pending;
    public string? ErrorCode { get; private set; }
    public string? ErrorMessage { get; private set; }
    public DateTimeOffset? ProcessedAt { get; private set; }

    private PaymentAttempt() { }

    public PaymentAttempt(Guid transactionId, int attemptNumber, PaymentMethod method, string? maskedReference)
    {
        TransactionId = transactionId;
        AttemptNumber = attemptNumber;
        Method = method;
        MaskedReference = maskedReference;
    }

    public void StartProcessing()
    {
        Status = AttemptStatus.Processing;
        Touch();
    }

    public void MarkSuccess(string processorReference)
    {
        Status = AttemptStatus.Success;
        ProcessorReference = processorReference;
        ProcessedAt = DateTimeOffset.UtcNow;
        Touch();
    }

    public void MarkFailed(string? errorCode, string? errorMessage)
    {
        Status = AttemptStatus.Failed;
        ErrorCode = errorCode;
        ErrorMessage = errorMessage;
        ProcessedAt = DateTimeOffset.UtcNow;
        Touch();
    }
}
