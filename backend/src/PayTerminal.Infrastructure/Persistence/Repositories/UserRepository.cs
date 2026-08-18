using Microsoft.EntityFrameworkCore;
using PayTerminal.Application.Abstractions;
using PayTerminal.Domain.Entities;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure.Persistence.Repositories;

public class UserRepository : IUserRepository
{
    private readonly PayTerminalDbContext _db;

    public UserRepository(PayTerminalDbContext db)
    {
        _db = db;
    }

    public async Task<User?> GetByEmailAsync(string email, CancellationToken ct = default)
    {
        return await _db.Users
            .Include(u => u.Merchant)
            .SingleOrDefaultAsync(u => u.Email == email, ct);
    }

    public void Add(User user) => _db.Users.Add(user);
}
