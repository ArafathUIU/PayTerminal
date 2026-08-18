namespace PayTerminal.Application.Exceptions;

public sealed class ResourceNotFoundException : PayTerminalException
{
    public ResourceNotFoundException(string resource)
        : base("NOT_FOUND", $"{resource} was not found.")
    {
    }
}