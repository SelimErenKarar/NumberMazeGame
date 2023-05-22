import enigma.core.Enigma;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import enigma.console.TextAttributes;
import java.awt.Color;

public class Game {
    public enigma.console.Console cn = Enigma.getConsole("Number Maze", 100, 30, 15, 1);
    public KeyListener klis;

    public int keypr; // key pressed?
    public int rkey; // key (for press/release)

    Random rnd = new Random();

    int x = 55, y = 23;
    Object[][] map = new Object[y][x];
    Number user;
    boolean gameover = false;
    int score = 0;
    boolean userMove = true;
    int step = 1;
    int freeze = 0;
    int freezeCounter = 4;

    public static TextAttributes blue = new TextAttributes(Color.blue);
    public static TextAttributes green = new TextAttributes(Color.green);
    public static TextAttributes yellow = new TextAttributes(Color.yellow);
    public static TextAttributes red = new TextAttributes(Color.red);
    public static TextAttributes white = new TextAttributes(Color.white);
    public static TextAttributes cyan = new TextAttributes(Color.cyan);

    Scanner sc = new Scanner(System.in);

    String difficulty;

    Game() throws Exception { // --- Contructor
    	
        cn.getTextWindow().setCursorPosition(35, 10);
        System.out.println("Please choose difficulty");
        cn.getTextWindow().setCursorPosition(35, 11);
        cn.setTextAttributes(green);
        System.out.println("1. Normal Mode");
        cn.getTextWindow().setCursorPosition(35, 12);
        cn.setTextAttributes(red);
        System.out.println("2. Hard Mode");
        cn.getTextWindow().setCursorPosition(35, 13);
        cn.setTextAttributes(white);
        System.out.print("--> ");
        
        while (true) {
        	cn.getTextWindow().setCursorPosition(39, 13);
        	cn.setTextAttributes(white);
        	difficulty = sc.next();
        	if(!(difficulty.equals("1") || difficulty.equals("2"))) {
        		cn.getTextWindow().setCursorPosition(35, 15);
        		cn.setTextAttributes(red);
        		System.out.print("You have entered an invalid number. Please try again.");
        		cn.setTextAttributes(white);
        		cn.getTextWindow().setCursorPosition(39, 13);
        		System.out.print("                                                      ");
        	}
        	else {
        		cn.getTextWindow().setCursorPosition(35, 15);
        		System.out.print("														");
        		break;
        	}
		} 
        
        cn.getTextWindow().setCursorPosition(35, 10);
        System.out.print("						");

        map = createMap();
        insertNumbers();
        user = user();
        printMap();

        if(difficulty.equals("1")) {
            cn.getTextWindow().setCursorPosition(75, 1);
            System.out.print("Difficulty : ");
            cn.setTextAttributes(green);
            System.out.print("Normal");
        }
        else {
            cn.getTextWindow().setCursorPosition(75, 1);
            System.out.print("Difficulty : ");
            cn.setTextAttributes(red);
            System.out.print("Hard");
        }
        cn.setTextAttributes(white);
        
        Queue inputs = createInputs();
        printInputs(inputs);

        Stack leftBP = createBackPack();
        Stack rightBP = createBackPack();

        printOthers();

        System.out.println();

        klis = new KeyListener() {
            public void keyTyped(KeyEvent e) {}

            public void keyPressed(KeyEvent e) {
                if (keypr == 0) {
                    keypr = 1;
                    rkey = e.getKeyCode();
                }
            }

            public void keyReleased(KeyEvent e) {}
        };
        cn.getTextWindow().addKeyListener(klis);


        int time = 0;

        while (!gameover) {
            if (keypr == 1) { // if keyboard button pressed
                String key = "";
                if(userMove) {
                    if (rkey == KeyEvent.VK_LEFT) {
                        key = "left";
                    }
                    if (rkey == KeyEvent.VK_RIGHT) {
                        key = "right";
                    }
                    if (rkey == KeyEvent.VK_UP) {
                        key = "up";
                    }
                    if (rkey == KeyEvent.VK_DOWN) {
                        key = "down";
                    }
                }
                if (rkey == KeyEvent.VK_Q)
                    swap(rightBP, leftBP);
                if (rkey == KeyEvent.VK_W)
                    swap(leftBP, rightBP);

                gameover = playerMovement(key, leftBP);
                keypr = 0; // last action
            }
            printBackPack(leftBP, 60, 15);
            printBackPack(rightBP, 66, 15);


            Object[][] map2 = new Object[23][55];
            for (int i = 0; i < 23; i++) {
                for (int j = 0; j < 55; j++) {
                    map2[i][j] = map[i][j];
                }
            }

            clearPath(map2);
            redPathFinding(map2);
            printMap();

            int speed = speed();
            if(step % speed == 0){
                gameover = yellowMovements(leftBP);
                gameover = redMovements(leftBP);
            }

            if(step % 20 == 0) {
                time++;
                if(!userMove) {
                    cn.getTextWindow().setCursorPosition(3, 25);
                    System.out.print("You are frozen :  ");
                    cn.setTextAttributes(cyan);
                    System.out.println(freezeCounter + " seconds");
                    freezeCounter--;
                }
                else {
                    cn.getTextWindow().setCursorPosition(3, 25);
                    System.out.print("												");
                }
            }

            if(step % 100 == 0) {
                spawnNumber(inputs);
            }

            if(!userMove && step == freeze + 80) {
                userMove = true;
                user.setValue(2);
            }

            score = MatchAndScore(leftBP, rightBP);

            cn.getTextWindow().setCursorPosition(69, 20);
            System.out.print(score);

            cn.getTextWindow().setCursorPosition(69, 22);
            System.out.print(time);

            Thread.sleep(50);
            step++;

            if(gameover) {
                cn.getTextWindow().setCursorPosition(2, 25);
                System.out.println("-GAME OVER-");
                String filepath = "Music/pacmandeath.wav";
                playDeathEffect(filepath);
            }
        }
    }

