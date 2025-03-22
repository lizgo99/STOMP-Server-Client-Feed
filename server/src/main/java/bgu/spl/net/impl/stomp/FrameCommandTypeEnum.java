package bgu.spl.net.impl.stomp;

enum frameCommandType{
    CONNECTED,
    MESSAGE,
    RECEIPT,
    ERROR,
    CONNECT,
    SEND,
    SUBSCRIBE,
    UNSUBSCRIBE,
    DISCONNECT
}
