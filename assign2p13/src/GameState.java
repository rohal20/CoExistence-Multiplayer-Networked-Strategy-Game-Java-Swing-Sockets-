import java.util.*;

class GameState {
    public enum Unit {
        AXE, HAMMER, SWORD, ARROW, EMPTY
    }

    private List<Unit> deck = new ArrayList<>();
    private Unit[] board1 = new Unit[6];
    private Unit[] board2 = new Unit[6];
    private int score1 = 0, score2 = 0;
    private int round = 1;
    private int turn = 0;
    private int passCount = 0;
    private Unit lastAttacker = null;
    private Unit lastDefender = null;
    private boolean moveJustMade = false;
    private boolean isPlayer1;

    public GameState(boolean isPlayer1) {
        this(isPlayer1, System.currentTimeMillis());
    }

    public GameState(boolean isPlayer1, long seed) {
        this.isPlayer1 = isPlayer1;
        // Create deck with exactly 3 of each card type
        for (int i = 0; i < 3; i++) {
            deck.add(Unit.AXE);
            deck.add(Unit.HAMMER);
            deck.add(Unit.SWORD);
            deck.add(Unit.ARROW);
        }
        Random rng = new Random(seed);
        Collections.shuffle(deck, rng);
        for (int i = 0; i < 6; i++) {
            board1[i] = deck.remove(0);
            board2[i] = deck.remove(0);
        }
    }

    public boolean applyMove(char from, char to) {
        int fromIdx = from - 'A';
        int toIdx = to - 'A';

        // Validate indices
        if (fromIdx < 0 || fromIdx >= 6 || toIdx < 0 || toIdx >= 6) {
            moveJustMade = false;
            lastAttacker = null;
            lastDefender = null;
            return false;
        }

        // Get the units based on whose turn it is
        Unit atk, def;
        if (turn == 0) { // Player 1's turn
            atk = board1[fromIdx];
            def = board2[toIdx];
        } else { // Player 2's turn
            atk = board2[fromIdx];
            def = board1[toIdx];
        }

        // Store the units before checking if move is valid
        lastAttacker = atk;
        lastDefender = def;

        // Check if the move is valid
        if (atk == Unit.EMPTY || !canDefeat(atk, def)) {
            moveJustMade = false;
            return false;
        }

        // Apply the move
        moveJustMade = true;

        // Remove the defender's card based on whose turn it is
        if (turn == 0) { // Player 1 attacking Player 2
            board2[toIdx] = Unit.EMPTY;
        } else { // Player 2 attacking Player 1
            board1[toIdx] = Unit.EMPTY;
        }

        // Score points only for non-ARROW victories
        // If either unit is an ARROW, no points are scored
        if (atk != Unit.ARROW && def != Unit.ARROW) {
            if (turn == 0) { // Player 1's turn
                score1++;
            } else { // Player 2's turn
                score2++;
            }
        }

        turn = 1 - turn; // Switch turns after a successful move
        passCount = 0; // Reset pass count when a valid move is made
        return true;
    }

    private boolean canDefeat(Unit atk, Unit def) {
        // If either unit is empty, no defeat is possible
        if (atk == Unit.EMPTY || def == Unit.EMPTY) {
            return false;
        }

        // Special case: Arrow can eliminate any unit, and any unit can eliminate arrow
        if (atk == Unit.ARROW || def == Unit.ARROW) {
            return true;
        }

        // Normal unit interactions - cyclic relationship:
        // AXE defeats HAMMER
        // HAMMER defeats SWORD
        // SWORD defeats AXE
        switch (atk) {
            case AXE:
                return def == Unit.HAMMER; // "cuts true; straight through the handle of the hammer"
            case HAMMER:
                return def == Unit.SWORD; // "is power; it overwhelms the sword"
            case SWORD:
                return def == Unit.AXE; // "is nimble; it dances about the axe"
            default:
                return false;
        }
    }

    public void passTurn() {
        moveJustMade = false;
        lastAttacker = null;
        lastDefender = null;

        // If we're at passCount 2 (NEW ROUND showing), reset it so we can start passing
        // again
        if (passCount == 2) {
            passCount = 0;
        }
        passCount++;

        // Switch turns before handling round increment
        turn = 1 - turn;

        // Only increment round and deal new cards if both players passed
        if (passCount == 2) {
            if (!isGameOver()) {
                round++; // Move to next round only after both passes
                dealNewRound();
                // Keep passCount at 2 to show NEW ROUND until someone makes a move or passes
                // again
            }
        }
    }

