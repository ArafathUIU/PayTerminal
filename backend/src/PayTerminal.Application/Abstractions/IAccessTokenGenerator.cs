using PayTerminal.Domain.Enums;

namespace PayTerminal.Application.Abstractions;

public interface IAccessTokenGenerator
{
    string Generate(Guid userId, string name, string email, Guid merchantId, UserRole role);
}
