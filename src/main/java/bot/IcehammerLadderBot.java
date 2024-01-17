package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IcehammerLadderBot extends TelegramLongPollingBot {

//    Баг на создание игрока при смене страниц
//Баг на внесение результатов с несуществующим именем оппа
//Фича на форматирования вывода ладдера 

    private final Map<Long, UserContext> userContexts = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            UserContext context = userContexts.computeIfAbsent(chatId, k -> new UserContext());
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            ;

            if (messageText.equals("Зарегистрироваться"))
                context.setCurrentState(RegistrationState.ENTER_NAME);

            if (messageText.equals("Показать ладдер"))
                context.setCurrentState(RegistrationState.SHOW_LADDER);

            if (messageText.equals("Внести результаты"))
                context.setCurrentState(RegistrationState.SENT_RESULT);

            if (messageText.equals("Показать историю матчей"))
                context.setCurrentState(RegistrationState.SHOW_MATCH_HISTORY);

            if (messageText.equals("Объявления"))
                context.setCurrentState(RegistrationState.ANNOUNCEMENT);

            if (messageText.equals("!wipe"))
                context.setCurrentState(RegistrationState.WIPE);

            if (messageText.equals("!clear"))
                context.setCurrentState(RegistrationState.CLEAR);

            if (messageText.contains("!delete"))
                context.setCurrentState(RegistrationState.DELETE);

            if (Faction.isValidFaction(messageText) &&
                    (
                            context.getCurrentState().equals(RegistrationState.ENTER_FACTION) ||
                                    context.getCurrentState().equals(RegistrationState.ENTER_FACTION_1) ||
                                    context.getCurrentState().equals(RegistrationState.ENTER_FACTION_2)
                    )
            )
                context.setCurrentState(RegistrationState.END_REGISTRATION);

            if (Faction.isValidFaction(messageText) &&
                    (
                            context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION) ||
                                    context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1) ||
                                    context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_2)
                    )
            )
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1);

            if (Faction.isValidFaction(messageText) &&
                    (
                            context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION) ||
                                    context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1) ||
                                    context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_2)
                    )
            )
                context.setCurrentState(RegistrationState.SENT_RESULT_FINISH);

            if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.ENTER_FACTION_1);
            else if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION_1) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.ENTER_FACTION_2);
            else if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION_1) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.ENTER_FACTION);
            else if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION_2) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.ENTER_FACTION_1);

            if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1_FACTION_2);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1_FACTION);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_2) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1);

            if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1) &&
                    messageText.equals("Дальше"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2_FACTION_2);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2_FACTION);
            else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_2) &&
                    messageText.equals("Назад"))
                context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1);

            switch (context.getCurrentState()) {
                case START -> {
                    message.setText("Привет! Выберите действие:");
                    sendKeyboardMainMenu(message);
                }
                case ENTER_NAME -> {
                    message.setText("Введите имя:");
                    context.setCurrentState(RegistrationState.ENTER_FACTION);
                }
                case ENTER_FACTION -> {
                    if (context.getPlayerName() == null || !context.getPlayerName().equals("Назад"))
                        context.setPlayerName(messageText);
                    message.setText("Выберите фракцию:");
                    sendKeyboardFaction(message, context);
                }
                case ENTER_FACTION_1, ENTER_FACTION_2 -> {
                    message.setText("Выберите фракцию:");
                    sendKeyboardFaction(message, context);
                }
                case END_REGISTRATION -> {
                    context.setFaction(messageText);
                    message.setText(PlayerRegistration.registerPlayer(context.getPlayerName(), context.getFaction()));
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case SHOW_LADDER -> {
                    message.setText(PlayerRegistration.displayAllPlayers());
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case SENT_RESULT -> {
                    message.setText("Введите своё имя, как указывали при регистрации.");
                    context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1_FACTION);
                }
                case SENT_RESULT_PLAYER1_FACTION -> {
                    if (context.getPlayerName() == null || !context.getPlayerName().equals("Назад"))
                        context.setPlayer1(messageText);
                    message.setText("Выберите свою фракцию");
                    sendKeyboardMatchResult(message, context);
                }
                case SENT_RESULT_PLAYER1_FACTION_1, SENT_RESULT_PLAYER1_FACTION_2, SENT_RESULT_PLAYER2_FACTION_1, SENT_RESULT_PLAYER2_FACTION_2 -> {
                    message.setText("Выберите свою фракцию");
                    sendKeyboardMatchResult(message, context);
                }
                case SENT_RESULT_PLAYER1 -> {
                    context.setFaction1(messageText);
                    message.setText("Выберите результат матча");
                    context.setCurrentState(RegistrationState.SENT_RESULT_MATCHRESULT);
                    sendKeyboardMatchResult(message, context);
                }
                case SENT_RESULT_MATCHRESULT -> {
                    context.setMatchResult(messageText);
                    message.setText("Введите имя оппонента.");
                    context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2);
                }
                case SENT_RESULT_PLAYER2 -> {
                    if (context.getPlayerName() == null || !context.getPlayerName().equals("Назад"))
                        context.setPlayer2(messageText);
                    context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2_FACTION);
                    message.setText("Выберите фракцию оппонента");
                    sendKeyboardMatchResult(message, context);
                }
                case SENT_RESULT_FINISH -> {
                    context.setFaction2(messageText);
                    String result = PlayerRegistration.updateRatings(
                            context.getPlayer1() + ":" + context.getFaction1(),
                            context.getPlayer2() + ":" + context.getFaction2(),
                            context.getMatchResult()
                    );
                    message.setText(result);
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case WIPE -> {
                    message.setText("Рейтинг сброшен");
                    PlayerRegistration.wipe();
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case CLEAR -> {
                    message.setText("Все участники удалены");
                    PlayerRegistration.clearAllPlayers();
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case SHOW_MATCH_HISTORY -> {
                    message.setText(PlayerRegistration.showMatchHistory());
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case DELETE -> {
                    PlayerRegistration.deletePlayer(messageText.split(" ")[1]);
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                case ANNOUNCEMENT -> {
                    message.setText(PlayerRegistration.announcement());
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboardMainMenu(message);
                }
                default -> {
                    message.setText("Что-то пошло не так. Попробуйте еще раз.");
                    context.setCurrentState(RegistrationState.START);
                }
            }

            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendKeyboardMainMenu(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton registerButton = new KeyboardButton("Показать ладдер");
        row1.add(registerButton);
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        registerButton = new KeyboardButton("Внести результаты");
        row2.add(registerButton);
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        registerButton = new KeyboardButton("Показать историю матчей");
        row3.add(registerButton);
        keyboard.add(row3);

        KeyboardRow row4 = new KeyboardRow();
        registerButton = new KeyboardButton("Объявления");
        row4.add(registerButton);
        keyboard.add(row4);

        KeyboardRow row5 = new KeyboardRow();
        registerButton = new KeyboardButton("Зарегистрироваться");
        row5.add(registerButton);
        keyboard.add(row5);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void sendKeyboardMatchResult(SendMessage message, UserContext context) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        if (message.getText().equals("Выберите результат матча")) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton("Победа");
            KeyboardButton button2 = new KeyboardButton("Ничья");
            KeyboardButton button3 = new KeyboardButton("Поражение");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            keyboard.add(row1);
        } else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION) ||
                context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.SPACE_MARINES.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.BLACK_TEMPLARS.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.BLOOD_ANGELS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.DARK_ANGELS.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.DEATHWATCH.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.SPACE_WOLVES.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.ADEPTA_SORORITAS.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.ADEPTUS_CUSTODES.getValue());
            KeyboardButton button9 = new KeyboardButton(Faction.ADEPTUS_MECHANICUS.getValue());
            KeyboardButton button10 = new KeyboardButton("Дальше");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row3.add(button9);
            row4.add(button10);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        } else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_1) ||
                context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_1)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.ASTRA_MILITARUM.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.GREY_KNIGHTS.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.IMPERIAL_KNIGHTS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.CHAOS_DAEMONS.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.CHAOS_KNIGHTS.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.CHAOS_SPACE_MARINES.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.DEATH_GUARD.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.THOUSAND_SONS.getValue());
            KeyboardButton button9 = new KeyboardButton(Faction.WORLD_EATERS.getValue());
            KeyboardButton button10 = new KeyboardButton("Назад");
            KeyboardButton button11 = new KeyboardButton("Дальше");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row3.add(button9);
            row4.add(button10);
            row4.add(button11);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        } else if (context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER1_FACTION_2) ||
                context.getCurrentState().equals(RegistrationState.SENT_RESULT_PLAYER2_FACTION_2)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.AELDARI.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.DRUKHARI.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.GENESTEALER_CULTS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.LEAGUES_OF_VOTANN.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.NECRONS.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.ORKS.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.TAU_EMPIRE.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.TYRANIDS.getValue());
            KeyboardButton button10 = new KeyboardButton("Назад");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row4.add(button10);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void sendKeyboardFaction(SendMessage message, UserContext context) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.SPACE_MARINES.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.BLACK_TEMPLARS.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.BLOOD_ANGELS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.DARK_ANGELS.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.DEATHWATCH.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.SPACE_WOLVES.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.ADEPTA_SORORITAS.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.ADEPTUS_CUSTODES.getValue());
            KeyboardButton button9 = new KeyboardButton(Faction.ADEPTUS_MECHANICUS.getValue());
            KeyboardButton button10 = new KeyboardButton("Дальше");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row3.add(button9);
            row4.add(button10);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        } else if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION_1)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.ASTRA_MILITARUM.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.GREY_KNIGHTS.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.IMPERIAL_KNIGHTS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.CHAOS_DAEMONS.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.CHAOS_KNIGHTS.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.CHAOS_SPACE_MARINES.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.DEATH_GUARD.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.THOUSAND_SONS.getValue());
            KeyboardButton button9 = new KeyboardButton(Faction.WORLD_EATERS.getValue());
            KeyboardButton button10 = new KeyboardButton("Назад");
            KeyboardButton button11 = new KeyboardButton("Дальше");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row3.add(button9);
            row4.add(button10);
            row4.add(button11);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        } else if (context.getCurrentState().equals(RegistrationState.ENTER_FACTION_2)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();
            KeyboardRow row4 = new KeyboardRow();
            KeyboardButton button1 = new KeyboardButton(Faction.AELDARI.getValue());
            KeyboardButton button3 = new KeyboardButton(Faction.DRUKHARI.getValue());
            KeyboardButton button2 = new KeyboardButton(Faction.GENESTEALER_CULTS.getValue());
            KeyboardButton button4 = new KeyboardButton(Faction.LEAGUES_OF_VOTANN.getValue());
            KeyboardButton button5 = new KeyboardButton(Faction.NECRONS.getValue());
            KeyboardButton button6 = new KeyboardButton(Faction.ORKS.getValue());
            KeyboardButton button7 = new KeyboardButton(Faction.TAU_EMPIRE.getValue());
            KeyboardButton button8 = new KeyboardButton(Faction.TYRANIDS.getValue());
            KeyboardButton button10 = new KeyboardButton("Назад");
            row1.add(button1);
            row1.add(button2);
            row1.add(button3);
            row2.add(button4);
            row2.add(button5);
            row2.add(button6);
            row3.add(button7);
            row3.add(button8);
            row4.add(button10);
            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
            keyboard.add(row4);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    @Override
    public String getBotUsername() {
        return "Шо ви имеете мине сказать?";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TEST_TOKEN");//test
    }

    private enum RegistrationState {
        START,
        ENTER_NAME,
        ENTER_FACTION,
        SHOW_LADDER, SENT_RESULT, SENT_RESULT_PLAYER1, SENT_RESULT_MATCHRESULT, SENT_RESULT_PLAYER2, WIPE, CLEAR, SHOW_MATCH_HISTORY, ENTER_FACTION_1, ENTER_FACTION_2, DELETE, SENT_RESULT_PLAYER1_FACTION, SENT_RESULT_PLAYER1_FACTION_1, SENT_RESULT_PLAYER1_FACTION_2, SENT_RESULT_PLAYER2_FACTION, SENT_RESULT_PLAYER2_FACTION_1, SENT_RESULT_PLAYER2_FACTION_2, SENT_RESULT_FINISH, ANNOUNCEMENT, END_REGISTRATION
    }

    private static class UserContext {
        private RegistrationState currentState;
        private String playerName;
        private String faction;
        private String player1;
        private String faction1;
        private MatchResult matchResult;
        private String player2;
        private String faction2;

        public UserContext() {
            this.currentState = RegistrationState.START;
        }

        public RegistrationState getCurrentState() {
            return currentState;
        }

        public void setCurrentState(RegistrationState currentState) {
            this.currentState = currentState;
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

        public String getPlayer1() {
            return player1;
        }

        public void setPlayer1(String player1) {
            this.player1 = player1;
        }

        public MatchResult getMatchResult() {
            return matchResult;
        }

        public void setMatchResult(String matchResult) {
            switch (matchResult) {
                case "Победа" -> this.matchResult = MatchResult.WIN;
                case "Ничья" -> this.matchResult = MatchResult.DRAW;
                case "Поражение" -> this.matchResult = MatchResult.LOSE;
            }
        }

        public void setMatchResult(MatchResult matchResult) {
            this.matchResult = matchResult;
        }

        public String getPlayer2() {
            return player2;
        }

        public void setPlayer2(String player2) {
            this.player2 = player2;
        }

        public String getFaction1() {
            return faction1;
        }

        public void setFaction1(String faction1) {
            this.faction1 = faction1;
        }

        public String getFaction2() {
            return faction2;
        }

        public void setFaction2(String faction2) {
            this.faction2 = faction2;
        }
    }

}
