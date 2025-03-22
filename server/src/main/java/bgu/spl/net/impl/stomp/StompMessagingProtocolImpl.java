package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.ConcurrentHashMap;
// do we need to implement StompMessagingProtocol?
public class StompMessagingProtocolImpl implements StompMessagingProtocol<String>{

    private boolean shouldTerminate = false;
    private Connections<String> connections;
    private Integer connectionId;
    

    /**
     * Used to initiate the current client protocol with it's personal connection ID
     * and the connections implementation
     **/
    @Override
    public void start(int connectionId, Connections<String> connections) {
        // we need to check if we need any changes
        this.connectionId = connectionId;
        this.connections = connections;
        
    }

    public void process(String message) {
        Frame frame = new Frame(message);
        frameCommandType commandType = frame.getCommandType();
        if (commandType == frameCommandType.CONNECT) {
            String validFrame = checkConnectFrame(frame);

            if (validFrame == null) { //valid frame
                String connectedOrError = handleConnect(frame);

                if(connectedOrError.equals("Wrong Passcode")){ // the client gave wrong passcode
                    String Error = errorOfUnvalidAct(frame, "Wrong Passcode");
                    Error = Error + "The client gave wrong passcode\n";
                    connections.send(connectionId, Error); 

                    if((connections.getUser(connectionId) != null)){ // the user is logged in tryed to connect again but with wrong passcode
                        connections.disconnect(connectionId);
                    }
                    else{ // the user didnt log in and try to connect with wrong passcode
                        connections.removeCH(connectionId);
                    }
                    shouldTerminate = true;
                }

                else if(connectedOrError.equals("Active User")){ //the client try to connect but he is already connected
                    String Error = errorOfUnvalidAct(frame, "The user is already logged in");
                    Error = Error + "The client try to connect but he is already connected\n";
                    connections.send(connectionId, Error); 
                    if(connections.getUser(connectionId) == null){ //the user is connect with other client(another socket) already
                        connections.removeCH(connectionId);
                    }
                    else{ // the user is connect with this client(this socket) already
                        connections.disconnect(connectionId); 
                    }
                    shouldTerminate = true;
                }

                else{ // valid connection
                    connections.send(connectionId, connectedOrError);
                    if(frame.headers.containsKey("receipt")){ //if the client ask for receipt for his act
                        String receiptID = frame.headers.get("receipt");
                        String receipt =  "RECEIPT\nreceipt-id:" + receiptID + "\n";
                        connections.send(connectionId, receipt);
                    }
                }
            } 

            else { //Error - invalid frame
                connections.send(connectionId, validFrame);
                if((connections.getUser(connectionId) != null)){ // the user is logged in and tried to connect again, but wrote invlid frame of connect
                    connections.disconnect(connectionId);
                }
                else{ // the user didnt log in and try to connect with invalid frame
                    connections.removeCH(connectionId);
                }
                shouldTerminate = true;
            }

        }
        else if(connections.getUser(connectionId) != null){ //the user is logged in
 
            if (commandType == frameCommandType.SEND) {
                String validFrame = checkSendFrame(frame);

                if (validFrame == null) { //valid frame
                    String channel = frame.headers.get("destination");
                    boolean isSubscribed = connections.getChannelSubscribers(channel) != null && connections.getChannelSubscriptionId(channel, connectionId) != null;  // true if subscribed and false if not subscribed
                    if(isSubscribed){ //the user is subscribed to the channel that he want to send a message
                        String sendMessage = sendMessage(frame);
                        connections.send(channel, sendMessage); //send to everyone in the channel

                        if(frame.headers.containsKey("receipt")){ //If the client ask for receipt for his act
                            String receiptID = frame.headers.get("receipt");
                            String receipt =  "RECEIPT\nreceipt-id:" + receiptID + "\n";
                            connections.send(connectionId, receipt);
                        }
                    }

                    else{ //Error- the user isn't subscribed to the channel that he want to send a message to 
                        String Error = errorOfUnvalidAct(frame, "You are not subscribed to the channel");
                        Error = Error + "The user isn't subscribed to the channel that he want to send a message to\n";
                        connections.send(connectionId, Error); 
                        connections.disconnect(connectionId);
                        shouldTerminate = true;
                    }
                }

                else { //Error- if the frame is not valid
                    connections.send(connectionId, validFrame);
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                }
            }

            else if (commandType == frameCommandType.SUBSCRIBE) {
                String validFrame = checkSubscribeFrame(frame);
                if (validFrame == null) { //valid frame
                    String receiptOrError = handleSubscribe(frame); 
                    if(receiptOrError == null){ // Error - invalid act, gave wrong game name
                        String Error = errorOfUnvalidAct(frame , "wrong game name");
                        Error = Error + "The user gave an unvalid channel name\n";
                        connections.send(connectionId, Error); 
                        connections.disconnect(connectionId);
                        shouldTerminate = true;
                    }
                    else{ //valid act
                        connections.send(connectionId, receiptOrError); 
                    }
                } else {//if the frame not Valid -Error
                    connections.send(connectionId, validFrame);
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                }

            }

            else if (commandType == frameCommandType.UNSUBSCRIBE) {
                String validFrame = checkUnsubscribeFrame(frame);
                if (validFrame == null) { //valid frame
                    String receiptOrError = handleUnSubscribe(frame);
                    if(receiptOrError == null){//Error- try do do unsubscribe to a channel that don't subscribe before
                        String Error = errorOfUnvalidAct(frame , "You are not subscribed to the channel");
                        Error = Error + "Try to unsubscribe to a channel that the user wasn't subscribed to\n";
                        connections.send(connectionId, Error); 
                        connections.disconnect(connectionId);
                        shouldTerminate = true;
                    }
                    else{
                        connections.send(connectionId, receiptOrError); 
                    }
                } 
                else {//if the frame not Valid -Error
                    connections.send(connectionId, validFrame);
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                }
            }
            
            else if (commandType == frameCommandType.DISCONNECT) {
                String validFrame = checkDisconnectFrame(frame);
                if (validFrame == null) {
                    String receipt = handleDisconnect(frame);
                    connections.send(connectionId, receipt); 
                    connections.disconnect(connectionId);
                } 
                else { //if the frame not Valid -Error
                    connections.send(connectionId, validFrame);
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                }
            }
            
            else{
                String receiptId = "";
                if(frame.headers.containsKey("receipt")){
                    receiptId = "receipt-id:" + frame.headers.get("receipt") + "\n";
                }
                String Error = "ERROR\n" + receiptId + "message: malformed frame received\n\nThe message:\n-----\n" +
                frame.toString() + "-----\n" + "Did not contain a valid Command Type\n";
                connections.send(connectionId, Error);
                connections.disconnect(connectionId);
                shouldTerminate = true;
            }
        }
        else{ //Error- the user is not logged in and try to do some acts
            String Error = errorOfUnvalidAct(frame , "You are not logged in");
            Error = Error + "The user is not logged in and try to do some acts\n";
            if(connections.getUser(connectionId) != null){
                connections.send(connectionId, Error);
                connections.removeCH(connectionId);
                shouldTerminate = true;
            }

        }
    }

