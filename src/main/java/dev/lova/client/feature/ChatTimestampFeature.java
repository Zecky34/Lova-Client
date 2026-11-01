package dev.lova.client.feature;

import dev.lova.client.LovaConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatTimestampFeature {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    /** Kapalıysa aynı nesneyi döner. Açıksa mesajın sonuna saat ekler. */
    public static Component stamp(Component message) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.chatTimeEnabled) {
            return message;
        }
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(cfg.chatTimeRgb & 0xFFFFFF));
        return Component.empty()
                .append(message)
                .append(Component.literal(" " + LocalTime.now().format(FMT)).withStyle(style));
    }
}
