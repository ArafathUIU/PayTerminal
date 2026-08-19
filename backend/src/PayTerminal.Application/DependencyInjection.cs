using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.DependencyInjection;
using PayTerminal.Domain.Entities;
using PayTerminal.Application.Services;

namespace PayTerminal.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IPasswordHasher<User>, PasswordHasher<User>>();

        services.AddScoped<AuthService>();
        services.AddScoped<MerchantsService>();
        services.AddScoped<TerminalService>();

        return services;
    }
}
