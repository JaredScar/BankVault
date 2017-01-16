package com.core.thewolfbadger.bankvault.main;

import com.core.thewolfbadger.bankvault.api.API;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/4/15
 * Time: 5:34 PM
 */
public class Main extends JavaPlugin implements Listener {
    private ArrayList<UUID> definers = new ArrayList<UUID>();
    private ArrayList<UUID> deleters = new ArrayList<UUID>();
    private API api = new API(this);
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    @Override
    public void onDisable() {}
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("bvault") || cmd.getName().equalsIgnoreCase("bv")) {
            if(sender instanceof Player) {
                if(sender.hasPermission("BankVault.Admin")) {
                    if(args.length == 1) {
                        if(!definers.contains(((Player) sender).getUniqueId())) {
                            if(!deleters.contains(((Player) sender).getUniqueId())) {
                                if(args[0].equalsIgnoreCase("define")) {
                                    // Set in select mode
                                    definers.add(((Player) sender).getUniqueId());
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You have been set in defining mode!"));
                                } else
                                    if(args[0].equalsIgnoreCase("del")) {
                                        // Set in delete mode
                                        deleters.add(((Player) sender).getUniqueId());
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4You have been set in deleting mode!"));
                                    }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    public File getUserFile(Player user) {
        File folder = new File(this.getDataFolder(), "users");
        File userFile = new File(this.getDataFolder(), "users"+File.separator+""+user.getUniqueId().toString()+".yml");
        if(!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(!folder.exists()) {
            try {
                folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userFile;
    }
    @EventHandler
    public void onPlayerHit(PlayerInteractEvent evt) {
        Player p = evt.getPlayer();
        if(evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = evt.getClickedBlock();
            if(b !=null) {
                if(b.getType() == Material.ENDER_CHEST) {
                    if(deleters.contains(p.getUniqueId())) {
                        //Delete
                        api.delVault(b.getLocation(), p);
                        deleters.remove(p.getUniqueId());
                        evt.setCancelled(true);
                    } else
                        if(definers.contains(p.getUniqueId())) {
                            //Define block
                            api.saveVault(b.getLocation(), p);
                            definers.remove(p.getUniqueId());
                            evt.setCancelled(true);
                    } else
                        if(this.getConfig().getStringList("Locations").contains(api.locToString(b.getLocation()))) {
                            if(p.hasPermission("BankVault.usage")) {
                                //Open up the inventory
                                evt.setCancelled(true);
                                int size = this.getConfig().getInt("Inventory.Size");
                                String title = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Inventory.Title"));
                                Inventory bankV = Bukkit.createInventory(p, size, title);
                                for(int i=0; i<size; i++) {
                                    if(p.hasPermission("BankVault.use."+(i+1))) {
                                        ItemStack chest = new ItemStack(Material.CHEST);
                                        ItemMeta chestMeta = chest.getItemMeta();
                                        List<String> lores = this.getConfig().getStringList("Inventory.Chests."+(i+1)+".Lores");
                                        ArrayList<String> loresColor = new ArrayList<String>();
                                        for(String string : lores) {
                                            loresColor.add(ChatColor.translateAlternateColorCodes('&', string));
                                        }
                                        chestMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.getConfig().getStringList("Inventory.ChestNames").get(i).replace("{SLOT}", ""+(i+1))));
                                        chestMeta.setLore(loresColor);
                                        chest.setItemMeta(chestMeta);
                                        bankV.setItem(i, chest);
                                    }
                                }
                                p.openInventory(bankV);
                            } else {
                                evt.setCancelled(true);
                                Vector v = p.getLocation().getDirection(); // Create the vector.
                                p.setVelocity(v.multiply(-1.25));
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermissionForBankVault")));
                            }

                        }
                }
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        Player p = (Player) evt.getWhoClicked();
        String title = evt.getInventory().getTitle();
        ItemStack item = evt.getCurrentItem();
        if(ChatColor.stripColor(title).equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Inventory.Title"))))) {
            evt.setCancelled(true);
            if(item !=null) {
                if(item.getType() == Material.CHEST) {
                    if(item.hasItemMeta()) {
                        boolean bool = false;
                        for(String displays : this.getConfig().getStringList("Inventory.ChestNames")) {
                            if(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', displays).replace("{SLOT}", ""+(evt.getSlot()+1))).equals(ChatColor.stripColor(item.getItemMeta().getDisplayName()))) {
                                bool = true;
                            }
                        }
                        if(bool) {
                            //Open up inventory
                            api.loadVault(p, evt.getSlot()+1);
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        Player p = (Player) evt.getPlayer();
        if(evt.getInventory().getTitle() !=null) {
            boolean bool = false;
            String title = evt.getInventory().getTitle();
            for(int i=0; i<this.getConfig().getInt("Inventory.Size"); i++) {
                if(ChatColor.stripColor(title).equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Inventory.Chests."+(i+1)+".InventoryTitle"))))) {
                    bool = true;
                }
            }
            if(bool) {
                api.saveVault(p, evt.getInventory());
            }
        }
    }
}
