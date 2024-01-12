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
     * @param matchResultsString Результаты матча в виде "Игрок1:Фракция1 победа/поражение Игрок2:Фракция2".
     * @return
     */
    public static String updateRatings(String matchResultsString) {
        String[] matchResultArray = matchResultsString.split(";");

        double matchResult = 0;
        double matchResultInvert = 0;
        switch (matchResultArray[1]) {
            case "Победа" -> {
                matchResult = 1.0;
                matchResultInvert = 0.0;
            }
            case "Ничья" -> {
                matchResult = 0.5;
                matchResultInvert = 0.5;
            }
            case "Поражение" -> {
                matchResult = 0.0;
                matchResultInvert = 1.0;
            }
            default -> {

            }
        }

        PlayerData player1 = playerDatabase.get(matchResultArray[0]);
        PlayerData player2 = playerDatabase.get(matchResultArray[2]);

        if (player1 != null) {
            if (player2 != null) {

                player1.setRating(
                        EloRatingCalculator.calculateEloRating(player1.getRating(), player2.getRating(), matchResult));
                player2.setRating(
                        EloRatingCalculator.calculateEloRating(player2.getRating(), player1.getRating(), matchResultInvert));

                savePlayerData();  // Сохраняем обновленные рейтинги в файл
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
}
