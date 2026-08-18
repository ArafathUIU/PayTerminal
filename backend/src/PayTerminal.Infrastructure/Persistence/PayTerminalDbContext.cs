using Microsoft.EntityFrameworkCore;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence;

public class PayTerminalDbContext : DbContext
{
    public PayTerminalDbContext(DbContextOptions<PayTerminalDbContext> options)
        : base(options)
    {
    }

    public DbSet<Merchant> Merchants => Set<Merchant>();
    public DbSet<User> Users => Set<User>();
    public DbSet<Terminal> Terminals => Set<Terminal>();
    public DbSet<Transaction> Transactions => Set<Transaction>();
    public DbSet<PaymentAttempt> PaymentAttempts => Set<PaymentAttempt>();
    public DbSet<Refund> Refunds => Set<Refund>();
    public DbSet<TransactionEvent> TransactionEvents => Set<TransactionEvent>();
    public DbSet<RefreshToken> RefreshTokens => Set<RefreshToken>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(PayTerminalDbContext).Assembly);
    }
}
