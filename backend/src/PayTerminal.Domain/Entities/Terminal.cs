using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class Terminal : Entity
{
    public Guid MerchantId { get; private set; }
    public Merchant Merchant { get; private set; } = null!;
    public string Code { get; private set; } = null!;
    public string Name { get; private set; } = null!;
    public string? PairingCode { get; private set; }
    public TerminalStatus Status { get; private set; } = TerminalStatus.Paired;
    public DateTimeOffset? PairedAt { get; private set; }
    public DateTimeOffset? LastHeartbeatAt { get; private set; }

    public ICollection<Transaction> Transactions { get; private set; } = new List<Transaction>();

    private Terminal() { }

    public Terminal(Guid merchantId, string code, string name, string pairingCode)
    {
        MerchantId = merchantId;
        Code = code;
        Name = name;
        PairingCode = pairingCode;
        PairedAt = DateTimeOffset.UtcNow;
    }

    public void Activate()
    {
        Status = TerminalStatus.Active;
        PairingCode = null;
        Touch();
    }

    public void RegisterHeartbeat()
    {
        LastHeartbeatAt = DateTimeOffset.UtcNow;
        if (Status == TerminalStatus.Paired)
        {
            Status = TerminalStatus.Active;
        }
        Touch();
    }

    public void MarkOffline()
    {
        Status = TerminalStatus.Offline;
        Touch();
    }
}
