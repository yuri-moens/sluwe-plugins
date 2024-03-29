package io.reisub.devious.tempoross.tasks;

import io.reisub.devious.tempoross.Tempoross;
import io.reisub.devious.utils.api.Activity;
import io.reisub.devious.utils.tasks.Task;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.NullObjectID;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.client.Static;

public class DodgeFire extends Task {
  @Inject private Tempoross plugin;

  private int last;

  @Override
  public String getStatus() {
    return "Dodging fire";
  }

  @Override
  public boolean validate() {
    if (!plugin.isInTemporossArea()
        || Players.getLocal().isMoving()
        || Static.getClient().getTickCount() <= last + 3) {
      return false;
    }

    TileObject fire =
        TileObjects.getNearest(
            (o) ->
                o.getId() == NullObjectID.NULL_41006
                    && (plugin.getIslandArea().contains(o) || plugin.getBoatArea().contains(o)));

    if (fire == null) {
      return false;
    }

    WorldArea fireArea = new WorldArea(fire.getWorldLocation(), 2, 2);

    return fireArea.contains(Players.getLocal());
  }

  @Override
  public void execute() {
    if (plugin.getBoatArea().contains(Players.getLocal())) {
      NPC southAmmoCrate = NPCs.getNearest(NpcID.AMMUNITION_CRATE_10577);
      southAmmoCrate.interact(0);
      Time.sleepTicksUntil(() -> Players.getLocal().isMoving(), 3);
      Time.sleepTicksUntil(() -> plugin.isCurrentActivity(Tempoross.STOCKING_CANNON), 3);
    } else {
      plugin.setActivity(Activity.IDLE);
    }

    last = Static.getClient().getTickCount();
  }
}