    public Object[][] createMap() throws IOException {
        File f = new File("map.txt"); // File operations
        if (f.exists()) {
            FileReader file = new FileReader("map.txt");
            BufferedReader bReader = new BufferedReader(file);

            for (int i = 0; i < y; i++) {
                int j = 0;
                while(j < x) {
                    char c = (char) bReader.read();
                    if(c == ' ' || c == '#') {
                        map[i][j] = c;
                        j++;
                    }
                }
            }

            bReader.close();

            return map;

        } else {
            System.out.println("File does not exist !!!");
            return null;
        }
    }

    public void printMap() {
        int x = 55, y = 23;
        cn.getTextWindow().setCursorPosition(0, 0);
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if(map[i][j] instanceof Number) {
                    Number n = (Number) map[i][j];
                    if(n.getColor().equals("green")) {
                        cn.setTextAttributes(green);
                        System.out.print(n.getValue());
                    }
                    else if(n.getColor().equals("yellow")) {
                        cn.setTextAttributes(yellow);
                        System.out.print(n.getValue());
                    }
                    else if(n.getColor().equals("red")) {
                        cn.setTextAttributes(red);
                        System.out.print(n.getValue());
                    }
                    else if(n.getColor().equals("blue")) {
                        cn.setTextAttributes(blue);
                        System.out.print(n.getValue());
                    }
                    cn.setTextAttributes(white);
                }
                else {
                    System.out.print(map[i][j]);
                }
            }
            System.out.println();
        }
    }

    public Number randomNumber() {
        int[] numbers = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int k = rnd.nextInt(100) + 1;
        int a;
        if(difficulty.equals("1")) {
            if(k > 25) {
                a = rnd.nextInt(3);
            }
            else if(k <= 25 && k > 5) {
                a = rnd.nextInt(3) + 3;
            }
            else {
                a = rnd.nextInt(3) + 6;
            }
        }
        else {
            if(k > 50) {
                a = rnd.nextInt(3);
            }
            else if(k <= 50 && k > 10) {
                a = rnd.nextInt(3) + 3;
            }
            else {
                a = rnd.nextInt(3) + 6;
            }
        }


        Number number = new Number(numbers[a]);

        return number;
    }

    public Queue createInputs() {
        Queue inputs = new Queue(10000);

        for (int i = 0; i < 10; i++) {
            Number input = randomNumber();

            inputs.enqueue(input);
        }

        return inputs;
    }

    public void printInputs(Queue queue) {
        cn.getTextWindow().setCursorPosition(60, 1);
        System.out.print("Input");

        for (int i = 0; i < queue.size(); i++)
        {
            cn.getTextWindow().setCursorPosition(i + 60, 2);
            System.out.print("<");
            cn.getTextWindow().setCursorPosition(i + 60, 3);
            if (((Number) queue.peek()).getColor().equals("green")) {
            	cn.setTextAttributes(green);
            }
            else if (((Number) queue.peek()).getColor().equals("yellow")) {
            	cn.setTextAttributes(yellow);
            }
            else {
            	cn.setTextAttributes(red);
            }
            System.out.print(((Number) queue.peek()).getValue());
            cn.setTextAttributes(white);
            cn.getTextWindow().setCursorPosition(i + 60, 4);
            System.out.print("<");
            queue.enqueue(queue.dequeue());
        }
    }

    public Stack createBackPack() {
        int stackSize = 8;
        Stack s = new Stack(stackSize);

        return s;
    }

    public void printBackPack(Stack stack, int a, int b) {
        Stack temp = new Stack(8);
        int k = 0;
        for (int i = 0; i < 8; i++)
        {
            if (stack.peek() != null) {
                cn.getTextWindow().setCursorPosition(a + 2, b - stack.size() + 1);
                if (((Number) stack.peek()).getColor().equals("green")) {
                	cn.setTextAttributes(green);
                }
                else if (((Number) stack.peek()).getColor().equals("yellow")) {
                	cn.setTextAttributes(yellow);
                }
                else {
                	cn.setTextAttributes(red);
                }
                System.out.print(((Number) stack.peek()).getValue());
                temp.push(stack.pop());
                k++;
                cn.setTextAttributes(white);
            }
            else {
                cn.getTextWindow().setCursorPosition(a + 2, b - i);
                System.out.print(" ");
            }
        }
        for (int i = 0; i < k; i++)
        {
            stack.push(temp.pop());
        }

        for (int i = 0; i < 8; i++)
        {
            cn.getTextWindow().setCursorPosition(a, b - i);
            System.out.print("|");

            cn.getTextWindow().setCursorPosition(a + 4, b - i);
            System.out.print("|");
        }
        for (int i = 0; i < 2; i++) {
            cn.getTextWindow().setCursorPosition(a + i*4, b + 1);
            System.out.print("+");
        }
        for (int i = 0; i < 3; i++) {
            cn.getTextWindow().setCursorPosition(a + 1 + i, b + 1);
            System.out.print("-");
        }
    }

    public void swap(Stack s1, Stack s2) {
        if(s1.peek() != null) {
            if(!s2.isFull()) {
                s2.push(s1.pop());
            }
            else {
                s2.pop();
                s2.push(s1.pop());
            }
        }
    }

    public void pushBackpack(Number number, Stack left){
        if (left.isFull()) {
            left.pop();
        }
        left.push(number);
    }

    public void printOthers() {
        cn.getTextWindow().setCursorPosition(60, 7);
        System.out.print("Backpacks");

        cn.getTextWindow().setCursorPosition(60, 17);
        System.out.print("Left");
        cn.getTextWindow().setCursorPosition(62, 18);
        System.out.print("Q");
        cn.getTextWindow().setCursorPosition(66, 17);
        System.out.print("Right");
        cn.getTextWindow().setCursorPosition(68, 18);
        System.out.print("W");

        cn.getTextWindow().setCursorPosition(60, 20);
        System.out.print("Score : ");

        cn.getTextWindow().setCursorPosition(60, 22);
        System.out.print("Time :  ");
    }

    public void insertNumbers() {
        int i = 0;
        while (i < 25) {
            int a = rnd.nextInt(55);
            int b = rnd.nextInt(23);

            if(map[b][a] == (Object) ' ') {
                map[b][a] = randomNumber();
                i++;
            }
        }
    }

    public Number user() {
        Number user = new Number(5);
        user.setColor("blue");
        while(true) {
            int userA = rnd.nextInt(55);
            int userB = rnd.nextInt(23);
            if(map[userB][userA] == (Object) ' ') {
                map[userB][userA] = user;
                break;
            }
        }

        return user;
    }

    public boolean playerMovement(String key, Stack left) {
        boolean flag = true;
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if(map[i][j] instanceof Number) {
                    if(((Number) map[i][j]).getColor().equals("blue")) {
                        if(key.equals("left") && map[i][j - 1] != (Object) '#') {
                            if(map[i][j - 1] instanceof Number && ((Number) map[i][j - 1]).getValue() <= user.getValue()) {
                                pushBackpack((Number) map[i][j - 1], left);
                            }
                            else if(map[i][j - 1] instanceof Number && ((Number) map[i][j - 1]).getValue() > user.getValue()) {
                                map[i][j] = ' ';
                                gameover = true;
                                break;
                            }
                            map[i][j - 1] = user;
                            map[i][j] = ' ';
                        }
                        else if(key.equals("right") && map[i][j + 1] != (Object) '#') {
                            if(map[i][j + 1] instanceof Number && ((Number) map[i][j + 1]).getValue() <= user.getValue()) {
                                pushBackpack((Number) map[i][j + 1], left);
                            }
                            else if(map[i][j + 1] instanceof Number && ((Number) map[i][j + 1]).getValue() > user.getValue()) {
                                map[i][j] = ' ';
                                gameover = true;
                                break;
                            }
                            map[i][j + 1] = user;
                            map[i][j] = ' ';
                        }
                        else if(key.equals("up") && map[i - 1][j] != (Object) '#') {
                            if(map[i - 1][j] instanceof Number && ((Number) map[i - 1][j]).getValue() <= user.getValue()) {
                                pushBackpack((Number) map[i - 1][j], left);
                            }
                            else if(map[i - 1][j] instanceof Number && ((Number) map[i - 1][j]).getValue() > user.getValue()) {
                                map[i][j] = ' ';
                                gameover = true;
                                break;
                            }
                            map[i - 1][j] = user;
                            map[i][j] = ' ';
                        }
                        else if(key.equals("down") && map[i + 1][j] != (Object) '#') {
                            if(map[i + 1][j] instanceof Number && ((Number) map[i + 1][j]).getValue() <= user.getValue()) {
                                pushBackpack((Number) map[i + 1][j], left);
                            }
                            else if(map[i + 1][j] instanceof Number && ((Number) map[i + 1][j]).getValue() > user.getValue()) {
                                map[i][j] = ' ';
                                gameover = true;
                                break;
                            }
                            map[i + 1][j] = user;
                            map[i][j] = ' ';
                        }
                        flag = false;
                        break;
                    }
                }
            }
            if(!flag) {
                break;
            }
            if(gameover) {
                break;
            }
        }

        return gameover;
    }

    public void spawnNumber(Queue inputs) {
        while(true) {
            int a = rnd.nextInt(55);
            int b = rnd.nextInt(23);
            if(map[b][a] == (Object) ' ') {
                map[b][a] = inputs.dequeue();
                inputs.enqueue(randomNumber());
                printInputs(inputs);
                printMap();
                break;
            }
        }
    }

    public boolean yellowMovements(Stack left){
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map[i][j] instanceof Number) {
                    if(((Number) map[i][j]).getColor().equals("yellow") && !((Number) map[i][j]).isMoved()) {
                        boolean[] canMove = new boolean[4];
                        boolean flag = false;
                        if (map[i][j - 1] == (Object) ' ' || map[i][j - 1] == user || map[i][j - 1] == (Object) '.' ){
                            canMove[0] = true;
                            flag = true;
                        }
                        if (map[i][j + 1] == (Object) ' ' || map[i][j + 1] == user || map[i][j + 1] == (Object) '.'){
                            canMove[1] = true;
                            flag = true;
                        }
                        if (map[i - 1][j] == (Object) ' ' || map[i - 1][j] == user || map[i - 1][j] == (Object) '.'){
                            canMove[2] = true;
                            flag = true;
                        }
                        if (map[i + 1][j] == (Object) ' ' || map[i + 1][j] == user || map[i + 1][j] == (Object) '.'){
                            canMove[3] = true;
                            flag = true;
                        }
                        if(!flag){
                            continue;
                        }

                        Object yellowNumber = map[i][j];
                        int randomDirection;
                        while (true) {
                            randomDirection = rnd.nextInt(4);
                            if (canMove[randomDirection]) {
                                break;
                            }
                        }
                        if (randomDirection == 0){	// left
                            if (map[i][j - 1] == (Object) ' ' || map[i][j - 1] == (Object) '.' ) {
                                map[i][j - 1] = yellowNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i][j - 1] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i][j - 1] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i][j - 1] = yellowNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else if (randomDirection == 1){	// right
                            if (map[i][j + 1] == (Object) ' ' || map[i][j + 1] == (Object) '.' ) {
                                map[i][j + 1] = yellowNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i][j + 1] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i][j + 1] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i][j + 1] = yellowNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else if (randomDirection == 2){ 	// up
                            if (map[i - 1][j] == (Object) ' ' || map[i - 1][j] == (Object) '.' ) {
                                map[i - 1][j] = yellowNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i - 1][j] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i - 1][j] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i - 1][j] = yellowNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else {		// down
                            if (map[i + 1][j] == (Object) ' ' || map[i + 1][j] == (Object) '.' ) {
                                map[i + 1][j] = yellowNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i + 1][j] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i + 1][j] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i + 1][j] = yellowNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        ((Number) yellowNumber).setMoved(true);
                    }
                }
            }
            if (gameover){
                break;
            }
        }

        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map[i][j] instanceof Number) {
                    if(((Number) map[i][j]).getColor().equals("yellow")) {
                        ((Number)map[i][j]).setMoved(false);
                    }
                }
            }
        }

        return gameover;
    }

    public void redPathFinding(Object[][] map2) {
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map2[i][j] instanceof Number) {
                    if(((Number) map2[i][j]).getColor().equals("red")) {
                        Stack path = new Stack(10000);
                        boolean pathOver = false;
                        int a = 0;

                        Coordinate coo = new Coordinate(j, i, a);
                        path.push(coo);
                        a++;

                        do {
                            if(path.peek() == null) {
                                break;
                            }
                            int checkX = ((Coordinate) path.peek()).getX();
                            int checkY = ((Coordinate) path.peek()).getY();

                            boolean userFound = false;
                            if(map2[checkY][checkX - 1] == user) {
                                userFound = true;
                            }
                            else if(map2[checkY][checkX + 1] == user) {
                                userFound = true;
                            }
                            else if(map2[checkY - 1][checkX] == user) {
                                userFound = true;
                            }
                            else if(map2[checkY + 1][checkX] == user) {
                                userFound = true;
                            }

                            if(!userFound) {
                                Coordinate previous = new Coordinate(checkX, checkY, 0);
                                if(!(map2[checkY][checkX] instanceof Number)) {
                                    previous = (Coordinate) map2[checkY][checkX];
                                }
                                boolean flag = false;
                                if (map2[checkY][checkX - 1] == (Object) ' ' || map2[checkY][checkX - 1] == (Object) '.'){
                                    flag = true;
                                    coo = new Coordinate(checkX - 1, checkY, previous, a);
                                    path.push(coo);
                                    map2[checkY][checkX - 1] = coo;
                                    a++;
                                }
                                if (map2[checkY - 1][checkX] == (Object) ' ' || map2[checkY - 1][checkX] == (Object) '.'){
                                    flag = true;
                                    coo = new Coordinate(checkX, checkY - 1, previous, a);
                                    path.push(coo);
                                    map2[checkY - 1][checkX] = coo;
                                    a++;
                                }
                                if (map2[checkY][checkX + 1] == (Object) ' ' || map2[checkY][checkX + 1] == (Object) '.'){
                                    flag = true;
                                    coo = new Coordinate(checkX + 1, checkY, previous, a);
                                    path.push(coo);
                                    map2[checkY][checkX + 1] = coo;
                                    a++;
                                }
                                if (map2[checkY + 1][checkX] == (Object) ' ' || map2[checkY + 1][checkX] == (Object) '.'){
                                    flag = true;
                                    coo = new Coordinate(checkX, checkY + 1, previous, a);
                                    path.push(coo);
                                    map2[checkY + 1][checkX] = coo;
                                    a++;
                                }
                                if(!flag) {
                                    path.pop();
                                    a--;
                                }
                            }
                            else {
                                pathOver = true;
                                while(checkY != i || checkX != j) {
                                    map[checkY][checkX] = (Object) '.';
                                    if (!(map[checkY][checkX] instanceof Number) && !(map2[checkY][checkX] instanceof Number)) {
                                        int preCheckY = ((Coordinate) map2[checkY][checkX]).getPrevious().getY();
                                        int preCheckX = ((Coordinate) map2[checkY][checkX]).getPrevious().getX();
                                        checkY = preCheckY;
                                        checkX = preCheckX;
                                    }
                                }
                                clearCoords(map2);
                            }
                        } while(!pathOver);
                    }
                }
            }
        }
    }

    public boolean redMovements(Stack left){
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map[i][j] instanceof Number) {
                    if(((Number) map[i][j]).getColor().equals("red") && !((Number) map[i][j]).isMoved()) {
                        boolean[] canMove = new boolean[4];
                        boolean flag = false;
                        if (map[i][j - 1] == user || map[i][j - 1] == (Object) '.' ){
                            canMove[0] = true;
                            flag = true;
                        }
                        if (map[i][j + 1] == user || map[i][j + 1] == (Object) '.'){
                            canMove[1] = true;
                            flag = true;
                        }
                        if (map[i - 1][j] == user || map[i - 1][j] == (Object) '.'){
                            canMove[2] = true;
                            flag = true;
                        }
                        if (map[i + 1][j] == user || map[i + 1][j] == (Object) '.'){
                            canMove[3] = true;
                            flag = true;
                        }
                        if(!flag){
                            continue;
                        }

                        Object redNumber = map[i][j];
                        int randomDirection;
                        do {
                            randomDirection = rnd.nextInt(4);
                        } while (!canMove[randomDirection]);

                        if (randomDirection == 0){	// left
                            if (map[i][j - 1] == (Object) '.') {
                                map[i][j - 1] = redNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i][j - 1] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i][j - 1] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i][j - 1] = redNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else if (randomDirection == 1){	// right
                            if (map[i][j + 1] == (Object) '.' ) {
                                map[i][j + 1] = redNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i][j + 1] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i][j + 1] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i][j + 1] = redNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else if (randomDirection == 2){ 	// up
                            if (map[i - 1][j] == (Object) '.' ) {
                                map[i - 1][j] = redNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i - 1][j] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i - 1][j] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i - 1][j] = redNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        else {		// down
                            if (map[i + 1][j] == (Object) '.' ) {
                                map[i + 1][j] = redNumber;
                                map[i][j] = ' ';
                            }
                            else if(map[i + 1][j] == user){
                                if (((Number) map[i][j]).getValue() <= user.getValue()){
                                    pushBackpack((Number) map[i][j], left);
                                    map[i + 1][j] = user;
                                    map[i][j] = ' ';

                                }
                                else{
                                    gameover = true;
                                    map[i + 1][j] = redNumber;
                                    map[i][j] = ' ';
                                    printMap();
                                    break;
                                }
                            }
                        }
                        ((Number) redNumber).setMoved(true);
                    }
                }
            }
            if (gameover){
                break;
            }
        }

        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map[i][j] instanceof Number) {
                    if(((Number) map[i][j]).getColor().equals("red")) {
                        ((Number)map[i][j]).setMoved(false);
                    }
                }
            }
        }

        return gameover;
    }

    public int MatchAndScore(Stack left, Stack right){
        Stack temp1 = new Stack(left.size());
        Stack temp2 = new Stack(right.size());
        int sizeLeft = left.size();
        int sizeRight = right.size();
        int diff;
        boolean flag;

        if(sizeLeft <= sizeRight) {
            diff = sizeRight - sizeLeft;
            flag = true;
            for (int i = 0; i < diff; i++) {
                temp2.push(right.pop());
            }
        }
        else {
            diff = sizeLeft - sizeRight;
            flag = false;
            for (int i = 0; i < diff; i++) {
                temp1.push(left.pop());
            }
        }

        if(!left.isEmpty() && !right.isEmpty()) {
            if(((Number) left.peek()).getValue() == ((Number) right.peek()).getValue()) {
                score += ((Number) left.peek()).getValue() * ((Number) left.peek()).getFactor();
                left.pop();
                right.pop();
                user.setValue(user.getValue() + 1);
                freezeCounter = 4;
                userMove = true;
                if(user.getValue() == 10) {
                    user.setValue(1);
                    userMove = false;
                    freeze = step;
                }
                printBackPack(left, 60, 15);
                printBackPack(right, 66, 15);
            }
        }

        if(flag) {
            for (int i = 0; i < diff; i++) {
                right.push(temp2.pop());
            }
        }
        else {
            for (int i = 0; i < diff; i++) {
                left.push(temp1.pop());
            }
        }

        return score;
    }

    public void clearPath(Object[][] map2) {
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if(map[i][j] == (Object) '.') {
                    map[i][j] = ' ';
                }

                if(map2[i][j] == (Object) '.') {
                    map2[i][j] = ' ';
                }
            }
        }
    }

    public int speed() {
        int speed;
        if(difficulty.equals("1")) {
            speed = 10;
        }
        else {
            speed = 5;
        }

        return speed;
    }

    public void printMap2(Object[][] map2){
        int x = 55, y = 23;
        cn.getTextWindow().setCursorPosition(0, 25);
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map[i][j] == (Object) '.'){
                    System.out.print('.');
                }
                else if (map2[i][j] instanceof Coordinate){
                    System.out.print(((Coordinate) map2[i][j]).getValue() % 10);
                }
                else if (map2[i][j] instanceof Number){
                    cn.setTextAttributes(yellow);
                    System.out.print(((Number) map2[i][j]).getValue());
                    cn.setTextAttributes(white);
                }
                else{
                    System.out.print(map2[i][j]);
                }

            }
            System.out.println();
        }
    }

    public void clearCoords(Object[][] map2){
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if (map2[i][j] instanceof Coordinate) {
                    map2[i][j] = (Object) ' ';
                }
            }
        }
    }

    public static void playDeathEffect(String path){
        try
        {
            File musicPath = new File(path);
            if(musicPath.exists())
            {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                Thread.sleep(1000);
            }
        }
        catch(Exception e)
        {
        e.printStackTrace();
        }
    }
}