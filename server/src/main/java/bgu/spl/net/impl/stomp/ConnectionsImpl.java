package bgu.spl.net.impl.stomp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl implements Connections<String> {

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, String>> map = new ConcurrentHashMap<>(); // <Channel,<ConnectionID,SubscriptionID>>
    public ConcurrentHashMap<Integer,User> connectionIdToUsers= new ConcurrentHashMap<>(); //<ConnectionID,User>
    public ConcurrentHashMap<String, User> userNameToUser = new ConcurrentHashMap<>(); //<UserName, User>
    public ConcurrentHashMap<Integer, ConnectionHandler<String>>  connectionIdToCH= new ConcurrentHashMap<>(); //<ConnectionId, CH>
    private Integer messageId =0;

    @Override
    public boolean send(int connectionId, String msg) { // for a single user
        // TODO Auto-generated method stub
        ConnectionHandler<String> clientCH = connectionIdToCH.get(connectionId);
        if(msg != null){
            clientCH.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, String msg) { //for all the channel
        // TODO Auto-generated method stub
        for(Map.Entry<Integer, String> entry : map.get(channel).entrySet()){
            Integer connectionId = entry.getKey();
            ConnectionHandler<String> clientCH = connectionIdToCH.get(connectionId);
            clientCH.send(msg);
        }
        
    }

    public void disconnect(int connectionId){
        User user = connectionIdToUsers.get(connectionId); //get the specific user

        for (Map.Entry<String, String> entry : user.UserChannels.entrySet()) { //iterate all of the channels that the user subscribed to.
            String subscriptionId = entry.getKey();
            String channel = entry.getValue();
            map.get(channel).remove(connectionId, subscriptionId); //remove them from the map.
        }
        connectionIdToUsers.remove(connectionId,user);
        connectionIdToCH.remove(connectionId);
        user.UserChannels.clear(); //clear the userChannel hashMap
        user.setLoggedIn(false);
        user.setConnectionId(null);
    }

    public User getUser(Integer connectionID){
        return connectionIdToUsers.get(connectionID);
    }

    public String getChannelSubscriptionId(String channel, Integer connectionId){
        return map.get(channel).get(connectionId);
    }

    public ConcurrentHashMap<Integer,String> getChannelSubscribers(String channel){
        return map.get(channel);
    }

    public void addSubscriberToChannel(String channel, Integer connectionId,String subscriptionId){
        map.get(channel).put(connectionId, subscriptionId);
    }

    public boolean isUserSubscribedToChannel(String channel, Integer connectionId){
         return map.get(channel).containsKey(connectionId);
    }

    public void createNewChannel(String channel, ConcurrentHashMap<Integer,String> subscribers){
        map.put(channel, subscribers);
    }
    public User getUserByName(String userName){
        return userNameToUser.get(userName);
    }

    public void removeSubscriberFromChannel(String channel, Integer connectionId,String subscriptionId){
        map.get(channel).remove(connectionId, subscriptionId);
    }

    public void addUser(Integer connectionId, User user){
        connectionIdToUsers.put(connectionId, user);
    }

    public void addUserName(String userName, User user){
        userNameToUser.put(userName, user);
    }

    public void removeCH(Integer connectionId){
        connectionIdToCH.remove(connectionId);
    }


   
    public void addConnectionHandler(Integer connectionId, ConnectionHandler<String> connectionHandler) {
        connectionIdToCH.put(connectionId, connectionHandler);
        
    }

    public Integer getNewMessageId(){
        messageId++;
        return messageId;
    }

  


}
