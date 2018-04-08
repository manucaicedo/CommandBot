package mongo;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.mongo.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;
import static com.mongodb.client.model.Updates.combine;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBClient {

    private static MongoDBClient instance;
    private MongoCollection<AdminRoom> adminRoomCollection;
    private MongoCollection<BoothAttendee> boothAttendeeCollection;
    private MongoCollection<BoothRoom> boothRoomCollection;
    private MongoCollection<AgendaItem> agendaItemCollection;
    private MongoCollection<Speaker> speakerCollection;
    private MongoCollection<SpeakerSpelling> speakerSpellingCollection;


    public static MongoDBClient getInstance(String mongoURL){
        if(instance == null){
            instance = new MongoDBClient(mongoURL);
        }
        return instance;
    }

    protected MongoDBClient(String mongoURL) {
        MongoClientURI connectionString = new MongoClientURI(mongoURL);
        MongoClient mongoClient = new MongoClient(connectionString);
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoDatabase database = mongoClient.getDatabase("InnovateBot");

        database = database.withCodecRegistry(pojoCodecRegistry);
        adminRoomCollection = database.getCollection("AdminRoom", AdminRoom.class);
        boothAttendeeCollection = database.getCollection("BoothAttendee", BoothAttendee.class);
        boothRoomCollection = database.getCollection("BoothRoom", BoothRoom.class);
        agendaItemCollection = database.getCollection("Agenda", AgendaItem.class);
        speakerCollection = database.getCollection("Speaker", Speaker.class);
        speakerSpellingCollection = database.getCollection("SpeakerSpellings", SpeakerSpelling.class);
    }

    public BoothRoom getBoothRoom(String booth){
        return boothRoomCollection.find(eq("booth",booth)).first();
    }

    public AdminRoom getAdminRoom(){
        return adminRoomCollection.find().first();
    }


    public List<BoothAttendee> getBoothAttendees(){
        List boothAttendees = new ArrayList();
        boothAttendeeCollection.find().forEach(new Block<BoothAttendee>() {

            @Override
            public void apply(final BoothAttendee boothAttendee) {
                boothAttendees.add(boothAttendee);
            }
        });
        return boothAttendees;
    }

    public void checkInAttendee(BoothAttendee attendee){
        boothAttendeeCollection.insertOne(attendee);
    }

    public BoothRoom getBoothFromStreamId(String streamId){
        return boothRoomCollection.find(eq("streamId", streamId)).first();
    }

    public boolean isUserCheckedIn(String email, String booth){
        return boothAttendeeCollection.count(combine(eq("attendeeEmail", email),eq("booth", booth))) > 0;
    }

    public List<AgendaItem> getAgenda(){
        List agenda = new ArrayList();
        agendaItemCollection.find().sort(orderBy(ascending("startTime","startMin"))).forEach(new Block<AgendaItem>() {

            @Override
            public void apply(final AgendaItem agendaItem) {
                agenda.add(agendaItem);
            }
        });
        return agenda;
    }

    public Speaker getSpeakerFromSymphonyId(String id){
        return speakerCollection.find(eq("symphonyId", id)).first();
    }

    public Speaker getSpeakerFromName(String name){
        SpeakerSpelling spelling = speakerSpellingCollection.find(eq("spelling",name.toLowerCase())).first();
        if(spelling!=null){
            return speakerCollection.find(eq("_id", new ObjectId(spelling.getSpeakerId()))).first();
        }
        else{
            return null;
        }

    }

}
