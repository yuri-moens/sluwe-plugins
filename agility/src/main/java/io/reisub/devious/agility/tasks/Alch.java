package io.reisub.devious.agility.tasks;

import io.reisub.devious.agility.Agility;
import io.reisub.devious.agility.Config;
import io.reisub.devious.utils.api.ConfigList;
import io.reisub.devious.utils.api.SluwePredicates;
import io.reisub.devious.utils.tasks.Task;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.client.Static;

public class Alch extends Task {

  private final Config config;
  private ConfigList configList;

  private int lastTick;
  private boolean ready;

  @Inject
  public Alch(Config config) {
    this.config = config;
    configList = ConfigList.parseList(config.alchItems());
  }

  @Override
  public String getStatus() {
    return "Alching";
  }

  @Override
  public boolean validate() {
    if (!config.highAlch()
        || (configList.getIntegers().isEmpty() && configList.getStrings().isEmpty())) {
      return false;
    }

    return canCast()
        && SpellBook.Standard.HIGH_LEVEL_ALCHEMY.canCast()
        && Inventory.contains(SluwePredicates.itemConfigList(configList));
  }

  @Override
  public void execute() {
    ready = false;

    if (Agility.DELAY_POINTS.contains(Players.getLocal().getWorldLocation())) {
      Time.sleepTick();
    }

    final Item item = Inventory.getFirst(SluwePredicates.itemConfigList(configList));

    if (item == null) {
      return;
    }

    SpellBook.Standard.HIGH_LEVEL_ALCHEMY.castOn(item);
    lastTick = Static.getClient().getTickCount();
  }

  @Subscribe
  private void onStatChanged(StatChanged event) {
    if (event.getSkill() == Skill.AGILITY) {
      ready = true;
    }
  }

  @Subscribe
  private void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals("sluweutils")) {
      return;
    }

    if (event.getKey().equals("alchItems")) {
      configList = ConfigList.parseList(config.alchItems());
    }
  }

  private boolean canCast() {
    if (Static.getClient().getTickCount() - lastTick <= 5) {
      return false;
    }

    if (ready) {
      return true;
    }

    return Movement.getDestination() != null && !Players.getLocal().isAnimating();
  }
}
