using PayTerminal.Domain.Entities;

namespace PayTerminal.Application.Abstractions;

public interface IMerchantRepository
{
    Task<bool> ExistsByEmailAsync(string email, CancellationToken ct = default);
    Task<Merchant?> GetByIdAsync(Guid id, CancellationToken ct = default);
    void Add(Merchant merchant);
}
