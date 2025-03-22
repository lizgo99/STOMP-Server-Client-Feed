package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String userName;
    private String password;
    private Integer connectionId;
    public ConcurrentHashMap<String,String> UserChannels = new ConcurrentHashMap<>(); // <SubscriptionId, channel>
    private boolean isLoggedIn;

    // when we loggedout we need to clear the UserChannels.

    //notice that if the user is logout and login maybe we need to clear the HashMap

    public User(String userName, String password, Integer connectionId){
        this.userName = userName;
        this.password = password;
        this.connectionId = connectionId;
        this.isLoggedIn = true; 
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public Integer getConnectionId(){
        return connectionId;
    }

    public String getChannel(String subscriptionId){
        return UserChannels.get(subscriptionId);
    }

    public boolean isLoggedIn(){
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn){
        isLoggedIn = loggedIn;
    }

    public void setConnectionId(Integer connectionId){
        this.connectionId = connectionId;
    }

}
