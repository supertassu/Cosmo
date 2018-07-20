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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.inject.Inject;
import me.tassu.cosmo.cosmetic.AbstractCosmetic;
import me.tassu.cosmo.plugin.Cosmo;
import me.tassu.cosmo.plugin.db.DatabaseLoader;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import static org.spongepowered.api.text.format.TextColors.GOLD;
import static org.spongepowered.api.text.format.TextColors.YELLOW;

@CommandAlias("cosmo|cosm|cosmetic")
@CommandPermission("cosmo.command.execute")
public class CosmoCommand extends BaseCommand {

    @Inject
    private DatabaseLoader loader;
    @Inject
    private CosmoGUI gui;

    @Default
    @Subcommand("gui")
    @Description("Opens the cosmetic gui.")
    public void onGui(Player player) {
        gui.open(player);
    }

    @Subcommand("list")
    @Description("Shows all cosmetics you have.")
    public void onList(Player player) {
        Cosmo.newChain()
                .asyncFirst(() -> loader.getCosmeticsForPlayer(player.getUniqueId()))
                .syncLast(cosmetics -> {
                    player.sendMessage(Text.of(YELLOW, "You have ",
                            GOLD, cosmetics.size(), YELLOW, " cosmetic(s)"));
                    cosmetics.forEach(cosmetic -> player.sendMessage(
                            Text.of(GOLD, "* ", YELLOW, cosmetic.getName() + " (" + cosmetic.getIdentifier() + ", " + cosmetic.getType() + ")")));
                })
                .execute();
    }

    @Subcommand("add")
    @CommandPermission("cosmo.command.sub.add")
    public void onAdd(CommandSource source, AbstractCosmetic cosmetic, UUID target) {
        Cosmo.newChain()
                .async(() -> loader.addCosmetic(target, cosmetic))
                .sync(() -> source.sendMessage(Text.of(YELLOW, "Added cosmetic ", GOLD,
                        cosmetic.getName(), YELLOW, " to " + target.toString() + ".")))
                .execute();
    }

    @Subcommand("select")
    public void onSelect(Player player, AbstractCosmetic cosmetic) {
        cosmetic.apply(player);
        player.sendMessage(Text.of(YELLOW, "You equipped ", GOLD, cosmetic.getName(), "."));
    }

}
