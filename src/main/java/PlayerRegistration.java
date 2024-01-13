import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerRegistration {

    private static final String DATA_FILE_PATH = "src/main/resources/players.json";
    private static final String MATCH_HISTORY_FILE_PATH = "src/main/resources/matchHistory.json";
    private static final String ANNOUNCEMENT_FILE_PATH = "src/main/resources/announcement.json";
    private static final  Map<String, PlayerData> playerDatabase = loadPlayerData();
    private static final List<MatchHistoryEntry> matchHistory = loadMatchHistory();
    private static final List<String> announcement = loadAnnouncement();

    private static final int DEFAULT_PLAYER_RATING = 1550;

    public static String registerPlayer(String playerName, String faction) {
        String playerKey = playerName + ":" + faction;

        if (!playerDatabase.containsKey(playerKey)) {

            if(playerName.length() > 20 || faction.length() > 20)
                return "Слишком длинное имя или название фракции";

            PlayerData newPlayer = new PlayerData(playerName, faction);
            playerDatabase.put(playerKey, newPlayer);
            savePlayerData();

            return "Игрок " + playerName + " с фракцией " + faction + " успешно зарегистрирован.";
        } else {
            return "Игрок " + playerName + " с фракцией " + faction + " уже зарегистрирован.";
        }
    }

    public static String updateRatings(String player1Name, String player2Name, MatchResult result) {

        double matchResult = 0;
        double matchResultInvert = 0;
        switch (result) {
            case WIN -> {
                matchResult = 1.0;
                matchResultInvert = 0.0;
            }
            case DRAW -> {
                matchResult = 0.5;
                matchResultInvert = 0.5;
            }
            case LOSE -> {
                matchResult = 0.0;
                matchResultInvert = 1.0;
            }
            default -> {

            }
        }

        PlayerData player1 = playerDatabase.get(player1Name);
        PlayerData player2 = playerDatabase.get(player2Name);

        if (player1 != null) {
            if (player2 != null) {
                int rating1Before = player1.getRating();
                int rating2Before = player2.getRating();
                int rating1After = EloRatingCalculator.calculateEloRating(rating1Before, rating2Before, matchResult);
                int rating2After = EloRatingCalculator.calculateEloRating(rating2Before, rating1Before, matchResultInvert);

                matchHistory.add(new MatchHistoryEntry(
                        player1.getPlayerName(),
                        player1.getFaction(),
                        rating1Before,
                        rating1After,
                        matchResult,
                        player2.getPlayerName(),
                        player2.getFaction(),
                        rating2Before,
                        rating2After,
                        matchResultInvert
                ));
                saveMatchHistory();

                player1.setRating(rating1After);
                player2.setRating(rating2After);

                savePlayerData();

            } else return "Профиль оппонента не найден в базе";
        } else return "Ваш профиль не найден в базе";

        return "Результат сохранен";
    }

    public static String displayAllPlayers() {
        if (playerDatabase.size() == 0)
            return "Никто не зарегистрирован";
        return playerDatabase.values().stream()
                .sorted(Comparator.comparing(PlayerData::getRating).reversed()
                        .thenComparing(PlayerData::getPlayerName))
                .map(player -> "Игрок: " + player.getPlayerName() +
                        ", Фракция: " + player.getFaction() +
                        ", Рейтинг: " + player.getRating())
                .collect(Collectors.joining("\n"));
    }

    private static Map<String, PlayerData> loadPlayerData() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, PlayerData> loadedData = new HashMap<>();

        try {
            File file = new File(DATA_FILE_PATH);
            if (file.exists()) {
                loadedData = objectMapper.readValue(file, objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, PlayerData.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedData;
    }

    private static void savePlayerData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(DATA_FILE_PATH), playerDatabase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void wipe() {
        playerDatabase.values().forEach(player -> player.setRating(DEFAULT_PLAYER_RATING));
        savePlayerData();
    }

    public static void clearAllPlayers() {
        playerDatabase.clear();
        savePlayerData();
    }

    public static void deletePlayer(String nameFaction) {
        playerDatabase.remove(nameFaction);
        savePlayerData();
    }

    private static List<MatchHistoryEntry> loadMatchHistory() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<MatchHistoryEntry> loadedData = new ArrayList<>();

        try {
            File file = new File(MATCH_HISTORY_FILE_PATH);
            if (file.exists()) {
                loadedData = objectMapper.readValue(
                        file, objectMapper.getTypeFactory()
                                .constructCollectionType(ArrayList.class, MatchHistoryEntry.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedData;
    }

    private static List<String> loadAnnouncement() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> loadedData = new ArrayList<>();

        try {
            File file = new File(ANNOUNCEMENT_FILE_PATH);
            if (file.exists()) {
                loadedData = objectMapper.readValue(
                        file, objectMapper.getTypeFactory()
                                .constructCollectionType(ArrayList.class, String.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedData;
    }

    private static void saveMatchHistory() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(MATCH_HISTORY_FILE_PATH), matchHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String announcement() {
        int startIndex = Math.max(0, announcement.size() - 10);
        List<String> last10 = announcement.subList(startIndex, announcement.size());

        StringBuilder result = new StringBuilder();

        for (String entry : last10) {
            result.append(entry);
            result.append("\n");
        }

        return result.toString();
    }

    private record MatchHistoryEntry(String player1Name, String faction1, int rating1Before, int rating1After,
                                     double result, String player2Name, String faction2, int rating2Before,
                                     int rating2After, double matchResultInvert) {}

    public static String showMatchHistory() {
        int startIndex = Math.max(0, matchHistory.size() - 10);
        List<MatchHistoryEntry> last10Matches = matchHistory.subList(startIndex, matchHistory.size());

        StringBuilder result = new StringBuilder();

        for (MatchHistoryEntry entry : last10Matches) {
            result.append(formatMatchEntry(entry));
        }

        return result.toString();
    }

    private static String formatMatchEntry(MatchHistoryEntry entry) {
        return String.format("%s:%s, Рейтинг до: %d, Рейтинг после: %d, Результат: %.1f\n%s:%s, Рейтинг до: %d, Рейтинг после: %d, Результат: %.1f\n\n",
                entry.player1Name(), entry.faction1(), entry.rating1Before(), entry.rating1After(), entry.result(),
                entry.player2Name(), entry.faction2(), entry.rating2Before(), entry.rating2After(), entry.matchResultInvert());
    }
}
