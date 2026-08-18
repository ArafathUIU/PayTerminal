namespace PayTerminal.Domain.Entities;

public class Merchant : Entity
{
    public string Name { get; private set; } = null!;
    public string BusinessName { get; private set; } = null!;
    public string Email { get; private set; } = null!;
    public string? Phone { get; private set; }

    public ICollection<User> Users { get; private set; } = new List<User>();
    public ICollection<Terminal> Terminals { get; private set; } = new List<Terminal>();
    public ICollection<Transaction> Transactions { get; private set; } = new List<Transaction>();

    private Merchant() { }

    public Merchant(string name, string businessName, string email, string? phone)
    {
        Name = name;
        BusinessName = businessName;
        Email = email;
        Phone = phone;
    }
}
