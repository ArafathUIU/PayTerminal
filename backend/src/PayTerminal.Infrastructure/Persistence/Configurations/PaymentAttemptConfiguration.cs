using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class PaymentAttemptConfiguration : IEntityTypeConfiguration<PaymentAttempt>
{
    public void Configure(EntityTypeBuilder<PaymentAttempt> builder)
    {
        builder.ToTable("payment_attempts");

        builder.HasKey(a => a.Id);

        builder.Property(a => a.AttemptNumber).IsRequired();
        builder.Property(a => a.Method).HasConversion<string>().HasMaxLength(20);
        builder.Property(a => a.MaskedReference).HasMaxLength(40);
        builder.Property(a => a.ProcessorReference).HasMaxLength(64);
        builder.Property(a => a.Status).HasConversion<string>().HasMaxLength(20);
        builder.Property(a => a.ErrorCode).HasMaxLength(40);
        builder.Property(a => a.ErrorMessage).HasMaxLength(255);
        builder.Property(a => a.ProcessedAt);

        builder.HasIndex(a => new { a.TransactionId, a.AttemptNumber }).IsUnique();
    }
}
