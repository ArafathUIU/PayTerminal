using Microsoft.EntityFrameworkCore;
using PayTerminal.Infrastructure;
using PayTerminal.Infrastructure.Persistence;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var connectionString = builder.Configuration.GetConnectionString("Default")
    ?? string.Empty;

builder.Services.AddInfrastructure(connectionString);

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<PayTerminalDbContext>();
    await DbSeeder.SeedAsync(db);
}

app.MapGet("/api/v1/health", async () =>
{
    var dbUp = false;
    if (!string.IsNullOrWhiteSpace(connectionString))
    {
        using var scope = app.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<PayTerminalDbContext>();
        dbUp = await db.Database.CanConnectAsync();
    }

    return Results.Ok(new
    {
        status = dbUp ? "ok" : "degraded",
        database = dbUp ? "up" : "unreachable",
        version = "0.1.0",
        timestamp = DateTimeOffset.UtcNow
    });
})
.WithName("Health")
.WithOpenApi();

app.Run();