package common.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RPCResponse implements Serializable {
    // Status info
    private Integer code;
    private String message;
    // Actual data
    private Object data;

    // Construct success info
    public static RPCResponse success(Object data) {
        return RPCResponse.builder().code(200).data(data).build();
    }

    // Construct failure info
    public static RPCResponse fail() {
        return RPCResponse.builder().code(500).message("Server Error!").build();
    }
}
