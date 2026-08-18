using System.Security.Cryptography;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Options;
using PayTerminal.Application.Abstractions;
using PayTerminal.Application.DTOs.Requests;
using PayTerminal.Application.DTOs.Responses;
using PayTerminal.Application.Exceptions;
using PayTerminal.Application.Options;
using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;

namespace PayTerminal.Application.Services;

public class AuthService
{
    private readonly IMerchantRepository _merchants;
    private readonly IUserRepository _users;
    private readonly IRefreshTokenRepository _refreshTokens;
    private readonly IUnitOfWork _unitOfWork;
    private readonly IPasswordHasher<User> _passwordHasher;
    private readonly IAccessTokenGenerator _tokenGenerator;
    private readonly JwtOptions _jwt;

    public AuthService(
        IMerchantRepository merchants,
        IUserRepository users,
        IRefreshTokenRepository refreshTokens,
        IUnitOfWork unitOfWork,
        IPasswordHasher<User> passwordHasher,
        IAccessTokenGenerator tokenGenerator,
        IOptions<JwtOptions> jwt)
    {
        _merchants = merchants;
        _users = users;
        _refreshTokens = refreshTokens;
        _unitOfWork = unitOfWork;
        _passwordHasher = passwordHasher;
        _tokenGenerator = tokenGenerator;
        _jwt = jwt.Value;
    }

    public async Task<MerchantRegistrationResponse> RegisterMerchantAsync(
        RegisterMerchantRequest request,
        CancellationToken ct = default)
    {
        if (await _merchants.ExistsByEmailAsync(request.Email, ct))
        {
            throw new DuplicateEmailException();
        }

        var merchant = new Merchant(
            request.Name,
            request.BusinessName,
            request.Email,
            request.Phone);

        var owner = new User(
            merchant.Id,
            request.Name,
            request.Email,
            string.Empty,
            UserRole.Owner);
        owner.SetPasswordHash(_passwordHasher.HashPassword(owner, request.Password));

        merchant.Users.Add(owner);
        _merchants.Add(merchant);
        await _unitOfWork.SaveChangesAsync(ct);

        return new MerchantRegistrationResponse
        {
            MerchantId = merchant.Id,
            BusinessName = merchant.BusinessName,
            Email = merchant.Email
        };
    }

    public async Task<AuthResponse> LoginAsync(
        LoginRequest request,
        CancellationToken ct = default)
    {
        var user = await _users.GetByEmailAsync(request.Email, ct)
            ?? throw new InvalidCredentialsException();

        if (!user.Active)
        {
            throw new InvalidCredentialsException();
        }

        var verification = _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (verification == PasswordVerificationResult.Failed)
        {
            throw new InvalidCredentialsException();
        }

        return await IssueTokensAsync(user, ct);
    }

    public async Task<AuthResponse> RefreshAsync(
        string refreshToken,
        CancellationToken ct = default)
    {
        var token = await _refreshTokens.GetActiveAsync(refreshToken, ct)
            ?? throw new InvalidCredentialsException();

        var user = token.User;
        if (!user.Active)
        {
            throw new InvalidCredentialsException();
        }

        token.Revoke(DateTimeOffset.UtcNow);
        return await IssueTokensAsync(user, ct);
    }

    private async Task<AuthResponse> IssueTokensAsync(User user, CancellationToken ct)
    {
        await _refreshTokens.RevokeAllForUserAsync(user.Id, ct);

        var accessToken = _tokenGenerator.Generate(
            user.Id,
            user.Name,
            user.Email,
            user.MerchantId,
            user.Role);

        var refreshToken = new RefreshToken(
            user.Id,
            GenerateRefreshTokenValue(),
            DateTimeOffset.UtcNow.AddDays(_jwt.RefreshTokenLifetimeDays));

        _refreshTokens.Add(refreshToken);
        await _unitOfWork.SaveChangesAsync(ct);

        return new AuthResponse
        {
            AccessToken = accessToken,
            RefreshToken = refreshToken.Token,
            ExpiresIn = _jwt.AccessTokenLifetimeMinutes * 60,
            User = new UserDto
            {
                Id = user.Id,
                Name = user.Name,
                Email = user.Email,
                Role = user.Role,
                MerchantId = user.MerchantId
            }
        };
    }

    private static string GenerateRefreshTokenValue()
    {
        var bytes = new byte[64];
        RandomNumberGenerator.Fill(bytes);
        return Convert.ToHexString(bytes);
    }
}