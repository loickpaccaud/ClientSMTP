package clientsmtp;


public class Message {
    
    public String target;
    
    public String content;
    
    
    public Message(String target, String content) {
        this.target = target;
        this.content = content;
    }
    
    public String getTarget() {
        return target;
    }
    
    public String getContent() {
        return content;
    }
    
    
}
