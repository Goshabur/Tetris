import jdk.nashorn.internal.ir.Block;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class GameTetris extends JFrame {
    public static void main(String[] args) {
        new GameTetris().go();//создаем новый обьект класса GameTetris и вызываем метод go, который определяется ниже.
    }

    final String Title = "Tetris";
    final int Side_Of_A_Square = 25;
    final int Field_Width = 10;//12 квадратов  -  это ширина поля
    final int Field_Height = 18;// 20 квадратов - его длинна
    final int Field_DX = 7;
    final int Field_DY = 26;
    final int Arc_Radius = 6;
    final int Up = 38;
    final int Down = 40;
    final int Right = 39;
    final int Left = 37;
    final int sleep = 1000;
    final int[][][] SHAPES = {
            {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {4, 0x00f0f0}}, // I       //Каждая фигурка
            {{0, 0, 0, 0}, {0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {4, 0xf0f000}}, // O
            {{1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x0000f0}}, // J
            {{0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf0a000}}, // L
            {{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x00f000}}, // S
            {{1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xa000f0}}, // T
            {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf00000}}  // Z
    };
    final int[] Scores = {100, 300, 700, 1500};//это массив очков (100 - за исчезновение 1 строки, 300 - 2, 700 - 3, 1500 - 4; больше никак)
    int GameScores = 0;//здесь мы храним очки за 1 игру.
    int[][] Field = new int[Field_Height + 1][Field_Width];//мы создали массив , а точнее само наше поле с заранее заданными параметрами
    JFrame frame;               //создали переменную окна
    Canvas CanvasPanel = new Canvas();   // создали ,,холст,, на котором рисуем а потом добавляем в окно.
    Random random = new Random();  // случайная генерация чисел(в нашем случае нужна для случайной генерации блоков)
    Figure figure = new Figure(); // создаём новый обьект класса Figure, фигуру(смотри класс Figure)
    Boolean Game_Over_Or_Not = false;

    final int[][] GAME_OVER_MSG = {
            {0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0},
            {1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1},
            {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},              // ещё одна константа, в ней зашифрованна фраза GameOver
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0},
            {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0},
            {1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0},
            {1, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0}};


    void go() {  // здесь будет запуск самого окна.
        frame = new JFrame(Title); // создали окно
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       //чтобы окно закрывалось при нажатии на крестик
        frame.setSize(Field_Width * Side_Of_A_Square + Field_DX, Field_Height * Side_Of_A_Square + Field_DY);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);


        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!Game_Over_Or_Not) {
                    if (e.getKeyCode() == Down) figure.drop();
                    if (e.getKeyCode() == Up) figure.rotate();
                    if (e.getKeyCode() == Left || e.getKeyCode() == Right) figure.move(e.getKeyCode());
                }
                CanvasPanel.repaint();
            }
        });


        frame.add(BorderLayout.CENTER, CanvasPanel);
        frame.setVisible(true);
        CanvasPanel.setBackground(Color.black);

        Arrays.fill(Field[Field_Height],1);

        // главный цикл нашей игры
        while (!Game_Over_Or_Not) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CanvasPanel.repaint();
            if (figure.isTouchGround()) {             //коснулась ли земли?(метод внизу)
                figure.leaveOnTheGround();  //покинуть фигуру(метод внизу)
                checkFillig();    //заполнена ли строка(метод внизу)
                figure = new Figure(); //новая фигура для управления
                Game_Over_Or_Not = figure.isCrossGround(); // пересекается ли фигура с землей при возникновении(т.е. мы дошли до потолка) ?(метод внизу)
            } else {
                figure.stepDown();   //фигура падает вниз на 1 шаг(метод внизу)    //это else , т.е. если фигура не коснулась земли
            }
        }


    }// доделываем окно, присоеденяем слушателя к событиям перемещения, пишем главный цикл игры

    void checkFillig() {
        int row = Field_Height - 1;
        int countFillRows = 0;
        while (row > 0) {
            int filled = 1;
            for (int col = 0; col < Field_Width; col++)
                filled *= Integer.signum(Field[row][col]);
            if (filled > 0) {
                countFillRows++;
                for (int i = row; i > 0; i--) System.arraycopy(Field[i - 1], 0, Field[i], 0, Field_Width);
            } else
                row--;
        }
        if (countFillRows > 0) {
            GameScores += Scores[countFillRows - 1];
            setTitle(Title + " : " + GameScores);
        }
    }//проверит заполнелись ли какие-нибудь строки


    class Figure {

        private ArrayList<Block> figure = new ArrayList<Block>();
        private int[][] shape = new int[4][4]; //массив для того чтобы мы могли крутить фигуры
        private int type, size, color;
        private int x = 3, y = 0;

        Figure() {
            type = random.nextInt(SHAPES.length);       //определяем тип, размер и цвет
            size = SHAPES[type][4][0];
            color = SHAPES[type][4][1];
            if (size == 4) y = -1;
            for (int i = 0; i < size; i++) {              //добавляем некоторые элементы массива SHAPES в масив shape
                System.arraycopy(SHAPES[type][i], 0, shape[i], 0, SHAPES[type][i].length);
            }
            createFromShape();


        }

        void createFromShape() {
            for (int x = 0; x < size; x++) {      //двойной цикл, он проходит по массиву shape и на основание этого массива он создаёт нашу фигурку
                for (int y = 0; y < size; y++) {
                    if (shape[y][x] == 1) figure.add(new Block(x + this.x, y + this.y));
                }
            }
        }

        boolean isCrossGround() {
            for (Block block : figure) if (Field[block.getY() + 1][block.getX()] > 0) return true;
            return false;
        }

        boolean isTouchGround() {   //метод который проверяет коснулся ли земли
            for (Block block : figure) {
                if (Field[block.getY() + 1][block.getX()] > 0) { // мы здесь проверяем следующую позицию y + 1
                    return true;
                }
            }


            return false;
        }


        void leaveOnTheGround() {  //метод с помощью которого мы оставляем фигуру и начинаем управлять следующей
            for (Block block : figure) Field[block.getY()][block.getX()] = color;
        }

        void stepDown() {            //определяет наш шаг вниз
            for (Block block : figure) block.SetY(block.getY() + 1); //изменяем положение кубика по y, он падает
            y++;

        }

        void drop() {// сбрасывание фигуры
            while (!isTouchGround()) stepDown();
        }

        boolean isTouchWall(int direction) {        //проверяем касается ли стены
            for (Block block : figure) {
                if (direction == Left && (block.getX() == 0 || Field[block.getY()][block.getX() - 1] > 0)) return true;
                if (direction == Right && (block.getX() == Field_Width - 1 || Field[block.getY()][block.getX() + 1] > 0))
                    return true;
            }
            return false;
        }


        boolean isWrongPosition() {
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    if (shape[y][x] == 1) {
                        if (y + this.y < 0) return true;
                        if (x + this.x < 0 || x + this.x > Field_Width - 1) return true;
                        if (Field[y + this.y][x + this.x] > 0) return true;
                    }
            return false;
        }

        void move(int direction) {
            if (!isTouchWall(direction)) {
                int dx = direction - 38;
                for (Block block : figure) block.SetX(block.getX() + dx);
                x += dx;
            }


        }

        void rotateShape(int direction) {
            for (int i = 0; i < size / 2; i++)
                for (int j = i; j < size - 1 - i; j++)
                    if (direction == Right) { // clockwise
                        int tmp = shape[size - 1 - j][i];
                        shape[size - 1 - j][i] = shape[size - 1 - i][size - 1 - j];
                        shape[size - 1 - i][size - 1 - j] = shape[j][size - 1 - i];
                        shape[j][size - 1 - i] = shape[i][j];
                        shape[i][j] = tmp;
                    } else { // counterclockwise
                        int tmp = shape[i][j];
                        shape[i][j] = shape[j][size - 1 - i];
                        shape[j][size - 1 - i] = shape[size - 1 - i][size - 1 - j];
                        shape[size - 1 - i][size - 1 - j] = shape[size - 1 - j][i];
                        shape[size - 1 - j][i] = tmp;
                    }
        }

        void rotate() {
            rotateShape(Right);
            if (!isWrongPosition()) {
                figure.clear();
                createFromShape();
            } else
                rotateShape(Left);
        }

        void paint(Graphics g) {
            for (Block block : figure)
                block.paint(g,color);//этот метод проходит по массиву блоков В ФИГУРЕ и ресуит их рандомным цветом и положеним
        }




    } //основной класс определяющий фигуры и действия с ними

    class Block {        // в этом классе определим 1 квадратик - то из чего состоят фигуры наши
        private int x, y;

        public Block(int x, int y) {
            SetX(x);
            SetY(y);
        }


        void SetX(int x) {
            this.x = x;
        }

        int getX() {
            return x;
        }                                //написали set и get для возможности изменения и получения значений x и y .

        void SetY(int y) {
            this.y = y;
        }

        int getY() {
            return y;
        }


        void paint(Graphics g, int color) {          //отрисовываем 1 блок определённого цвета
            g.setColor(new Color(color));
            g.drawRoundRect(x * Side_Of_A_Square + 1, y * Side_Of_A_Square + 1, Side_Of_A_Square - 2, Side_Of_A_Square - 2, Arc_Radius, Arc_Radius);
        }
    } //  тут у нас хранится всё связанное с 1 блоком его сеттеры и гетеры и его отрисовка определённого цвета

    class Canvas extends JPanel {       // my canvas for painting
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int x = 0; x < Field_Width; x++)   // рисуе крестики.
                for (int y = 0; y < Field_Height; y++) {
                    if (x < Field_Width - 1 && y < Field_Height - 1) {
                        g.setColor(Color.BLUE);
                        g.drawLine((x + 1) * Side_Of_A_Square - 2, (y + 1) * Side_Of_A_Square, (x + 1) * Side_Of_A_Square + 2, (y + 1) * Side_Of_A_Square);
                        g.drawLine((x + 1) * Side_Of_A_Square, (y + 1) * Side_Of_A_Square - 2, (x + 1) * Side_Of_A_Square, (y + 1) * Side_Of_A_Square + 2);
                    }
                    if (Field[y][x] > 0) {      //отрисовка приземлившихся фигур
                        g.setColor(new Color(Field[y][x]));
                        g.fill3DRect(x * Side_Of_A_Square + 1, y * Side_Of_A_Square + 1, Side_Of_A_Square - 1, Side_Of_A_Square - 1, true);
                    }
                }
            if (Game_Over_Or_Not) {
                g.setColor(Color.white);
                for (int y = 0; y < GAME_OVER_MSG.length; y++)
                    for (int x = 0; x < GAME_OVER_MSG[y].length; x++)
                        if (GAME_OVER_MSG[y][x] == 1) g.fill3DRect(x * 11 + 18, y * 11 + 160, 10, 10, true);
            } else
                figure.paint(g);
        }
    } // тут мы заполняем фигурки когда они касаются земли и отрисовываем Game over
}