namespace PayTerminal.Application.Exceptions;

public sealed class InvalidCredentialsException : PayTerminalException
{
    public InvalidCredentialsException()
        : base("INVALID_CREDENTIALS", "Email or password is incorrect.")
    {
    }
}