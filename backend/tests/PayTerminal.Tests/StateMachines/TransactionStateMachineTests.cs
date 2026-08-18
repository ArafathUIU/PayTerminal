using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;
using PayTerminal.Domain.StateMachines;

namespace PayTerminal.Tests.StateMachines;

public class TransactionStateMachineTests
{
    private static Transaction NewTransaction() => new(
        "TXN-00001",
        "idempotency-key-1",
        Guid.NewGuid(),
        Guid.NewGuid(),
        Guid.NewGuid(),
        1250m,
        "BDT",
        PaymentMethod.Card);

    [Fact]
    public void SuccessfulPayment_FollowsValidPath()
    {
        var txn = NewTransaction();

        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.StartProcessing(txn);
        TransactionStateMachine.MarkSuccess(txn);

        Assert.Equal(TransactionStatus.Success, txn.Status);
    }

    [Fact]
    public void FailedPayment_ThenRetry_ReachesSuccess()
    {
        var txn = NewTransaction();

        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.StartProcessing(txn);
        TransactionStateMachine.MarkFailed(txn);
        Assert.Equal(TransactionStatus.Failed, txn.Status);

        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.StartProcessing(txn);
        TransactionStateMachine.MarkSuccess(txn);
        Assert.Equal(TransactionStatus.Success, txn.Status);
    }

    [Fact]
    public void PendingPayment_CanBeCancelled()
    {
        var txn = NewTransaction();

        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.Cancel(txn);

        Assert.Equal(TransactionStatus.Cancelled, txn.Status);
    }

    [Fact]
    public void CannotSkipFromInitiatedToSuccess()
    {
        var txn = NewTransaction();

        Assert.Throws<InvalidOperationException>(() =>
            TransactionStateMachine.MarkSuccess(txn));
    }

    [Fact]
    public void Success_IsTerminal()
    {
        var txn = NewTransaction();
        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.StartProcessing(txn);
        TransactionStateMachine.MarkSuccess(txn);

        Assert.Throws<InvalidOperationException>(() =>
            TransactionStateMachine.Cancel(txn));
    }

    [Fact]
    public void EveryTransition_AppendsAnAuditEvent()
    {
        var txn = NewTransaction();

        TransactionStateMachine.MoveToPending(txn);
        TransactionStateMachine.StartProcessing(txn);
        TransactionStateMachine.MarkSuccess(txn);

        Assert.Equal(3, txn.Events.Count);
        Assert.All(txn.Events, e => Assert.Equal("TRANSITION", e.EventType));
        Assert.Equal(TransactionStatus.Success, txn.Events.Last().ToStatus);
    }

    [Fact]
    public void CanTransition_ReflectsAllowedEdges()
    {
        Assert.True(TransactionStateMachine.CanTransition(TransactionStatus.Initiated, TransactionStatus.Pending));
        Assert.True(TransactionStateMachine.CanTransition(TransactionStatus.Pending, TransactionStatus.Cancelled));
        Assert.True(TransactionStateMachine.CanTransition(TransactionStatus.Processing, TransactionStatus.Failed));
        Assert.True(TransactionStateMachine.CanTransition(TransactionStatus.Failed, TransactionStatus.Pending));
        Assert.False(TransactionStateMachine.CanTransition(TransactionStatus.Success, TransactionStatus.Cancelled));
        Assert.False(TransactionStateMachine.CanTransition(TransactionStatus.Cancelled, TransactionStatus.Pending));
    }
}
