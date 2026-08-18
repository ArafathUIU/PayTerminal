using PayTerminal.Domain.Entities;

namespace PayTerminal.Application.Abstractions;

public interface IUserRepository
{
    Task<User?> GetByEmailAsync(string email, CancellationToken ct = default);
    void Add(User user);
}
