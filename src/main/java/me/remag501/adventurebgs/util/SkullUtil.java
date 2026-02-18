package me.remag501.adventurebgs.util;


import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

public class SkullUtil {

    public static ItemStack fromTexture(String textureUrl) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (textureUrl == null || textureUrl.isEmpty()) return skull;

        // Use editMeta for better performance and 1.21.8 safety
        skull.editMeta(SkullMeta.class, skullMeta -> {
            // 1. Create a profile with a fixed or random UUID
            // Names can be anything for custom heads
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "CustomHead");

            // 2. Get the textures object from the profile
            PlayerTextures textures = profile.getTextures();

            try {
                // 3. Set the skin using a URL
                // Note: textures.minecraft.net URLs are highly recommended
                textures.setSkin(URI.create(textureUrl).toURL());

                // 4. Apply the textures back to the profile
                profile.setTextures(textures);

                // 5. Apply the profile to the meta
                skullMeta.setPlayerProfile(profile);

            } catch (MalformedURLException | IllegalArgumentException e) {
                Bukkit.getLogger().severe("Failed to set skull texture: " + e.getMessage());
            }
        });

        return skull;
    }
}