    private String checkConnectFrame(Frame frame){
        if(frame.headers.containsKey("accept-version") && frame.headers.containsKey("host") && frame.headers.containsKey("login") && frame.headers.containsKey("passcode") && frame.isValid()){
            return null;
        }
        else{
            String Error = errorOfMissingOneHeader(frame , "Missing header");
            if(!frame.headers.containsKey("accept-version")){
                Error = Error + "Did not contain a Accept-version header\n";
            }
            if(!frame.headers.containsKey("host")){
                Error = Error + "Did not contain a Host header\n";
            }
            if(!frame.headers.containsKey("login")){
                Error = Error + "Did not contain a Login header\n";
            }
            if(!frame.headers.containsKey("passcode")){
                Error = Error + "Did not contain a Passcode header\n";
            }
            if(!frame.isValid()){
                Error = Error + "The frame format is not valid\n";
            }
            return Error;
        }
    }

    private String checkSendFrame(Frame frame){
        if(frame.headers.containsKey("destination") && frame.isValid()){
            return null;
        }
        else{
            String Error = errorOfMissingOneHeader(frame , "Missing header");
            if(!frame.headers.containsKey("destination")){
                Error = Error + "Did not contain a Destination header\n";
            }
            if(!frame.isValid()){
                Error = Error + "The frame format is not valid\n";
            }
            return Error;
        }
    }

    private String checkSubscribeFrame(Frame frame){
        if(frame.headers.containsKey("receipt") && frame.headers.containsKey("id") && frame.headers.containsKey("destination") && frame.isValid()){
            return null;
        }
        else{
            String Error = errorOfMissingOneHeader(frame , "Missing header");
            if(!frame.headers.containsKey("receipt")){
                Error = Error + "Did not contain a Receipt header\n";
            }
            if(!frame.headers.containsKey("id")){
                Error = Error + "Did not contain a Id header\n";
            }
            if(!frame.headers.containsKey("destination")){
                Error = Error + "Did not contain a Destination header\n";
            }
            if(!frame.isValid()){
                Error = Error + "The frame format is not valid\n";
            }
            return Error;
        }
    }

    private String checkUnsubscribeFrame(Frame frame){
        if(frame.headers.containsKey("receipt") && frame.headers.containsKey("id") && frame.isValid()){
            return null;
        }
        else{
            String Error = errorOfMissingOneHeader(frame , "Missing header");
            if(!frame.headers.containsKey("receipt")){
                Error = Error + "Did not contain a Receipt header\n";
            }
            if(!frame.headers.containsKey("id")){
                Error = Error + "Did not contain a Id header\n";
            }
            if(!frame.isValid()){
                Error = Error + "The frame format is not valid\n";
            }
            return Error;
        }
    }

    private String checkDisconnectFrame(Frame frame){
        if(frame.headers.containsKey("receipt") && frame.isValid()){
            return null;
        }
        else{
            String Error = errorOfMissingOneHeader(frame , "Missing header");
            if(!frame.headers.containsKey("receipt")){
                Error = Error + "Did not contain a Receipt header\n";
            }
            if(!frame.isValid()){
                Error = Error + "The frame format is not valid\n";
            }
            return Error;
        }
    }

