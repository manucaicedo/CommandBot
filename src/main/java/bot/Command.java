package bot;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.symphony.clients.model.SymMessage;

public abstract class Command {
    private String description;
    private String command;
    private SymphonyClient symClient;


    public Command(){}

    public abstract void execute(SymMessage message);

    public Command(String command, String description, SymphonyClient symphonyClient) {
        this.description = description;
        this.command = command;
        this.symClient = symphonyClient;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
