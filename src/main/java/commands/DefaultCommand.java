package commands;

import bot.Command;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;
import utils.ContentConstants;

public class DefaultCommand extends Command {

    private SymphonyClient symClient;
    public DefaultCommand(String command, String description, SymphonyClient symphonyClient) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
    }

    @Override
    public void execute(SymMessage message) {
        SymMessage response = new SymMessage();

        if(message.getStream().getStreamType().equals(SymStreamTypes.Type.IM)) {
            response.setMessage(ContentConstants.ERROR_MESSAGE);
            try {
                symClient.getMessagesClient().sendMessage(message.getStream(), response);
            } catch (MessagesException e) {
                e.printStackTrace();
            }
        }

    }
}
