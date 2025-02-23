package v1.common.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RPCRequest implements Serializable {
    // Name of the service class; the client only knows the interface
    private String interfaceName;
    // Name of the method to invoke
    private String methodName;
    // List of parameters
    private Object[] params;
    // Types of parameters
    private Class<?>[] paramTypes;
}
