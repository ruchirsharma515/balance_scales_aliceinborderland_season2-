import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Player {
    String name;
    int score;
    boolean isAlive;
    int chosenNumber;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.isAlive = true;
    }

    public int pickNumber(JFrame frame) {
        String input;
        int number = -1;
        do {
            input = JOptionPane.showInputDialog(frame, name + ", enter a number between 0 and 100:");
            if (input == null) return -1;
            try {
                number = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                number = -1;
            }
        } while (number < 0 || number > 100);
        chosenNumber = number;
        return number;
    }

    public void changeScore(int delta) {
        score += delta;
        if (score <= -10) {
            isAlive = false;
            JOptionPane.showMessageDialog(null, name + " has reached -10. GAME OVER for that player");
        }
    }
}

public class KingOfDiamondsGame {
    static java.util.List<Player> players = new ArrayList<>();
    static int eliminatedCount = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> startGame());
    }

    private static void startGame() {
        JFrame frame = new JFrame("King of Diamonds Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        // Show startup dialog with image and title
        ImageIcon icon = new ImageIcon("king_of_diamonds.jpg"); // ensure the image is in the project root
        JLabel label = new JLabel("Difficulty: King of Diamonds", icon, JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setFont(new Font("Serif", Font.BOLD, 16));
        JOptionPane.showMessageDialog(frame, label, "Difficulty: King of Diamonds", JOptionPane.PLAIN_MESSAGE);

        // Initialize players
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        players.add(new Player("Player 3"));
        players.add(new Player("Player 4"));
        players.add(new Player("Player 5"));

        int round = 1;

        while (alivePlayers().size() > 1) {
            JOptionPane.showMessageDialog(frame, "========== Round " + round + " ==========");
            if (round == 1 || eliminatedCountChanged()) {
                JOptionPane.showMessageDialog(frame, " New rule activated!");
            }

            Map<Player, Integer> choices = new HashMap<>();
            java.util.List<Integer> numberHistory = new ArrayList<>();

            // Step 1: All alive players choose numbers
            for (Player p : alivePlayers()) {
                int num = p.pickNumber(frame);
                if (num == -1) return;
                choices.put(p, num);
                numberHistory.add(num);
            }

            // Step 2: Calculate average and target
            double avg = numberHistory.stream().mapToDouble(i -> i).average().orElse(0);
            double target = 0.8 * avg;
            JOptionPane.showMessageDialog(frame, String.format("Average: %.2f | Target (0.8 × avg): %.2f", avg, target));

            // Step 3: Apply escalation rules based on eliminations
            Set<Integer> duplicates = new HashSet<>();
            Set<Integer> seen = new HashSet<>();
            for (int num : numberHistory) {
                if (!seen.add(num)) {
                    duplicates.add(num);
                }
            }

            Player winner = null;
            double closestDiff = Double.MAX_VALUE;

            for (Map.Entry<Player, Integer> entry : choices.entrySet()) {
                Player p = entry.getKey();
                int num = entry.getValue();

                if (eliminatedCount >= 1 && duplicates.contains(num)) {
                    JOptionPane.showMessageDialog(frame, p.name + " chose a duplicate number (" + num + ") and it is invalid.");
                    continue;
                }

                double diff = Math.abs(num - target);
                if (diff < closestDiff) {
                    closestDiff = diff;
                    winner = p;
                }
            }

            if (eliminatedCount >= 3 && numberHistory.contains(0) && numberHistory.contains(100)) {
                for (Map.Entry<Player, Integer> entry : choices.entrySet()) {
                    if (entry.getValue() == 100 && entry.getKey().isAlive) {
                        winner = entry.getKey();
                        JOptionPane.showMessageDialog(frame, "? Rule 3 Activated: " + winner.name + " chose 100 while another chose 0. Instant win!");
                        break;
                    }
                }
            }

            if (winner != null) {
                JOptionPane.showMessageDialog(frame, "? Winner of this round: " + winner.name);
                for (Player p : alivePlayers()) {
                    if (p != winner) {
                        if (eliminatedCount >= 2 && choices.get(p) != null && Math.abs(choices.get(winner) - target) == 0) {
                            p.changeScore(-2);
                        } else {
                            p.changeScore(-1);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "No valid winner this round due to duplicates.");
                for (Player p : alivePlayers()) {
                    p.changeScore(-1);
                }
            }

            StringBuilder status = new StringBuilder("\n-- Player Scores --\n");
            for (Player p : players) {
                status.append(p.name).append(" | Score: ").append(p.score).append(p.isAlive ? "\n" : " ❌ DEAD\n");
            }
            JOptionPane.showMessageDialog(frame, status.toString());

            round++;
        }

        for (Player p : players) {
            if (p.isAlive) {
                JOptionPane.showMessageDialog(frame, "? GAME CLEAR!\n? Final Winner: " + p.name);
            }
        }

        frame.dispose();
    }

    private static java.util.List<Player> alivePlayers() {
        java.util.List<Player> alive = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive) alive.add(p);
        }
        return alive;
    }

    private static boolean eliminatedCountChanged() {
        int alive = (int) players.stream().filter(p -> p.isAlive).count();
        if (players.size() - alive > eliminatedCount) {
            eliminatedCount = players.size() - alive;
            return true;
        }
        return false;
    }
}
