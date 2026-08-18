namespace PayTerminal.Domain.Entities;

public class RefreshToken : Entity
{
    public Guid UserId { get; private set; }
    public User User { get; private set; } = null!;
    public string Token { get; private set; } = null!;
    public DateTimeOffset ExpiresAt { get; private set; }
    public bool Revoked { get; private set; }
    public DateTimeOffset? RevokedAt { get; private set; }

    private RefreshToken() { }

    public RefreshToken(Guid userId, string token, DateTimeOffset expiresAt)
    {
        UserId = userId;
        Token = token;
        ExpiresAt = expiresAt;
    }

    public bool IsExpired(DateTimeOffset now) => now >= ExpiresAt;

    public void Revoke(DateTimeOffset now)
    {
        Revoked = true;
        RevokedAt = now;
        Touch();
    }
}
