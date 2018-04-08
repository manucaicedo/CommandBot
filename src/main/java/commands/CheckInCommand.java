package commands;

import bot.Command;
import model.MessageEntities;
import model.mongo.BoothAttendee;
import model.mongo.BoothRoom;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.MemberInfo;
import org.symphonyoss.symphony.pod.model.Stream;
import utils.ContentConstants;
import utils.MessageParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CheckInCommand extends Command {

    private MongoDBClient mongoDBClient;
    private SymphonyClient symClient;

    public CheckInCommand(String command, String description, String mongoURL, SymphonyClient client) {
        super(command, description, client);
        this.symClient = client;
        this.mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {
        SymMessage confirmationMsg = new SymMessage();
        MessageParser parser = new MessageParser();
        MessageEntities entities = parser.getMessageEntities(message.getEntityData());
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("<messageML>");
        if(message.getStream().getStreamType().equals(SymStreamTypes.Type.IM)){
            String booth = entities.getHashtags().get(0);
            BoothRoom boothRoom = mongoDBClient.getBoothRoom(booth);
            if(boothRoom!=null) {
                if(!mongoDBClient.isUserCheckedIn(message.getSymUser().getEmailAddress(), booth)) {
                    BoothAttendee attendee = new BoothAttendee(message.getSymUser().getEmailAddress(), booth);
                    mongoDBClient.checkInAttendee(attendee);
                    messageBuilder.append("Checked in successfully to <hash tag=\"" + booth + "\"/> ");
                    SymMessage boothRoomMessage = new SymMessage();
                    boothRoomMessage.setMessage("<messageML><mention uid=\""+message.getSymUser().getId()+"\"/> just checked-in to your booth. Connect!</messageML>");
                    Stream stream = new Stream();
                    stream.setId(boothRoom.getStreamId());
                    try {
                        symClient.getMessagesClient().sendMessage(stream, boothRoomMessage);
                    } catch (MessagesException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    messageBuilder.append("You are already checked in to <hash tag=\"" + booth + "\"/> ");
                }
                try {
                    ArrayList<MemberInfo> members = symClient.getRoomMembershipClient().getRoomMembership(mongoDBClient.getBoothRoom(booth).getStreamId());

                    messageBuilder.append("Connect with representatives: ");
                    for (MemberInfo member : members) {
                        if (!member.getId().equals(symClient.getLocalUser().getId()))
                            messageBuilder.append("<mention uid=\"" + member.getId() + "\"/> ");
                    }

                } catch (SymException e) {
                    e.printStackTrace();
                }
            }

        }
        else {
            //TODO: CHECK IN BY EMAIL
            for (String mention: entities.getUsers()) {
                try {
                    SymUser user = symClient.getUsersClient().getUserFromId(Long.parseLong(mention));
                    BoothRoom boothRoom =  mongoDBClient.getBoothFromStreamId(message.getStreamId());
                    if(!mongoDBClient.isUserCheckedIn(user.getEmailAddress(), boothRoom.getBooth())) {
                        BoothAttendee attendee = new BoothAttendee(user.getEmailAddress(), boothRoom.getBooth());
                        mongoDBClient.checkInAttendee(attendee);
                        messageBuilder.append("<mention uid=\"" + mention + "\"/> checked-in<br/>");
                        Chat chat = new Chat();
                        chat.setLocalUser(symClient.getLocalUser());
                        Set<SymUser> recipients = new HashSet<>();
                        try {
                            recipients.add(user);
                            chat.setRemoteUsers(recipients);
                            symClient.getChatService().addChat(chat);
                            SymMessage checkedInMsg = new SymMessage();
                            checkedInMsg.setMessage("<messageML>You were checked in to <hash tag=\""+boothRoom.getBooth()+"\"/> booth by <mention uid=\""+message.getSymUser().getId()+"\"/></messageML>");
                            symClient.getMessagesClient().sendMessage(chat.getStream(), checkedInMsg);
                        } catch (MessagesException e) {
                            e.printStackTrace();
                            System.out.println(e.getMessage());
                        }
                    }
                    else{
                        messageBuilder.append("<mention uid=\"" + mention + "\"/> was already checked-in<br/>");
                    }
                } catch (UsersClientException e) {
                    e.printStackTrace();
                }
            }
        }
        messageBuilder.append("</messageML>");
        confirmationMsg.setMessage(messageBuilder.toString());
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(), confirmationMsg);
        } catch (MessagesException e) {
            e.printStackTrace();
        }
    }
}
