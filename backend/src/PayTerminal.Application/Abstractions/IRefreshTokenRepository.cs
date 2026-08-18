using PayTerminal.Domain.Entities;

namespace PayTerminal.Application.Abstractions;

public interface IRefreshTokenRepository
{
    Task<RefreshToken?> GetActiveAsync(string token, CancellationToken ct = default);
    Task RevokeAllForUserAsync(Guid userId, CancellationToken ct = default);
    void Add(RefreshToken refreshToken);
}
