namespace PayTerminal.Application.Exceptions;

public sealed class DuplicateEmailException : PayTerminalException
{
    public DuplicateEmailException()
        : base("DUPLICATE_EMAIL", "An account with this email already exists.")
    {
    }
}