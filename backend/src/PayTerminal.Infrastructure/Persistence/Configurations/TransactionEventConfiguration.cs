using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class TransactionEventConfiguration : IEntityTypeConfiguration<TransactionEvent>
{
    public void Configure(EntityTypeBuilder<TransactionEvent> builder)
    {
        builder.ToTable("transaction_events");

        builder.HasKey(e => e.Id);

        builder.Property(e => e.EventType).HasMaxLength(40).IsRequired();
        builder.Property(e => e.FromStatus).HasConversion<string>().HasMaxLength(20);
        builder.Property(e => e.ToStatus).HasConversion<string>().HasMaxLength(20);
        builder.Property(e => e.Payload);

        builder.HasIndex(e => new { e.TransactionId, e.CreatedAt });
    }
}
