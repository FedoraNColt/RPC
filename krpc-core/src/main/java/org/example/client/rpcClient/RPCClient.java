package org.example.client.rpcClient;

import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;

public interface RPCClient {

    RPCResponse sendRequest(RPCRequest request);
}
