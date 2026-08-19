using PayTerminal.Application.Abstractions;
using PayTerminal.Application.Exceptions;
using PayTerminal.Application.Services;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Tests.Services;

public class MerchantsServiceTests
{
    private sealed class TestMerchant : Merchant
    {
        public TestMerchant(Guid id, string name, string businessName, string email, string? phone)
            : base(name, businessName, email, phone)
        {
            Id = id;
        }
    }

    private sealed class FakeMerchantRepository : IMerchantRepository
    {
        private readonly Merchant? _merchant;

        public FakeMerchantRepository(Merchant? merchant)
        {
            _merchant = merchant;
        }

        public Task<bool> ExistsByEmailAsync(string email, CancellationToken ct = default)
            => Task.FromResult(_merchant != null && _merchant.Email == email);

        public Task<Merchant?> GetByIdAsync(Guid id, CancellationToken ct = default)
            => Task.FromResult(_merchant);

        public void Add(Merchant merchant)
        {
        }
    }

    [Fact]
    public async Task GetMerchantAsync_ExistingMerchant_ReturnsMappedResponse()
    {
        var id = Guid.NewGuid();
        var merchant = new TestMerchant(id, "Farhana Rahman", "Dhaka Electronics", "farhana@example.com", "01712345678");
        var service = new MerchantsService(new FakeMerchantRepository(merchant));

        var result = await service.GetMerchantAsync(id);

        Assert.Equal(id, result.MerchantId);
        Assert.Equal("Dhaka Electronics", result.BusinessName);
        Assert.Equal("farhana@example.com", result.Email);
        Assert.Equal("01712345678", result.Phone);
    }

    [Fact]
    public async Task GetMerchantAsync_MissingMerchant_ThrowsNotFound()
    {
        var service = new MerchantsService(new FakeMerchantRepository(null));

        await Assert.ThrowsAsync<ResourceNotFoundException>(() =>
            service.GetMerchantAsync(Guid.NewGuid()));
    }
}
