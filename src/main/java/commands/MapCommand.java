package commands;

import bot.Command;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MapCommand extends Command {

    private SymphonyClient symClient;
    private MongoDBClient mongoDBClient;

    public MapCommand(String command, String description, SymphonyClient symphonyClient, String mongoURL) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
        mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {

        File pathToFile = new File("/Users/manuela.caicedo/Documents/Bots/InnovateBot/src/main/resources/map.png");
        SymMessage response = new SymMessage();
        response.setMessage("<messageML>Map</messageML>");
        response.setAttachment(pathToFile);
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }


    }
}
