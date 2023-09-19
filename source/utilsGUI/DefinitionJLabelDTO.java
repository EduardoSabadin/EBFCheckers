package utilsGUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static checkersGUI.CheckersGUI.*;

public class DefinitionJLabelDTO {
    public boolean isOpaque = false;
    public int horizontalAlignment = 0;
    public String tooltipText = "";
    public Color foregroundColor = PLAYER1_COLOR;
    public Color backgroundColor = NEUTRAL_BG_COLOR;

    public Border border = BORDER;

    public DefinitionJLabelDTO(
            boolean isOpaque,
            int horizontalAlignment,
            String tooltipText,
            Color foregroundColor,
            Color backgroundColor,
            Border border
    ) {
        this.isOpaque = isOpaque;
        this.horizontalAlignment = horizontalAlignment;
        this.tooltipText = tooltipText;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.border = border;

    }
}
