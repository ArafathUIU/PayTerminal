using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.StateMachines;

public static class TransactionStateMachine
{
    private static readonly Dictionary<TransactionStatus, TransactionStatus[]> Allowed =
        new()
        {
            [TransactionStatus.Initiated] = new[] { TransactionStatus.Pending },
            [TransactionStatus.Pending] = new[] { TransactionStatus.Processing, TransactionStatus.Cancelled },
            [TransactionStatus.Processing] = new[] { TransactionStatus.Success, TransactionStatus.Failed },
            [TransactionStatus.Failed] = new[] { TransactionStatus.Pending },
            [TransactionStatus.Success] = Array.Empty<TransactionStatus>(),
            [TransactionStatus.Cancelled] = Array.Empty<TransactionStatus>()
        };

    public static bool CanTransition(TransactionStatus from, TransactionStatus to) =>
        Allowed.TryGetValue(from, out var targets) && targets.Contains(to);

    public static void Transition(Transaction transaction, TransactionStatus to)
    {
        var from = transaction.Status;
        if (!CanTransition(from, to))
        {
            throw new InvalidOperationException(
                $"Invalid transaction transition {from} -> {to}");
        }

        transaction.ApplyStatus(to);
        transaction.AddEvent("TRANSITION", from, to);
        transaction.Touch();
    }

    public static void MoveToPending(Transaction transaction) =>
        Transition(transaction, TransactionStatus.Pending);

    public static void StartProcessing(Transaction transaction) =>
        Transition(transaction, TransactionStatus.Processing);

    public static void MarkSuccess(Transaction transaction)
    {
        Transition(transaction, TransactionStatus.Success);
        transaction.ApplyProcessedAt(DateTimeOffset.UtcNow);
    }

    public static void MarkFailed(Transaction transaction)
    {
        Transition(transaction, TransactionStatus.Failed);
        transaction.ApplyProcessedAt(DateTimeOffset.UtcNow);
    }

    public static void Cancel(Transaction transaction) =>
        Transition(transaction, TransactionStatus.Cancelled);
}
