
package model.jsonSchema;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "element"
})
public class Node implements Serializable
{

    @JsonProperty("id")
    private int id;
    @JsonProperty("element")
    private Element element;
    private final static long serialVersionUID = 788337461154696224L;

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("element")
    public Element getElement() {
        return element;
    }

    @JsonProperty("element")
    public void setElement(Element element) {
        this.element = element;
    }

}
