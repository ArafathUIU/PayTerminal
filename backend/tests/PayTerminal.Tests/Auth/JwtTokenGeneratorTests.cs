using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using PayTerminal.Application.Options;
using PayTerminal.Domain.Enums;
using PayTerminal.Infrastructure.Auth;

namespace PayTerminal.Tests.Auth;

public class JwtTokenGeneratorTests
{
    private static JwtTokenGenerator CreateGenerator()
    {
        var options = Options.Create(new JwtOptions
        {
            Issuer = "PayTerminal",
            Audience = "PayTerminalClients",
            SecretKey = "test-secret-key-0123456789abcdef0123456789abcdef",
            AccessTokenLifetimeMinutes = 60
        });
        return new JwtTokenGenerator(options);
    }

    [Fact]
    public void Generate_ProducesTokenWithExpectedClaims()
    {
        var userId = Guid.NewGuid();
        var merchantId = Guid.NewGuid();
        var generator = CreateGenerator();

        var token = generator.Generate(userId, "Rahim Ahmed", "owner@myshop.com", merchantId, UserRole.Owner);

        var jwt = new JwtSecurityTokenHandler().ReadJwtToken(token);
        Assert.Equal("PayTerminal", jwt.Issuer);
        Assert.Equal("PayTerminalClients", jwt.Audiences.Single());
        Assert.Equal(userId.ToString(), jwt.Claims.Single(c => c.Type == JwtRegisteredClaimNames.Sub).Value);
        Assert.Equal("owner@myshop.com", jwt.Claims.Single(c => c.Type == JwtRegisteredClaimNames.Email).Value);
        Assert.Equal("Rahim Ahmed", jwt.Claims.Single(c => c.Type == "name").Value);
        Assert.Equal(merchantId.ToString(), jwt.Claims.Single(c => c.Type == "merchant_id").Value);
        Assert.Equal(UserRole.Owner.ToString(), jwt.Claims.Single(c => c.Type == ClaimTypes.Role).Value);
        Assert.True(jwt.ValidTo > DateTime.UtcNow.AddMinutes(55));
    }

    [Fact]
    public void Generate_ProducesTokenWithValidSignature()
    {
        var generator = CreateGenerator();
        var token = generator.Generate(Guid.NewGuid(), "Cashier", "cashier@myshop.com", Guid.NewGuid(), UserRole.Cashier);

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes("test-secret-key-0123456789abcdef0123456789abcdef"));
        var parameters = new TokenValidationParameters
        {
            ValidIssuer = "PayTerminal",
            ValidAudience = "PayTerminalClients",
            IssuerSigningKey = key,
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };

        new JwtSecurityTokenHandler().ValidateToken(token, parameters, out var validated);
        Assert.NotNull(validated);
        Assert.Equal("PayTerminal", validated.Issuer);
    }
}
