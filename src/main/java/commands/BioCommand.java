package commands;

import bot.Command;
import io.swagger.models.auth.In;
import model.MessageEntities;
import model.mongo.Speaker;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import utils.MessageParser;

import java.io.File;

public class BioCommand extends Command {

    private SymphonyClient symClient;
    private MongoDBClient mongoDBClient;

    public BioCommand(String command, String description, SymphonyClient symphonyClient, String mongoURL) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
        mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {

        MessageParser parser = new MessageParser();
        MessageEntities entities = parser.getMessageEntities(message.getEntityData());
        SymMessage response = new SymMessage();
        Speaker speaker = null;
        if(!entities.getUsers().isEmpty()){
            speaker = mongoDBClient.getSpeakerFromSymphonyId(entities.getUsers().get(0));
        }
        else{
            String[] messageArray = message.getMessageText().split(" ");
            String name = messageArray[1];
            if(messageArray.length>2){
                name = name +" "+messageArray[2];
            }
            speaker = mongoDBClient.getSpeakerFromName(name);
        }
        if(speaker!=null){
            response.setMessage(parseBioMessage(speaker));
            if(speaker.getPhotoLocation()!=null) {
                File pathToFile = new File(speaker.getPhotoLocation());
                response.setAttachment(pathToFile);
            }
        }
        else {
            response.setMessage("<messageML>Could not find that speaker please try again.</messageML>");
        }
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }


    }

    private String parseBioMessage(Speaker speaker){
        StringBuilder builder = new StringBuilder();
        builder.append("<messageML>");
        if(speaker.getSymphonyId()!=null){
            builder.append("<mention uid=\""+speaker.getSymphonyId()+"\"/>");
        } else{
            builder.append(speaker.getName());
        }
        builder.append("<br/>"+speaker.getBio()+"<br/></messageML>");

        return builder.toString();
    }
}
