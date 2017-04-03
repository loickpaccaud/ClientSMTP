package clientsmtp;

import java.util.ArrayList;


public class Message {
    
    private String[] targets;
    
    private String content;
    
    private String source;
    
    public Message(String source, ArrayList<String> targets, String content) {
        this.targets = (String[]) targets.toArray();
        this.content = content;
        this.source = source;
    }
    
    public String[] getTargets() {
        return targets.clone();
    }
    
    public String getSource() {
        return source;
    }
    
    public String getContent() {
        return content;
    }
    
    
}
