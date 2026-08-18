using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PayTerminal.Domain.Entities;

namespace PayTerminal.Infrastructure.Persistence.Configurations;

public class MerchantConfiguration : IEntityTypeConfiguration<Merchant>
{
    public void Configure(EntityTypeBuilder<Merchant> builder)
    {
        builder.ToTable("merchants");

        builder.HasKey(m => m.Id);

        builder.Property(m => m.Name).HasMaxLength(120).IsRequired();
        builder.Property(m => m.BusinessName).HasMaxLength(120).IsRequired();
        builder.Property(m => m.Email).HasMaxLength(255).IsRequired();
        builder.Property(m => m.Phone).HasMaxLength(20);

        builder.HasIndex(m => m.Email).IsUnique();

        builder.HasMany(m => m.Users)
            .WithOne(u => u.Merchant)
            .HasForeignKey(u => u.MerchantId);

        builder.HasMany(m => m.Terminals)
            .WithOne(t => t.Merchant)
            .HasForeignKey(t => t.MerchantId);

        builder.HasMany(m => m.Transactions)
            .WithOne(t => t.Merchant)
            .HasForeignKey(t => t.MerchantId);
    }
}
