using Microsoft.AspNetCore.Mvc;
using PayTerminal.Application.DTOs.Requests;
using PayTerminal.Application.DTOs.Responses;
using PayTerminal.Application.Services;

namespace PayTerminal.Api.Controllers;

[ApiController]
[Route("api/v1/merchants")]
public class MerchantsController : ControllerBase
{
    private readonly AuthService _authService;

    public MerchantsController(AuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register")]
    [ProducesResponseType(typeof(MerchantRegistrationResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<ActionResult<MerchantRegistrationResponse>> Register(
        [FromBody] RegisterMerchantRequest request,
        CancellationToken ct)
    {
        var result = await _authService.RegisterMerchantAsync(request, ct);
        return CreatedAtAction(nameof(Register), result);
    }
}