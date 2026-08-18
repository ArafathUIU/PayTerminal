using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.StateMachines;

public static class RefundStateMachine
{
    private static readonly Dictionary<RefundStatus, RefundStatus[]> Allowed =
        new()
        {
            [RefundStatus.Requested] = new[] { RefundStatus.Processing },
            [RefundStatus.Processing] = new[] { RefundStatus.Completed, RefundStatus.Rejected },
            [RefundStatus.Completed] = Array.Empty<RefundStatus>(),
            [RefundStatus.Rejected] = Array.Empty<RefundStatus>()
        };

    public static bool CanTransition(RefundStatus from, RefundStatus to) =>
        Allowed.TryGetValue(from, out var targets) && targets.Contains(to);

    public static void Transition(Refund refund, RefundStatus to)
    {
        if (!CanTransition(refund.Status, to))
        {
            throw new InvalidOperationException(
                $"Invalid refund transition {refund.Status} -> {to}");
        }

        switch (to)
        {
            case RefundStatus.Processing:
                refund.StartProcessing();
                break;
            case RefundStatus.Completed:
                refund.Complete();
                break;
            case RefundStatus.Rejected:
                refund.Reject();
                break;
        }
    }
}
