package bot;

import commands.DefaultCommand;
import commands.HelpCommand;
import commands.TargetStringCommand;
import config.BotConfig;
import model.MessageEntities;
import mongo.MongoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import utils.MessageParser;

import java.util.HashMap;
import java.util.Map;

public class CommandBot implements ChatListener, ChatServiceListener, RoomServiceEventListener, RoomEventListener {

    private static CommandBot instance;
    private final Logger logger = LoggerFactory.getLogger(CommandBot.class);
    private SymphonyClient symClient;
    private BotConfig config;
    private Map<String, Command> commandMap;
    private Command defaultCommand;
    private MessageParser messageParser;
    private MongoDBClient mongoDBClient;
    private RoomService roomService;

    protected CommandBot(SymphonyClient symClient, BotConfig config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static CommandBot getInstance(SymphonyClient symClient, BotConfig config){
        if(instance==null){
            instance = new CommandBot(symClient,config);
        }
        return instance;
    }

    private void init() {
        symClient.getChatService().addListener(this);

        roomService = symClient.getRoomService();
        roomService.addRoomServiceEventListener(this);
        commandMap = new HashMap<>();
        Command helpCommand = new HelpCommand("help", "Get all commands this bot accepts",symClient);
        Command targetStringCommand = new TargetStringCommand("search", "Search across all accounts", symClient);
        defaultCommand = new DefaultCommand("error", "Could not understand command", symClient);
        commandMap.put("help", helpCommand);
        commandMap.put("search", targetStringCommand);
        messageParser = new MessageParser();
        mongoDBClient = MongoDBClient.getInstance(config.getMongoURL());

    }


    @Override
    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());
        processMessage(message);
    }

    @Override
    public void onNewChat(Chat chat) {
        chat.addListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getStreamId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {

    }

    @Override
    public void onRoomMessage(SymMessage symMessage) {
        processMessage(symMessage);
    }

    @Override
    public void onNewRoom(Room room) {
        room.addEventListener(this);
    }

    @Override
    public void onMessage(SymMessage symMessage) {

    }

    @Override
    public void onSymRoomDeactivated(SymRoomDeactivated symRoomDeactivated) {

    }

    @Override
    public void onSymRoomMemberDemotedFromOwner(SymRoomMemberDemotedFromOwner symRoomMemberDemotedFromOwner) {

    }

    @Override
    public void onSymRoomMemberPromotedToOwner(SymRoomMemberPromotedToOwner symRoomMemberPromotedToOwner) {

    }

    @Override
    public void onSymRoomReactivated(SymRoomReactivated symRoomReactivated) {

    }

    @Override
    public void onSymRoomUpdated(SymRoomUpdated symRoomUpdated) {

    }

    @Override
    public void onSymUserJoinedRoom(SymUserJoinedRoom symUserJoinedRoom) {

    }

    @Override
    public void onSymUserLeftRoom(SymUserLeftRoom symUserLeftRoom) {

    }

    @Override
    public void onSymRoomCreated(SymRoomCreated symRoomCreated) {

    }

    public void processMessage(SymMessage message){
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
            defaultCommand.execute(message);
        }
    }
}
