import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Field extends JPanel implements ActionListener {

    private static final long DELAY = 80;
    private BufferedImage background;
    private BufferedImage freeCellImage;
    private BufferedImage bodyCellImage;
    private BufferedImage headCellImage;

    private static final int FIRST_X = 40;
    private static final int FIRST_Y = 35;
    private static final int WIDTH_OF_FIELD = 1020;
    private static final int HEIGHT_OF_FIELD = 500;
    private static final int LAST_X = 1060;
    private static final int LAST_Y = 535;
    private static final int WIDTH_OF_LINE = 5;
    private boolean gameOver = false;
    private boolean gameWin = false;
    private boolean gameStop = true;
    private boolean isFreeCellInMap = false;
    private Cell freeCell;
    private Cell HIDE_CELL;
    private ArrayList<Cell> snake;
    private ArrayList<Cell> map;
    private int score;
    private int level;
    private Clip clip_for_eating;
    private Clip clip_for_gameOver;
    private Clip clip_for_gameWin;
    private Clip clip_for_gaming;
    private Font dialogFont;

    public Field() {

        setBackground(Color.BLACK);
        showInitialMassage();
        addKeyListener(new KeyHandlerForPanel());
        addComponentListener(new ComponentHandlerForPanel());
        setFocusable(true);
        requestFocus();
        snake = new ArrayList<>();
        runThread();
    }

    private void gameSnake() {

        setFiles();
        makeMap();
        createFirstSnake();
        HIDE_CELL = snake.get(0);
        clip_for_gaming.setFramePosition(0);
        clip_for_gaming.start();

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {

            if (gameWin) {

                showGameWinDialog();
                clearSaveFile();
                break;
            } else if (gameOver) {

                showGameOverDialog();
                clearSaveFile();
                break;
            }

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            beforeTime = System.currentTimeMillis();
            repaint();

            jumpHead();
            makeFreeCell();
            if (!gameStop)
                moveSnake();

            eatApple();
            setScore();
            setLevel();
            gameOver = isGameOver();
            gameWin = isGameWin();
            saveGame();
        }
    }

    private void saveGame() {

        try {

            ObjectOutputStream saveSnakeStream = new ObjectOutputStream(Files.newOutputStream(Paths.get("saveSnake.bin")));
            for (Cell cell : snake)
                saveSnakeStream.writeObject(cell);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearSaveFile() {

        try {
            ObjectOutputStream saveSnakeStream = new ObjectOutputStream(Files.newOutputStream(Paths.get("saveSnake.bin")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawScoreAndLevel(g);
        drawSnake(g);
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawBackground(Graphics g) {

        g.drawImage(background, 0, 0, this);

        Graphics2D d = (Graphics2D) g;

        if (gameOver)
            d.setPaint(Color.RED);
        else if (gameWin)
            d.setPaint(Color.GREEN);
        else
            d.setPaint(Color.WHITE);

        float[] dash = {10};
        d.setStroke(new BasicStroke(WIDTH_OF_LINE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                10, dash, 5));
        d.draw(new Rectangle2D.Double(FIRST_X - WIDTH_OF_LINE, FIRST_Y - WIDTH_OF_LINE
                , WIDTH_OF_FIELD + 2 * WIDTH_OF_LINE, HEIGHT_OF_FIELD + 2 * WIDTH_OF_LINE));
    }

    private void drawCells(Graphics g, Cell cell) {

        if (cell.kindOfCell == Cell.KindOfCell.HideCell)
            return;

        if (cell.kindOfCell == Cell.KindOfCell.BodyCell)
            g.drawImage(bodyCellImage, cell.getX(), cell.getY(), this);
        if (cell.kindOfCell == Cell.KindOfCell.FreeCell)
            g.drawImage(freeCellImage, cell.getX(), cell.getY(), this);
        if (cell.kindOfCell == Cell.KindOfCell.HeadCell)
            g.drawImage(headCellImage, cell.getX(), cell.getY(), this);
    }

    private void drawSnake(Graphics g) {

        if (isFreeCellInMap)
            drawCells(g, freeCell);

        for (Cell cell : snake)
            drawCells(g, cell);
    }

    private void drawScoreAndLevel(Graphics g) {

        g.setFont(dialogFont);
        g.setColor(new Color(158, 11, 15));
        g.drawString(String.format("Level  :  " + level + "  ,  " + "Score  :  " + score), LAST_X - 230, LAST_Y - 20);
    }

    private void createFirstSnake() {

        int num = 0;
        try {

            ObjectInputStream saveSnakeStream = new ObjectInputStream(Files.newInputStream(Paths.get("saveSnake.bin")));

            while (true) {

                Cell cell = (Cell) saveSnakeStream.readObject();
                snake.add(cell);
                num++;
            }

        } catch (IOException | ClassNotFoundException e) {

            if (num == 0) {

                Cell hideCell = new Cell(540, 215, Cell.KindOfCell.HideCell,
                        Cell.fDirection, Cell.nDirection);
                snake.add(hideCell);

                Cell head = new Cell(540, 235, Cell.KindOfCell.HeadCell,
                        Cell.fDirection, Cell.nDirection);
                snake.add(head);

                for (int i = 0; i < 4; i++)
                    addCell();
            }
        }
    }

    private void addCell() {

        Cell cell = snake.get(snake.size() - 1);
        Cell newCell = new Cell(cell.getX() - cell.hDirection, cell.getY() - cell.vDirection,
                Cell.KindOfCell.BodyCell, cell.hDirection, cell.vDirection);
        snake.add(newCell);
    }

    private void moveSnake() {

        Cell temp = snake.get(snake.size() - 1);

        for (int i = snake.size() - 2; i >= 0; i--) {

            temp.setX(snake.get(i).getX());
            temp.setY(snake.get(i).getY());
            temp = snake.get(i);
        }

        HIDE_CELL.setX(HIDE_CELL.getX() + HIDE_CELL.hDirection);
        HIDE_CELL.setY(HIDE_CELL.getY() + HIDE_CELL.vDirection);
    }

    private boolean isGameOver() {

        Cell head = snake.get(1);

        for (Cell cell : snake)
            if (cell != head)
                if (head.getX() == cell.getX() && head.getY() == cell.getY())
                    return true;

        return false;
    }

    private void runThread() {

        (new Thread() {

            public void run() {
                gameSnake();
            }
        }).start();
    }

    private void makeMap() {

        int i = FIRST_X;
        int j = FIRST_Y;
        Cell temp;
        map = new ArrayList<>();

        for (int r = 0; r < 25 * 51; r++) {

            temp = new Cell(i, j, Cell.KindOfCell.VoidCell);
            map.add(temp);
            i += Cell.pDirection;

            if (i == LAST_X) {
                i = FIRST_X;
                j += Cell.pDirection;
            }
        }
    }

    private boolean isCellInSnake(Cell cell) {

        for (Cell temp : snake)
            if (cell.getX() == temp.getX() && cell.getY() == temp.getY())
                return true;

        return false;
    }

    private void makeFreeCell() {

        SecureRandom random = new SecureRandom();
        Cell temp = map.get(random.nextInt(map.size()));

        if (!isFreeCellInMap) {
            do {

                freeCell = new Cell(temp.getX(), temp.getY(), Cell.KindOfCell.FreeCell);
                temp = map.get(random.nextInt(map.size()));
            } while (isCellInSnake(freeCell));

            isFreeCellInMap = true;
        }
    }

    private void eatApple() {

        int i = snake.get(1).getX();
        int j = snake.get(1).getY();
        int i1 = freeCell.getX();
        int j1 = freeCell.getY();

        if (i == i1 && j == j1) {

            clip_for_eating.setFramePosition(0);
            clip_for_eating.start();

            isFreeCellInMap = false;
            addCell();
        }
    }

    private void jumpHead() {

        switch (HIDE_CELL.getX()) {

            case FIRST_X - Cell.WIDTH_OF_CELL:
                HIDE_CELL.setX(LAST_X - Cell.WIDTH_OF_CELL);
                break;
            case LAST_X:
                HIDE_CELL.setX(FIRST_X);
                break;
        }

        switch (HIDE_CELL.getY()) {

            case FIRST_Y - Cell.WIDTH_OF_CELL:
                HIDE_CELL.setY(LAST_Y - Cell.WIDTH_OF_CELL);
                break;
            case LAST_Y:
                HIDE_CELL.setY(FIRST_Y);
                break;
        }
    }

    private void setScore() {
        score = snake.size() - 6;
    }

    private void setLevel() {

        if (score < 50)
            level = score / 10 + 1;
        else if (score < 80)
            level = 5 + (score - 40) / 15;
        else
            level = 8 + (score - 70) / 20;
    }

    private boolean isGameWin() {

        if (level == 11) return true;

        return false;
    }

    private void showGameWinDialog() {

        repaint();
        clip_for_gaming.stop();
        clip_for_gameWin.start();

        JLabel label = new JLabel("   wow you win");
        Font scoreFont;
        try {

            scoreFont = Font.createFont(Font.TRUETYPE_FONT,
                    ClassLoader.getSystemResourceAsStream("Source/EarthKid.ttf")).deriveFont(19f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(scoreFont);
            label.setFont(scoreFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
        Icon icon = new ImageIcon(getClass().getResource("Source/icon.png"));
        String[] strings = {"Play again", "   Exit   "};

        int answer = JOptionPane.showOptionDialog(this, label, "Congratulations",
                JOptionPane.DEFAULT_OPTION, 0, icon, strings, strings[0]);

        switch (answer) {

            case 0:
                repeatGame();
                break;
            case 1:
                System.exit(0);
                break;
        }
    }

    private void showGameOverDialog() {

        repaint();
        clip_for_gaming.stop();
        clip_for_gameOver.start();

        JLabel label = new JLabel("   game over ");
        Font scoreFont;
        try {

            scoreFont = Font.createFont(Font.TRUETYPE_FONT,
                    ClassLoader.getSystemResourceAsStream("Source/EarthKid.ttf")).deriveFont(19f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(scoreFont);
            label.setFont(scoreFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
        Icon icon = new ImageIcon(getClass().getResource("/Source/icon.png"));
        String[] strings = {"Play again", "   Exit   "};

        int answer = JOptionPane.showOptionDialog(this, label, "  gamever  ",
                JOptionPane.DEFAULT_OPTION, 0, icon, strings, strings[0]);

        switch (answer) {

            case 0:
                repeatGame();
                break;
            case 1:
                System.exit(0);
                break;
        }
    }

    private void showInitialMassage() {

        JLabel label = new JLabel("Press space key to start and stop");
        Font scoreFont;
        try {

            scoreFont = Font.createFont(Font.TRUETYPE_FONT,
                    ClassLoader.getSystemResourceAsStream("Source/EarthKid.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(scoreFont);
            label.setFont(scoreFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        }
        Icon icon = new ImageIcon(getClass().getResource("Source/icon.png"));
        String[] strings = {"    OK    ", "   Exit   "};

        int answer = JOptionPane.showOptionDialog(this, label, "Your Welcome",
                JOptionPane.DEFAULT_OPTION, 0, icon, strings, strings[0]);

        if (answer == 1)
            System.exit(0);
    }

    private void repeatGame() {

        clip_for_gaming.close();
        clip_for_eating.close();
        clip_for_gameWin.close();
        clip_for_gameOver.close();
        snake.clear();
        gameWin = false;
        gameStop = true;
        gameOver = false;
        runThread();
    }

    private void setFiles() {

        try {

            dialogFont = Font.createFont(Font.TRUETYPE_FONT,
                    ClassLoader.getSystemResourceAsStream("Source/myFont.ttf")).deriveFont(19f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(dialogFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }

        try {
            bodyCellImage = ImageIO.read(getClass().getResource("Source/head.png"));
            freeCellImage = ImageIO.read(getClass().getResource("Source/apple.png"));
            headCellImage = ImageIO.read(getClass().getResource("Source/cell.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            background = ImageIO.read(getClass().getResource("Source/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AudioInputStream music_for_eating = AudioSystem.getAudioInputStream(
                    getClass().getResource("Source/eat.wav"));
            clip_for_eating = AudioSystem.getClip();
            clip_for_eating.open(music_for_eating);
            FloatControl volume = (FloatControl) clip_for_eating.getControl(FloatControl.Type.MASTER_GAIN);
            double gain = 2;
            float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            volume.setValue(dB);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            AudioInputStream music_for_eating = AudioSystem.getAudioInputStream(
                    getClass().getResource("Source/gem.wav"));
            clip_for_gaming = AudioSystem.getClip();
            clip_for_gaming.open(music_for_eating);
            clip_for_gaming.loop(Clip.LOOP_CONTINUOUSLY);
            FloatControl volume = (FloatControl) clip_for_gaming.getControl(FloatControl.Type.MASTER_GAIN);
            double gain = 0.1;
            float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            volume.setValue(dB);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            AudioInputStream music_for_gameWin = AudioSystem.getAudioInputStream(
                    getClass().getResource("Source/eat.wav")
            );
            clip_for_gameWin = AudioSystem.getClip();
            clip_for_gameWin.open(music_for_gameWin);
            FloatControl volume = (FloatControl) clip_for_gameWin.getControl(FloatControl.Type.MASTER_GAIN);
            double gain = 0.2;
            float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            volume.setValue(dB);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            AudioInputStream music_for_gameOver = AudioSystem.getAudioInputStream(
                    getClass().getResource("Source/gameover.wav"));
            clip_for_gameOver = AudioSystem.getClip();
            clip_for_gameOver.open(music_for_gameOver);
            FloatControl volume = (FloatControl) clip_for_gameOver.getControl(FloatControl.Type.MASTER_GAIN);
            double gain = 0.25;
            float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            volume.setValue(dB);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private int saveX;
    private int saveY;

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private class KeyHandlerForPanel extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            super.keyTyped(e);

            if (snake.size() < 1)
                return;


            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                gameStop = !gameStop;

            if (HIDE_CELL.getX() == saveX && HIDE_CELL.getY() == saveY)
                return;

            if (e.getKeyCode() == KeyEvent.VK_RIGHT && HIDE_CELL.getVDirection() != Cell.fDirection) {

                HIDE_CELL.setHDirection(Cell.pDirection);
                HIDE_CELL.setVDirection(Cell.fDirection);
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT && HIDE_CELL.getVDirection() != Cell.fDirection) {

                HIDE_CELL.setHDirection(Cell.nDirection);
                HIDE_CELL.setVDirection(Cell.fDirection);
            } else if (e.getKeyCode() == KeyEvent.VK_UP && HIDE_CELL.getHDirection() != Cell.fDirection) {

                HIDE_CELL.setHDirection(Cell.fDirection);
                HIDE_CELL.setVDirection(Cell.nDirection);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && HIDE_CELL.getHDirection() != Cell.fDirection) {

                HIDE_CELL.setHDirection(Cell.fDirection);
                HIDE_CELL.setVDirection(Cell.pDirection);
            }

            saveX = HIDE_CELL.getX();
            saveY = HIDE_CELL.getY();
        }
    }

    private class ComponentHandlerForPanel implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            requestFocus();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            requestFocus();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            requestFocus();
        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }
}