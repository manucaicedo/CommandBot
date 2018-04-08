package commands;

import bot.Command;
import model.CalendarEvent;
import model.mongo.AgendaItem;
import mongo.MongoDBClient;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.util.Calendar;
import java.util.List;

public class UpNextCommand extends Command {

    private SymphonyClient symClient;
    private MongoDBClient mongoDBClient;
    public UpNextCommand(String command, String description, SymphonyClient symphonyClient, String mongoURL) {
        super(command, description, symphonyClient);
        this.symClient = symphonyClient;
        mongoDBClient = MongoDBClient.getInstance(mongoURL);
    }

    @Override
    public void execute(SymMessage message) {

        List<AgendaItem> agenda = mongoDBClient.getAgenda();
        Calendar now = Calendar.getInstance();
        String nextEvent = null;
        String nextEventLocation = null;
        for (int i = 0; i < agenda.size() ; i++) {
            CalendarEvent event = new CalendarEvent();
            event.setEvent(agenda.get(i).getEvent());
            event.setLocation(agenda.get(i).getLocation());
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, agenda.get(i).getStartTime());
            start.set(Calendar.MINUTE, agenda.get(i).getEndMin());
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            event.setStart(start);
            Calendar end = Calendar.getInstance();
            end.set(Calendar.HOUR_OF_DAY, agenda.get(i).getEndMin());
            end.set(Calendar.MINUTE, agenda.get(i).getEndMin());
            end.set(Calendar.SECOND, 0);
            end.set(Calendar.MILLISECOND, 0);
            event.setEnd(end);
            if ((event.getStart().equals(now)||event.getStart().before(now)) && (event.getEnd().equals(now) || event.getEnd().after(now))){
                if(agenda.get(i+1)!=null) {
                    nextEvent = agenda.get(i + 1).getEvent();
                    nextEventLocation = agenda.get(i + 1).getLocation();
                }
            }

            }

        SymMessage response = new SymMessage();
        if(nextEvent!=null) {
            response.setMessage("<messageML>Next session is: " + nextEvent+ " at " + nextEventLocation + "</messageML>");
        }
        else {
            response.setMessage("<messageML>No more events for today. Hope you enjoyed Innovate Asia 2018!</messageML>");
        }
        try {
            symClient.getMessagesClient().sendMessage(message.getStream(),response);
        } catch (MessagesException e) {
            e.printStackTrace();
        }


    }
}

