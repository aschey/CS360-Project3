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
         *
         * I chose to use an ArrayList because it allows for log(n)
         * searching with a custom comparator
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
    private ArrayList<String> words;

    public Puzzle(int size, ArrayList<String> words) {
        this.size = size;
        this.letters = new CoordinateMatrix(size);
        this.words = words;
    }

    public void add(char letter) {
        /**
         * Adds a letter to the puzzle
         */
        try {
            letters.set(letter);
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
        // Exit if the puzzle file didn't contain enough letters
        if (!this.letters.isFull()) {
            System.out.println("Error: puzzle dimensions incorrect");
            System.exit(1);
        }
        // Start searching words of length 4 because that's the smallest valid word
        final int MIN_LENGTH = 4;
        Point wordStart = new Point(this.size);
        while (wordStart.hasNext()) {
            // Start at a point and depth-first search for words in each direction
            for (Direction searchDir : Direction.values()) {
                String prefix = this.letters.getString(wordStart, searchDir, MIN_LENGTH);
                // If a valid string of length 4 doesn't exist at this point and direction,
                // move to the next one
                if (!prefix.equals("")) {
                    // At first, search using all valid words
                    // This list will be narrowed down as the recursive function runs
                    // in order to decrease search time
                    this.findWordsRec(prefix, wordStart, searchDir, this.words);
                }
            }
            wordStart.getNext();
        }
    }

    private void findWordsRec(String prefix, Point wordStart, Direction searchDir, ArrayList<String> searchList) {
        /**
         * Recursive helper for findWords
         */
        // Get the list of words that match the prefix
        // If there are none, this path contains no words, stop searching
        searchList = this.allWordsWithPrefix(prefix, searchList);
        if (searchList.size() == 0) {
            return;
        }
        // Check if the prefix equals the shortest word in the list of valid words
        // The only case where the list will have more than one word is when the previous word is a substring of the next word
        // i.e. get, gets, getting
        // Therefore, we always want to check the smallest word first to insure we don't skip any
        String word = searchList.get(0);
        if (prefix.equals(word)) {
            System.out.println(new Result(word, wordStart.getX() + 1, wordStart.getY() + 1, searchDir));
        }
        // If we can keep searching on this same path, add the next letter to the prefix and recurse,
        // using the new list of valid words to search
        char nextLetter = this.letters.get(wordStart, searchDir, prefix.length());
        if (nextLetter != ' ') {
            this.findWordsRec(prefix + nextLetter, wordStart, searchDir, searchList);
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
        /**
         * Sets the x and y value to the next valid point
         */
        // If we are at the end of a row, start at the next row
        if (this.x == this.max - 1) {
            this.x = 0;
            this.y++;
        }
        // Otherwise, move the x value along the row
        else {
            this.x++;
        }
    }

    public boolean hasNext() {
        /**
         * Returns true if we're not at the maximum value for this point
         */
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
    private int size;
    private Point addPoint;

    public CoordinateMatrix(int size) {
        this.letters = new char[size][size];
        this.size = size;
        this.addPoint = new Point(size);
    }

    private boolean isValid(int i) {
        /**
         * Returns true if i is within index constraints
         */
        return i < this.size && i >= 0;
    }
    private boolean isValid(int x, int y) {
        /**
         * Returns true if x and y are within index constraints
         */
        return this.isValid(x) && this.isValid(y);
    }

    public boolean isFull() {
        /**
         * Returns true if all x and y values have been filled
         */
        return !this.addPoint.hasNext();
    }

    public void set(char c) throws InputMismatchException {
        /**
         * Inserts c at the next valid x and y value
         */
        if (!this.isValid(this.addPoint.getY())) {
            throw new InputMismatchException("Point value out of bounds");
        }
        this.letters[this.addPoint.getX()][this.addPoint.getY()] = c;
        this.addPoint.getNext();
    }

    public char get(Point start, Direction dir, int distance) {
        /**
         * Returns the character in the specified direction and distance from the point
         */
        int x = start.getX() + (dir.getX() * distance);
        int y = start.getY() + (dir.getY() * distance);
        // The new point is not in bounds
        if (!this.isValid(x, y)) {
            return ' ';
        }
        return this.letters[x][y];
    }

    public String getString(Point start, Direction dir, int length) {
        /**
         * Returns a string consisting of letters in the specified direction
         */
        StringBuilder builder = new StringBuilder();
        int x = start.getX();
        int y = start.getY();
        for (int i = 0; i < length; i++) {
            if (!isValid(x, y)) {
                return "";
            }
            builder.append(this.letters[x][y]);
            x += dir.getX();
            y += dir.getY();
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