package iafenvoy.accountswitcher.utils;

import net.minecraft.text.Text;

public class ButtonWidget extends net.minecraft.client.gui.widget.ButtonWidget {
    public ButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, textSupplier -> Text.empty()); // 1.19.3+
//        super(x, y, width, height, message, onPress);   // 1.19-1.19.3
    }
}
