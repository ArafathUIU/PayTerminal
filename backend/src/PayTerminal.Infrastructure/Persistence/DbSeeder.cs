using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using PayTerminal.Domain.Entities;
using PayTerminal.Domain.Enums;

namespace PayTerminal.Infrastructure.Persistence;

public static class DbSeeder
{
    public static async Task SeedAsync(PayTerminalDbContext db)
    {
        if (await db.Merchants.AnyAsync())
        {
            return;
        }

        var hasher = new PasswordHasher<User>();

        var merchant = new Merchant(
            "Rahim Ahmed",
            "Rahim Electronics",
            "owner@myshop.com",
            "+8801XXXXXXXXX");

        var owner = new User(
            merchant.Id,
            "Rahim Ahmed",
            "owner@myshop.com",
            string.Empty,
            UserRole.Owner);
        owner.SetPasswordHash(hasher.HashPassword(owner, "password123"));

        var terminal = new Terminal(
            merchant.Id,
            "TERM-0001",
            "Front Counter",
            "ABC-1234");

        merchant.Users.Add(owner);
        merchant.Terminals.Add(terminal);

        db.Merchants.Add(merchant);
        await db.SaveChangesAsync();
    }
}
