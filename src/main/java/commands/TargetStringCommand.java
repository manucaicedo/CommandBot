package commands;

import bot.Command;
import io.swagger.models.auth.In;
import model.MessageEntities;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import utils.MessageParser;


public class TargetStringCommand extends Command {

    private SymphonyClient symClient;

    public TargetStringCommand(String command, String description, SymphonyClient symphonyClient) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
    }

    @Override
    public void execute(SymMessage message) {

        MessageParser parser = new MessageParser();
        MessageEntities entities = parser.getMessageEntities(message.getEntityData());
        SymMessage response = new SymMessage();

        String[] messageArray = message.getMessageText().split(" ");
        String target = messageArray[1];
        if(messageArray.length>2){
            target = target +" "+messageArray[2];
        }
        response.setMessage(parseMessage(target));

        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }


    }

    private String parseMessage(String target){
        StringBuilder builder = new StringBuilder();
        builder.append("<messageML>");
        builder.append("<br/>"+target+"<br/></messageML>");

        return builder.toString();
    }
}
