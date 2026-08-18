using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using PayTerminal.Infrastructure.Persistence;

namespace PayTerminal.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        string connectionString)
    {
        services.AddDbContext<PayTerminalDbContext>(options =>
            options.UseNpgsql(connectionString));

        return services;
    }
}
