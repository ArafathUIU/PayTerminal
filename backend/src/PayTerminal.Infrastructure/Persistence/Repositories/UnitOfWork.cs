using PayTerminal.Application.Abstractions;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure.Persistence.Repositories;

public class UnitOfWork : IUnitOfWork
{
    private readonly PayTerminalDbContext _db;

    public UnitOfWork(PayTerminalDbContext db)
    {
        _db = db;
    }

    public Task<int> SaveChangesAsync(CancellationToken ct = default)
        => _db.SaveChangesAsync(ct);
}
