
package model.jsonSchema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entity",
    "context"
})
public class Element implements Serializable
{

    @JsonProperty("entity")
    private Entity entity;
    @JsonProperty("context")
    private List<Context> context = new ArrayList<Context>();
    private final static long serialVersionUID = -5923934404561362131L;

    @JsonProperty("entity")
    public Entity getEntity() {
        return entity;
    }

    @JsonProperty("entity")
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @JsonProperty("context")
    public List<Context> getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(List<Context> context) {
        this.context = context;
    }

}
