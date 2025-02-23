package Client.rpcClient;

import common.message.RPCRequest;
import common.message.RPCResponse;

public interface RPCClient {

    RPCResponse sendRequest(RPCRequest request);
}
