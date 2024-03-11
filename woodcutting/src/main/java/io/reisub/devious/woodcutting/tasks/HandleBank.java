package io.reisub.devious.woodcutting.tasks;

import io.reisub.devious.utils.api.SluweBank;
import io.reisub.devious.utils.tasks.BankTask;
import io.reisub.devious.woodcutting.Config;
import io.reisub.devious.woodcutting.Woodcutting;
import java.time.Duration;
import javax.inject.Inject;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;

public class HandleBank extends BankTask {

  @Inject
  private Woodcutting plugin;
  @Inject
  private Config config;
  @Inject
  private Chop chopTask;

  @Override
  public boolean validate() {
    return Inventory.isFull()
        && config.location().getBankPoint() != null
        && !config.drop()
        && Players.getLocal().distanceTo(config.location().getBankPoint()) < 10
        && isLastBankDurationAgo(Duration.ofSeconds(5));
  }

  @Override
  public void execute() {
    chopTask.setCurrentTreePosition(null);

    open();

    SluweBank.depositAllExcept(false, Predicates.nameContains("axe"));

    plugin.setLastBankTick(Static.getClient().getTickCount());
  }
}
