package bot;

public enum Faction {
    SPACE_MARINES("SPACE MARINES"),
    BLACK_TEMPLARS("BLACK TEMPLARS"),
    BLOOD_ANGELS("BLOOD ANGELS"),
    DARK_ANGELS("DARK ANGELS"),
    DEATHWATCH("DEATHWATCH"),
    SPACE_WOLVES("SPACE WOLVES"),
    ADEPTA_SORORITAS("ADEPTA SORORITAS"),
    ADEPTUS_CUSTODES("ADEPTUS CUSTODES"),
    ADEPTUS_MECHANICUS("ADEPTUS MECHANICUS"),

    ADEPTUS_TITANICUS("ADEPTUS TITANICUS"),

    AGENTS_OF_THE_IMPERIUM("AGENTS OF THE IMPERIUM"),
    ASTRA_MILITARUM("ASTRA MILITARUM"),
    GREY_KNIGHTS("GREY KNIGHTS"),
    IMPERIAL_KNIGHTS("IMPERIAL KNIGHTS"),
    CHAOS_DAEMONS("CHAOS DAEMONS"),
    CHAOS_KNIGHTS("CHAOS KNIGHTS"),
    CHAOS_SPACE_MARINES("CHAOS SPACE MARINES"),
    DEATH_GUARD("DEATH GUARD"),
    THOUSAND_SONS("THOUSAND SONS"),
    WORLD_EATERS("WORLD EATERS"),
    AELDARI("AELDARI"),
    DRUKHARI("DRUKHARI"),
    GENESTEALER_CULTS("GENESTEALER CULTS"),
    LEAGUES_OF_VOTANN("LEAGUES OF VOTANN"),
    NECRONS("NECRONS"),
    ORKS("ORKS"),
    TAU_EMPIRE("T'AU EMPIRE"),
    TYRANIDS("TYRANIDS");

    private final String value;

    Faction(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
    public static boolean isValidFaction(String input) {
        for (Faction faction : Faction.values()) {
            if (faction.getValue().equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }
}
