namespace PayTerminal.Application.Exceptions;

public sealed class InvalidPairingCodeException : PayTerminalException
{
    public InvalidPairingCodeException()
        : base("INVALID_PAIRING_CODE", "The pairing code is invalid or has already been used.")
    {
    }
}