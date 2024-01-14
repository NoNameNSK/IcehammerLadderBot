package bot;

public class PlayerData {

    private String playerName;
    private String faction;
    private int rating;

    private static final int DEFAULT_PLAYER_RATING = 1550;

    public PlayerData() {
    }

    public PlayerData(String playerName, String faction) {
        this.playerName = playerName;
        this.faction = faction;
        this.rating = DEFAULT_PLAYER_RATING;
    }

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
