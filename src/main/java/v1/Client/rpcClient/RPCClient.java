package v1.Client.rpcClient;

import v1.common.message.RPCRequest;
import v1.common.message.RPCResponse;

public interface RPCClient {

    RPCResponse sendRequest(RPCRequest request);
}
