public class EloRatingCalculator {

    // Константы, которые могут быть настроены в соответствии с требованиями
    private static final int DEFAULT_INITIAL_RATING = 1000;
    private static final int DEFAULT_K_FACTOR = 32;

    /**
     * Рассчитать новый рейтинг по системе Эло.
     *
     * @param playerRating  Рейтинг игрока перед матчем.
     * @param opponentRating Рейтинг соперника перед матчем.
     * @param result        Результат матча (1 - победа, 0.5 - ничья, 0 - поражение).
     * @return Новый рейтинг игрока после матча.
     */
    public static int calculateEloRating(int playerRating, int opponentRating, double result) {
        double expectedScore = calculateExpectedScore(playerRating, opponentRating);

        // Расчет изменения рейтинга
        int ratingChange = (int) Math.round(DEFAULT_K_FACTOR * (result - expectedScore));

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
    private static double calculateExpectedScore(int playerRating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));
    }
}
