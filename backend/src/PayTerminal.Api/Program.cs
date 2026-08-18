using PayTerminal.Infrastructure;

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

app.MapGet("/api/v1/health", () =>
{
    var dbStatus = string.IsNullOrWhiteSpace(connectionString)
        ? "not_configured"
        : "configured";
    return Results.Ok(new
    {
        status = "ok",
        database = dbStatus,
        version = "0.1.0",
        timestamp = DateTimeOffset.UtcNow
    });
})
.WithName("Health")
.WithOpenApi();

app.Run();