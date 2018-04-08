package commands;

import bot.Command;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.ConnectionsException;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.clients.model.SymUserConnection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlastCommand extends Command{

    private MongoDBClient mongoDBClient;
    private String description;
    private String command;
    private SymphonyClient symClient;

    public BlastCommand(String command, String description, SymphonyClient symphonyClient, String mongoURL) {
        super(command, description,symphonyClient);
        this.symClient = symphonyClient;

        mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {
        if(message.getStream().getStreamId().equals(mongoDBClient.getAdminRoom().getStreamId())){
            try {
                List<SymUserConnection> connections =  symClient.getConnectionsClient().getAcceptedRequests();

                for (SymUserConnection connection: connections) {
                    Chat chat = new Chat();
                    chat.setLocalUser(symClient.getLocalUser());
                    Set<SymUser> recipients = new HashSet<>();
                    try {
                        SymUser recipient = symClient.getUsersClient().getUserFromId(connection.getUserId());
                        recipients.add(recipient);
                        chat.setRemoteUsers(recipients);
                        symClient.getChatService().addChat(chat);
                        SymMessage blastMessage = new SymMessage();
                        blastMessage.setMessageText(message.getMessageText().replace("#blast",""));
                        symClient.getMessagesClient().sendMessage(chat.getStream(), blastMessage);
                    } catch (UsersClientException e) {
                        e.printStackTrace();
                    }catch (MessagesException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ConnectionsException e) {
                e.printStackTrace();
            }
        }
    }
}
