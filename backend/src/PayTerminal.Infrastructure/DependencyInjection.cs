using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using PayTerminal.Application.Abstractions;
using PayTerminal.Infrastructure.Auth;
using PayTerminal.Infrastructure.Persistence;
using PayTerminal.Infrastructure.Persistence.Repositories;

namespace PayTerminal.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        string connectionString)
    {
        services.AddDbContext<PayTerminalDbContext>(options =>
            options.UseNpgsql(connectionString));

        services.AddScoped<IUnitOfWork, UnitOfWork>();
        services.AddScoped<IMerchantRepository, MerchantRepository>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<ITerminalRepository, TerminalRepository>();
        services.AddScoped<IRefreshTokenRepository, RefreshTokenRepository>();

        services.AddScoped<IAccessTokenGenerator, JwtTokenGenerator>();

        return services;
    }
}
