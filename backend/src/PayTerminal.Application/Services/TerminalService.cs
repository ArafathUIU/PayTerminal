using PayTerminal.Application.Abstractions;
using PayTerminal.Application.DTOs.Requests;
using PayTerminal.Application.DTOs.Responses;
using PayTerminal.Application.Exceptions;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Application.Services;

public class TerminalService
{
    private readonly IMerchantRepository _merchants;
    private readonly ITerminalRepository _terminals;
    private readonly IUnitOfWork _unitOfWork;

    public TerminalService(
        IMerchantRepository merchants,
        ITerminalRepository terminals,
        IUnitOfWork unitOfWork)
    {
        _merchants = merchants;
        _terminals = terminals;
        _unitOfWork = unitOfWork;
    }

    public async Task<TerminalResponse> RegisterTerminalAsync(
        RegisterTerminalRequest request,
        CancellationToken ct = default)
    {
        var merchant = await _merchants.GetByIdAsync(request.MerchantId, ct)
            ?? throw new ResourceNotFoundException("Merchant");

        var terminal = await _terminals.GetByPairingCodeAsync(
            merchant.Id,
            request.PairingCode,
            ct)
            ?? throw new InvalidPairingCodeException();

        terminal.Activate();
        await _unitOfWork.SaveChangesAsync(ct);

        return Map(terminal);
    }

    public async Task<TerminalResponse> GetTerminalAsync(Guid id, CancellationToken ct = default)
    {
        var terminal = await _terminals.GetByIdAsync(id, ct)
            ?? throw new ResourceNotFoundException("Terminal");

        return Map(terminal);
    }

    public async Task<TerminalResponse> RegisterHeartbeatAsync(Guid id, CancellationToken ct = default)
    {
        var terminal = await _terminals.GetByIdAsync(id, ct)
            ?? throw new ResourceNotFoundException("Terminal");

        terminal.RegisterHeartbeat();
        await _unitOfWork.SaveChangesAsync(ct);

        return Map(terminal);
    }

    private static TerminalResponse Map(Terminal terminal) => new()
    {
        Id = terminal.Id,
        MerchantId = terminal.MerchantId,
        Code = terminal.Code,
        Name = terminal.Name,
        PairingCode = terminal.PairingCode,
        Status = terminal.Status.ToString().ToUpperInvariant(),
        PairedAt = terminal.PairedAt,
        LastHeartbeatAt = terminal.LastHeartbeatAt
    };
}