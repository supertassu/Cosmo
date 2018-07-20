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

package me.tassu.cosmo.plugin.ui;

import com.github.xemiru.sponge.boxboy.Boxboy;
import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.ActionButton;
import com.github.xemiru.sponge.boxboy.button.Button;
import com.github.xemiru.sponge.boxboy.button.DummyButton;
import com.github.xemiru.sponge.boxboy.util.MenuPattern;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.val;
import me.tassu.cosmo.cosmetic.AbstractCosmetic;
import me.tassu.cosmo.cosmetic.CosmeticManager;
import me.tassu.cosmo.plugin.Cosmo;
import me.tassu.cosmo.plugin.db.DatabaseLoader;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Color;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CosmoGUI {

    @Inject
    private DatabaseLoader loader;

    @Inject
    private CosmeticManager manager;

    private ItemStack getErrorConcrete() {
        val item = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        item.offer(Keys.DYE_COLOR, DyeColors.RED);
        item.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, TextColors.RESET, "You do not have"));
        item.offer(Keys.ITEM_LORE, Lists.newArrayList(
                Text.of(TextStyles.RESET, TextColors.RESET, "any cosmetics."),
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.RESET, "Go play some games"),
                Text.of(TextStyles.RESET, TextColors.RESET, "to earn cosmetics!"),
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.DARK_RED, ":/")
        ));
        return item;
    }

    private ItemStack getSpacer() {
        val item = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        item.offer(Keys.DYE_COLOR, DyeColors.ORANGE);
        item.offer(Keys.DISPLAY_NAME, Text.EMPTY);
        return item;
    }

    private Menu provideMenu(Set<AbstractCosmetic> cosmetics) {
        val menu = Boxboy.get().createMenu(cosmetics.isEmpty() ? 5 : 6, Text.of(
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::",
                TextStyles.RESET, TextColors.GREEN, " Cosmetics ",
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::"
        ));

        if (cosmetics.isEmpty()) {
            new MenuPattern()
                    .setButton('A', DummyButton.of(getErrorConcrete()))
                    .setPattern("         ",
                            "         ",
                            "    A    ")
                    .apply(menu);
        } else {
            new MenuPattern()
                    .setButton('A', DummyButton.of(getSpacer()))
                    .setButton('B', getClearButton())
                    .setPattern("AAAAAAAAB")
                    .apply(menu);

            AtomicInteger slot = new AtomicInteger(9);

            cosmetics.forEach(cosmetic -> menu.setButton(slot.getAndIncrement(), getCosmeticButton(cosmetic)));
        }

        return menu;
    }

    private Button getClearButton() {
        val item = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        item.offer(Keys.DYE_COLOR, DyeColors.RED);
        item.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, "Click to remove all cosmetics"));
        return ActionButton.of(item, ctx -> manager.unequip(ctx.getClicker()));
    }

    private Button getCosmeticButton(AbstractCosmetic cosmetic) {
        return ActionButton.of(cosmetic.getButton(), ctx -> manager.equip(ctx.getClicker(), cosmetic));
    }

    public void open(Player player) {
        Cosmo.newChain()
                .asyncFirst(() -> loader.getCosmeticsForPlayer(player.getUniqueId()))
                .syncLast(cosmetics -> provideMenu(cosmetics).open(player))
                .execute();
    }

}
