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

            if (messageText.equals("!wipe"))
                context.setCurrentState(RegistrationState.WIPE);

            if (messageText.equals("!clear"))
                context.setCurrentState(RegistrationState.CLEAR);

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
                    message.setText("Введите фракцию:");
                    context.setCurrentState(RegistrationState.END_REGISTRATION);
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
                    message.setText("Введите своё имя и фракцию в формате Name:Faction. Как указывали при регистрации.");
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
            registerButton = new KeyboardButton("Зарегистрироваться");
            row3.add(registerButton);
            keyboard.add(row3);
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
        SHOW_LADDER, SENT_RESULT, SENT_RESULT_PLAYER1, SENT_RESULT_MATCHRESULT, SENT_RESULT_PLAYER2, WIPE, CLEAR, END_REGISTRATION
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
