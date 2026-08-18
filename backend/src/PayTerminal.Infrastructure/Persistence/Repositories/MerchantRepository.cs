using Microsoft.EntityFrameworkCore;
using PayTerminal.Application.Abstractions;
using PayTerminal.Domain.Entities;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure.Persistence.Repositories;

public class MerchantRepository : IMerchantRepository
{
    private readonly PayTerminalDbContext _db;

    public MerchantRepository(PayTerminalDbContext db)
    {
        _db = db;
    }

    public async Task<bool> ExistsByEmailAsync(string email, CancellationToken ct = default)
    {
        return await _db.Merchants.AnyAsync(m => m.Email == email, ct);
    }

    public async Task<Merchant?> GetByIdAsync(Guid id, CancellationToken ct = default)
    {
        return await _db.Merchants.FindAsync([id], ct);
    }

    public void Add(Merchant merchant) => _db.Merchants.Add(merchant);
}
