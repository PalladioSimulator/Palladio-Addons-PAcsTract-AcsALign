
package model.jsonSchema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dataFlowGraphs",
    "dataTypeUsage"
})
public class DataTypeUsage implements Serializable
{

    @JsonProperty("dataFlowGraphs")
    private List<DataFlowGraph> dataFlowGraphs = new ArrayList<DataFlowGraph>();
    @JsonProperty("dataTypeUsage")
    private List<DataTypeUsage_> dataTypeUsage = new ArrayList<DataTypeUsage_>();
    private final static long serialVersionUID = -2377965180744256549L;

    @JsonProperty("dataFlowGraphs")
    public List<DataFlowGraph> getDataFlowGraphs() {
        return dataFlowGraphs;
    }

    @JsonProperty("dataFlowGraphs")
    public void setDataFlowGraphs(List<DataFlowGraph> dataFlowGraphs) {
        this.dataFlowGraphs = dataFlowGraphs;
    }

    @JsonProperty("dataTypeUsage")
    public List<DataTypeUsage_> getDataTypeUsage() {
        return dataTypeUsage;
    }

    @JsonProperty("dataTypeUsage")
    public void setDataTypeUsage(List<DataTypeUsage_> dataTypeUsage) {
        this.dataTypeUsage = dataTypeUsage;
    }

}
