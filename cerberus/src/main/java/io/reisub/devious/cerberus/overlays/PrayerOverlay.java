/*
 * Copyright (c) 2020 dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019 Im2be <https://github.com/Im2be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.reisub.devious.cerberus.overlays;

import io.reisub.devious.cerberus.CerberusConfig;
import io.reisub.devious.cerberus.SluweCerberus;
import io.reisub.devious.cerberus.domain.CerberusAttack;
import io.reisub.devious.cerberus.util.OverlayUtil;
import io.reisub.devious.cerberus.util.Utility;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class PrayerOverlay extends Overlay {
  private static final int TICK_PIXEL_SIZE = 60;
  private static final int BOX_WIDTH = 10;
  private static final int BOX_HEIGHT = 5;

  private final Client client;
  private final SluweCerberus plugin;
  private final CerberusConfig config;

  private final Map<Widget, Integer> lastBoxBaseyMap = new HashMap<>();

  @Inject
  private PrayerOverlay(
      final Client client, final SluweCerberus plugin, final CerberusConfig config) {
    this.client = client;
    this.plugin = plugin;
    this.config = config;

    setPriority(OverlayPriority.HIGHEST);
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
  }

  @Override
  public Dimension render(final Graphics2D graphics2D) {
    renderPrayer(graphics2D);

    return null;
  }

  private void renderPrayer(final Graphics2D graphics2D) {
    if (!config.guitarHeroMode() || plugin.getCerberus() == null) {
      return;
    }

    final int gameTick = plugin.getGameTick();

    final List<CerberusAttack> upcomingAttacks = plugin.getUpcomingAttacks();

    boolean first = true;

    for (final CerberusAttack attack : upcomingAttacks) {
      final int tick = attack.getTick() - gameTick;

      if (tick > config.guitarHeroTicks()) {
        continue;
      }

      final Prayer prayer = attack.getAttack().getPrayer();

      renderDescendingBoxes(graphics2D, prayer, tick);

      if (first) {
        renderPrayerWidget(graphics2D, prayer, tick);

        first = false;
      }
    }
  }

  private void renderDescendingBoxes(
      final Graphics2D graphics2D, final Prayer prayer, final int tick) {
    final Widget prayerWidget = client.getWidget(prayer.getWidgetInfo());

    if (prayerWidget == null || prayerWidget.isHidden()) {
      return;
    }

    final long lastTick = plugin.getLastTick();

    int baseX = (int) prayerWidget.getBounds().getX();
    baseX += prayerWidget.getBounds().getWidth() / 2;
    baseX -= BOX_WIDTH / 2;

    int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BOX_HEIGHT;
    baseY +=
        TICK_PIXEL_SIZE - ((lastTick + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

    if (baseY > (int) prayerWidget.getBounds().getY() - BOX_HEIGHT) {
      return;
    }

    if (System.currentTimeMillis() - lastTick > 600) {
      lastBoxBaseyMap.put(prayerWidget, baseY);
    } else if (lastBoxBaseyMap.containsKey(prayerWidget)) {
      if (lastBoxBaseyMap.get(prayerWidget) >= baseY
          && lastBoxBaseyMap.get(prayerWidget)
              < (int) (prayerWidget.getBounds().getY() - BOX_HEIGHT)) {
        baseY = lastBoxBaseyMap.get(prayerWidget) + 1;
        lastBoxBaseyMap.put(prayerWidget, baseY);
      } else {
        lastBoxBaseyMap.remove(prayerWidget);
      }
    }

    final Rectangle boxRectangle = new Rectangle(BOX_WIDTH, BOX_HEIGHT);
    boxRectangle.translate(baseX, baseY);

    OverlayUtil.renderFilledPolygon(graphics2D, boxRectangle, Color.ORANGE);
  }

  private void renderPrayerWidget(
      final Graphics2D graphics2D, final Prayer prayer, final int tick) {
    final Rectangle rectangle =
        OverlayUtil.renderPrayerOverlay(
            graphics2D, client, prayer, Utility.getColorFromPrayer(prayer));

    if (rectangle == null) {
      return;
    }

    final String text = String.valueOf(tick);

    final int fontSize = 16;
    final int fontStyle = Font.BOLD;
    final Color fontColor = tick == 1 ? Color.RED : Color.WHITE;

    final int x = (int) (rectangle.getX() + rectangle.getWidth() / 2);
    final int y = (int) (rectangle.getY() + rectangle.getHeight() / 2);

    final Point point = new Point(x - 3, y + 6);

    OverlayUtil.renderTextLocation(
        graphics2D, text, fontSize, fontStyle, fontColor, point, true, 0);
  }
}
