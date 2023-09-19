package checkersGUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import utilsGUI.Constants;

public class ChangeTheme extends JDialog implements ActionListener,
        KeyListener, ItemListener {

    public static final Font FONT = new Font(Constants.FONT_ARIAL, Font.BOLD, 20);

    private boolean accepted;
    private SimpleComboBox comboBox1;
    private JComponent component1;
    private JButton ok, cancel;
    private JPanel panel;

    // Cores disponíveis
    public Color colorTheme;
    private String[] colorsPt = { "Vermelho", "Preto", "Azul", "Verde", "Roxo", "Pink" };
    private Color[] colorsEg = { Color.RED, Color.BLACK, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.PINK };

    public ChangeTheme(JFrame parent, Color colorsTheme) {
        super(parent, "Change theme color", true);

        this.colorTheme = colorsTheme;

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(CheckersGUI.NEUTRAL_BG_COLOR);

        ListCellRenderer<? super String> cellRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                JComponent c = (JComponent) super.getListCellRendererComponent(
                        list, value, index, isSelected, false);

                if (isSelected) {
                    c.setForeground(CheckersGUI.NEUTRAL_BG_COLOR);
                    c.setBackground(list.getForeground());
                }

                return c;
            }
        };

        component1 = new JComponent() {
        };

        component1.setLayout(new GridBagLayout());

        TitledBorder border = BorderFactory.createTitledBorder(
                CheckersGUI.BORDER, "Theme color");
        border.setTitleFont(FONT);
        border.setTitleColor(CheckersGUI.PLAYER1_COLOR);
        component1.setBorder(border);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        // ComboBox para selecionar a cor do Tema
        comboBox1 = new SimpleComboBox(colorsPt);
        comboBox1.setSelectedIndex(0); // Selecionar a cor padrão
        comboBox1.setSelectedValue("");
        comboBox1.addItemListener(this);
        comboBox1.setBackground(Color.white);
        comboBox1.setFocusBackground(Color.gray);
        comboBox1.setForeground(Color.gray);
        comboBox1.getList().setCellRenderer(cellRenderer);
        comboBox1.getPopup().setBorder(CheckersGUI.BORDER);
        component1.add(comboBox1, c);

        c.gridy++;

        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(component1, c);

        c.gridwidth = 1;
        c.weighty = 0;
        c.gridy++;
        ok = new JButton("OK");
        ok.addActionListener(this);
        panel.add(ok, c);

        c.gridx--;
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        panel.add(cancel, c);

        add(panel);
        setFocusCycleRoot(true);

        comboBox1.addKeyListener(this);
        cancel.addKeyListener(this);
        ok.addKeyListener(this);
        addKeyListener(this);

        setMinimumSize(new Dimension(400, 300));
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == ok)
            accepted = true;
        setVisible(false);
    }

    public Color getColorTheme() {
        return colorTheme;
    }

    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            int index = comboBox1.getSelectedIndex();
            this.colorTheme = colorsEg[index];
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error, "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            error.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            cancel.doClick();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
