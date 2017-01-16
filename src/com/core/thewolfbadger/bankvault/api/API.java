package com.core.thewolfbadger.bankvault.api;

import com.core.thewolfbadger.bankvault.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/4/15
 * Time: 5:49 PM
 */
public class API {
    private Main m;
    public API(Main m) {
        this.m = m;
    }
    public String locToString(Location loc) {
        String string = loc.getWorld().toString()+"|"+loc.getX()+"|"+loc.getY()+"|"+loc.getZ()+"|"+loc.getYaw()+"|"+loc.getPitch();
        return string;
    }
    public Location stringToLoc(String s) {
        String[] components = s.split("|");
        Location loc = new Location(Bukkit.getWorld(components[0]), Double.parseDouble(components[1]), Double.parseDouble(components[2]), Double.parseDouble(components[3]),
                Float.parseFloat(components[4]), Float.parseFloat(components[5]));
        return loc;
    }
    public void saveVault(Location loc, Player p) {
        String serialized = this.locToString(loc);
        List<String> locs = m.getConfig().getStringList("Locations");
        if(!locs.contains(serialized)) {
            locs.add(serialized);
            m.getConfig().set("Locations", locs);
            m.saveConfig();
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4The BankVault has been set!"));
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis is already a BankVault!"));
        }
    }
    public void delVault(Location l, Player p) {
        String serialized = this.locToString(l);
        List<String> locs = m.getConfig().getStringList("Locations");
        if(locs.contains(serialized)) {
            locs.remove(serialized);
            m.getConfig().set("Locations", locs);
            m.saveConfig();
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4The BankVault has been deleted!"));
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis was never a BankVault!"));
        }
    }
    public void saveVault(Player p, Inventory inv) {
        ItemStack[] contents = inv.getContents();
        File userFile = m.getUserFile(p);
        YamlConfiguration user = YamlConfiguration.loadConfiguration(userFile);
        user.set("Inventories."+ChatColor.stripColor(inv.getTitle()), contents);
        try {
            user.save(userFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadVault(Player p, int i) {
        int size = m.getConfig().getInt("Inventory.Sizes."+i);
        String title = ChatColor.translateAlternateColorCodes('&', m.getConfig().getString("Inventory.Chests."+i+".InventoryTitle"));
        Inventory inv = Bukkit.createInventory(p, size, title);
        File userFile = m.getUserFile(p);
        YamlConfiguration user = YamlConfiguration.loadConfiguration(userFile);
        if(user.contains("Inventories."+ChatColor.stripColor(title))) {
            ArrayList<ItemStack> contents = (ArrayList<ItemStack>) user.getList("Inventories."+ChatColor.stripColor(title));
            if(contents.size() >= 1) {
                for(int j=0; j<size; j++) {
                    if(contents.get(j) !=null) {
                        inv.setItem(j, contents.get(j));
                    } else {
                        inv.setItem(j, new ItemStack(Material.AIR));
                    }
                }
            }
            p.openInventory(inv);
        } else {
            p.openInventory(inv);
        }
    }
}
