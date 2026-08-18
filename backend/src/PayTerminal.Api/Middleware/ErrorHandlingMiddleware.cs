using System.Text.Json;
using PayTerminal.Application.Exceptions;

namespace PayTerminal.Api.Middleware;

public class ErrorHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ErrorHandlingMiddleware> _logger;

    public ErrorHandlingMiddleware(RequestDelegate next, ILogger<ErrorHandlingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (PayTerminalException ex)
        {
            await WriteErrorAsync(context, StatusFor(ex), ex.Code, ex.Message);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unhandled exception while processing request");
            await WriteErrorAsync(context, StatusCodes.Status500InternalServerError, "INTERNAL_ERROR", "An unexpected error occurred.");
        }
    }

    private static int StatusFor(PayTerminalException ex) => ex switch
    {
        ResourceNotFoundException => StatusCodes.Status404NotFound,
        InvalidCredentialsException => StatusCodes.Status401Unauthorized,
        DuplicateEmailException => StatusCodes.Status409Conflict,
        InvalidPairingCodeException => StatusCodes.Status409Conflict,
        _ => StatusCodes.Status400BadRequest
    };

    private static async Task WriteErrorAsync(HttpContext context, int status, string code, string message)
    {
        if (context.Response.HasStarted)
        {
            return;
        }

        context.Response.StatusCode = status;
        context.Response.ContentType = "application/json";

        var body = JsonSerializer.Serialize(new
        {
            code,
            message,
            detail = (object?)null
        });

        await context.Response.WriteAsync(body);
    }
}