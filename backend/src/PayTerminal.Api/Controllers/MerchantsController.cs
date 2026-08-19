using Microsoft.AspNetCore.Authorization;
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
    private readonly MerchantsService _merchantsService;

    public MerchantsController(AuthService authService, MerchantsService merchantsService)
    {
        _authService = authService;
        _merchantsService = merchantsService;
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

    [Authorize]
    [HttpGet("{id:guid}")]
    [ProducesResponseType(typeof(MerchantResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<MerchantResponse>> Get(Guid id, CancellationToken ct)
    {
        return Ok(await _merchantsService.GetMerchantAsync(id, ct));
    }
}