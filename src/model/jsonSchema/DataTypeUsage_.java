
package model.jsonSchema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dataFlowGraphId",
    "entryLevelSystemCall",
    "readDataTypes",
    "writeDataTypes"
})
public class DataTypeUsage_ implements Serializable
{

    @JsonProperty("dataFlowGraphId")
    private int dataFlowGraphId;
    @JsonProperty("entryLevelSystemCall")
    private EntryLevelSystemCall entryLevelSystemCall;
    @JsonProperty("readDataTypes")
    private List<ReadDataType> readDataTypes = new ArrayList<ReadDataType>();
    @JsonProperty("writeDataTypes")
    private List<WriteDataType> writeDataTypes = new ArrayList<WriteDataType>();
    private final static long serialVersionUID = -1600096684064699883L;

    @JsonProperty("dataFlowGraphId")
    public int getDataFlowGraphId() {
        return dataFlowGraphId;
    }

    @JsonProperty("dataFlowGraphId")
    public void setDataFlowGraphId(int dataFlowGraphId) {
        this.dataFlowGraphId = dataFlowGraphId;
    }

    @JsonProperty("entryLevelSystemCall")
    public EntryLevelSystemCall getEntryLevelSystemCall() {
        return entryLevelSystemCall;
    }

    @JsonProperty("entryLevelSystemCall")
    public void setEntryLevelSystemCall(EntryLevelSystemCall entryLevelSystemCall) {
        this.entryLevelSystemCall = entryLevelSystemCall;
    }

    @JsonProperty("readDataTypes")
    public List<ReadDataType> getReadDataTypes() {
        return readDataTypes;
    }

    @JsonProperty("readDataTypes")
    public void setReadDataTypes(List<ReadDataType> readDataTypes) {
        this.readDataTypes = readDataTypes;
    }

    @JsonProperty("writeDataTypes")
    public List<WriteDataType> getWriteDataTypes() {
        return writeDataTypes;
    }

    @JsonProperty("writeDataTypes")
    public void setWriteDataTypes(List<WriteDataType> writeDataTypes) {
        this.writeDataTypes = writeDataTypes;
    }

}
