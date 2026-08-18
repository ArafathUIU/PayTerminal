using Microsoft.AspNetCore.Identity;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Tests.Auth;

public class PasswordHasherTests
{
    private readonly PasswordHasher<User> _hasher = new();

    [Fact]
    public void Hash_ThenVerify_CorrectPasswordSucceeds()
    {
        var hash = _hasher.HashPassword(null!, "password123");

        Assert.NotEqual("password123", hash);
        Assert.Equal(PasswordVerificationResult.Success,
            _hasher.VerifyHashedPassword(null!, hash, "password123"));
    }

    [Fact]
    public void Hash_ThenVerify_WrongPasswordFails()
    {
        var hash = _hasher.HashPassword(null!, "password123");

        Assert.Equal(PasswordVerificationResult.Failed,
            _hasher.VerifyHashedPassword(null!, hash, "wrong-password"));
    }

    [Fact]
    public void Hashes_AreUniquePerCall()
    {
        var hash1 = _hasher.HashPassword(null!, "password123");
        var hash2 = _hasher.HashPassword(null!, "password123");

        Assert.NotEqual(hash1, hash2);
    }
}
