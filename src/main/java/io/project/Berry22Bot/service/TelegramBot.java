package io.project.Berry22Bot.service;

import io.project.Berry22Bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    final BotConfig config;

    static final String HELP_TEXT = " Цей бот створений , щоб допомогти вам зв'язатися із потрібною службою.\n\n" +
            "Можна користуватися командами з меню\n\n " +
            "/start щоб розпочати\n\n" +
            "/mydata отримати особисту інформацію\n\n" +
            "/help щоб побачити це повідомлення знову ";

    static final String FIRE = "Виклик служби порятунку";
    static final String POLICE = "Виклик поліції";
    static final String AMBULANCE = "Викшлик швидкої медичної допомоги";

    public TelegramBot(BotConfig config){

        this.config = config;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Розпочати чат"));
        listOfCommands.add(new BotCommand("/mydata","Отримати інформацію профілю"));
        listOfCommands.add(new BotCommand("/deletedata","Видалити інформацію профілю"));
        listOfCommands.add(new BotCommand("/help","Інформація з використання боту"));
        listOfCommands.add(new BotCommand("/settings", "Налаштування"));
        listOfCommands.add(new BotCommand("101","Служба порятунку"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));

        }

        catch (TelegramApiException e){

            log.error("Error setting bot`s command list: " + e.getMessage());

        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        SendMessage sendMessage = new SendMessage();
        if (update.hasMessage()){

            Message message = update.getMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            String chatID = String.valueOf(message.getChatId());

            if (update.hasMessage() && update.getMessage().hasText()){
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                switch (messageText){
                    case "/start":
                        startCommandReceived(chatId,update.getMessage().getChat().getFirstName());

                        break;

                    case "/help":

                        sendMessage(chatId, HELP_TEXT);
                        break;

                    case "101":
                        sendMessage(chatId,FIRE);
                        break;



                    default: sendMessage(chatId, "Sorry command was not recognized");

                }
            }


            sendMessage.setText("Оберіть потрібну службу");
            sendMessage.setReplyMarkup(inlineKeyboard());}


        else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Message message = callbackQuery.getMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));

            if (callbackQuery.getData().equals("101")) {
                sendMessage.setText("Переадресація дзвінка до служби порятунку");
            } else if (callbackQuery.getData().equals("102")) {
                sendMessage.setText("Переадресація дзвінка до поліції");
            } else if (callbackQuery.getData().equals("103")) {
                sendMessage.setText("Відбувається переадресація дзвінка на швидку медичну допомогу");
            }



        }
        try {
            execute(sendMessage);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
        }




    private InlineKeyboardMarkup inlineKeyboard(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder().text("Служба порятунку").callbackData("101").build()));

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder().text("Поліція").callbackData("102").build()));

        keyboard.add(Collections.singletonList(InlineKeyboardButton.builder().text("Швидка медична допомога").callbackData("103").build()));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }


    private void startCommandReceived(long chatId , String name){
        String answer = "Привіт, " + name + ", обирай потрібну службу!";

        log.info("Replied to user " + name);

        sendMessage(chatId, answer);


    }

    private  void  sendMessage(long chatId , String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);

        }
        catch (TelegramApiException e){

            log.error("Error occurred: " + e.getMessage());

        }
    }

}

