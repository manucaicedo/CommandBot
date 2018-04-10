package mongo;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
    //private MongoCollection<AdminRoom> adminRoomCollection;


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
        //adminRoomCollection = database.getCollection("AdminRoom", AdminRoom.class);
    }

//    public BoothRoom getBoothRoom(String booth){
//        return boothRoomCollection.find(eq("booth",booth)).first();
//    }



//    public List<BoothAttendee> getBoothAttendees(){
//        List boothAttendees = new ArrayList();
//        boothAttendeeCollection.find().forEach(new Block<BoothAttendee>() {
//
//            @Override
//            public void apply(final BoothAttendee boothAttendee) {
//                boothAttendees.add(boothAttendee);
//            }
//        });
//        return boothAttendees;
//    }


}
