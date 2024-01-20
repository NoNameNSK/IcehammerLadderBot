package bot;

public enum MatchResult {
    WIN("Победа"),
    LOSE("Поражение"),
    DRAW("Ничья");

    private final String value;

    MatchResult(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
    public static boolean isValidResult(String input) {
        for (MatchResult matchResult : MatchResult.values()) {
            if (matchResult.getValue().equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }
}
