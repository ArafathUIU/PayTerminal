using PayTerminal.Domain.Entities;

namespace PayTerminal.Application.Abstractions;

public interface ITerminalRepository
{
    Task<Terminal?> GetByIdAsync(Guid id, CancellationToken ct = default);
    Task<Terminal?> GetByPairingCodeAsync(Guid merchantId, string pairingCode, CancellationToken ct = default);
    void Add(Terminal terminal);
}
