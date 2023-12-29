import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CombinedPlugin extends JavaPlugin implements Listener {

    private final Map<Player, EnhancedItemSet> playerEnhancedItems = new HashMap<>();
    private final Random random = new Random();

    private final HashMap<UUID, UUID> tpaRequests = new HashMap<>();
    private ItemStack targetItem;
    private int enchantmentCount = 0;

    private Material[] enchantableItems = {
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS,
            Material.TRIDENT,
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
    };

    @Override
public void onEnable() {
    saveDefaultConfig();
    getServer().getPluginManager().registerEvents(this, this);
    getCommand("tpa").setExecutor((sender, command, label, args) -> onTpaCommand((Player) sender, args));
    getCommand("tpaccept").setExecutor((sender, command, label, args) -> onTpAcceptCommand((Player) sender));
    getCommand("tpdeny").setExecutor((sender, command, label, args) -> onTpDenyCommand((Player) sender));
}

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        if (enchantmentCount < MAX_ENCHANTMENTS) {
            enchantmentCount++;
            ItemStack enchantedItem = event.getItem();

            if (enchantmentCount == UNBREAKABLE_ENCHANT_LEVEL) {
                enchantedItem.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
            }

            performEnchantment(player, enchantedItem, enchantmentCount);
            openEnchantBox(player, enchantedItem);
        } else {
            player.sendMessage("더 이상 강화할 수 없습니다. 최대 강화 횟수에 도달했습니다.");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.getTitle().equals("강화 상자")) {
            event.setCancelled(true);

            if (event.getRawSlot() == 4 + 3) {
                targetItem = event.getCurrentItem();
            } else if (event.getRawSlot() == 6 + 3) {
                if (isEnchantable(targetItem)) {
                    ItemStack diamond = new ItemStack(Material.DIAMOND, 3);
                    if (player.getInventory().containsAtLeast(diamond, 3)) {
                        player.getInventory().removeItem(diamond);
                        enchantmentCount++;
                        ItemStack enchantedItem = targetItem.clone();

                        if (enchantmentCount == UNBREAKABLE_ENCHANT_LEVEL) {
                            enchantedItem.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                        }

                        performEnchantment(player, enchantedItem, enchantmentCount);
                        player.closeInventory();
                    } else {
                        player.sendMessage("다이아몬드가 부족합니다.");
                    }
                } else {
                    player.sendMessage("이 아이템은 강화할 수 없습니다.");
                }
            } else if (event.getRawSlot() == 5 + 3) {
                if (isEnchantable(targetItem)) {
                    enchantmentCount++;
                    ItemStack enchantedItem = targetItem.clone();

                    if (enchantmentCount == UNBREAKABLE_ENCHANT_LEVEL) {
                        enchantedItem.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                    }

                    performEnchantment(player, enchantedItem, enchantmentCount);
                    player.closeInventory();
                } else {
                    player.sendMessage("강화할 수 없는 아이템입니다.");
                }
            }
        }
    }

    private boolean isEnchantable(ItemStack item) {
        for (Material enchantableMaterial : enchantableItems) {
            if (item.getType() == enchantableMaterial) {
                return true;
            }
        }
        return false;
    }

    private void openEnchantBox(Player player, ItemStack enchantedItem) {
        Inventory enchantBox = Bukkit.createInventory(null, 54, "강화 상자");

        ItemStack guideItem = new ItemStack(Material.BOOK);
        guideItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        enchantBox.setItem(4 + 3, guideItem);

        ItemStack diamondItem = new ItemStack(Material.DIAMOND, 3);
        enchantBox.setItem(6 + 3, diamondItem);

        ItemStack enchantButton = new ItemStack(Material.ENCHANTING_TABLE);
        enchantBox.setItem(5 + 3, enchantButton);

        String itemName = enchantedItem.getType().toString();
        ItemStack enchantedItemWithName = new ItemStack(enchantedItem);
        enchantedItemWithName.getItemMeta().setDisplayName(itemName + " [강화 " + enchantmentCount + "]");
        enchantBox.setItem(13, enchantedItemWithName);

        player.openInventory(enchantBox);
    }

    private void performEnchantment(Player player, ItemStack item, int enchantmentLevel) {
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(item.getType().toString() + " [강화 " + enchantmentCount + "]");
        item.setItemMeta(itemMeta);
    }

    public boolean onTpaCommand(Player player, String[] args) {
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target != null) {
                player.sendMessage("TPA 요청을 보냈습니다. '/tpaccept' 또는 '/tpdeny'로 응답하세요.");
                target.sendMessage(player.getName() + "님께서 TPA 요청을 보냈습니다. '/tpaccept' 또는 '/tpdeny'로 응답하세요.");
                tpaRequests.put(target.getUniqueId(), player.getUniqueId());
            } else {
                player.sendMessage("플레이어가 온라인 상태가 아닙니다.");
            }
        } else {
            player.sendMessage("사용법: /tpa <플레이어>");
        }

        return true;
    }

    public void onTpAcceptCommand(Player target) {
        UUID requesterUUID = tpaRequests.get(target.getUniqueId());

        if (requesterUUID != null) {
            Player requester = Bukkit.getPlayer(requesterUUID);

            if (requester != null) {
                requester.teleport(target.getLocation());
                requester.sendMessage(target.getName() + "님이 TPA 요청을 수락했습니다.");
                target.sendMessage(requester.getName() + "님과 TPA 되었습니다.");
            }

            tpaRequests.remove(target.getUniqueId());
        }
    }

    public void onTpDenyCommand(Player target) {
        UUID requesterUUID = tpaRequests.get(target.getUniqueId());

        if (requesterUUID != null) {
            Player requester = Bukkit.getPlayer(requesterUUID);

            if (requester != null) {
                requester.sendMessage(target.getName() + "님이 TPA 요청을 거절했습니다.");
            }

            tpaRequests.remove(target.getUniqueId());
        }
    }

    public class EnhancedItemSet {

        private boolean hatEnhanced = false;
        private boolean chestplateEnhanced = false;
        private boolean leggingsEnhanced = false;
        private boolean bootsEnhanced = false;
        private boolean swordEnhanced = false;
        private boolean tridentEnhanced = false;

        public boolean isEnhanceable(ItemStack item) {
            Material itemType = item.getType();
            return (itemType == Material.LEATHER_HELMET || itemType == Material.DIAMOND_CHESTPLATE ||
                    itemType == Material.IRON_LEGGINGS || itemType == Material.DIAMOND_BOOTS ||
                    itemType == Material.IRON_SWORD || itemType == Material.TRIDENT);
        }

        public void applyEnhancement(ItemStack item) {
            Material itemType = item.getType();

            if (itemType == Material.LEATHER_HELMET) {
                hatEnhanced = true;
            } else if (itemType == Material.DIAMOND_CHESTPLATE) {
                chestplateEnhanced = true;
            } else if (itemType == Material.IRON_LEGGINGS) {
                leggingsEnhanced = true;
            } else if (itemType == Material.DIAMOND_BOOTS) {
                bootsEnhanced = true;
            } else if (itemType == Material.IRON_SWORD) {
                swordEnhanced = true;
            } else if (itemType == Material.TRIDENT) {
                tridentEnhanced = true;
            }
        }

        public void applyEnhancedEffects(Player player) {
            if (hatEnhanced >= 10) {
                // 10강 이상의 모자 착용 시 호흡 및 야간투시 2 효과 부여
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                
                // 2강마다 최대 체력 1증가
                int additionalHealth = (hatEnhanced / 2);
                player.setMaxHealth(20 + additionalHealth);
            }
            
            if (chestplateEnhanced > 0) {
                // 갑옷은 1강마다 최대 체력 1증가
                int additionalHealth = chestplateEnhanced;
                player.setMaxHealth(20 + additionalHealth);
            }
            
            if (leggingsEnhanced >= 10) {
                // 10강 이상의 레깅스 착용 시 신속2 및 점프강화 2 부여
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            }
            
            if (bootsEnhanced >= 10) {
                // 10강 이상의 신발 착용 시 더블 점프 가능 (쿨타임: 1분)
                if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 60, 1));
                }
            }
            
            if (swordEnhanced >= 5) {
                // 5강 이상의 칼 착용 시 무적시간 무시 및 데미지 증가
                player.setInvulnerable(false);
                double baseDamage = 1.0; // 기본 데미지
                double damageIncrease = swordEnhanced - 4; // 5강 이상일 때부터 1씩 증가
                player.setDamage(baseDamage + damageIncrease);
            }
            
            if (tridentEnhanced >= 10) {
                // 10강 이상의 삼지창 착용 시 급류 인첸트 및 멀티샷 부여
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0));
                
                // 멀티샷 구현 (주위로 7개 추가로 날라감)
                for (int i = 0; i < 7; i++) {
                    Trident trident = player.launchProjectile(Trident.class);
                    trident.setVelocity(trident.getVelocity().multiply(2)); // 속도 증가
                }
            }
        }
        
    }

    private static final int MAX_ENCHANTMENTS = 10;
    private static final int UNBREAKABLE_ENCHANT_LEVEL = 10;
}
