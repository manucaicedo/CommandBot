# ChatBotSample
Simple java Symphony bot using the Symphony OSF Java Client

Set the src/main/resources/sample-config.yml to look like this

    sessionAuthURL: https://your-pod.symphony.com/sessionauth
    keyAuthUrl: https://your-km.symphony.com:443/keyauth
    localKeystorePath: complete path to your jks keystore
    localKeystorePassword: keystore password
    botCertPath: complete path to your bot's p12 file
    botCertPassword: password
    botEmailAddress: bot.user@example.com
    agentAPIEndpoint: https://your-agent.symphony.com/agent
    podAPIEndpoint: https://your-pod.symphony.com/pod
    userEmailAddress: your-email@company.com
    

Run BotMainApp.java

Existing functionality is listening to the string "test" in messages that are sent to conversations that the Bot is included in and respond with "Message received".

To expand functionality edit ChatBot.java for actions related to IM and MIMs and RoomChatBot.java for actions triggered by events in a room. 

If you are developing a bot that lives within an enterprise pod with on-premise components (KM and Agent), un-comment the relevant code in SymphonyAuth.java to use custom HTTP clients that allow for proxy settings.