    private String errorOfMissingOneHeader(Frame frame, String errorMessage){
        String receiptId = "";
        if(frame.headers.containsKey("receipt")){
            receiptId = "receipt-id:" + frame.headers.get("receipt") + "\n";
        }
        String Error = "ERROR\n" + receiptId + "message:" +  errorMessage + "\n\nThe message:\n-----\n" +
        frame.toString() + "-----\n";
        return Error;
    }

    private String errorOfUnvalidAct(Frame frame , String errorMessage){
        String receiptId = "";
        if(frame.headers.containsKey("receipt")){
            receiptId = "receipt-id:" + frame.headers.get("receipt") + "\n";
        }
        String Error = "ERROR\n" + receiptId + "message:" +  errorMessage + "\n\nThe message:\n-----\n" +
        frame.toString() + "-----\n";
        return Error;
    }
    /**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private String handleSubscribe(Frame frame) {
        String channel = frame.headers.get("destination");
        String subscriprionID = frame.headers.get("id");
        String receiptID = frame.headers.get("receipt");
        String[] teamNames = channel.split("_");
        if(teamNames.length != 2){ //Error- invalid channel name
            return null;
        }
        else{ //valid name channel
            String reverseChannel = teamNames[1] + "_" + teamNames[0];
            User user = connections.getUser(connectionId);

            if (connections.getChannelSubscribers(channel) != null) { // the channel exists
                if (!connections.isUserSubscribedToChannel(channel, connectionId)) { // check if the user is not allready subscibe the channel.
                    connections.addSubscriberToChannel(channel, connectionId, subscriprionID);
                    user.UserChannels.put(subscriprionID, channel);
                }
            } 
            else if (connections.getChannelSubscribers(reverseChannel) != null) { // the channel exists with the reverse name
                if (!connections.isUserSubscribedToChannel(reverseChannel, connectionId)) {
                    connections.addSubscriberToChannel(reverseChannel, connectionId, subscriprionID);
                    user.UserChannels.put(subscriprionID, reverseChannel);
                }
            } 
            else { //open a new channel
                ConcurrentHashMap<Integer, String> newChannelSubscribers = new ConcurrentHashMap<Integer, String>();
                newChannelSubscribers.put(connectionId, subscriprionID);
                connections.createNewChannel(channel, newChannelSubscribers);
                user.UserChannels.put(subscriprionID, channel);
             }
        }
        String receipt = "RECEIPT\nreceipt-id:" + receiptID + "\n";
        return receipt;
    }

    private String handleUnSubscribe(Frame frame) {
        User user = connections.getUser(connectionId);
        String subscriprionID = frame.headers.get("id");
        String channel = user.getChannel(subscriprionID);

        if(channel == null){ //worng subscription id- try do do unsubscribe to a channel that don't subscribe before
            return null;
        }
        connections.removeSubscriberFromChannel(channel,connectionId, subscriprionID); // remove from the map.
        user.UserChannels.remove(subscriprionID, channel); // remove from the users channels.
        String receiptID = frame.headers.get("receipt");
        return "RECEIPT\nreceipt-id:" + receiptID + "\n";
    }
    
    private String handleConnect(Frame frame){
        String userName = frame.headers.get("login");
        String userPassword = frame.headers.get("passcode");
        boolean userNameExists = connections.getUserByName(userName) != null;
        if(userNameExists){ //the user exists
            User user = connections.getUserByName(userName);
            if(!user.getPassword().equals(userPassword)){ //Error- wrong passcode
                return "Wrong Passcode";
            }
            else{ //correct passcode 
                if(!user.isLoggedIn()){ //the user not active right now
                    user.setLoggedIn(true);
                    user.setConnectionId(connectionId);
                    connections.addUser(connectionId, user);
                    return "CONNECTED\nversion:1.2\n";
                }
                else{ //Error- the user is active right now 
                    return "Active User";
                }
            }
        }
        else{ //the user doesn't exist
            User user = new User(userName,userPassword,connectionId);
            connections.addUser(connectionId, user);
            connections.addUserName(userName, user);
            return "CONNECTED\nversion:1.2\n";
        }
    }

    private String handleDisconnect(Frame frame){
        // connections.disconnect(connectionId);
        // shouldTerminate = true;
        String receiptID = frame.headers.get("receipt");
        return "RECEIPT\nreceipt-id:" + receiptID + "\n";
    }
   
    private String sendMessage(Frame frame){
        String channel = frame.headers.get("destination");
        String subscriptionId = connections.getChannelSubscriptionId(channel,connectionId);
        String body = frame.getBody();
        String output = "";
        if(body.length() == 0){ //empty body frame
            output = "MESSAGE\nsubscription:"+ subscriptionId + "\nmessage-id:" + connections.getNewMessageId()
            + "\ndestination:" + channel;
        }
        else{ // with body frame
            output = "MESSAGE\nsubscription:"+ subscriptionId + "\nmessage-id:" + connections.getNewMessageId()
            + "\ndestination:" + channel+"\n" + body;
        }
        return output;
    }

}
