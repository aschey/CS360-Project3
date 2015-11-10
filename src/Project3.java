import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Austin Schey
 * CS360
 * 11/19/2015
 * Project 3
 */
public class Project3 {
    public static void main(String[] args) {
        try {
            ArrayList<String> words = readWords();
            Puzzle puzzle = createPuzzle(words);
            puzzle.DFS();
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    public static Puzzle createPuzzle(ArrayList<String> words) throws FileNotFoundException {
         try (Scanner scan = new Scanner(new File("puzzle.txt"))) {
            int size = scan.nextInt();
            Puzzle puzzle = new Puzzle(size, words);
            while (scan.hasNext()) {
                char next = scan.next().charAt(0);
                puzzle.add(next);
            }
            return puzzle;
        }
    }

    public static ArrayList<String> readWords() throws FileNotFoundException {
        try (Scanner scan = new Scanner(new File("words.txt"))) {
            ArrayList<String> words = new ArrayList<>();
            while (scan.hasNext()) {
                words.add(scan.next());
            }
            Collections.sort(words);
            return words;
        }
    }
}

class Puzzle {
    private CoordinateMatrix letters;
    private int size;
    private Point point;
    private ArrayList<String> words;

    public Puzzle(int size, ArrayList<String> words) {
        this.size = size;
        this.letters = new CoordinateMatrix(size);
        this.point = new Point(size);
        this.words = words;
    }

    public void add(char letter) {
        letters.set(point, letter);
        point.getNext();
    }

    public void DFS() {
        final int MIN_LENGTH = 3;
        Point p = new Point(this.size);
        while (p.hasNext()) {
            for (Direction d : Direction.values()) {
                String check = this.letters.getString(p, d, MIN_LENGTH);
                if (!check.equals("")) {
                    this.DFSVisit(check, p, d, this.words);
                }
            }
            p.getNext();
        }
    }

    private ArrayList<String> allWordsWithPrefix(String prefix, ArrayList<String> searchList) {
        ArrayList<String> results = new ArrayList<>();
        int wordIndex = Collections.binarySearch(searchList, prefix,
            (check, key) -> check.startsWith(key) ? 0 : check.compareTo(key));
        if (wordIndex < 0 || wordIndex >= searchList.size()) {
            return results;
        }
        while (wordIndex > 0 && this.words.get(wordIndex - 1).startsWith(prefix)) {
            wordIndex--;
        }
        for (int i = wordIndex; i < searchList.size() && searchList.get(i).startsWith(prefix); i++) {
            results.add(searchList.get(i));
        }
        return results;
    }

    private void DFSVisit(String prefix, Point p, Direction d, ArrayList<String> searchList) {
        searchList = this.allWordsWithPrefix(prefix, searchList);
        if (searchList.size() == 0) {
            return;
        }
        String word = searchList.get(0);
        if (prefix.equals(word)) {
            System.out.println(new Result(word, p.getX() + 1, p.getY() + 1, d));
        }
        char nextLetter = this.letters.get(p, d, prefix.length());
        if (nextLetter != ' ') {
            this.DFSVisit(prefix + nextLetter, p, d, searchList);
        }
    }
}

enum Direction {
    n (0, -1),
    s (0, 1),
    e (1, 0),
    w (-1, 0),
    ne (1, -1),
    nw (-1, -1),
    se (1, 1),
    sw (-1, 1);

    private final int x;
    private final int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}

class Point {
    private int x;
    private int y;
    private int max;

    public Point(int max) {
        this.x = 0;
        this.y = 0;
        this.max = max;
    }

    public void getNext() {
        if (this.x == this.max - 1) {
            this.x = 0;
            this.y++;
        }
        else {
            this.x++;
        }
    }

    public boolean hasNext() {
        return this.y < this.max;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}

class CoordinateMatrix {
    private char[][] letters;
    int size;

    public CoordinateMatrix(int size) {
        this.letters = new char[size][size];
        this.size = size;
    }

    private boolean isValid(int x, int y) {
        return x < this.size && y < this.size && x >= 0 && y >= 0;
    }

    public void set(Point p, char c) {
        this.letters[p.getX()][p.getY()] = c;
    }

    public char get(Point p, Direction d, int offset) {
        int x = p.getX() + (d.getX() * offset);
        int y = p.getY() + (d.getY() * offset);
        if (!isValid(x, y)) {
            return ' ';
        }
        return this.letters[x][y];
    }

    public String getString(Point p, Direction d, int numLetters) {
        StringBuilder builder = new StringBuilder();
        int x = p.getX();
        int y = p.getY();
        for (int i = 0; i < numLetters; i++) {
            if (!isValid(x, y)) {
                return "";
            }
            builder.append(this.letters[x][y]);
            x += d.getX();
            y += d.getY();
        }
        return builder.toString();
    }
}
class Result {
    public String word;
    public int over;
    public int down;
    public Direction direction;

    public Result(String word, int over, int down, Direction direction) {
        this.word = word;
        this.over = over;
        this.down = down;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return this.word + " (" + this.over + ", " + this.down + ", " + this.direction + ")";
    }
}