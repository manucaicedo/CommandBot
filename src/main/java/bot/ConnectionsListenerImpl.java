package bot;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.ConnectionsException;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ConnectionsListener;
import org.symphonyoss.client.services.ConnectionsService;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.clients.model.SymUserConnection;
import utils.ContentConstants;

import java.util.HashSet;
import java.util.Set;

public class ConnectionsListenerImpl implements ConnectionsListener {

    private SymphonyClient symClient;

    public ConnectionsListenerImpl(SymphonyClient symphonyClient) {
        this.symClient = symphonyClient;
    }

    @Override
    public void onConnectionNotification(SymUserConnection symUserConnection) {
            try {
                symClient.getConnectionsClient().acceptConnectionRequest(symUserConnection);

                Chat chat = new Chat();
                chat.setLocalUser(symClient.getLocalUser());
                Set<SymUser> recipients = new HashSet<>();
                try {
                    SymUser recipient = symClient.getUsersClient().getUserFromId(symUserConnection.getUserId());
                    recipients.add(recipient);
                    chat.setRemoteUsers(recipients);
                    symClient.getChatService().addChat(chat);
                    SymMessage message = new SymMessage();
                    message.setMessage(ContentConstants.WELCOME_MESSAGE);
                    symClient.getMessagesClient().sendMessage(chat.getStream(), message);
                } catch (UsersClientException e) {
                    e.printStackTrace();
                }catch (MessagesException e) {
                    e.printStackTrace();                }
            } catch (ConnectionsException e) {
                e.printStackTrace();
            }

    }
}
