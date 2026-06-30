package com.astrabuild.gui;

import com.astrabuild.item.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import org.bukkit.inventory.InventoryHolder;

@SuppressWarnings("deprecation")
public class GuideMenuGui implements InventoryHolder {

    private final RecipeRegistry recipeRegistry;

    public GuideMenuGui(RecipeRegistry recipeRegistry) {
        this.recipeRegistry = recipeRegistry;
    }

    private Inventory inventory;

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, "§8AstraBuild: Руководство");

        // Книга с гайдом
        ItemStack guideBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) guideBook.getItemMeta();
        bookMeta.setTitle("§eПолный Гайд (Эпоха Атома)");
        bookMeta.setAuthor("AstraBuild");
        bookMeta.setPages(
                "§lДобро пожаловать в AstraBuild!§r\n\nЭтот плагин добавляет логистику и индустриализацию на сервер.\nДля начала создайте карьер для автоматической добычи ресурсов и соедините его трубами.",
                "§lЭпоха Атома§r\n\nЯдерный Реактор - мощный источник энергии (500 EU/t). Но он требует тщательного охлаждения.\n\n§cВНИМАНИЕ!§r Перегрев приведет к катастрофическому взрыву и радиационному заражению!",
                "§lКак построить Реактор?§r\n\nЭто мультиблок 3x3x3.\n1. Поставьте Контроллер Ядерного Реактора.\n2. Окружите его Железными Блоками (нужно минимум 20 блоков).\nВнутри конструкция должна быть полой.",
                "§lОхлаждение Реактора§r\n\nРеактору нужна вода.\n1. Создайте Резервуар Охладителя.\n2. Поставьте рядом бесконечный источник воды (или кидайте лед/ведра через его GUI).\n3. Соедините Резервуар и Контроллер Реактора Логистической Трубой.",
                "§lОрбитальный Удар (Ядерка)§r\n\nЧтобы получить пульт управления ударом, постройте Спутниковый Терминал. Ему потребуется огромная энергия — 36,000,000 EU (1 час работы реактора).\nУдар произойдет через 10 сек после клика.",
                "§lРадиация§r\n\nВзрыв реактора загрязняет чанки на 24 часа. Нахождение в них вызывает иссушение, голод и слабость.\n\nСкрафтите и наденьте полный сет Костюма Химзащиты (Hazmat Suit), чтобы выжить!"
        );
        guideBook.setItemMeta(bookMeta);
        this.inventory.setItem(11, guideBook);

        // Кнопка перехода к крафтам
        ItemStack craftsBtn = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftsMeta = craftsBtn.getItemMeta();
        craftsMeta.setDisplayName("§aМеню крафтов");
        craftsMeta.setLore(List.of("§7Нажмите, чтобы открыть", "§7меню создания предметов."));
        craftsBtn.setItemMeta(craftsMeta);
        this.inventory.setItem(15, craftsBtn);

        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (clicked.getType() == Material.CRAFTING_TABLE) {
            new CraftsGui(recipeRegistry).open(player);
        } else if (clicked.getType() == Material.WRITTEN_BOOK) {
            player.sendMessage("§a[AstraBuild] §fВозьмите эту книгу, чтобы читать гайд в пути!");
            player.getInventory().addItem(clicked.clone());
            player.closeInventory();
        }
    }
}
