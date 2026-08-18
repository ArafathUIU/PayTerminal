using System.ComponentModel.DataAnnotations;

namespace PayTerminal.Application.DTOs.Requests;

public class RegisterTerminalRequest
{
    [Required]
    public Guid MerchantId { get; set; }

    [Required, StringLength(10)]
    public string PairingCode { get; set; } = string.Empty;

    [Required, MaxLength(120)]
    public string Name { get; set; } = string.Empty;
}