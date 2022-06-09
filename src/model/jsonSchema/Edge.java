
package model.jsonSchema;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "to"
})
public class Edge implements Serializable
{

    @JsonProperty("from")
    private int from;
    @JsonProperty("to")
    private int to;
    private final static long serialVersionUID = 184609462184234234L;

    @JsonProperty("from")
    public int getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(int from) {
        this.from = from;
    }

    @JsonProperty("to")
    public int getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(int to) {
        this.to = to;
    }

}
