namespace PayTerminal.Domain.Enums;

public enum PaymentMethod
{
    Card,
    Qr,
    EWallet
}

public enum TransactionStatus
{
    Initiated,
    Pending,
    Processing,
    Success,
    Failed,
    Cancelled
}

public enum AttemptStatus
{
    Pending,
    Processing,
    Success,
    Failed
}

public enum RefundStatus
{
    Requested,
    Processing,
    Completed,
    Rejected
}

public enum TerminalStatus
{
    Paired,
    Active,
    Offline
}

public enum UserRole
{
    Owner,
    Cashier
}
