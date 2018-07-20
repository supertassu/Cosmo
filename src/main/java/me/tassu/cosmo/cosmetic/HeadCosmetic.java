/*
 * The MIT License
 *
 * Copyright © 2018 Tassu <hello@tassu.me>
 * Copyright © 2018 Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.tassu.cosmo.cosmetic;

import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;

import java.util.UUID;

public final class HeadCosmetic extends SimpleCosmetic {

    @Getter private final UUID uuid;

    @Builder
    public HeadCosmetic(String identifier, String name, String uuid) {
        super(identifier, name, CosmeticType.HEAD);
        this.uuid = UUID.fromString(uuid);
    }

    private ItemStack generateItemStack() {
        val item = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .build();

        item.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
        item.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, getName()));
        item.offer(Keys.REPRESENTED_PLAYER, GameProfile.of(uuid));

        return item;
    }

    @Override
    public void apply(Player player) {
        player.setHelmet(generateItemStack());
    }

    @Override
    public void unequip(Player player) {
        player.setHelmet(ItemStack.empty());
    }

    @Override
    public ItemStack getButton() {
        return generateItemStack();
    }
}
