import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerRegistration {

    private static final String DATA_FILE_PATH = "src/main/resources/players.json";
    private static final String MATCH_HISTORY_FILE_PATH = "src/main/resources/matchHistory.json";
    private static final  Map<String, PlayerData> playerDatabase = loadPlayerData();
    private static final List<MatchHistoryEntry> matchHistory = loadMatchHistory();

    /**
     * Регистрация нового игрока.
     *
     * @param playerName Имя игрока.
     * @param faction    Фракция игрока.
     */
    public static String registerPlayer(String playerName, String faction) {
        String playerKey = playerName + ":" + faction;

        if (!playerDatabase.containsKey(playerKey)) {
            PlayerData newPlayer = new PlayerData(playerName, faction);
            playerDatabase.put(playerKey, newPlayer);
            savePlayerData();
            return "Игрок " + playerName + " с фракцией " + faction + " успешно зарегистрирован.";
        } else {
            return "Игрок " + playerName + " с фракцией " + faction + " уже зарегистрирован.";
        }
    }

    /**
     * Обновить рейтинги игроков согласно результатам матча.
     *
     * @param player1Name Имя и фракция первого игрока
     * @param player2Name Имя и фракция второго игрока
     * @param result      результат матча
     * @return
     */
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

    /**
     * Вывести информацию о всех игроках, отсортированных по рейтингу и имени.
     */
    public static String displayAllPlayers() {
        return playerDatabase.values().stream()
                .sorted(Comparator.comparing(PlayerData::getRating).reversed()
                        .thenComparing(PlayerData::getPlayerName))
                .map(player -> "Игрок: " + player.getPlayerName() +
                        ", Фракция: " + player.getFaction() +
                        ", Рейтинг: " + player.getRating())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Загрузка данных об игроках из файла JSON.
     *
     * @return Карта данных об игроках.
     */
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

    /**
     * Сохранение данных об игроках в файл JSON.
     */
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
        playerDatabase.values().forEach(player -> player.setRating(1000));
        savePlayerData();
    }

    public static void clearAllPlayers() {
        playerDatabase.clear();
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

    private static void saveMatchHistory() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            objectMapper.writeValue(new File(MATCH_HISTORY_FILE_PATH), matchHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record MatchHistoryEntry(String player1Name, String faction1, int rating1Before, int rating1After,
                                     double result, String player2Name, String faction2, int rating2Before,
                                     int rating2After, double matchResultInvert) {}
}
