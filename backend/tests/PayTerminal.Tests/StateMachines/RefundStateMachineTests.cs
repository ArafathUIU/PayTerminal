using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;
using PayTerminal.Domain.StateMachines;

namespace PayTerminal.Tests.StateMachines;

public class RefundStateMachineTests
{
    private static Refund NewRefund() => new("REF-00001", Guid.NewGuid(), 1250m, "customer returned item");

    [Fact]
    public void SuccessfulRefund_FollowsValidPath()
    {
        var refund = NewRefund();

        RefundStateMachine.Transition(refund, RefundStatus.Processing);
        RefundStateMachine.Transition(refund, RefundStatus.Completed);

        Assert.Equal(RefundStatus.Completed, refund.Status);
        Assert.NotNull(refund.ProcessedAt);
    }

    [Fact]
    public void RejectedRefund_IsTerminal()
    {
        var refund = NewRefund();

        RefundStateMachine.Transition(refund, RefundStatus.Processing);
        RefundStateMachine.Transition(refund, RefundStatus.Rejected);

        Assert.Equal(RefundStatus.Rejected, refund.Status);
        Assert.Throws<InvalidOperationException>(() =>
            RefundStateMachine.Transition(refund, RefundStatus.Completed));
    }

    [Fact]
    public void CannotCompleteFromRequested()
    {
        var refund = NewRefund();

        Assert.Throws<InvalidOperationException>(() =>
            RefundStateMachine.Transition(refund, RefundStatus.Completed));
    }

    [Fact]
    public void CanTransition_ReflectsAllowedEdges()
    {
        Assert.True(RefundStateMachine.CanTransition(RefundStatus.Requested, RefundStatus.Processing));
        Assert.True(RefundStateMachine.CanTransition(RefundStatus.Processing, RefundStatus.Completed));
        Assert.True(RefundStateMachine.CanTransition(RefundStatus.Processing, RefundStatus.Rejected));
        Assert.False(RefundStateMachine.CanTransition(RefundStatus.Rejected, RefundStatus.Requested));
        Assert.False(RefundStateMachine.CanTransition(RefundStatus.Completed, RefundStatus.Processing));
    }
}