    private void dealNewRound() {
        // Clear both boards
        for (int i = 0; i < 6; i++) {
            board1[i] = Unit.EMPTY;
            board2[i] = Unit.EMPTY;
        }

        // Create and shuffle new deck with exactly 3 of each card type
        deck.clear();
        for (int i = 0; i < 3; i++) {
            deck.add(Unit.AXE);
            deck.add(Unit.HAMMER);
            deck.add(Unit.SWORD);
            deck.add(Unit.ARROW);
        }
        Collections.shuffle(deck);

        // Deal 6 cards to each player
        for (int i = 0; i < 6; i++) {
            board1[i] = deck.remove(0);
            board2[i] = deck.remove(0);
        }
        moveJustMade = false; // Reset move flag for new round
    }

    public boolean isGameOver() {
        // Game ends if either player reaches 9 points
        if (score1 >= 9 || score2 >= 9) {
            return true;
        }

        // Game ends if round 5 is reached
        if (round >= 5) {
            return true;
        }

        return false;
    }

    public int getTurn() {
        return turn;
    }

    public String generateFrame(boolean isPlayer1) {
        StringBuilder sb = new StringBuilder();

        // Required header to establish frame start
        sb.append("/----------------------------------------\\\n");

        // Column headers with proper spacing
        sb.append("    A    B    C    D    E    F             |\n");

        // Always show current player's board at bottom and opponent's board at top
        Unit[] top = isPlayer1 ? board2 : board1;
        Unit[] bot = isPlayer1 ? board1 : board2;
        int topScore = isPlayer1 ? score2 : score1;
        int botScore = isPlayer1 ? score1 : score2;

        // Turn indicators - Using correct symbols for arrow (^, |, v)
        String topArrow = " "; // Top arrow component
        String midArrow = " "; // Middle arrow component
        String botArrow = " "; // Bottom arrow component

        if (!isGameOver()) {
            if ((isPlayer1 && turn == 0) || (!isPlayer1 && turn == 1)) {
                // Current player's turn - show downward arrow
                topArrow = " ";
                midArrow = "|";
                botArrow = "v";
            } else {
                // Waiting for opponent - show upward arrow
                topArrow = "^";
                midArrow = "|";
                botArrow = " ";
            }
        } else {
            // Game over - show dashes
            topArrow = "-";
            midArrow = "|";
            botArrow = "-";
        }

        // Top board
        sb.append("  /---\\");
        for (int i = 1; i < 6; i++) {
            sb.append("/---\\");
        }
        sb.append("      " + topArrow + "\n");

        // First line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardFirstLine(top[i]));
        }
        sb.append("      " + midArrow + "\n");

