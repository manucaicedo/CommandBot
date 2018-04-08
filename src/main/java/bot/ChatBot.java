package bot;

import commands.*;
import config.BotConfig;
import model.MessageEntities;
import mongo.MongoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import utils.ContentConstants;
import utils.MessageParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatBot implements ChatListener, ChatServiceListener {

    private static ChatBot instance;
    private final Logger logger = LoggerFactory.getLogger(ChatBot.class);
    private SymphonyClient symClient;
    private BotConfig config;
    private Map<String, Command> commandMap;
    private Command checkInCommand;
    private MessageParser messageParser;
    private MongoDBClient mongoDBClient;


    protected ChatBot(SymphonyClient symClient, BotConfig config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static ChatBot getInstance(SymphonyClient symClient, BotConfig config){
        if(instance==null){
            instance = new ChatBot(symClient,config);
        }
        return instance;
    }

    private void init() {


        symClient.getChatService().addListener(this);
        commandMap = new HashMap<>();
        Command helpCommand = new HelpCommand("help", "Get all commands this bot accepts",symClient);
        Command agendaCommand = new AgendaCommand("agenda", "See today's agenda", symClient);
        Command nowCommand = new NowCommand("now", "See what is going on right now",symClient, config.getMongoURL());
        Command upNextCommand = new UpNextCommand("upnext", "See which event is happening next.", symClient, config.getMongoURL());
        Command mapCommand = new MapCommand("map", "Get map of event", symClient, config.getMongoURL());
        Command bioCommand = new BioCommand("bio", "Get bio of speakers", symClient, config.getMongoURL());
        checkInCommand = new CheckInCommand("checkin", "Check-in to sponsor booths by sending the hashtag on their table",config.getMongoURL(),symClient);
        commandMap.put("agenda", agendaCommand);
        commandMap.put("help", helpCommand);
        commandMap.put("now", nowCommand);
        commandMap.put("next", upNextCommand);
        commandMap.put("upnext", upNextCommand);
        commandMap.put("map", mapCommand);
        commandMap.put("bio", bioCommand);
        messageParser = new MessageParser();
        mongoDBClient = MongoDBClient.getInstance(config.getMongoURL());

    }


    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

        MessageEntities messageEntities = messageParser.getMessageEntities(message.getEntityData());
        boolean done = false;
        for (String hashtag: messageEntities.getHashtags()) {
            if(commandMap.containsKey(hashtag)){
                Command command = commandMap.get(hashtag);
                command.execute(message);
                done = true;
            }
        }
        if (!done){
            for (String hashtag: messageEntities.getHashtags()) {
                if (mongoDBClient.getBoothRoom(hashtag)!=null){
                    checkInCommand.execute(message);
                    done=true;
                }
            }
        }
        if(!done){
            SymMessage errorMsg = new SymMessage();
            errorMsg.setMessage(ContentConstants.ERROR_MESSAGE);
            try {
                symClient.getMessagesClient().sendMessage(message.getStream(),errorMsg);
                done=true;
            } catch (MessagesException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onNewChat(Chat chat) {

        chat.addListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getStreamId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {

    }
}
