import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score;
    int maxTile;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() > 0) {
            int get = (int) (emptyTiles.size() * Math.random());
            int a = Math.random() < 0.9 ? 2 : 4;
            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    if (gameTiles[i][j] == emptyTiles.get(get))
                        gameTiles[i][j].setValue(a);
                }
            }
        }
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> emptyTiles = new ArrayList<>();
        for (int y = 0; y < FIELD_WIDTH; y++) {
            for (int x = 0; x < FIELD_WIDTH; x++) {
                if (gameTiles[y][x].value == 0) {
                    emptyTiles.add(gameTiles[y][x]);
                }
            }
        }
        return emptyTiles;
    }

    public void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int y = 0; y < FIELD_WIDTH; y++){
            for (int x = 0; x < FIELD_WIDTH; x++){
                gameTiles[y][x] = new Tile();
            }
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }

    private boolean consolidateTiles(Tile[] tiles) {
        boolean moved = false;
        Tile temp;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles.length - i - 1; j++) {
                if (tiles[j].value == 0 && tiles[j + 1].value != 0) {
                    moved = true;
                    temp = tiles[j];
                    tiles[j] = tiles[j + 1];
                    tiles[j + 1] = temp;
                }
            }
        }
        return moved;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean merged = false;
        int n = tiles.length;
        for (int i = 0; i < n - 1; i++) {
            if (tiles[i].value != 0 && tiles[i].value == tiles[i+1].value) {
                merged = true;
                int newValue = tiles[i].value * 2;
                tiles[i].setValue(newValue);
                score += newValue;
                if (maxTile < newValue)
                    maxTile = newValue;
                for (int j = i + 1; j < n - 1; j++) {
                    tiles[j].setValue(tiles[j+1].value);
                }
                tiles[n-1].setValue(0);
            }
        }
        return merged;
    }

    public void left(){
        if (isSaveNeeded){
            saveState(gameTiles);
        }
        boolean changed = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            boolean change1 = consolidateTiles(gameTiles[i]);
            boolean change2 = mergeTiles(gameTiles[i]);
            if (!changed){
                if(change1 || change2){
                    changed = true;
                }
            }
        }
        if (changed){
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
    }

    public void up() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
    }

    public void down() {
        saveState(gameTiles);
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
    }

    private void rotateClockwise() {
        Tile[][] rotated = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int y = 0; y < FIELD_WIDTH; y++) {
            for (int x = 0; x < FIELD_WIDTH; x++) {
                rotated[y][x] = gameTiles[FIELD_WIDTH - x - 1][y];
            }
        }
        gameTiles = rotated;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (j != gameTiles[i].length - 1) {
                    if (gameTiles[i][j].value == gameTiles[i][j + 1].value ||
                            gameTiles[i][j].value == 0 && gameTiles[i][j + 1].value != 0) {
                        return true;
                    }
                }

                if (i != gameTiles.length - 1) {
                    if (gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                            gameTiles[i][j].value == 0 && gameTiles[i + 1][j].value != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void saveState(Tile[][] gameTiles){
        Tile[][] newTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++){
            for (int j = 0; j < FIELD_WIDTH; j++){
                newTiles[i][j] = new Tile(gameTiles[i][j].value);
            }
        }
        previousStates.push(newTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if (!previousScores.isEmpty() && !previousStates.isEmpty()){
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove(){
        int n = ((int)(Math.random()*100)) % 4;
        switch (n){
            case 0: left(); break;
            case 1: up(); break;
            case 2: right(); break;
            case 3: down(); break;
        }
    }
    public boolean hasBoardChanged() {
        return getTileWeights(gameTiles) != getTileWeights(previousStates.peek());
    }

    private int getTileWeights(Tile[][] tiles) {
        int result = 0;
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++)
                result += tiles[i][j].value;
        return result;
    }

    public MoveFitness getMoveFitness(Move move){
        move.move();
        MoveFitness moveFitness;
        if (!hasBoardChanged()){
            moveFitness = new MoveFitness(-1, 0 , move);
        }else {
            moveFitness = new MoveFitness(getEmptyTiles().size(), score, move);
        }
        rollback();
        return moveFitness;
    }

    public void autoMove() {
        PriorityQueue<MoveFitness> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveFitness(this::left));
        queue.offer(getMoveFitness(this::right));
        queue.offer(getMoveFitness(this::up));
        queue.offer(getMoveFitness(this::down));
        Objects.requireNonNull(queue.poll()).getMove().move();


    }
}
