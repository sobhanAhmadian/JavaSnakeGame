import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Home extends JFrame {

    private final BorderLayout layoutForJFrame;
    private final Field field;
    private JMenuBar menuBar;
    private JMenuItem[] menuItems;
    public static final Color color = new Color(77, 182, 139);

    public Home() {

        // property of JFrame
        super("Snake");
        setBounds(135, 70, 1100, 625);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        // declare Layout for JFrame
        layoutForJFrame = new BorderLayout(0, 0);

        // set Layout to JFrame
        setLayout(layoutForJFrame);

        // declare and add Field for game
        field = new Field();
        add(field, BorderLayout.CENTER);

        // manage menuBar
        manageMenuBar();

        // this method should be end of this constructor
        getContentPane().validate();
    }

    private void manageMenuBar() {

        menuBar = new JMenuBar();
        menuBar.setBackground(color);
        menuItems = new JMenuItem[20];

        JMenuItem menuItem1 = new JMenuItem("File");
        menuItem1.setBackground(color);
        Font usualFont;

        try {

            usualFont = Font.createFont(Font.TRUETYPE_FONT,
                    ClassLoader.getSystemResourceAsStream("Source/basictitlefont.ttf")).deriveFont(17f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(usualFont);
            menuItem1.setFont(usualFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
        menuBar.add(menuItem1);

        for (JMenuItem menuItem : menuItems) {

            menuItem = new JMenuItem("");
            menuItem.setBackground(color);
            menuBar.add(menuItem);
        }

        add(menuBar, BorderLayout.NORTH);
    }
}
