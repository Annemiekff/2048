public class MoveFitness implements Comparable<MoveFitness> {
    private int numberOfEmptyTiles;
    private int score;
    private Move move;

    public MoveFitness(int numberOfEmptyTiles, int score, Move move) {
        this.numberOfEmptyTiles = numberOfEmptyTiles;
        this.score = score;
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public int compareTo(MoveFitness o) {
        if (this.numberOfEmptyTiles == o.numberOfEmptyTiles) {
            return Integer.compare(this.score, o.score);
        }
        return Integer.compare(this.numberOfEmptyTiles, o.numberOfEmptyTiles);
    }
}
