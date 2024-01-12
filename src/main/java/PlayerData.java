public class PlayerData {

    private String playerName;
    private String faction;
    private int rating;  // добавим рейтинг

    // Конструктор по умолчанию для Jackson
    public PlayerData() {
    }

    // Конструктор для создания нового игрока
    public PlayerData(String playerName, String faction) {
        this.playerName = playerName;
        this.faction = faction;
        this.rating = 1000;  // начальное значение рейтинга (вы можете установить любое значение)
    }

    // Геттеры и сеттеры для всех полей
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
