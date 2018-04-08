package bot;

import commands.BlastCommand;
import commands.CheckInCommand;
import commands.HelpCommand;
import commands.ReturnInfoCommand;
import config.BotConfig;
import model.MessageEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.RoomEventListener;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.client.services.RoomServiceEventListener;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;
import utils.MessageParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomChatBot implements RoomServiceEventListener, RoomEventListener {

    private static RoomChatBot instance;
    private final Logger logger = LoggerFactory.getLogger(RoomChatBot.class);
    private SymphonyClient symClient;
    private RoomService roomService;
    private BotConfig config;
    private Map<String, Command> commandMap;
    private Command defaultCommand;
    private MessageParser messageParser;

    protected RoomChatBot(SymphonyClient symClient, BotConfig config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static RoomChatBot getInstance(SymphonyClient symClient, BotConfig config){
        if(instance==null){
            instance = new RoomChatBot(symClient,config);
        }
        return instance;
    }

    private void init() {

        roomService = symClient.getRoomService();
        roomService.addRoomServiceEventListener(this);
        commandMap = new HashMap<>();
        Command helpCommand = new HelpCommand("help", "Get all commands this bot accepts",symClient);
        Command blastCommand = new BlastCommand("blast","Blast all users connected to the bot.",symClient,config.getMongoURL());
        Command checkInCommand = new CheckInCommand("checkin", "Check-in user to this room's booth. by sending <hash tag=\"checkin\"/> [@mention (preferred) or email address]",config.getMongoURL(),symClient);
        Command defaultCommand = new ReturnInfoCommand();
        commandMap.put("help", helpCommand);
        commandMap.put("blast",blastCommand);
        commandMap.put("checkin", checkInCommand);
        messageParser = new MessageParser();
    }

    @Override
    public void onRoomMessage(SymMessage message) {

        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

        MessageEntities messageEntities = messageParser.getMessageEntities(message.getEntityData());

        for (String hashtag: messageEntities.getHashtags()) {
            if(commandMap.containsKey(hashtag)){
                Command command = commandMap.get(hashtag);
                command.execute(message);
            }
        }
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

}
