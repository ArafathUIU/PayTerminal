using PayTerminal.Application.Abstractions;
using PayTerminal.Application.DTOs.Responses;
using PayTerminal.Application.Exceptions;

namespace PayTerminal.Application.Services;

public class MerchantsService
{
    private readonly IMerchantRepository _merchants;

    public MerchantsService(IMerchantRepository merchants)
    {
        _merchants = merchants;
    }

    public async Task<MerchantResponse> GetMerchantAsync(Guid id, CancellationToken ct = default)
    {
        var merchant = await _merchants.GetByIdAsync(id, ct)
            ?? throw new ResourceNotFoundException("Merchant");

        return new MerchantResponse
        {
            MerchantId = merchant.Id,
            BusinessName = merchant.BusinessName,
            Email = merchant.Email,
            Phone = merchant.Phone
        };
    }
}
