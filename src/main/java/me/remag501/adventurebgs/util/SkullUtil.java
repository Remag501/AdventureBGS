package me.remag501.adventurebgs.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.Base64;

public class SkullUtil {

    public static ItemStack fromTexture(String textureUrl) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (textureUrl == null || textureUrl.isEmpty()) return skull;

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID().toString());
        PlayerTextures playerTexture = profile.getTextures();

        try {
            URL url = new URL(textureUrl);
            playerTexture.setSkin(url);
            profile.setTextures(playerTexture);
            skullMeta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().severe("Invalid skin URL: " + textureUrl);
            e.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }
}
