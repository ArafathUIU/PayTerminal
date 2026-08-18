using System.ComponentModel.DataAnnotations;

namespace PayTerminal.Application.DTOs.Requests;

public class RefreshRequest
{
    [Required]
    public string RefreshToken { get; set; } = string.Empty;
}