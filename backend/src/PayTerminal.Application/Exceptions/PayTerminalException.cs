namespace PayTerminal.Application.Exceptions;

public abstract class PayTerminalException : Exception
{
    public string Code { get; }

    protected PayTerminalException(string code, string message)
        : base(message)
    {
        Code = code;
    }
}