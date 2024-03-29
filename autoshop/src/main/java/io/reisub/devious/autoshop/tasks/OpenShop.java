package io.reisub.devious.autoshop.tasks;

import io.reisub.devious.autoshop.Config;
import io.reisub.devious.autoshop.Shopper;
import io.reisub.devious.utils.api.SluweMovement;
import io.reisub.devious.utils.tasks.Task;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.movement.Movement;

public class OpenShop extends Task {
  @Inject private Shopper plugin;

  @Inject private Config config;

  @Override
  public String getStatus() {
    return "Opening shop";
  }

  @Override
  public boolean validate() {
    return !Inventory.isFull() && !Shop.isOpen();
  }

  @Override
  public void execute() {
    if (!Movement.isRunEnabled() && Movement.getRunEnergy() >= Rand.nextInt(20, 40)) {
      Movement.toggleRun();
    }

    if (plugin.getNpcLocation() != null
        && Players.getLocal().distanceTo(plugin.getNpcLocation()) > 8) {
      SluweMovement.walkTo(plugin.getNpcLocation(), 1);
    }

    if (config.isGameObject()) {
      TileObject obj = TileObjects.getNearest(config.npcName());

      if (obj == null) {
        return;
      }

      GameThread.invoke(() -> obj.interact(config.tradeAction()));
    } else {
      NPC npc = NPCs.getNearest(config.npcName());
      if (npc == null) {
        return;
      }

      GameThread.invoke(() -> npc.interact(config.tradeAction()));
    }

    Time.sleepTicksUntil(Shop::isOpen, 20);
  }
}
