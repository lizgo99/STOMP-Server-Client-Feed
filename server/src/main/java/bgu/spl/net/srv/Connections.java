package bgu.spl.net.srv;
import bgu.spl.net.impl.stomp.User;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    public User getUser(Integer connectionID);

    public String getChannelSubscriptionId(String channel, Integer connectionId);

    public ConcurrentHashMap<Integer,String> getChannelSubscribers(String channel);

    public void addSubscriberToChannel(String channel, Integer connectionId,String subscriptionId);

    public boolean isUserSubscribedToChannel(String channel, Integer connectionId);

    public Integer getNewMessageId();
    
    public void createNewChannel(String channel, ConcurrentHashMap<Integer,String> subscribers);

    public User getUserByName(String userName);

    public void removeSubscriberFromChannel(String channel, Integer connectionId,String subscriptionId);

    public void addUser(Integer connectionId, User user);

    public void addUserName(String userName, User user);

    public void removeCH(Integer connectionId);

    public void addConnectionHandler(Integer connectionId,ConnectionHandler<T> connectionHandler);

}
