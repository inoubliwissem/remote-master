package graphTools;

import java.io.Serializable;

public class SubGraph implements Serializable {
    private String content;

    public SubGraph(String content) {
        this.content = content;
    }

    public SubGraph() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
