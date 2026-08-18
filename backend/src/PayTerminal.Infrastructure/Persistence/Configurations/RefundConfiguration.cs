using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class RefundConfiguration : IEntityTypeConfiguration<Refund>
{
    public void Configure(EntityTypeBuilder<Refund> builder)
    {
        builder.ToTable("refunds");

        builder.HasKey(r => r.Id);

        builder.Property(r => r.RefundNumber).HasMaxLength(40).IsRequired();
        builder.Property(r => r.Amount).HasPrecision(18, 2).IsRequired();
        builder.Property(r => r.Reason).HasMaxLength(255);
        builder.Property(r => r.Status).HasConversion<string>().HasMaxLength(20);
        builder.Property(r => r.ProcessedAt);

        builder.HasIndex(r => r.RefundNumber).IsUnique();
        builder.HasIndex(r => r.TransactionId);
    }
}
