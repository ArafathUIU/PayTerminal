using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PayTerminal.Application.DTOs.Requests;
using PayTerminal.Application.DTOs.Responses;
using PayTerminal.Application.Services;

namespace PayTerminal.Api.Controllers;

[ApiController]
[Route("api/v1/terminals")]
public class TerminalsController : ControllerBase
{
    private readonly TerminalService _terminalService;

    public TerminalsController(TerminalService terminalService)
    {
        _terminalService = terminalService;
    }

    [HttpPost("register")]
    [ProducesResponseType(typeof(TerminalResponse), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<ActionResult<TerminalResponse>> Register(
        [FromBody] RegisterTerminalRequest request,
        CancellationToken ct)
    {
        var result = await _terminalService.RegisterTerminalAsync(request, ct);
        return CreatedAtAction(nameof(Get), new { id = result.Id }, result);
    }

    [Authorize]
    [HttpGet("{id:guid}")]
    [ProducesResponseType(typeof(TerminalResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<TerminalResponse>> Get(Guid id, CancellationToken ct)
    {
        return Ok(await _terminalService.GetTerminalAsync(id, ct));
    }

    [Authorize]
    [HttpPost("{id:guid}/heartbeat")]
    [ProducesResponseType(typeof(TerminalResponse), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<TerminalResponse>> Heartbeat(Guid id, CancellationToken ct)
    {
        return Ok(await _terminalService.RegisterHeartbeatAsync(id, ct));
    }
}