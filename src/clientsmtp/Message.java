package clientsmtp;

import java.util.ArrayList;
import java.util.List;


public class Message {
    
    private ArrayList<String> targets;
    
    private String content;
    
    private String source;
    
    public Message(String source, ArrayList<String> targets, String content) {
        this.targets = (ArrayList<String>) targets.clone();
        this.content = content;
        this.source = source;
    }
    
    public List<String> getTargets() {
        return (List<String>) targets.clone();
    }
    
    public String getSource() {
        return source;
    }
    
    public String getContent() {
        return content;
    }
    
    
}
