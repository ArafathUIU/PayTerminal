using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class TransactionConfiguration : IEntityTypeConfiguration<Transaction>
{
    public void Configure(EntityTypeBuilder<Transaction> builder)
    {
        builder.ToTable("transactions");

        builder.HasKey(t => t.Id);

        builder.Property(t => t.TransactionNumber).HasMaxLength(40).IsRequired();
        builder.Property(t => t.IdempotencyKey).HasMaxLength(64).IsRequired();
        builder.Property(t => t.Amount).HasPrecision(18, 2).IsRequired();
        builder.Property(t => t.Currency).HasColumnType("char(3)").IsRequired();
        builder.Property(t => t.PaymentMethod).HasConversion<string>().HasMaxLength(20);
        builder.Property(t => t.Status).HasConversion<string>().HasMaxLength(20);
        builder.Property(t => t.ProcessedAt);
        builder.Property(t => t.RefundedAt);
        builder.Property(t => t.RefundedAmount).HasPrecision(18, 2);

        builder.HasIndex(t => t.TransactionNumber).IsUnique();
        builder.HasIndex(t => t.IdempotencyKey).IsUnique();
        builder.HasIndex(t => new { t.MerchantId, t.CreatedAt });
        builder.HasIndex(t => new { t.TerminalId, t.CreatedAt });

        builder.HasMany(t => t.Attempts)
            .WithOne(a => a.Transaction)
            .HasForeignKey(a => a.TransactionId);

        builder.HasMany(t => t.Events)
            .WithOne(e => e.Transaction)
            .HasForeignKey(e => e.TransactionId);

        builder.HasMany(t => t.Refunds)
            .WithOne(r => r.Transaction)
            .HasForeignKey(r => r.TransactionId);
    }
}
