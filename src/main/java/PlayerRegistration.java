import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerRegistration {

    private static final String DATA_FILE_PATH = "src/main/resources/players.json";
    private final static Map<String, PlayerData> playerDatabase = loadPlayerData();

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
     * @param matchResults Результаты матча в виде "Игрок1:Фракция1 победа/поражение Игрок2:Фракция2".
     */
    public void updateRatings(String matchResults) {
        String[] playerResults = matchResults.split(";");
        for (String result : playerResults) {
            String[] results = result.split(" ");
            String[] parts = results[0].split(":");
            String playerName = parts[0];
            String faction = parts[1];
            double matchResult = Double.parseDouble(results[1]);  // 1.0 для победы, 0.0 для поражения

            String playerKey = playerName + ":" + faction;
            PlayerData player = playerDatabase.get(playerKey);

            if (player != null) {
                int opponentRating = getOpponentRating(playerResults, playerName, faction);
                int newRating = calculateEloRating(player.getRating(), opponentRating, matchResult);
                player.setRating(newRating);
            }
        }

        savePlayerData();  // Сохраняем обновленные рейтинги в файл
    }

    /**
     * Рассчитать новый рейтинг по системе Эло.
     *
     * @param playerRating  Рейтинг игрока перед матчем.
     * @param opponentRating Рейтинг соперника перед матчем.
     * @param result        Результат матча (1.0 - победа, 0.0 - поражение).
     * @return Новый рейтинг игрока после матча.
     */
    private int calculateEloRating(int playerRating, int opponentRating, double result) {
        double expectedScore = calculateExpectedScore(playerRating, opponentRating);
        int kFactor = 32;

        // Расчет изменения рейтинга
        int ratingChange = (int) Math.round(kFactor * (result - expectedScore));

        // Новый рейтинг игрока
        return playerRating + ratingChange;
    }

    /**
     * Рассчитать ожидаемый результат в матче по системе Эло.
     *
     * @param playerRating  Рейтинг игрока перед матчем.
     * @param opponentRating Рейтинг соперника перед матчем.
     * @return Ожидаемый результат в диапазоне от 0 до 1.
     */
    private double calculateExpectedScore(int playerRating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));
    }

    /**
     * Получить рейтинг соперника.
     *
     * @param playerResults Результаты матча.
     * @param playerName    Имя текущего игрока.
     * @param faction       Фракция текущего игрока.
     * @return Рейтинг соперника.
     */
    private int getOpponentRating(String[] playerResults, String playerName, String faction) {
        for (String result : playerResults) {
            String[] parts = result.split(":");
            String opponentName = parts[0];
            String opponentFaction = parts[1];
            double matchResult = Double.parseDouble(parts[2]);

            if (!opponentName.equals(playerName) || !opponentFaction.equals(faction)) {
                String opponentKey = opponentName + ":" + opponentFaction;
                PlayerData opponent = playerDatabase.get(opponentKey);
                if (opponent != null) {
                    return opponent.getRating();
                }
            }
        }
        return 1000; // Возвращаем значение по умолчанию, если не удалось найти рейтинг соперника
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

    public static void main(String[] args) {
        PlayerRegistration playerRegistration = new PlayerRegistration();

        // Пример обновления рейтингов после матча и вывода обновленной информации
        playerRegistration.updateRatings("Игрок1:Фракция1 1.0;Игрок2:Фракция3 0.0");
        playerRegistration.displayAllPlayers();
    }
}
