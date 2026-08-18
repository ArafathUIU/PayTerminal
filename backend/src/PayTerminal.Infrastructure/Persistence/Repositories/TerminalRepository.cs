using Microsoft.EntityFrameworkCore;
using PayTerminal.Application.Abstractions;
using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure.Persistence.Repositories;

public class TerminalRepository : ITerminalRepository
{
    private readonly PayTerminalDbContext _db;

    public TerminalRepository(PayTerminalDbContext db)
    {
        _db = db;
    }

    public async Task<Terminal?> GetByIdAsync(Guid id, CancellationToken ct = default)
    {
        return await _db.Terminals
            .Include(t => t.Merchant)
            .SingleOrDefaultAsync(t => t.Id == id, ct);
    }

    public async Task<Terminal?> GetByPairingCodeAsync(
        Guid merchantId,
        string pairingCode,
        CancellationToken ct = default)
    {
        return await _db.Terminals
            .SingleOrDefaultAsync(
                t => t.MerchantId == merchantId
                    && t.PairingCode == pairingCode
                    && t.Status == TerminalStatus.Paired,
                ct);
    }

    public void Add(Terminal terminal) => _db.Terminals.Add(terminal);
}
