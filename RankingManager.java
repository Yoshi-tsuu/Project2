import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RankingManager {
    private static final String FILE_NAME = "rankings.txt";
    private Map<String, Integer> rankings;

    public RankingManager() {
        rankings = new HashMap<>();
        loadRankings();
    }

    // Metoda do wczytywania rankingów z pliku
    private void loadRankings() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();  // Tworz plik, jeśli nie istnieje
            } catch (IOException e) {
                System.out.println("Nie udało się utworzyć pliku rankingu: " + e.getMessage());
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    rankings.put(name, score);
                }
            }
        } catch (IOException e) {
            System.out.println("Nie udało się wczytać rankingów: " + e.getMessage());
        }
    }

    // Metoda do zapisywania rankingów do pliku
    public void saveRankings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : rankings.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.out.println("Nie udało się zapisać rankingów: " + e.getMessage());
        }
    }

    // Pobieranie najlepszego wyniku gracza
    public int getHighScore(String name) {
        return rankings.getOrDefault(name, 0);
    }

    // Aktualizacja wyniku gracza, jeśli jest lepszy
    public void updateScore(String name, int newScore) {
        int currentHighScore = rankings.getOrDefault(name, 0);
        if (newScore > currentHighScore) {
            rankings.put(name, newScore);
            saveRankings();
        }
    }
}
