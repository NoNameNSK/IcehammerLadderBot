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

    private Map<Long, RegistrationContext> userContexts = new HashMap<>();

    private static class RegistrationContext {
        private RegistrationState currentState;
        private String playerName;
        private String faction;

        public RegistrationContext() {
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
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            RegistrationContext context = userContexts.computeIfAbsent(chatId, k -> new RegistrationContext());
            SendMessage message;

            if(messageText.equals("Зарегистрироваться"))
                context.setCurrentState(RegistrationState.ENTER_NAME);

            if(messageText.equals("Показать ладдер"))
                context.setCurrentState(RegistrationState.SHOW_LADDER);

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
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton registerButton = new KeyboardButton("Показать ладдер");
        row1.add(registerButton);
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        registerButton = new KeyboardButton("Зарегистрироваться");
        row2.add(registerButton);
        keyboard.add(row2);

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
        START, // начальное состояние
        ENTER_NAME, // ввод имени
        ENTER_FACTION, // ввод фракции
        SHOW_LADDER, END_REGISTRATION
    }

}
