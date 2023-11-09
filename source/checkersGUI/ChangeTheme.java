package checkersGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChangeTheme extends JDialog implements ActionListener {

    private boolean accepted;
    private JSlider redSlider, greenSlider, blueSlider;
    private JPanel colorPanel;

    public ChangeTheme(JFrame parent, Color colorTheme) {
        super(parent, "Change theme color", true);

        colorPanel = new JPanel();
        colorPanel.setLayout(new BorderLayout());

        redSlider = new JSlider(0, 255);
        greenSlider = new JSlider(0, 255);
        blueSlider = new JSlider(0, 255);

        redSlider.setValue(colorTheme.getRed());
        greenSlider.setValue(colorTheme.getGreen());
        blueSlider.setValue(colorTheme.getBlue());

        redSlider.addChangeListener(e -> updateColor());
        greenSlider.addChangeListener(e -> updateColor());
        blueSlider.addChangeListener(e -> updateColor());

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(3, 2));

        sliderPanel.add(new JLabel("Red:"));
        sliderPanel.add(redSlider);
        sliderPanel.add(new JLabel("Green:"));
        sliderPanel.add(greenSlider);
        sliderPanel.add(new JLabel("Blue:"));
        sliderPanel.add(blueSlider);

        colorPanel.add(sliderPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(colorPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(400, 200));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateColor() {
        int red = redSlider.getValue();
        int green = greenSlider.getValue();
        int blue = blueSlider.getValue();
        colorPanel.setBackground(new Color(red, green, blue));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            accepted = true;
        } else if (e.getActionCommand().equals("Cancel")) {
            accepted = false;
        }
        setVisible(false);
    }

    public Color getColorTheme() {
        if (accepted) {
            int red = redSlider.getValue();
            int green = greenSlider.getValue();
            int blue = blueSlider.getValue();
            return new Color(red, green, blue);
        }
        return null;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
