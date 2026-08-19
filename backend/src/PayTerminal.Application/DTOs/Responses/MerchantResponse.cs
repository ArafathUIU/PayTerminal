namespace PayTerminal.Application.DTOs.Responses;

public class MerchantResponse
{
    public Guid MerchantId { get; set; }
    public string BusinessName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Phone { get; set; }
}
