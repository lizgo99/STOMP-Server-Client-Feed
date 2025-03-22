package bgu.spl.net.impl.stomp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Frame {

    private boolean isValid = true;
    private frameCommandType commandType = null;
    private String stringCommandType = null;
    public ConcurrentHashMap<String,String> headers;
    private String body = "";
    private String bodyMessage = ""; // maybe we dont need this

    private void stringToCommandType(String string){
        if(string.equals("CONNECTED")){
            commandType = frameCommandType.CONNECTED;
        }
        else if(string.equals("MESSAGE")){
            commandType = frameCommandType.MESSAGE;
        }
        else if(string.equals("RECEIPT")){
            commandType = frameCommandType.RECEIPT;
        }
        else if(string.equals("ERROR")){
            commandType = frameCommandType.ERROR;
        }
        else if(string.equals("CONNECT")){
            commandType = frameCommandType.CONNECT;
        }
        else if(string.equals("SEND")){
            commandType = frameCommandType.SEND;
        }
        else if(string.equals("SUBSCRIBE")){
            commandType = frameCommandType.SUBSCRIBE;
        }
        else if(string.equals("UNSUBSCRIBE")){
            commandType = frameCommandType.UNSUBSCRIBE;
        }
        else if(string.equals("DISCONNECT")){
            commandType = frameCommandType.DISCONNECT;
        }
    }

    Frame(String frameString){

        String frameMessage = (String)frameString; 
        headers = new ConcurrentHashMap<>();
        String[] splittedMessage = frameMessage.split("\n");
        stringCommandType = splittedMessage[0];
        stringToCommandType(splittedMessage[0]);
        boolean isBody = false;
        for(int i = 1; i < splittedMessage.length; i++){
            if(!isBody && splittedMessage[i].equals("")){
                isBody = true;
            }
            if(!splittedMessage[i].equals("\u0000")){ 
                if(isBody){
                        body = body + splittedMessage[i] + "\n";
                        if(!splittedMessage[i].equals(""))
                            bodyMessage = bodyMessage + splittedMessage[i] + "\\n";
                }
                else{
                    if(splittedMessage[i].contains(":")){
                        String[] header = splittedMessage[i].split(":");
                        headers.putIfAbsent(header[0], header[1]);
                    }
                    else{
                        isValid = false;
                    }
                    
                }
            }   
        }
    } 

    public boolean isValid(){
        return isValid;
    }

    public frameCommandType getCommandType(){
        return commandType;
    }
    

    public String getBody(){
        return body;
    }

    public String getBodyMessage(){
        return bodyMessage;
    }

    @Override
    public String toString(){
        String frame= "";
        frame = frame + stringCommandType +"\n";
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String header = entry.getKey();
            String value = entry.getValue();
            frame =frame + header + ":" + value + "\n";
        }
        frame = frame + body;
        return frame;
    } 
}
