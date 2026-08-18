using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.DependencyInjection;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IPasswordHasher<User>, PasswordHasher<User>>();

        return services;
    }
}
