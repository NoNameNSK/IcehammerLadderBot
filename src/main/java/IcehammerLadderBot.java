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

    private final Map<Long, UserContext> userContexts = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            UserContext context = userContexts.computeIfAbsent(chatId, k -> new UserContext());
            SendMessage message = null;

            if (messageText.equals("Зарегистрироваться"))
                context.setCurrentState(RegistrationState.ENTER_NAME);

            if (messageText.equals("Показать ладдер"))
                context.setCurrentState(RegistrationState.SHOW_LADDER);

            if (messageText.equals("Внести результаты"))
                context.setCurrentState(RegistrationState.SENT_RESULT);

            if (messageText.equals("Показать историю матчей"))
                context.setCurrentState(RegistrationState.SHOW_MATCH_HISTORY);

            if (messageText.equals("!wipe"))
                context.setCurrentState(RegistrationState.WIPE);

            if (messageText.equals("!clear"))
                context.setCurrentState(RegistrationState.CLEAR);

            if (messageText.contains("!delete"))
                context.setCurrentState(RegistrationState.DELETE);

            if (Faction.isValidFaction(messageText))
                context.setCurrentState(RegistrationState.END_REGISTRATION);

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

            switch (context.getCurrentState()) {
                case START -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Привет! Выберите действие:");
                    sendKeyboard(message);
                }
                case ENTER_NAME -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Введите имя:");
                    context.setCurrentState(RegistrationState.ENTER_FACTION);
                }
                case ENTER_FACTION -> {
                    context.setPlayerName(messageText);
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Выберите фракцию:");
                    sendKeyboardFaction(message, context);
                }
                case ENTER_FACTION_1, ENTER_FACTION_2 -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Выберите фракцию:");
                    sendKeyboardFaction(message, context);
                }
                case END_REGISTRATION -> {
                    context.setFaction(messageText);
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(PlayerRegistration.registerPlayer(context.getPlayerName(), context.getFaction()));
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case SHOW_LADDER -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(PlayerRegistration.displayAllPlayers());
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case SENT_RESULT -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Введите своё имя, как указывали при регистрации.");
                    context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER1);
                    sendKeyboard(message);
                }
                case SENT_RESULT_PLAYER1 -> {
                    context.setPlayer1(messageText);
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Выберите результат матча");
                    context.setCurrentState(RegistrationState.SENT_RESULT_MATCHRESULT);
                    sendKeyboard(message);
                }
                case SENT_RESULT_MATCHRESULT -> {
                    context.setMatchResult(messageText);
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Введите имя и фракцию оппонента в формате Name:Faction. Как он указывал при регистрации.");
                    context.setCurrentState(RegistrationState.SENT_RESULT_PLAYER2);
                    sendKeyboard(message);
                }
                case SENT_RESULT_PLAYER2 -> {
                    context.setPlayer2(messageText);
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(
                            PlayerRegistration.updateRatings(
                                    context.getPlayer1(),
                                    context.getPlayer2(),
                                    context.getMatchResult()
                            ));
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case WIPE -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Рейтинг сброшен");
                    PlayerRegistration.wipe();
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case CLEAR -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Все участники удалены");
                    PlayerRegistration.clearAllPlayers();
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case SHOW_MATCH_HISTORY -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(PlayerRegistration.showMatchHistory());
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                case DELETE -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
                    PlayerRegistration.deletePlayer(messageText.split(" ")[1]);
                    context.setCurrentState(RegistrationState.START);
                    sendKeyboard(message);
                }
                default -> {
                    message = new SendMessage();
                    message.setChatId(chatId);
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

    private void sendKeyboard(SendMessage message) {
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
        } else {
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
            registerButton = new KeyboardButton("Зарегистрироваться");
            row4.add(registerButton);
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
        return System.getenv("BOT_TOKEN");
    }

    private enum RegistrationState {
        START,
        ENTER_NAME,
        ENTER_FACTION,
        SHOW_LADDER, SENT_RESULT, SENT_RESULT_PLAYER1, SENT_RESULT_MATCHRESULT, SENT_RESULT_PLAYER2, WIPE, CLEAR, SHOW_MATCH_HISTORY, ENTER_FACTION_1, ENTER_FACTION_2, DELETE, END_REGISTRATION
    }

    private static class UserContext {
        private RegistrationState currentState;
        private String playerName;
        private String faction;
        private String player1;
        private MatchResult matchResult;
        private String player2;

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

        public String getPlayer2() {
            return player2;
        }

        public void setPlayer2(String player2) {
            this.player2 = player2;
        }
    }

}
