package commands;

import bot.Command;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import utils.ContentConstants;

public class AgendaCommand extends Command{

    private SymphonyClient symClient;

    public AgendaCommand(String command, String description, SymphonyClient symClient) {
        super(command, description, symClient);
        this.symClient = symClient;

    }

    @Override
    public void execute(SymMessage message) {
        SymMessage response = new SymMessage();
        response.setMessage(ContentConstants.AGENDA);
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        }
    }
}
