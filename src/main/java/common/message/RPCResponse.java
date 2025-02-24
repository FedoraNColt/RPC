package common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RPCResponse implements Serializable {
    // Status info
    private Integer code;
    private String message;
    // Actual data
    private Object data;
    private Class<?> dataType;

    // Construct success info
    public static RPCResponse success(Object data) {
        return RPCResponse.builder().code(200).data(data).dataType(data.getClass()).build();
    }

    // Construct failure info
    public static RPCResponse fail() {
        return RPCResponse.builder().code(500).message("Server Error!").build();
    }
}