        // Second line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardSecondLine(top[i]));
        }
        sb.append("      " + midArrow + "\n");

        // Third line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardThirdLine(top[i]));
        }
        sb.append("      " + midArrow + "\n");

        // Bottom borders of cards
        sb.append("  \\---/");
        for (int i = 1; i < 6; i++) {
            sb.append("\\---/");
        }
        sb.append("\n");

        // Scores and round number
        sb.append(String.format("|%40s[%d]\n", "", topScore));
        sb.append("<====================================R" + round + "\n");
        sb.append(String.format("|%40s[%d]\n", "", botScore));

        // Bottom board
        sb.append("  /---\\");
        for (int i = 1; i < 6; i++) {
            sb.append("/---\\");
        }
        sb.append("      " + midArrow + "\n");

        // First line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardFirstLine(bot[i]));
        }
        sb.append("      " + midArrow + "\n");

        // Second line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardSecondLine(bot[i]));
        }
        sb.append("      " + midArrow + "\n");

        // Third line of cards
        sb.append("  ");
        for (int i = 0; i < 6; i++) {
            sb.append(getCardThirdLine(bot[i]));
        }
        sb.append("      " + botArrow + "\n");

        // Bottom borders of cards
        sb.append("  \\---/");
        for (int i = 1; i < 6; i++) {
            sb.append("\\---/");
        }
        sb.append("\n");

        // Column labels
        sb.append("  A    B    C    D    E    F\n");

        // Single message box
        sb.append("/----------------------------------------\\\n");
        String message = getLastMoveMessage();
        if (!message.isEmpty()) {
            // Center the message in the 40-character width box
            sb.append(String.format("|%-40s|\n", String.format(
                    "%" + (20 + message.length() / 2) + "s%" + (20 - message.length() / 2) + "s", message, "")));
        } else {
            sb.append("|                                        |\n");
        }
        sb.append("\\----------------------------------------/\n");

        return sb.toString();
    }

    private String getCardFirstLine(Unit u) {
        switch (u) {
            case AXE:
                return "|<7>|";
            case HAMMER:
                return "|[=]|";
            case SWORD:
                return "|  /|";
            case ARROW:
                return "| ^ |";
            default:
                return "|   |";
        }
    }

    private String getCardSecondLine(Unit u) {
        switch (u) {
            case AXE:
                return "| I |";
            case HAMMER:
                return "| | |";
            case SWORD:
                return "| / |";
            case ARROW:
                return "| | |";
            default:
                return "|   |";
        }
    }

    private String getCardThirdLine(Unit u) {
        switch (u) {
            case AXE:
                return "| L |";
            case HAMMER:
                return "| | |";
            case SWORD:
                return "|X  |";
            case ARROW:
                return "|/^\\|";
            default:
                return "|   |";
        }
    }

    private String getLastMoveMessage() {
        // Check for game over first
        if (isGameOver()) {
            return getGameOverMessage();
        }

        // At the start of the game, show NEW GAME
        if (round == 1 && !moveJustMade && passCount == 0) {
            return "NEW GAME";
        }

        // Show pass messages
        if (passCount > 0) {
            // When both players have passed, show NEW ROUND
            if (passCount == 2) {
                return "NEW ROUND";
            }
            // Show pass message for the player who just passed
            return (turn == 0 ? "PLAYER 2" : "PLAYER 1") + " PASSED";
        }

        // If a move was just made, show the move
        if (moveJustMade && lastAttacker != null && lastDefender != null) {
            String attackerName = unitToName(lastAttacker);
            String defenderName = unitToName(lastDefender);
            String playerNum = (turn == 1) ? "PLAYER 1" : "PLAYER 2";
            return playerNum + ": " + attackerName + " TAKES " + defenderName;
        }

        // If no specific message, show empty string
        return "";
    }

    private String unitToName(Unit u) {
        switch (u) {
            case AXE:
                return "AXE";
            case HAMMER:
                return "HAMMER";
            case SWORD:
                return "SWORD";
            case ARROW:
                return "ARROW";
            default:
                return "EMPTY";
        }
    }

    private String getGameOverMessage() {
        if (score1 >= 9) {
            return "GAME OVER - PLAYER 1 WINS WITH " + score1 + " POINTS!";
        }
        if (score2 >= 9) {
            return "GAME OVER - PLAYER 2 WINS WITH " + score2 + " POINTS!";
        }
        if (round >= 5) {
            if (score1 == 0 && score2 == 0) {
                return "GAME OVER - BOTH PLAYERS LOST)";
            }
            if (score1 > score2) {
                return "GAME OVER - PLAYER 1 WINS WITH " + score1 + " POINTS!";
            } else if (score2 > score1) {
                return "GAME OVER - PLAYER 2 WINS WITH " + score2 + " POINTS!";
            } else {
                return "GAME OVER - DRAW (TIED POINTS IN ROUND 5)";
            }
        }
        return "Game Over!";
    }

    // Process a command string from client
    public boolean processCommand(String command) {
        // If game is over, don't process any more commands
        if (isGameOver()) {
            return false;
        }

        // Handle empty or invalid length commands
        if (command == null || command.length() != 2) {
            moveJustMade = false;
            lastAttacker = null;
            lastDefender = null;
            return false;
        }

        // Convert to uppercase for canonical processing
        command = command.toUpperCase();

        // Handle pass command
        if (command.equals("PS")) {
            passTurn();
            return true;
        }

        // Handle move command
        char from = command.charAt(0);
        char to = command.charAt(1);

        // Check if characters are valid column letters
        if (from < 'A' || from > 'F' || to < 'A' || to > 'F') {
            moveJustMade = false;
            lastAttacker = null;
            lastDefender = null;
            return false;
        }

        // Process the move immediately if it's the player's turn
        return applyMove(from, to);
    }
}