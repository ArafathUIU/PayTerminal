using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class TerminalConfiguration : IEntityTypeConfiguration<Terminal>
{
    public void Configure(EntityTypeBuilder<Terminal> builder)
    {
        builder.ToTable("terminals");

        builder.HasKey(t => t.Id);

        builder.Property(t => t.Code).HasMaxLength(20).IsRequired();
        builder.Property(t => t.Name).HasMaxLength(120).IsRequired();
        builder.Property(t => t.PairingCode).HasMaxLength(10);
        builder.Property(t => t.Status).HasConversion<string>().HasMaxLength(20);
        builder.Property(t => t.PairedAt);
        builder.Property(t => t.LastHeartbeatAt);

        builder.HasIndex(t => t.Code).IsUnique();

        builder.HasMany(t => t.Transactions)
            .WithOne(x => x.Terminal)
            .HasForeignKey(x => x.TerminalId);
    }
}
