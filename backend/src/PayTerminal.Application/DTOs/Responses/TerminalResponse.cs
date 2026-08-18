namespace PayTerminal.Application.DTOs.Responses;

public class MerchantRegistrationResponse
{
    public Guid MerchantId { get; set; }
    public string BusinessName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
}

public class TerminalResponse
{
    public Guid Id { get; set; }
    public Guid MerchantId { get; set; }
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string? PairingCode { get; set; }
    public string Status { get; set; } = string.Empty;
    public DateTimeOffset? PairedAt { get; set; }
    public DateTimeOffset? LastHeartbeatAt { get; set; }
}