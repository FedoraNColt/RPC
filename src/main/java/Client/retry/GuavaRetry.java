package Client.retry;

import Client.rpcClient.RPCClient;
import com.github.rholder.retry.*;
import common.message.RPCRequest;
import common.message.RPCResponse;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GuavaRetry {
    /**
     * Sends an RPC request with automatic retry logic in case of failures.
     *
     * @param rpcRequest   The RPC request to be sent.
     * @param rpcClient The RPC client instance to handle the request.
     * @return The RPC response, or a failure response if all retry attempts fail.
     */
    public RPCResponse sendServiceWithRetry(RPCRequest rpcRequest, RPCClient rpcClient) {

        // Creates a retry mechanism for handling RPC failures
        Retryer<RPCResponse> retryer = RetryerBuilder.<RPCResponse>newBuilder()
                // Retries when an exception occurs
                .retryIfException()
                // Retries if the response has an abnormal status code (e.g., 500)
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // Waits 2 seconds between each retry attempt
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // Stops retrying after 3 attempts
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // Configures a retry listener to track retry attempts
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("RetryListener: Attempt #" + attempt.getAttemptNumber());
                    }
                })
                .build();

        try {
            // Executes the RPC request with retry logic
            return retryer.call(() -> rpcClient.sendRequest(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            // Returns a failure response if all retry attempts fail
            return RPCResponse.fail();
        }
    }
}
