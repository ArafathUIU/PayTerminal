using PayTerminal.Domain.Enums;

namespace PayTerminal.Domain.Entities;

public class User : Entity
{
    public Guid MerchantId { get; private set; }
    public Merchant Merchant { get; private set; } = null!;
    public string Name { get; private set; } = null!;
    public string Email { get; private set; } = null!;
    public string PasswordHash { get; private set; } = null!;
    public UserRole Role { get; private set; }
    public bool Active { get; private set; } = true;

    public ICollection<RefreshToken> RefreshTokens { get; private set; } = new List<RefreshToken>();
    public ICollection<Transaction> Transactions { get; private set; } = new List<Transaction>();

    private User() { }

    public User(Guid merchantId, string name, string email, string passwordHash, UserRole role)
    {
        MerchantId = merchantId;
        Name = name;
        Email = email;
        PasswordHash = passwordHash;
        Role = role;
    }

    public void SetPasswordHash(string passwordHash) => PasswordHash = passwordHash;
}
