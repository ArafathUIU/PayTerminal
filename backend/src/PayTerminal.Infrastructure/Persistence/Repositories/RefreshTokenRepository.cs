using Microsoft.EntityFrameworkCore;
using PayTerminal.Application.Abstractions;
using PayTerminal.Domain.Entities;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure.Persistence.Repositories;

public class RefreshTokenRepository : IRefreshTokenRepository
{
    private readonly PayTerminalDbContext _db;

    public RefreshTokenRepository(PayTerminalDbContext db)
    {
        _db = db;
    }

    public async Task<RefreshToken?> GetActiveAsync(string token, CancellationToken ct = default)
    {
        return await _db.RefreshTokens
            .Include(r => r.User)
            .SingleOrDefaultAsync(
                r => r.Token == token && !r.Revoked && r.ExpiresAt > DateTimeOffset.UtcNow,
                ct);
    }

    public async Task RevokeAllForUserAsync(Guid userId, CancellationToken ct = default)
    {
        var active = await _db.RefreshTokens
            .Where(r => r.UserId == userId && !r.Revoked)
            .ToListAsync(ct);

        foreach (var token in active)
        {
            token.Revoke(DateTimeOffset.UtcNow);
        }
    }

    public void Add(RefreshToken refreshToken) => _db.RefreshTokens.Add(refreshToken);
}
