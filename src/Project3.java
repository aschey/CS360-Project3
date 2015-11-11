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
            // Create a sorted list of words from words.txt
            ArrayList<String> words = readWords();
            // Create the puzzle graph
            Puzzle wordSearch = createPuzzle(words);
            // Find all words in the list and print them out
            wordSearch.findWords();
        }
        // Exit if words.txt or puzzle.txt doesn't exist
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
        catch (InputMismatchException ex) {
            System.out.println("Error: puzzle text file malformed: " + ex.toString());
        }
    }

    public static Puzzle createPuzzle(ArrayList<String> words) throws FileNotFoundException, InputMismatchException {
        /**
         * Creates the puzzle object to store the word search
         */
        try (Scanner scan = new Scanner(new File("puzzle.txt"))) {
            // Get the size of each row and column
            int size = scan.nextInt();
            Puzzle puzzle = new Puzzle(size, words);
            // Insert each letter into the puzzle
            while (scan.hasNext()) {
                char next = scan.next().charAt(0);
                puzzle.add(next);
            }
            return puzzle;
        }
    }

    public static ArrayList<String> readWords() throws FileNotFoundException {
        /**
         * Read the list of words to search into a sorted list
         */
        try (Scanner scan = new Scanner(new File("words.txt"))) {
            ArrayList<String> words = new ArrayList<>();
            while (scan.hasNext()) {
                words.add(scan.next());
            }
            // Sort the words so they can be easily searched
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
        /**
         * Adds a letter to the puzzle
         */
        try {
            letters.set(point, letter);
            // Keep track of the x and y value the next character will be stored at
            point.getNext();
        }
        // Exit if the number of letters added exceeded the size
        catch (InputMismatchException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
    }

    public void findWords() {
        /**
         * Searches the list of words and prints out the ones it finds
         */
        if (point.hasNext()) {
            System.out.println("Error: puzzle dimensions incorrect");
            System.exit(1);
        }
        // Start searching words of length 4 because that's the smallest valid word
        final int MIN_LENGTH = 4;
        Point wordStart = new Point(this.size);
        while (wordStart.hasNext()) {
            // Start at a point and depth-first search for words in each direction
            for (Direction searchDir : Direction.values()) {
                String check = this.letters.getString(wordStart, searchDir, MIN_LENGTH);
                // If a valid string of length 4 doesn't exist at this point and direction,
                // move to the next one
                if (!check.equals("")) {
                    this.findWordsRec(check, wordStart, searchDir, this.words);
                }
            }
            wordStart.getNext();
        }
    }

    private ArrayList<String> allWordsWithPrefix(String prefix, ArrayList<String> searchList) {
        /**
         * Returns an ArrayList consisting of the subset of words in the original list
         * that match the specified prefix
         */
        ArrayList<String> results = new ArrayList<>();
        // Binary search to find one index in the set of possible words
        int wordIndex = Collections.binarySearch(searchList, prefix,
            (check, key) -> check.startsWith(key) ? 0 : check.compareTo(key));
        // Return an empty list if no matching words were found
        if (wordIndex < 0 || wordIndex >= searchList.size()) {
            return results;
        }
        // Find the first index of a matching word. We need to start at the first index
        // because words need to be added to the new list in sorted order
        while (wordIndex > 0 && this.words.get(wordIndex - 1).startsWith(prefix)) {
            wordIndex--;
        }
        for (int i = wordIndex; i < searchList.size() && searchList.get(i).startsWith(prefix); i++) {
            results.add(searchList.get(i));
        }
        return results;
    }

    private void findWordsRec(String prefix, Point wordStart, Direction searchDir, ArrayList<String> searchList) {
        /**
         * Prints a word
         */
        searchList = this.allWordsWithPrefix(prefix, searchList);
        if (searchList.size() == 0) {
            return;
        }
        String word = searchList.get(0);
        if (prefix.equals(word)) {
            System.out.println(new Result(word, wordStart.getX() + 1, wordStart.getY() + 1, searchDir));
        }
        char nextLetter = this.letters.get(wordStart, searchDir, prefix.length());
        if (nextLetter != ' ') {
            this.findWordsRec(prefix + nextLetter, wordStart, searchDir, searchList);
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

    public void getNext() throws InputMismatchException {
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

    private boolean isValid(int i) {
        return i < this.size && i >= 0;
    }
    private boolean isValid(int x, int y) {
        return this.isValid(x) && this.isValid(y);
    }

    public void set(Point p, char c) {
        if (p.getY() >= this.size) {
            throw new InputMismatchException("Point value out of bounds");
        }
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