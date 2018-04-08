package commands;

import bot.Command;
import model.CalendarEvent;
import model.mongo.AgendaItem;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import utils.ContentConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NowCommand extends Command {

    private SymphonyClient symClient;
    private MongoDBClient mongoDBClient;
    public NowCommand(String command, String description, SymphonyClient symphonyClient, String mongoURL) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
        mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {

        List<AgendaItem> agenda = mongoDBClient.getAgenda();
        Calendar now = Calendar.getInstance();
        CalendarEvent currentEvent=null;
        for (AgendaItem agendaItem: agenda) {
            CalendarEvent event = new CalendarEvent();
            event.setEvent(agendaItem.getEvent());
            event.setLocation(agendaItem.getLocation());
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, agendaItem.getStartTime());
            start.set(Calendar.MINUTE, agendaItem.getEndMin());
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            event.setStart(start);
            Calendar end = Calendar.getInstance();
            end.set(Calendar.HOUR_OF_DAY, agendaItem.getEndMin());
            end.set(Calendar.MINUTE, agendaItem.getEndMin());
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.MILLISECOND, 0);
            event.setEnd(end);
            if ((event.getStart().equals(now)||event.getStart().before(now)) && (event.getEnd().equals(now) || event.getEnd().after(now))){
                currentEvent = event;
            }
        }
        SymMessage response = new SymMessage();
        response.setMessage("<messageML>Current session is: "+currentEvent.getEvent()+" at "+currentEvent.getLocation()+"</messageML>");
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        }


    }
}
